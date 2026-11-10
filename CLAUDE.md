# 모아(MOA) 백엔드

방 단위 팀 협업 서비스의 API 서버. Spring Boot 4 / Java 25 / PostgreSQL(pgvector) / Redis / S3 호환 스토리지 / STOMP.

## 작업 전에 읽을 문서

| 문서 | 내용 |
|---|---|
| [docs/conventions.md](docs/conventions.md) | 폴더 구조, 코드 규칙, 새 API 추가 순서 |
| [docs/collaboration.md](docs/collaboration.md) | 도메인 경계, 도메인 간 호출 방법, 브랜치·PR |
| [docs/pull-request.md](docs/pull-request.md) | PR 대상 브랜치, 제목, 본문 양식, 올리는 절차 |
| [docs/local-setup.md](docs/local-setup.md) | 로컬 실행, MinIO ↔ AWS 전환 |
| [docs/ai-server.md](docs/ai-server.md) | Python AI 서버 호출 계약과 장애 처리 |
| [docs/deploy.md](docs/deploy.md) | 운영 배포(오라클 + Docker), Nginx·HTTPS, 포트 구성 |

기능의 기준은 저장소 밖 `moa_document/` 의 요구사항 명세, API 명세, 테이블 정의서다. 코드와 명세가 다르면 임의로 고치지 말고 차이를 먼저 알린다.

## 반드시 지킬 규칙

- 시각은 UTC 로 저장한다. `LocalDateTime` 은 전부 UTC 이며 `KstTime.nowUtc()` 로 만든다. `LocalDateTime.now()` 금지.
- 응답의 KST 변환은 `TimeJacksonConfig` 가 한다. 응답 DTO 에서 직접 변환하지 않는다. 요청 시각은 `OffsetDateTime` 으로 받아 `KstTime.toUtc` 로 바꾼다. DATE 는 한국 날짜(`KstTime.todayKst`).
- 응답은 `ApiResponse` / `ErrorResponse` 형식만 쓴다. 에러 코드 문자열은 API 명세와 같아야 한다.
- 스키마 변경은 새 Flyway 파일로만 한다. 적용된 `V*.sql` 은 수정하지 않는다. 운영은 `ddl-auto: validate` 다.
- AI 는 `AiGateway`(도메인 코드) 또는 `AiServerPort` 로만 호출한다. AI 서버 장애가 사용자 요청을 깨면 안 된다.
- 스토리지는 `FileStoragePort`, 실시간 전송은 `RealtimePublisher`, 알림은 `NotificationSender`, 브리핑 이벤트는 `BoardEventRecorder` 만 쓴다.
- 비밀값은 커밋하지 않는다. `.env` 는 gitignore 대상이고, 새 환경변수는 `.env.example` 에 키만 추가한다.
- PR 은 지정이 없으면 `develop` 으로 올린다. 만들기 전에 [docs/pull-request.md](docs/pull-request.md) 와 `.github/PULL_REQUEST_TEMPLATE.md` 를 읽고 그 양식대로 작성한다.

## 자주 쓰는 명령

```bash
docker compose up -d
```

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

```bash
./gradlew test
```
