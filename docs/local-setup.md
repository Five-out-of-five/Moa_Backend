# 로컬 실행

## 실행

PostgreSQL, Redis, MinIO 는 Docker 로 띄운다. 다른 Redis 가 이미 6379 를 쓰고 있으면 `.env` 의 `REDIS_PORT` 를 바꾼다.

```bash
docker compose up -d
```

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Swagger: http://localhost:8080/swagger-ui/index.html · MinIO 콘솔: http://localhost:9001 (minioadmin / minioadmin)

이미 로컬 PostgreSQL 이 5432 를 쓰고 있으면 둘 중 하나를 선택한다.
- 로컬 PostgreSQL 사용: `moa` DB·계정과 pgvector 확장이 있어야 한다. Docker 의 postgres 는 띄우지 않아도 된다.
- Docker 사용: `POSTGRES_PORT=5433` 으로 띄우고 `DB_URL=jdbc:postgresql://localhost:5433/moa` 를 주입한다.

환경변수 목록은 `.env.example` 에 있다. Spring 이 `.env` 를 자동으로 읽지는 않으므로 IntelliJ 실행 설정이나 터미널로 주입한다.

## 스토리지: MinIO ↔ AWS S3

같은 코드로 동작하며 환경변수만 다르다.

| 변수 | MinIO | AWS |
|---|---|---|
| `S3_ENDPOINT` | `http://localhost:9000` | 비움 |
| `S3_PUBLIC_ENDPOINT` | 브라우저 접근 주소가 다를 때만 | 비움 |
| `S3_BUCKET` | `moa-local` | 실제 버킷명 |
| `AWS_ACCESS_KEY` / `AWS_SECRET_KEY` | `minioadmin` | 비우면 IAM 역할 사용 (권장) |
| `AWS_REGION` | 아무 값 | 버킷 리전 |
| `S3_CDN_URL` | 비움 | CloudFront 를 쓸 때만 |

AWS 버킷에도 MinIO 와 같은 권한이 필요하다: `public/*` 만 공개 읽기(프로필·방 대표 이미지), 나머지(첨부)는 비공개이며 서명 URL 로만 받는다. 버킷은 애플리케이션이 만들지 않는다.

`local` 프로파일은 MinIO 기본값을 넣으므로, AWS 를 쓸 때는 프로파일 없이 실행하거나 위 변수를 명시한다.

공식 `minio/minio` 이미지는 공개 배포가 중단되어 받을 수 없어 `bitnamilegacy/minio` 를 사용한다.

## 프론트 연동 메모

STOMP 연결:

```js
client.connectHeaders = { Authorization: `Bearer ${accessToken}` };
```

연결 중에 토큰이 만료돼도 연결은 유지된다. 재연결할 때 새 토큰을 보낸다. 하트비트는 20초 간격으로 맞춘다(`heartbeatOutgoing: 20000`). 60초 동안 끊기면 오프라인 처리된다.

회원 탈퇴의 `confirmText` 는 `탈퇴합니다` 다.
