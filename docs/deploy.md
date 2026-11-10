# 운영 배포 (오라클 클라우드 + Docker)

`moarang.kro.kr` 을 오라클 클라우드 인스턴스 한 대에 Docker 로 올리는 방법이다.
AWS 로 옮길 때 바뀌는 값은 [aws-migration.md](aws-migration.md) 에 정리했다.

## 구성

```
브라우저
  │  http://moarang.kro.kr  ──301──▶  https://moarang.kro.kr
  ▼
호스트 Nginx (apt 설치, 80·443)          TLS 종료, 인증서(Let's Encrypt)
  │  127.0.0.1:8080
  ▼
┌──────────────── Docker (compose 프로젝트 이름: moa) ────────────────┐
│ nginx :8080 ──┬── /ws, /api, /oauth2, /login ... ──▶ app :8080      │
│               └── /{S3_BUCKET}/ (파일) ───────────▶ minio :9000     │
│ app ──▶ postgres :5432 (pgvector)                                    │
│ app ──▶ redis :6379                                                  │
└──────────────────────────────────────────────────────────────────────┘
```

| 서비스 | 호스트에 여는 포트 | 비고 |
|---|---|---|
| 호스트 Nginx | 80, 443 | 이미 설치된 Nginx 에 사이트 설정 하나를 추가한다 |
| Docker nginx | 8080 (`NGINX_PORT`) | 외부에서 직접 `http://서버IP:8080` 으로도 접속된다 |
| app, postgres, redis, minio | 없음 | Docker 내부망에서만 통신한다 |

### 기존 서비스와 포트가 겹치지 않는 이유

- 컨테이너끼리는 서비스 이름(`postgres:5432`, `redis:6379`)으로 통신하고 호스트 포트를 쓰지 않는다. 서버에 이미 PostgreSQL(5432)·Redis(6379)가 떠 있어도 충돌하지 않는다.
- 호스트에 바인딩하는 것은 Docker nginx 의 8080 하나다. 8080 을 다른 프로그램이 쓰고 있으면 `.env.prod` 의 `NGINX_PORT` 를 바꾸고, 호스트 Nginx 설정의 `proxy_pass` 포트도 같이 바꾼다.
- compose 프로젝트 이름을 `moa` 로 고정했다. 컨테이너·볼륨 이름이 `moa-*` 로 만들어져 다른 compose 프로젝트와 섞이지 않는다.

DB 에 직접 접속해야 할 때는 포트를 열지 말고 컨테이너 안에서 접속한다.

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml exec postgres psql -U moa -d moa
```

## 처음 한 번

### 1. 서버 준비

Docker 와 compose 플러그인을 설치한다. (Ubuntu 기준)

```bash
curl -fsSL https://get.docker.com | sudo sh
```

```bash
sudo usermod -aG docker $USER && newgrp docker
```

지금 열려 있는 포트를 확인한다. 80·443 은 호스트 Nginx, 8080 은 비어 있어야 한다.

```bash
sudo ss -tlnp | grep -E ':(80|443|8080)\s'
```

### 2. DNS

내도메인.한국(kro.kr) 관리 화면에서 `moarang.kro.kr` 의 A 레코드를 인스턴스 공인 IP 로 지정한다.

```bash
dig +short moarang.kro.kr
```

### 3. 방화벽

오라클은 두 군데를 모두 열어야 한다.

1. 콘솔: VCN → Security List(또는 NSG) 에 TCP 80, 443, 8080 Ingress 규칙 추가
2. 인스턴스 안의 iptables: 오라클 Ubuntu 이미지는 기본으로 막혀 있다.

```bash
sudo iptables -I INPUT 6 -m state --state NEW -p tcp -m multiport --dports 80,443,8080 -j ACCEPT
```

```bash
sudo netfilter-persistent save
```

8080 을 외부에 열지 않으려면 위 두 곳에서 8080 을 빼고, `docker-compose.prod.yml` 의 포트를 `"127.0.0.1:${NGINX_PORT:-8080}:8080"` 으로 바꾼다.

### 4. 소스와 환경변수

```bash
git clone https://github.com/Five-out-of-five/Moa_Backend.git && cd Moa_Backend
```

```bash
cp .env.prod.example .env.prod && chmod 600 .env.prod
```

`.env.prod` 의 빈 값을 채운다. 비밀번호와 키는 서버에서 만든다.

```bash
openssl rand -hex 24
```

- `POSTGRES_PASSWORD`, `REDIS_PASSWORD`, `MINIO_ROOT_PASSWORD`, `JWT_SECRET_KEY` 는 필수다. 비어 있으면 compose 가 시작을 거부한다.
- 소셜 로그인 콘솔의 리디렉션 URI 는 `https://moarang.kro.kr/login/oauth2/code/{google|kakao|naver}` 로 등록한다.
- 프론트엔드가 다른 도메인이면 `CORS_ALLOWED_ORIGINS`, `FRONTEND_URL` 을 그 주소로 바꾼다.

### 5. 컨테이너 실행

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

첫 빌드는 Gradle 의존성을 받느라 몇 분 걸린다. 앱이 뜨면 Flyway 가 스키마를 만든다.

```bash
curl -s http://127.0.0.1:8080/actuator/health
```

### 6. HTTPS (호스트 Nginx)

인증서를 먼저 발급한다. `certonly` 는 기존 Nginx 설정을 고치지 않는다.

```bash
sudo apt install -y certbot python3-certbot-nginx
```

```bash
sudo certbot certonly --nginx -d moarang.kro.kr
```

사이트 설정을 넣고 다시 읽는다.

```bash
sudo cp deploy/host-nginx/moarang.kro.kr.conf /etc/nginx/sites-available/moarang.kro.kr
```

```bash
sudo ln -sf /etc/nginx/sites-available/moarang.kro.kr /etc/nginx/sites-enabled/moarang.kro.kr
```

```bash
sudo nginx -t && sudo systemctl reload nginx
```

인증서는 certbot 타이머가 자동 갱신한다. 갱신 후 Nginx 를 다시 읽게 한다.

```bash
printf '#!/bin/sh
systemctl reload nginx
' | sudo tee /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh && sudo chmod +x /etc/letsencrypt/renewal-hooks/deploy/reload-nginx.sh
```

```bash
sudo certbot renew --dry-run
```

### 7. 확인

```bash
curl -sI http://moarang.kro.kr | head -3
```

```bash
curl -s https://moarang.kro.kr/actuator/health
```

- 첫 번째는 `301` 과 `Location: https://moarang.kro.kr/` 이 나와야 한다.
- WebSocket 은 프론트에서 `wss://moarang.kro.kr/ws` 로 연결하고, CONNECT 헤더에 `Authorization: Bearer {accessToken}` 을 넣는다.

## 업데이트

```bash
git pull && docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

바뀐 서비스만 다시 만든다. 앱 컨테이너가 바뀌어도 nginx 는 내부 DNS 로 새 주소를 찾으므로 재시작하지 않아도 된다.

## 운영 명령

로그 (앱 로그는 `app-logs` 볼륨의 파일에도 남는다):

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml logs -f app
```

DB 백업:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml exec -T postgres pg_dump -U moa -d moa | gzip > moa_$(date +%Y%m%d).sql.gz
```

중지 (`down -v` 는 DB·파일 볼륨을 지우므로 쓰지 않는다):

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml down
```

## 설계 메모

- **X-Forwarded-Proto:** 호스트 Nginx 를 거쳐 온 요청(Docker 게이트웨이·루프백)만 Docker nginx 가 믿는다. 8080 으로 직접 들어온 요청이 https 라고 속여도 앱은 http 로 처리한다. 앱은 `server.forward-headers-strategy: native` 로 이 헤더를 읽어 OAuth 리디렉션 주소를 `https://moarang.kro.kr` 로 만든다.
- **파일 URL:** 앱은 내부 주소(`http://minio:9000`)로 업로드하고, 브라우저에 주는 URL 은 `PUBLIC_URL/{S3_BUCKET}/...` 로 만든다. 서명 URL 은 Host 와 경로가 서명에 포함되므로 두 Nginx 모두 경로를 바꾸지 않고 Host 를 그대로 넘긴다. 그래서 버킷 이름은 API 경로와 겹치지 않아야 한다.
- **8080 직접 접속:** 평문 HTTP 다. `COOKIE_SECURE=true` 라서 리프레시 토큰 쿠키는 HTTPS 에서만 동작한다. 8080 은 점검용으로만 쓴다.
- **로그 프로필:** `prod` 프로필은 콘솔 로그를 끄므로(logback-spring.xml) 켜지 않았다. 기본 프로필이라 `docker logs` 와 파일에 모두 남는다.
- **서버 한 대 기준:** STOMP 는 인메모리 브로커라 앱 컨테이너를 여러 개로 늘리면 실시간 전송이 인스턴스 사이에 전달되지 않는다.
