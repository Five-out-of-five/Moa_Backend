# 운영 배포 — EC2 + CDN (Nginx 없음)

EC2 한 대에 Docker 로 프론트(3000)·백엔드(8080)·Redis·AI 서버를 띄우고, 앞에 CDN(CloudFront)을 둔다.
HTTPS 와 경로 나누기는 CloudFront 가 맡는다. DB 는 RDS, 파일은 S3 를 쓴다.

```
브라우저 ── https://moarang.kro.kr ──▶ CloudFront (HTTPS, ACM 인증서)
                                          │
   /api/v1/*  /oauth2/*  /login/oauth2/*   │──▶ EC2:8080  app   (Spring)
   /ws*  /swagger-ui*  /v3/api-docs*       │
   그 밖의 모든 경로 (기본)                  │──▶ EC2:3000  front (Next)

app ─▶ redis(내부망) · ai(내부망 8000) · RDS · S3
```

브라우저 입장에서는 주소가 하나라서 쿠키·CORS·SameSite 가 단순하다(D안과 같은 효과).

| 파일 | 역할 |
|---|---|
| `docker-compose.yml` | 서비스 4개 (app, front, ai, redis) |
| `backend.Dockerfile` | Spring 이미지 (빌드 컨텍스트: 저장소 루트) |
| `front.Dockerfile` | Next 이미지 (소스: 프론트 저장소) |
| `.env.example` | compose 값 (Redis 비밀번호, 다른 저장소 위치) |
| `env/*.env.example` | 서비스별 환경변수 (backend, front, ai) |

AI 서버 이미지는 `Moa_Python/Dockerfile` 을 그대로 쓴다.

---

## 1. AWS 준비

### EC2
- **인스턴스**: 메모리 4GB 이상 (`t3.medium` / `t4g.medium`). 서버에서 Spring·Next 를 빌드하므로 2GB 이하면 빌드 중 멈춘다.
- **탄력적 IP** 를 할당해 연결한다. (CloudFront 원본은 IP 가 아니라 도메인이 필요하므로, 탄력적 IP 의 퍼블릭 DNS `ec2-x-x-x-x.ap-northeast-2.compute.amazonaws.com` 을 원본으로 쓴다)
- **보안 그룹 인바운드**
  | 포트 | 소스 |
  |---|---|
  | 22 | 내 IP |
  | 3000, 8080 | **CloudFront 관리형 접두사 목록** `com.amazonaws.global.cloudfront.origin-facing` |

  3000·8080 을 0.0.0.0/0 으로 열면 CloudFront 를 거치지 않고 평문 HTTP 로 직접 들어올 수 있다.
- **IAM 역할** (S3 접근): 이 버킷에 `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject`, `s3:ListBucket` 만 허용하고 EC2 에 연결한다.
- **IMDS 홉 제한 2**: 앱이 컨테이너 안에서 돌기 때문에 기본값(1)이면 IAM 역할 자격증명을 못 받는다.
  ```bash
  aws ec2 modify-instance-metadata-options --instance-id {인스턴스ID} --http-tokens required --http-put-response-hop-limit 2
  ```
  (콘솔: 인스턴스 → 작업 → 인스턴스 설정 → 인스턴스 메타데이터 옵션 수정)

### RDS (PostgreSQL 17)
- 퍼블릭 액세스 **끔**. 보안 그룹 5432 인바운드는 **EC2 보안 그룹에서만** 허용.
- 파라미터 그룹 `timezone` = `UTC` (기본값).
- Flyway 가 `CREATE EXTENSION vector` 를 실행하므로 **마스터 사용자로 첫 기동**하거나, 미리 접속해 `CREATE EXTENSION vector;` 를 실행해 둔다.
- DB 이름 `moa` 를 만들어 둔다 (생성 시 "초기 데이터베이스 이름").

### S3
- 퍼블릭 액세스 차단에서 **"새 퍼블릭 버킷 정책 차단"만 해제**하고, 버킷 정책으로 `public/*` 만 공개한다. 첨부 파일은 비공개(서명 URL).
  ```json
  {
    "Version": "2012-10-17",
    "Statement": [{
      "Sid": "PublicImages", "Effect": "Allow", "Principal": "*",
      "Action": "s3:GetObject", "Resource": "arn:aws:s3:::{버킷}/public/*"
    }]
  }
  ```
- 업로드는 서버를 거치므로 S3 CORS 는 필요 없다.

---

## 2. CloudFront (Nginx 대신)

### 인증서
- **ACM 은 반드시 미국 동부(버지니아 북부, us-east-1)** 에서 `moarang.kro.kr` 인증서를 발급한다. (CloudFront 는 이 리전 인증서만 쓴다)
- DNS 검증용 CNAME 을 내도메인.한국 관리 화면에 추가한다.

### 원본 2개 (둘 다 EC2 퍼블릭 DNS, 프로토콜 HTTP only)
| 원본 이름 | 도메인 | HTTP 포트 |
|---|---|---|
| `front` | EC2 퍼블릭 DNS | 3000 |
| `api` | EC2 퍼블릭 DNS | 8080 |

- 원본 **응답 제한 시간 60초** (기본 30초). AI 어시스턴트 스트리밍(SSE)이 첫 응답까지 오래 걸릴 수 있다.

### 동작(Behavior) — 위에서부터 순서대로
| 경로 패턴 | 원본 | 캐시 정책 | 원본 요청 정책 |
|---|---|---|---|
| `/api/v1/*` | api | CachingDisabled | AllViewerExceptHostHeader |
| `/oauth2/*` | api | CachingDisabled | AllViewerExceptHostHeader |
| `/login/oauth2/*` | api | CachingDisabled | AllViewerExceptHostHeader |
| `/ws*` | api | CachingDisabled | AllViewerExceptHostHeader |
| `/swagger-ui*` | api | CachingDisabled | AllViewerExceptHostHeader |
| `/v3/api-docs*` | api | CachingDisabled | AllViewerExceptHostHeader |
| 기본값 (`*`) | front | CachingDisabled | AllViewerExceptHostHeader |

공통: 뷰어 프로토콜 **Redirect HTTP to HTTPS**, 허용 메서드 **GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE**.

- **캐시를 끄고 쿠키·쿼리·헤더를 모두 넘겨야 한다.** 로그인(JSESSIONID·state), refresh 쿠키, 페이지네이션 쿼리가 원본에 그대로 가야 동작한다.
- `/login` 자체는 프론트 화면이다. Spring 으로 보내는 것은 `/login/oauth2/*` 뿐이다.
- 프론트 BFF 경로 `/api/rooms/...` 는 `/api/v1/*` 에 걸리지 않아 기본값(front)으로 간다.
- WebSocket(`/ws`)은 CloudFront 가 그대로 지원한다.
- 나중에 정적 파일만 캐시하고 싶으면 `/_next/static/*` → front, CachingOptimized 동작을 기본값 위에 추가한다.

### 도메인
- 배포 설정의 대체 도메인에 `moarang.kro.kr`, 인증서는 위 ACM 인증서.
- 내도메인.한국에서 `moarang.kro.kr` 을 CloudFront 도메인(`dxxxx.cloudfront.net`)으로 **CNAME** 연결한다.
  (kro.kr 이 루트 도메인 CNAME 을 지원하지 않으면 `www.moarang.kro.kr` 같은 하위 이름을 쓰고, 아래 env 의 주소도 그 이름으로 맞춘다)

---

## 3. 서버에서 실행

### 처음 한 번
```bash
curl -fsSL https://get.docker.com | sudo sh
```
```bash
sudo usermod -aG docker $USER && newgrp docker
```
세 저장소를 나란히 받는다.
```bash
mkdir -p ~/moa && cd ~/moa
```
```bash
git clone https://github.com/Five-out-of-five/Moa_Backend.git
```
```bash
git clone https://github.com/Five-out-of-five/Moa_Front.git
```
```bash
git clone https://github.com/Five-out-of-five/Moa_Python.git
```
(프론트·AI 저장소 주소와 배포할 브랜치는 각 저장소 담당자에게 확인한다)

### 환경변수 파일
제공받은 파일을 아래 위치에 둔다. 형식은 같은 이름의 `.example` 파일을 본다.
```
Moa_Backend/deploy/cdn/.env            REDIS_PASSWORD, FRONT_DIR, AI_DIR
Moa_Backend/deploy/cdn/env/backend.env Spring
Moa_Backend/deploy/cdn/env/front.env   Next
Moa_Backend/deploy/cdn/env/ai.env      Python AI
```
```bash
chmod 600 ~/moa/Moa_Backend/deploy/cdn/.env ~/moa/Moa_Backend/deploy/cdn/env/*.env
```
- `backend.env` 의 `AI_INTERNAL_API_KEY` 와 `ai.env` 의 `INTERNAL_API_KEY` 는 같은 값이어야 한다.
- 소셜 콘솔(구글·카카오·네이버)에 `https://moarang.kro.kr/login/oauth2/code/{google|kakao|naver}` 를 등록한다.

### 실행
```bash
cd ~/moa/Moa_Backend/deploy/cdn && docker compose up -d --build
```
첫 빌드는 의존성을 받느라 10분 가까이 걸린다. 앱이 뜨면 Flyway 가 RDS 에 스키마를 만든다.

### 확인
```bash
docker compose ps
```
```bash
curl -s http://127.0.0.1:8080/actuator/health
```
```bash
curl -sI http://127.0.0.1:3000 | head -1
```
CloudFront 연결 후:
```bash
curl -s https://moarang.kro.kr/actuator/health
```
`/actuator/*` 는 CloudFront 동작에 없으므로 위 주소는 프론트로 간다. 외부 헬스 체크가 필요하면 `/actuator/health` 동작을 api 원본으로 추가한다.

---

## 업데이트
```bash
cd ~/moa/Moa_Backend && git pull && cd ../Moa_Front && git pull && cd ../Moa_Python && git pull
```
```bash
cd ~/moa/Moa_Backend/deploy/cdn && docker compose up -d --build
```
`front.env` 의 `NEXT_PUBLIC_*` 를 바꾸면 프론트 이미지를 다시 빌드해야 반영된다(`--build`).

## 운영 명령
```bash
docker compose logs -f app
```
```bash
docker compose restart front
```
중지 (`down -v` 는 Redis 데이터를 지우므로 쓰지 않는다):
```bash
docker compose down
```

## 주의
- **서버 한 대 기준**: STOMP 는 인메모리 브로커라 앱 컨테이너를 여러 개로 늘리면 실시간 전송이 인스턴스 사이에 전달되지 않는다.
- **OAuth 콜백 주소**: CDN 뒤에서는 Spring 이 자기 공개 주소를 알 수 없어 `backend.env` 에 직접 지정한다(`SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_*_REDIRECTURI`). 도메인을 바꾸면 이 값과 각 콘솔을 함께 바꾼다.
- **8080 직접 접속**은 평문 HTTP 라 `COOKIE_SECURE=true` 인 refresh 쿠키가 동작하지 않는다. 점검용으로만 쓴다.
- **SSE 타임아웃**: CloudFront 원본 응답 제한 시간(최대 60초, 더 늘리려면 AWS 한도 증가 요청)을 넘겨 데이터가 끊기면 스트림이 잘린다. AI 서버 마감이 25초라 보통은 걸리지 않는다.
