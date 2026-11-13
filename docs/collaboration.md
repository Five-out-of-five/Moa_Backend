# 도메인별 협업 가이드

## 담당 영역

담당자 칸은 팀에서 채운다. 담당자는 해당 폴더의 리뷰어이자 명세 질문 창구다.

| 영역 | 폴더 | 담당 |
|---|---|---|
| 인증·계정 | `auth`, `account` | |
| 방·멤버·초대 | `room` | |
| 대시보드·상태값 | `dashboard`, `mood` | |
| 아이디어 보드 | `session`, `ideacard` | |
| 업무·투표 | `task`, `vote` | |
| 브리핑·알림 | `briefing`, `notification` | |
| 채팅·파일 | `chat`, `file` | |
| AI·중재 | `ai`, `mediation` | |
| 연동 | `integration` | |
| 운영 | `report`, `notice`, `admin` | |
| 실시간 | `global/websocket`, `global/config/StompWebSocketConfig` | BE-3 |
| 공통 | `global` 나머지, `docker-compose.yml` | |

## 도메인 사이의 규칙

| 하고 싶은 일 | 방법 |
|---|---|
| 방 권한 확인, 표시 이름 | `RoomAccessService` |
| 다른 도메인 데이터 **읽기** | 해당 도메인의 `domain/repository` 포트를 주입해 읽기 메서드만 호출 |
| 다른 도메인 데이터 **변경** | 그 도메인의 `UseCase` 호출 또는 도메인 이벤트 발행 |
| 알림 | `NotificationSender.send(...)` |
| 브리핑 기록 | `BoardEventRecorder.record(...)` |
| 실시간 전송 | `RealtimePublisher` (커밋 후 전송을 보장) |
| 첨부 검증 | `FileAttachSupport` |
| AI 호출 | `AiGateway` |

다른 도메인의 `JpaEntity`, `SpringData*Repository`, `infrastructure` 는 참조하지 않는다.

### 도메인 이벤트

한 동작의 후처리가 여러 도메인에 걸치면 호출을 늘리지 않고 이벤트를 쓴다.

| 이벤트 | 발행 | 구독 |
|---|---|---|
| `RoomCreatedEvent` | room | dashboard(기본 위젯), chat(기본 채널) |
| `RoomMemberJoinedEvent` | room | chat(기본 채널 참여) |
| `RoomMemberLeftEvent` | room, account | task(미담당 전환), chat, notification, briefing |
| `CardChangedEvent` | ideacard | mediation(임베딩·충돌 감지) |
| `CardDeletedEvent` | ideacard, admin | mediation |
| `SessionClosedEvent` | session | mediation, integration(노션·슬랙) |
| `MediationWidgetAddedEvent` | dashboard | mediation(진행 중 회차 기존 카드 충돌 분석) |
| `VoteLifecycleEvent` | vote | integration(슬랙) |
| `TaskDueSoonEvent` | task | integration(슬랙) |

- 데이터 정합성이 필요한 처리는 `@EventListener`(같은 트랜잭션)로 받는다.
- 외부 호출은 `@Async` + `@TransactionalEventListener`(커밋 후)로 받고, 실패가 원래 요청에 영향을 주지 않게 한다.

## 공유 파일을 바꿀 때

아래는 여러 도메인이 함께 쓰므로 먼저 팀에 알리고 PR 에 영향 범위를 적는다.

- `global/**`, `application*.yaml`, `build.gradle`, `docker-compose.yml`
- `global/websocket/**` 은 담당인 BE-3 에게 먼저 알린다.
- `src/main/resources/db/migration` — 버전 번호가 겹치지 않게 PR 생성 순서대로 번호를 잡고, 머지 전에 최신 develop 으로 다시 확인한다.
- API 계약(경로·필드·에러 코드)을 바꾸면 명세 문서를 먼저 고치고 프론트에 공유한다.

## 브랜치와 PR

- `main`, `develop` 에 직접 커밋하지 않는다. `develop` 에서 `feature/{작업명}`, `fix/{작업명}` 브랜치를 만든다.
- 커밋 메시지는 `[Feature] 내용`, `[Fix] 내용`, `[Refactor] 내용`, `[Docs] 내용`.
- PR 은 `develop` 으로 올리고 [pull-request.md](pull-request.md) 의 양식을 따른다. 다른 도메인을 건드리면 해당 담당자를 리뷰어로 지정한다.
- 머지 전 확인: `./gradlew test` 통과, 명세와 다른 부분은 PR 에 명시.

## AI 도구로 작업할 때

`CLAUDE.md` 와 이 문서들을 먼저 읽게 한다. 생성된 변경도 사람이 작성한 코드와 같은 기준으로 리뷰한다.
