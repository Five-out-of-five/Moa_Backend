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

어시스턴트 SSE 는 `data:` 한 줄에 JSON 하나를 보낸다.

```
data: {"delta": "답변 조각"}

data: {"references": {"cards": [], "tasks": [], "votes": []}}
```

연결을 닫으면 완료로 처리한다. 임베딩 차원은 `content_embedding.embedding vector(768)` 과 같아야 한다.
