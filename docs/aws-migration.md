# AWS 이전 시 바뀌는 값

지금의 오라클 클라우드 배포([deploy.md](deploy.md))를 AWS 로 옮길 때 바꿔야 하는 값과 파일을 정리한다.
**애플리케이션 코드는 바꾸지 않는다.** 바뀌는 것은 `.env.prod`, `docker-compose.prod.yml`, Nginx 설정, AWS 콘솔 설정이다.

## 현재 구성 (기준)

| 구성 요소 | 지금 (오라클) |
|---|---|
| 서버 | 오라클 인스턴스 1대, Docker |
| HTTPS·리다이렉트 | 호스트 Nginx + Let's Encrypt(certbot) |
| 리버스 프록시 | Docker Nginx (호스트 8080) |
| DB | Docker PostgreSQL(pgvector) |
| Redis | Docker Redis |
| 파일 저장소 | Docker MinIO (`https://moarang.kro.kr/{버킷}/` 로 공개) |
| 방화벽 | VCN Security List + 인스턴스 iptables |
| 도메인 | `moarang.kro.kr` A 레코드 → 오라클 공인 IP |

## 이전 범위 고르기

한 번에 다 옮길 필요는 없다. 아래 단계는 서로 독립이라 필요한 것만 고르면 된다.

| 단계 | 바꾸는 것 | 비용·관리 |
|---|---|---|
| A. 서버만 이전 | 오라클 → EC2. 나머지는 지금과 같은 Docker 구성 | 가장 단순. EC2 비용만 발생 |
| B. 파일 → S3 | MinIO 대신 S3 | 서버 디스크를 쓰지 않음. 요청·저장 요금 |
| C. DB → RDS | Docker PostgreSQL 대신 RDS | 백업·패치를 AWS 가 관리. 상시 비용이 큼 |
| D. Redis → ElastiCache | Docker Redis 대신 ElastiCache | 필요성이 낮음. 상시 비용 |
| E. HTTPS → ALB + ACM | 호스트 Nginx·certbot 대신 ALB | 인증서 자동 관리. ALB 상시 비용 |

권장 순서: **A → B**. C·D·E 는 트래픽이나 운영 요구가 생길 때 고려한다.

---

## A. 서버만 이전 (오라클 → EC2)

`.env.prod` 와 compose 파일은 **그대로** 쓴다.

| 항목 | 오라클 | AWS |
|---|---|---|
| 인스턴스 | Ampere(ARM) | Graviton(`t4g.*`, ARM) 또는 `t3.*`(x86). 서버에서 빌드하므로 둘 다 된다 |
| 공인 IP | 오라클 공인 IP | **Elastic IP** 를 할당해 인스턴스에 연결 (재시작해도 IP 유지) |
| 방화벽 | Security List + iptables | **Security Group** 인바운드 TCP 22(내 IP만), 80, 443, (필요 시) 8080. iptables 설정은 필요 없다 |
| DNS | A → 오라클 IP | A → Elastic IP |
| 인증서 | certbot | 같은 방법으로 새 서버에서 다시 발급 (DNS 변경 후) |
| 접속 사용자 | `ubuntu`/`opc` | Ubuntu AMI 는 `ubuntu`, Amazon Linux 는 `ec2-user` |

절차는 [deploy.md](deploy.md) 의 1~7단계와 같다. 데이터 옮기기는 아래 "데이터 이전"을 본다.

---

## B. 파일 저장소 → S3

### `.env.prod`

| 키 | 지금 (MinIO) | S3 |
|---|---|---|
| `S3_BUCKET` | `moa-storage` | 실제 S3 버킷명 (전 세계에서 유일해야 함) |
| `AWS_REGION` | 아무 값 | 버킷 리전. 예: `ap-northeast-2` |
| `S3_CDN_URL` | 비움 | CloudFront 를 쓸 때만 그 도메인 |
| `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD` | 사용 | 삭제 |
| `AWS_ACCESS_KEY`, `AWS_SECRET_KEY` | (compose 가 MinIO 값으로 채움) | **비움** → EC2 IAM 역할 사용 (권장) |

공개 이미지 URL 은 `S3_CDN_URL` 이 비어 있으면 자동으로 `https://{버킷}.s3.{리전}.amazonaws.com/...` 이 된다. 서명 URL 도 S3 주소로 만들어진다.

### `docker-compose.prod.yml` (반드시 수정)

지금 compose 는 MinIO 값을 `environment` 에 고정해 `.env.prod` 보다 우선한다. 그대로 두면 S3 로 바뀌지 않는다.

1. `app.environment` 에서 아래 4줄을 삭제한다.
   ```yaml
   S3_ENDPOINT: http://minio:9000
   S3_PUBLIC_ENDPOINT: ${PUBLIC_URL:?...}
   AWS_ACCESS_KEY: ${MINIO_ROOT_USER:-moa}
   AWS_SECRET_KEY: ${MINIO_ROOT_PASSWORD:?...}
   ```
2. `app.depends_on` 에서 `minio-init` 을 삭제한다.
3. `minio`, `minio-init` 서비스와 `minio-data` 볼륨을 삭제한다.
4. `nginx.depends_on` 에서 `minio` 를 삭제한다.

### `deploy/nginx/moa.conf.template`

- `location ^~ /${S3_BUCKET}/ { ... }` 블록을 삭제한다. 파일은 브라우저가 S3 에서 직접 받는다.
- 그러면 `nginx.environment.S3_BUCKET` 도 필요 없다.

### AWS 콘솔

| 설정 | 값 |
|---|---|
| 버킷 퍼블릭 액세스 차단 | 버킷 정책으로 공개할 수 있게 "새 퍼블릭 버킷 정책 차단"만 해제 |
| 버킷 정책 | `public/*` 만 `s3:GetObject` 공개. 나머지(첨부)는 비공개, 서명 URL 로만 받음 |
| IAM 역할 | `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject`, `s3:ListBucket` 를 이 버킷에만 허용하고 EC2 에 연결 |
| **IMDS 홉 제한** | **2 로 변경** (아래 참고) |
| CORS | 브라우저가 S3 에 직접 업로드하지 않으므로 필요 없음 (업로드는 서버를 거침) |

버킷 정책 예시 (`{버킷}` 만 바꾼다):

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Sid": "PublicImages",
    "Effect": "Allow",
    "Principal": "*",
    "Action": "s3:GetObject",
    "Resource": "arn:aws:s3:::{버킷}/public/*"
  }]
}
```

**IMDS 홉 제한:** 앱은 Docker 컨테이너 안에서 돌기 때문에, 최근 EC2 기본값(IMDSv2, 홉 제한 1)이면 컨테이너가 IAM 역할 자격증명을 받지 못한다. 그러면 S3 호출이 자격증명 없음 에러로 실패한다. 인스턴스 설정에서 홉 제한을 2 로 올린다.

```bash
aws ec2 modify-instance-metadata-options --instance-id {인스턴스ID} --http-tokens required --http-put-response-hop-limit 2
```

IAM 역할을 쓰지 않고 액세스 키를 쓰려면 `.env.prod` 에 `AWS_ACCESS_KEY`, `AWS_SECRET_KEY` 를 넣는다. 키 유출 위험이 있어 권장하지 않는다.

---

## C. DB → RDS PostgreSQL

### `.env.prod`

| 키 | 지금 | RDS |
|---|---|---|
| `DB_URL` | (compose 가 `jdbc:postgresql://postgres:5432/moa` 로 고정) | `jdbc:postgresql://{RDS 엔드포인트}:5432/moa?sslmode=require` |
| `DB_USERNAME` | `POSTGRES_USER` | RDS 마스터 사용자 또는 앱 전용 사용자 |
| `DB_PASSWORD` | `POSTGRES_PASSWORD` | RDS 비밀번호 |
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | 사용 | 삭제 |

### `docker-compose.prod.yml`

1. `app.environment` 에서 `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 3줄을 삭제한다. (`.env.prod` 값을 쓰게 한다)
2. `app.depends_on` 에서 `postgres` 를 삭제한다.
3. `postgres` 서비스와 `postgres-data` 볼륨을 삭제한다.

### AWS 콘솔

- 엔진: PostgreSQL 17 (pgvector 지원). Flyway 가 `CREATE EXTENSION vector` 를 실행하므로 **마스터 사용자로 첫 기동**하거나, 미리 `CREATE EXTENSION vector;` 를 실행해 둔다.
- 타임존: 파라미터 그룹 `timezone` 을 `UTC` 로 둔다 (기본값이 UTC).
- 보안 그룹: 5432 인바운드를 **EC2 보안 그룹에서만** 허용한다. 퍼블릭 액세스는 끈다.

---

## D. Redis → ElastiCache

### `.env.prod`

| 키 | 지금 | ElastiCache |
|---|---|---|
| `REDIS_HOST` | (compose 가 `redis` 로 고정) | ElastiCache 기본 엔드포인트 |
| `REDIS_PORT` | (compose 가 `6379` 로 고정) | `6379` |
| `REDIS_SSL_ENABLED` | `false` | 전송 중 암호화를 켰으면 `true` |
| `SPRING_DATA_REDIS_PASSWORD` | (compose 가 `REDIS_PASSWORD` 로 채움) | AUTH 토큰 (쓰는 경우) |
| `REDIS_PASSWORD` | 사용 | 삭제 |

### `docker-compose.prod.yml`

1. `app.environment` 에서 `REDIS_HOST`, `REDIS_PORT`, `SPRING_DATA_REDIS_PASSWORD` 3줄을 삭제한다.
2. `app.depends_on` 에서 `redis` 를 삭제한다.
3. `redis` 서비스와 `redis-data` 볼륨을 삭제한다.

보안 그룹: 6379 인바운드를 EC2 보안 그룹에서만 허용한다.

---

## E. HTTPS → ALB + ACM

호스트 Nginx 와 certbot 을 없애고 ALB 가 TLS 를 끝낸다.

| 항목 | 지금 | ALB |
|---|---|---|
| 인증서 | Let's Encrypt (90일, certbot 갱신) | ACM 인증서 (자동 갱신). DNS 검증용 CNAME 을 kro.kr 에 추가 |
| 80→443 | 호스트 Nginx `return 301` | ALB 리스너 80 → 443 리다이렉트 규칙 |
| 443 | 호스트 Nginx → 127.0.0.1:8080 | ALB 리스너 443 → 대상 그룹(EC2:8080, HTTP) |
| 헬스 체크 | 없음 | 대상 그룹 경로 `/actuator/health` |
| DNS | A → 서버 IP | ALB 는 IP 가 바뀌므로 **CNAME → ALB DNS 이름** (kro.kr 에서 CNAME 지원 여부 확인) |
| 유휴 시간 제한 | Nginx `proxy_read_timeout 300s` | ALB 기본 60초. SSE(AI 스트리밍)를 위해 **300초로 늘린다** |
| WebSocket | Nginx Upgrade 헤더 | ALB 가 기본 지원. STOMP 하트비트 20초라 60초로도 끊기지 않음 |

### `deploy/nginx/moa.conf.template` (반드시 수정)

Docker Nginx 는 호스트 Nginx(127.0.0.1, 172.16.0.0/12)에서 온 `X-Forwarded-Proto` 만 믿는다. ALB 는 VPC 사설 IP 에서 들어오므로 **VPC 대역을 추가하지 않으면** 앱이 요청을 http 로 판단한다. 그러면 OAuth 리디렉션 주소가 `http://` 로 만들어져 소셜 로그인이 실패한다.

```nginx
geo $from_host_proxy {
    default        0;
    127.0.0.1/32   1;
    172.16.0.0/12  1;
    10.0.0.0/16    1;   # ← 실제 VPC CIDR 로 바꾼다 (기본 VPC 는 172.31.0.0/16 이라 위 줄에 이미 포함)
}
```

### 기타

- 보안 그룹: EC2 의 8080 은 **ALB 보안 그룹에서만** 허용하고, 80·443 은 ALB 에만 연다.
- `deploy/host-nginx/moarang.kro.kr.conf` 는 쓰지 않는다.

---

## 바뀌지 않는 값

도메인이 같으면 아래는 그대로 둔다.

| 키 | 값 |
|---|---|
| `PUBLIC_URL` | `https://moarang.kro.kr` (B 를 하면 사용처가 없어 지워도 됨) |
| `JWT_SECRET_KEY` | 같은 값을 쓰면 기존 로그인이 유지된다. 바꾸면 전원 재로그인 |
| `INTEGRATION_SECRET_KEY` | **반드시 같은 값.** 바꾸면 저장된 슬랙 Webhook·노션 토큰을 복호화하지 못한다 (비워 두었다면 JWT 키에서 파생되므로 JWT 키도 같아야 함) |
| `COOKIE_DOMAIN`, `COOKIE_SECURE` | 그대로 |
| `CORS_ALLOWED_ORIGINS`, `FRONTEND_URL` | 그대로 |
| 소셜 로그인 키, 콘솔 리디렉션 URI | 그대로 (`https://moarang.kro.kr/login/oauth2/code/...`) |
| `NOTION_REDIRECT_URI` | 그대로 |
| `AI_SERVER_URL` | AI 서버도 같은 EC2 로 옮기면 그대로. 다른 곳이면 그 주소 |

---

## 데이터 이전

옮기는 동안 쓰기가 생기지 않도록 기존 서버의 앱을 먼저 멈춘다.

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml stop app nginx
```

### DB

기존 서버에서 덤프한다.

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml exec -T postgres pg_dump -U moa -d moa -Fc > moa.dump
```

새 서버로 복사한 뒤, **앱을 띄우기 전에** 복원한다. Flyway 이력 테이블도 함께 옮겨지므로 앱은 마이그레이션을 다시 하지 않는다.

Docker PostgreSQL 로 복원할 때:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d postgres
```

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml exec -T postgres pg_restore -U moa -d moa --no-owner < moa.dump
```

RDS 로 복원할 때:

```bash
pg_restore -h {RDS 엔드포인트} -U {사용자} -d moa --no-owner moa.dump
```

### 파일 (MinIO → S3)

기존 서버의 MinIO 컨테이너 안에서 S3 로 바로 복사한다. `mc` 가 이미지에 들어 있다.

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml exec minio sh -c 'mc alias set src http://localhost:9000 "$MINIO_ROOT_USER" "$MINIO_ROOT_PASSWORD" && mc alias set s3 https://s3.amazonaws.com {AWS 액세스 키} {AWS 시크릿 키} && mc mirror src/{기존 버킷} s3/{새 버킷}'
```

- 이 명령의 키는 이전 작업용으로만 쓰고, 끝나면 IAM 콘솔에서 비활성화한다.
- DB 에는 객체 키(`public/...`, 첨부 경로)만 저장되어 있다. 같은 키로 복사하면 버킷 이름이 바뀌어도 그대로 동작한다.

MinIO 를 유지한 채 서버만 옮길 때(A 만)는 볼륨을 통째로 옮긴다.

```bash
docker run --rm -v moa_minio-data:/data -v "$PWD":/backup alpine tar czf /backup/minio-data.tgz -C /data .
```

---

## 전환 순서 체크리스트

1. AWS 리소스 준비 (EC2·Elastic IP·보안 그룹, 필요 시 S3·RDS·ALB·IAM 역할)
2. 새 서버에 Docker 설치, 저장소 클론, `.env.prod` 작성 (위 표 기준)
3. compose·Nginx 파일을 고른 범위에 맞게 수정
4. 기존 서버 앱 중지 → DB 덤프·파일 복사 → 새 서버에 복원
5. 새 서버에서 `docker compose ... up -d --build` 후 `curl http://127.0.0.1:8080/actuator/health`
6. DNS 를 새 서버(Elastic IP 또는 ALB)로 변경. 반영되기 전까지 기존 서버는 지우지 않는다
7. HTTPS 인증서 발급 (certbot 은 DNS 변경 후, ACM 은 미리 가능)
8. 확인: 로그인(소셜), 파일 업로드·다운로드, `wss://moarang.kro.kr/ws` 연결, AI 스트리밍
9. 며칠 문제없으면 오라클 리소스를 정리한다
