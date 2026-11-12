# Python AI 서버 연동

Spring 은 AI 로직을 갖지 않고 Python 서버를 호출한다. 주소는 `AI_SERVER_URL`(기본 `http://localhost:8000`).

## 연결과 장애 처리

| 상황 | 동작 |
|---|---|
| 기동 시 서버 없음 | 정상 기동, 경고 로그 1회 |
| 연결됨 | HTTP keep-alive 로 커넥션 재사용, 15초마다 `GET /health` 로 유지 |
| 연결 실패 | 끊김 표시. 이후 호출은 네트워크를 타지 않고 바로 '사용 불가' 처리 |
| 복구 | 다음 헬스 체크가 성공하면 재시작 없이 자동 재연결 |

연결 코드: `global/infrastructure/ai/AiServerConnection`, `PythonAiServerAdapter`. 포트 `AiServerPort` 는 어떤 실패도 예외로 던지지 않는다.

서버가 없을 때 기능별 결과:

| 기능 | 결과 |
|---|---|
| 톤앤매너 | `needsChange=false` (원문 그대로 등록) |
| 임베딩·충돌 감지 | 건너뜀. 사용자 요청과 무관 |
| 어시스턴트·요약·절충안·대시보드 추천 | `503 AI_UNAVAILABLE` (명세의 재시도 안내용 응답) |

새 AI 기능은 `AiGateway` 를 쓴다: 사용자가 결과를 기다리면 `call`, 실패해도 흐름을 막지 않으면 `callQuietly`. 한도(`app.ai.quota`)와 `ai_call_log` 기록이 자동으로 처리된다.

## 호출 계약

Python 서버가 구현할 API. 요청·응답 모두 JSON 이다.

| 메서드·경로 | 요청 | 응답 |
|---|---|---|
| `GET /health` | | 2xx |
| `POST /v1/tone-checks` | `text`, `toneMode` | `needsChange`, `suggestion` |
| `POST /v1/summaries` | `maxLength`, `cards[{id,type,title,content}]` | `content` |
| `POST /v1/embeddings` | `text` | `embedding[768]`, `model` |
| `POST /v1/conflicts/detect` | `card{id,title,content}`, `candidates[]` | `conflicts[{cardId,reason}]` |
| `POST /v1/compromises` | `cardType`, `cardA{title,content}`, `cardB` | `title`, `content` |
| `POST /v1/layouts/recommend` | `purpose`, `widgetTypes[]`, `gridColumns` | `widgets[{widgetType,x,y,w,h}]`, `reason` |
| `POST /v1/assistant/stream` | `roomId`, `question`, `context{cards,tasks,votes,moods}` | SSE |

어시스턴트 SSE 는 `data:` 한 줄에 JSON 하나를 보낸다. 답변 조각(`delta`)을 다 보낸 뒤 근거(`references`)를 한 번 보낸다(API 명세 순서).

```
data: {"delta": "답변 조각"}

data: {"references": {"cards": [], "tasks": [], "votes": []}}
```

연결을 닫으면 완료로 처리한다. 임베딩 차원은 `content_embedding.embedding vector(768)` 과 같아야 한다.

## 어시스턴트 스트리밍 — 프론트용

`POST /api/v1/rooms/{roomId}/ai/assistant/messages`, 요청 `{ "question": "…" }`(200자), 응답 `text/event-stream`.
POST 라서 브라우저 `EventSource` 는 쓸 수 없다. `fetch` 의 `response.body` 를 읽어 빈 줄 단위로 이벤트를 나눈다.

### 스트림을 열기 전에 실패하면 일반 JSON 에러

응답 `Content-Type` 이 `text/event-stream` 이 아니면 보통 API 와 같은 `{ success: false, error: { code, message } }` 다.

| 상황 | HTTP | code |
|---|---|---|
| 질문이 비었거나 200자 초과 | 400 | `COMMON_INVALID_INPUT` |
| 토큰 없음·잘못됨 / 만료 | 401 | `COMMON_UNAUTHORIZED` / `AUTH_ACCESS_TOKEN_EXPIRED` |
| 방 멤버가 아님 | 403 | `ROOM_NOT_MEMBER` |
| 오늘 한도 초과 (방당 30회) | 429 | `AI_QUOTA_EXCEEDED` |
| AI 서버 연결이 끊긴 상태 | 503 | `AI_UNAVAILABLE` |

### 스트림 이벤트

한 이벤트는 `event:{이름}` 한 줄과 `data:{JSON}` 한 줄이다. `data` 는 항상 JSON 한 줄이다(답변 속 줄바꿈은 `\n` 으로 들어 있다).

| event | data | 횟수 |
|---|---|---|
| `delta` | `{ "text": "답변 조각" }` | 0번 이상. Markdown 조각을 이어 붙여 표시 |
| `references` | `{ "cards": [...], "tasks": [...], "votes": [...] }` | 0~1번. AI 서버가 준 근거를 그대로 전달 |
| `done` | `{ "messageId": 1, "status": "COMPLETED" }` | 마지막 1번 (`status` 는 `COMPLETED` 또는 `PARTIAL`) |
| `error` | `{ "code": "AI_UNAVAILABLE" }` | 마지막 1번 |

- 마지막 이벤트는 `done` 과 `error` 중 하나이고, 그 뒤 서버가 연결을 닫는다.
- 순서는 AI 서버가 보낸 그대로라 보통 `delta…` → `references` → `done` 이다. 순서에 기대지 말고 이벤트 이름으로 처리한다.
- `PARTIAL`: 답변 도중 AI 서버와 연결이 끊겼다(응답 대기 30초 초과 등). 받은 데까지 저장되어 있으니 "답변이 중간에 끊겼어요"와 재시도를 안내한다.
- `error`: 스트림은 열렸지만 답변이 한 글자도 오기 전에 AI 서버가 실패했다. 대화는 `FAILED` 로 저장된다.
- 사용자가 중간에 끊으면(`AbortController`) 서버는 남은 이벤트를 보내지 않고 `PARTIAL` 로 저장한다(답변이 한 글자도 없었으면 `FAILED`). AI 서버의 답변은 끝까지 받아 저장하므로 이력에서 전체 답변을 볼 수 있다.
- 한도는 `COMPLETED`·`PARTIAL` 일 때만 1회 차감한다. 시작 전 실패와 `error` 는 차감하지 않는다.
- 시간 제한: 스트림 전체 120초, AI 서버 응답 대기 30초.

실제 응답 예(성공 / 실패 / 중간 끊김):

```
event:delta
data:{"text":"이번 회차에는 "}

event:delta
data:{"text":"카드가 3건\n있습니다."}

event:references
data:{"cards":[{"id":1,"title":"카드1"}],"tasks":[],"votes":[]}

event:done
data:{"status":"COMPLETED","messageId":1}
```

```
event:error
data:{"code":"AI_UNAVAILABLE"}
```

```
event:delta
data:{"text":"이번 회차에는 "}

event:done
data:{"status":"PARTIAL","messageId":3}
```

### 중간 서버(BFF·Nginx)

- Next BFF(`/bff/**`)는 응답을 모았다가 보내지 말고 스트림 그대로 흘려보낸다. 사용자가 끊으면 Spring 쪽 요청도 끊는다.
- Nginx 등 프록시는 이 경로의 응답 버퍼링을 끄고(`proxy_buffering off`) 읽기 타임아웃을 120초보다 길게 둔다.

### 대화 이력

`GET /api/v1/rooms/{roomId}/ai/assistant/messages?before={messageId}&size=20` → `{ items[], hasNext, nextCursor }`, 질문한 본인 것만 최신순. 다음 페이지는 `before=nextCursor`(그보다 오래된 것, `size` 최대 100).
항목: `messageId`, `asker`(MemberRef), `question`, `answer`, `status`(`COMPLETED`/`PARTIAL`/`FAILED`), `references`, `createdAt`.
`references` 는 지금 JSON **문자열**(없으면 `null`)이라 화면에서 `JSON.parse` 해서 쓴다.
