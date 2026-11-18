# 운영 배포 — EC2 + CloudFront (Nginx 없음)

EC2 한 대에 Docker 로 프론트(3000)·백엔드(8080)·Redis·AI 서버를 띄우고, 앞에 CloudFront 를 둔다.
HTTPS 와 경로 나누기는 CloudFront 가 맡는다. DB 는 RDS, 파일은 S3, DNS 는 Route 53 을 쓴다.

```
브라우저 ── https://moayo.site ──▶ CloudFront (HTTPS, ACM 인증서)
                                        │
   /api/v1/*  /oauth2/*  /login/oauth2/* │──▶ EC2:8080  app   (Spring)
   /ws*  /swagger-ui*  /v3/api-docs*     │
   그 밖의 모든 경로 (기본)                │──▶ EC2:3000  front (Next)

app ─▶ redis(내부망) · ai(내부망 8000) · RDS · S3
```

- 브라우저 입장에서는 주소가 하나라서 쿠키·CORS·SameSite 가 단순하다(D안과 같은 효과).
- **포트가 아니라 경로로 나눈다.** `https://moayo.site:8080` 처럼 포트를 붙이면 열리지 않는다(CloudFront 는 80·443 만 받는다).
  백엔드는 `https://moayo.site/api/v1/...`, `https://moayo.site/swagger-ui/index.html` 로 들어간다.

| 파일 | 역할 |
|---|---|
| `docker-compose.prod.yml` (저장소 루트) | 서비스 4개 (app, front, ai, redis) |
| `deploy/cdn/backend.Dockerfile` | Spring 이미지 (빌드 컨텍스트: 저장소 루트) |
| `deploy/cdn/front.Dockerfile` | Next 이미지 (소스: 프론트 저장소) |
| `.backend.env.example` (저장소 루트) | Spring 환경변수 (+ Redis 비밀번호) |
| `.front.env.example` (저장소 루트) | Next 환경변수 |
| `.python.env.example` (저장소 루트) | Python AI 서버 환경변수 |

AI 서버 이미지는 `Moa_Python/Dockerfile` 을 그대로 쓴다.

---

## 1. AWS 준비

### EC2
- **리전**: 아시아 태평양(서울) `ap-northeast-2`, Ubuntu AMI.
- **인스턴스 유형**: 실제로 돌 때 컨테이너 4개 합계는 약 1.1GB (Spring 약 950MB, Next 약 120MB, AI 약 50MB, Redis 수 MB).
  | 상황 | 유형 |
  |---|---|
  | 평소 운영·개발 기간 | `t3.small`(2GB) + **스왑 4GB** (아래) |
  | 본격 연동 테스트·발표, 자주 빌드할 때 | `m7i-flex.large`(8GB) 또는 유료 플랜이면 `t3.medium`(4GB) |
  - **무료 플랜**은 프리 티어 대상 유형만 고를 수 있다(`t3.micro`, `t3.small`, `c7i-flex.large`, `m7i-flex.large` 등). `t3.medium` 은 유료 플랜에서만 보인다.
  - `c7i-flex.large`(4GB)는 가용 영역 `ap-northeast-2a` 에서 지원되지 않아 `m7i-flex.large` 를 썼다. 이미 만든 인스턴스는 가용 영역을 옮길 수 없다.
  - 유형 변경: 인스턴스 **중지 → 작업 → 인스턴스 설정 → 인스턴스 유형 변경 → 시작**. ("종료"는 서버 삭제다. 누르지 않는다)
  - 2GB 에서도 스왑이 있으면 빌드는 되지만 20~30분 걸린다. 급하면 빌드할 때만 잠깐 올린다.
- **디스크**: 30GB (기본 8GB 는 이미지·빌드 캐시·스왑을 담기에 부족하다).
- **탄력적 IP** 를 할당해 연결한다. 인스턴스를 껐다 켜도 주소와 퍼블릭 DNS 가 바뀌지 않아 CloudFront 원본을 다시 고칠 필요가 없다.
- **보안 그룹 인바운드**
  | 유형 | 포트 | 소스 |
  |---|---|---|
  | SSH | 22 | 내 IP |
  | 사용자 지정 TCP | **3000-8080** | 관리형 접두사 목록 `com.amazonaws.global.cloudfront.origin-facing` |

  - 3000 과 8080 을 따로 넣으면 접두사 목록이 규칙 여러 개로 계산돼 **"규칙 수 한도 초과"** 로 저장이 안 된다. `3000-8080` 한 줄로 넣는다(그 사이 다른 포트는 서버에서 열려 있지 않다).
  - 0.0.0.0/0 으로 열면 CloudFront 를 거치지 않고 평문 HTTP 로 직접 들어올 수 있다. 점검용으로 잠깐 "내 IP" 만 열고 바로 지운다.
- **IAM 역할** (S3 접근): 이 버킷에 `s3:PutObject`, `s3:GetObject`, `s3:DeleteObject`, `s3:ListBucket` 만 허용하고 EC2 에 연결한다.
- **IMDS 홉 제한 2**: 앱이 컨테이너 안에서 돌기 때문에 기본값(1)이면 IAM 역할 자격증명을 못 받는다.
  ```bash
  aws ec2 modify-instance-metadata-options --instance-id {인스턴스ID} --http-tokens required --http-put-response-hop-limit 2
  ```
  (콘솔: 인스턴스 → 작업 → 인스턴스 설정 → 인스턴스 메타데이터 옵션 수정)

### RDS (PostgreSQL 17)
- 퍼블릭 액세스 **끔**. 보안 그룹 5432 인바운드는 **EC2 보안 그룹에서만** 허용.
- 파라미터 그룹 `timezone` = `UTC` (기본값).
- **DB 인스턴스(`moa-db`)와 데이터베이스(`moa`)는 다르다.** 생성할 때 "추가 구성 → 초기 데이터베이스 이름" 을 비워 두면 `moa` 데이터베이스가 없어서 앱이
  `FATAL: database "moa" does not exist` 로 계속 재시작한다. 이 값은 나중에 바꿀 수 없으므로 서버 실행 후 아래 [RDS 데이터베이스 만들기](#rds-데이터베이스-만들기) 로 만든다.

### S3
- 리전 `ap-northeast-2`, 객체 소유권 "ACL 비활성화", 버전 관리 끔.
- **기본 암호화는 SSE-S3**(Amazon S3 관리형 키). SSE-KMS 로 하면 익명 사용자가 `public/*` 이미지를 열 때 KMS 복호화 권한이 없어 프로필·방 대표 이미지가 깨진다.
- 퍼블릭 액세스 차단에서 **버킷 정책 관련 2개만 해제**하고(ACL 관련 2개는 유지), 버킷 정책으로 `public/*` 만 공개한다. 첨부 파일은 비공개(서명 URL).
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
- 공개 이미지 주소는 `https://{버킷}.s3.ap-northeast-2.amazonaws.com/public/...` 이 된다. 프론트가 `next/image` 로 보여 주려면 프론트 `next.config.ts` 의 `images.remotePatterns` 에 이 호스트를 추가해야 한다.

---

## 2. 도메인 (Route 53)

도메인은 GoDaddy 에서 샀고 DNS 는 **Route 53** 이 맡는다. GoDaddy DNS 는 루트 도메인(`moayo.site`)을 CloudFront 에 연결할 수 없다(루트 CNAME 불가).

1. Route 53 → **호스팅 영역 생성** → `moayo.site`, 퍼블릭.
2. GoDaddy → 내 도메인 → `moayo.site` → **DNS → 네임서버 탭 → 네임서버 변경 → 내 네임서버 사용** → Route 53 호스팅 영역의 NS 4개 입력(끝의 `.` 는 빼고).
3. 반영 확인. 레지스트리에 직접 물어보면 저장 즉시 확인된다.
   ```bash
   nslookup -type=NS moayo.site ns10.trs-dns.info
   ```
   응답 아래쪽에 `awsdns` 4개가 나오면 된다. 일반 DNS(`8.8.8.8`)는 캐시 때문에 늦게 바뀔 수 있다.
4. 네임서버를 옮기면 **GoDaddy DNS 레코드는 더 이상 쓰이지 않는다.** 필요한 레코드는 Route 53 에 다시 만든다(아래 3장 마지막).

---

## 3. CloudFront (Nginx 대신)

### 인증서 (ACM)
- **반드시 미국 동부(버지니아 북부, us-east-1)** 에서 발급한다. CloudFront 는 이 리전 인증서만 쓴다.
- 도메인: `moayo.site` (www 를 쓸 거면 `www.moayo.site` 도 함께).
- DNS 검증: 인증서 화면의 **"Route 53에서 레코드 생성"** 버튼을 누른다. 이 CNAME 은 **자동 갱신(약 1년마다) 때도 다시 확인**하므로 지우지 않는다. 없으면 1년 뒤 HTTPS 가 끊긴다.

### 원본 2개
배포 생성 마법사의 오리진 유형은 **Other** 를 고른다(로드 밸런서를 쓰지 않는다). 마법사에서는 원본을 하나만 만들 수 있으므로 백엔드 원본은 생성 후 **오리진 탭 → 원본 생성** 으로 추가한다.

| 이름 | 오리진 도메인 | 프로토콜 | HTTP 포트 | 오리진 경로 |
|---|---|---|---|---|
| `front-3000` | EC2 퍼블릭 IPv4 DNS (`ec2-x-x-x-x.ap-northeast-2.compute.amazonaws.com`) | **HTTP만 해당** | **3000** | **비움** |
| `api-8080` | 같음 | **HTTP만 해당** | **8080** | **비움** |

- 프로토콜이 "HTTPS만 해당"(기본값 443)이나 "뷰어 일치"면 CloudFront 가 EC2 의 443 으로 가서 **504** 가 난다.
- **오리진 경로는 반드시 비운다.** `/api-8080` 같은 값을 넣으면 모든 요청 앞에 붙어서 백엔드가 `/api-8080/swagger-ui/...` 를 받고, 로그인이 필요 없는 주소까지 전부 **401** 이 난다. "이름" 칸과 헷갈리지 않는다.
- 오리진 도메인은 프라이빗 DNS(`ip-172-31-…`)가 아니라 **퍼블릭** DNS 다.
- 응답 제한 시간 **60초** (기본 30초). AI 어시스턴트 스트리밍(SSE)이 첫 응답까지 오래 걸릴 수 있다.

### 동작(Behavior)
**동작 탭 → 동작 생성** 으로 아래 6개를 만든다. 새로 만든 동작은 기본값(`*`)보다 위에 들어간다.

| 경로 패턴 | 원본 |
|---|---|
| `/api/v1/*` | api-8080 |
| `/oauth2/*` | api-8080 |
| `/login/oauth2/*` | api-8080 |
| `/ws*` | api-8080 |
| `/swagger-ui*` | api-8080 |
| `/v3/api-docs*` | api-8080 |
| 기본값 (`*`) | front-3000 |

모든 동작 공통:
| 항목 | 값 |
|---|---|
| 뷰어 프로토콜 정책 | Redirect HTTP to HTTPS |
| 허용된 HTTP 방법 | GET, HEAD, OPTIONS, PUT, POST, PATCH, DELETE |
| 캐시 정책 | **CachingDisabled** |
| 원본 요청 정책 | **AllViewer** |

- 콘솔이 "EC2에 권장" 으로 `UseOriginCacheControlHeaders` 를 고르는데 **쓰지 않는다.** 사람마다 응답이 다른 API 를 캐시하면 다른 사용자의 응답이 섞일 수 있고, 캐시 키에 없는 `Authorization` 헤더가 원본으로 전달되지 않을 수 있다.
- 원본 요청 정책은 `AllViewerExceptHostHeader` 가 아니라 **`AllViewer`** 다. Next 의 Server Action 은 요청의 Origin 과 Host 가 같은지 검사하는데, Host 를 넘기지 않으면 Next 가 자기 주소를 `ec2-…:3000` 으로 알아 저장 요청을 거절할 수 있다.
- 기본값(`*`) 동작도 허용 방법을 전체로 둔다(Server Action 이 POST).
- `/login` 자체는 프론트 화면이다. Spring 으로 보내는 것은 `/login/oauth2/*` 뿐이다. `/login/*` 로 넣으면 프론트 로그인 화면까지 백엔드로 간다.
- 프론트 BFF 경로 `/api/rooms/...` 는 `/api/v1/*` 에 걸리지 않아 기본값(front)으로 간다.
- 외부에서 헬스 체크가 필요하면 `/actuator/health` 동작을 api-8080 으로 하나 더 만든다(없으면 이 주소는 프론트로 간다).
- 나중에 정적 파일만 캐시하고 싶으면 `/_next/static/*` → front-3000, CachingOptimized 동작을 기본값 위에 추가한다.

### 일반 설정 (대체 도메인)
배포 → **일반 → 설정 편집**
- 대체 도메인 이름: `moayo.site` (www 를 쓰면 `www.moayo.site` 도). 인증서에 포함된 도메인만 넣을 수 있다.
- 사용자 정의 SSL 인증서: 위 ACM 인증서.
- 보안 정책 TLSv1.2_2021(권장), HTTP/2 는 기본값 그대로.
- 대체 도메인에 없는 이름으로 들어오면 HTTPS 는 `ERR_SSL_VERSION_OR_CIPHER_MISMATCH`, HTTP 는 403 이 난다.

### Route 53 레코드
| 이름 | 유형 | 값 |
|---|---|---|
| (비움) = `moayo.site` | A, **별칭 켜기** | CloudFront 배포에 대한 별칭 → `dxxxx.cloudfront.net` |
| ACM 검증용 `_xxxx` | CNAME | ACM 의 "Route 53에서 레코드 생성" 버튼으로 생성 |
| (www 를 쓸 때) `www` | A, 별칭 | 같은 CloudFront 배포 |

수정 후 배포 목록의 상태가 "배포 중" → 날짜로 바뀔 때까지(3~5분) 기다린다.

---

## 4. 서버에서 실행

### 처음 한 번
Docker 는 우분투의 `docker.io` 가 아니라 공식 스크립트로 설치한다. `docker compose` 플러그인까지 함께 깔린다.
(`sudo apt install docker.io` 는 새 서버에서 `apt update` 전이면 "no installation candidate" 가 나고, compose 플러그인이 따로 필요하다)
```bash
curl -fsSL https://get.docker.com | sudo sh
```
```bash
sudo usermod -aG docker $USER && newgrp docker
```
```bash
docker --version && docker compose version
```

**스왑 4GB** (2GB 인스턴스에서는 필수. 메모리가 넘칠 때 리눅스가 Spring 을 강제로 끄는 것을 막는다):
```bash
sudo fallocate -l 4G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile && echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
```
```bash
free -h
```

디스크를 늘렸다면 파일시스템도 늘린다(`lsblk` 로 이름 확인):
```bash
sudo growpart /dev/nvme0n1 1 && sudo resize2fs /dev/nvme0n1p1
```

세 저장소를 홈에 나란히 받는다. **프론트 저장소 이름은 `Moa_Frontend` 이고, 폴더는 `Moa_Front` 로 받는다**(compose 가 이 이름을 찾는다).
```bash
cd ~ && git clone https://github.com/Five-out-of-five/Moa_Backend.git
```
```bash
cd ~ && git clone https://github.com/Five-out-of-five/Moa_Frontend.git Moa_Front
```
```bash
cd ~ && git clone https://github.com/Five-out-of-five/Moa_Python.git && cd Moa_Python && git checkout develop
```
```
~/Moa_Backend   ~/Moa_Front   ~/Moa_Python
```
배포할 브랜치는 각 저장소 담당자에게 확인한다. 다른 위치에 받았으면 실행 전에 `export FRONT_DIR=... AI_DIR=...` 로 지정한다(기본값은 백엔드 저장소 루트 기준 `../Moa_Front`, `../Moa_Python`).

### 환경변수 파일
제공받은 파일 3개를 **백엔드 저장소 루트**에 둔다. 형식은 같은 이름의 `.example` 파일을 본다.
```
~/Moa_Backend/.backend.env   Spring + Redis 비밀번호(SPRING_DATA_REDIS_PASSWORD)
~/Moa_Backend/.front.env     Next
~/Moa_Backend/.python.env    Python AI
```
```bash
cd ~/Moa_Backend && chmod 600 .backend.env .front.env .python.env
```
윈도우에서 만든 파일이면 숨은 문자(BOM)와 줄바꿈(CRLF)을 정리한다. BOM 이 붙으면 첫 줄의 키 이름이 깨진다.
```bash
sed -i '1s/^\xEF\xBB\xBF//; s/\r$//' .backend.env .front.env .python.env
```
필수 키가 있는지 **값은 보이지 않게** 확인한다(`1` 이 나와야 한다):
```bash
grep -c '^SPRING_DATA_REDIS_PASSWORD=.' .backend.env
```
없으면 서버에서 만들어 넣는다(Redis 와 Spring 만 쓰는 값이라 아무 값이어도 된다). 한 번만 실행한다.
```bash
printf '\nSPRING_DATA_REDIS_PASSWORD=%s\n' "$(openssl rand -hex 24)" >> .backend.env
```
예시 파일과 키 이름만 비교해 빠진 키를 찾는다(`<` 로 시작하는 줄이 빠진 키):
```bash
diff <(grep -o '^[A-Z_0-9]*' .backend.env.example | sort -u) <(grep -o '^[A-Z_0-9]*' .backend.env | sort -u)
```
- `.backend.env` 의 `AI_INTERNAL_API_KEY` 와 `.python.env` 의 `INTERNAL_API_KEY` 는 같은 값이어야 한다.
- 소셜 콘솔(구글·카카오·네이버)에 `https://moayo.site/login/oauth2/code/{google|kakao|naver}` 를 등록한다.

### 실행
```bash
cd ~/Moa_Backend && BUILDX_BAKE_ENTITLEMENTS_FS=0 docker compose -f docker-compose.prod.yml up -d --build
```
- 루트의 `docker-compose.yml` 은 로컬 개발용(PostgreSQL·Redis·MinIO)이라 운영 파일은 `-f docker-compose.prod.yml` 로 지정한다.
- **`BUILDX_BAKE_ENTITLEMENTS_FS=0` 이 필요하다.** 최신 Docker Compose 는 빌드를 Bake 로만 하고, 프로젝트 폴더 밖(`../Moa_Front`, `../Moa_Python`)을 읽으려면
  `additional privileges requested: pass "--allow=fs.read=.."` 로 멈춘다. `COMPOSE_BAKE=false` 는 최신 버전에서 듣지 않는다.
- 위 방법으로도 같은 메시지가 나오면 이미지를 하나씩 만든 뒤 띄운다(이미지 이름이 compose 와 같아 그대로 쓴다):
  ```bash
  docker build -t moa-backend:latest -f deploy/cdn/backend.Dockerfile .
  ```
  ```bash
  docker build -t moa-ai:latest ../Moa_Python
  ```
  ```bash
  docker build -t moa-front:latest -f deploy/cdn/front.Dockerfile --build-context front_src=../Moa_Front --secret id=front_env,src=.front.env deploy/cdn
  ```
  ```bash
  docker compose -f docker-compose.prod.yml up -d --no-build
  ```
- 첫 빌드는 의존성을 받느라 10분 가까이(2GB 인스턴스에서는 20~30분) 걸린다.

### RDS 데이터베이스 만들기
처음 한 번. `.backend.env` 의 접속 정보로 잠깐 PostgreSQL 도구 컨테이너를 띄워 `moa` 데이터베이스와 pgvector 를 만든다(값은 화면에 나오지 않는다).
```bash
cd ~/Moa_Backend && docker run --rm --env-file .backend.env postgres:17-alpine sh -c 'H=$(echo "$DB_URL" | sed -E "s#jdbc:postgresql://([^:/?]+).*#\1#"); PGPASSWORD="$DB_PASSWORD" psql "host=$H port=5432 user=$DB_USERNAME dbname=postgres sslmode=require" -c "CREATE DATABASE moa;"'
```
```bash
docker run --rm --env-file .backend.env postgres:17-alpine sh -c 'H=$(echo "$DB_URL" | sed -E "s#jdbc:postgresql://([^:/?]+).*#\1#"); PGPASSWORD="$DB_PASSWORD" psql "host=$H port=5432 user=$DB_USERNAME dbname=moa sslmode=require" -c "CREATE EXTENSION IF NOT EXISTS vector;"'
```
```bash
docker compose -f docker-compose.prod.yml restart app
```
마스터 사용자가 아니면 권한 오류가 난다. 앱이 처음 뜨면 Flyway 가 테이블을 만든다.

### 확인
서버 터미널에서 (`127.0.0.1` 은 서버 자신이다. 노트북 브라우저에서 열면 노트북으로 간다):
```bash
docker compose -f docker-compose.prod.yml ps
```
```bash
curl -sI http://127.0.0.1:3000 | head -1
```
```bash
curl -s http://127.0.0.1:8080/actuator/health
```
Spring 은 켜지는 데 30초~1분 걸린다. 재시작 직후 응답이 비어 있으면 잠시 뒤 다시 확인한다. `app` 의 `Up` 시간이 계속 몇 초로 초기화되면 재시작 반복이다.

외부(노트북)에서:
| 주소 | 정상 응답 |
|---|---|
| `https://moayo.site/` | 프론트 화면 |
| `https://moayo.site/api/v1/auth/signup-tickets/test` | 400 `AUTH_SIGNUP_TICKET_INVALID` |
| `https://moayo.site/api/v1/notices` | 401 `COMMON_UNAUTHORIZED` (로그인 전이라 정상) |
| `https://moayo.site/swagger-ui/index.html` | Swagger 화면 |

---

## 5. 문제 해결 (실제로 겪은 것)

| 증상 | 원인 | 해결 |
|---|---|---|
| `additional privileges requested: pass "--allow=fs.read=.."` | Bake 의 폴더 밖 읽기 검사 | `BUILDX_BAKE_ENTITLEMENTS_FS=0` 을 붙이거나 이미지를 하나씩 빌드 |
| `failed to get build context front_src: stat .../Moa_Front` | 프론트 저장소를 안 받음 | `Moa_Frontend.git` 을 `Moa_Front` 폴더로 clone |
| `dependency failed to start: container moa-redis-1 is unhealthy` | `.backend.env` 에 `SPRING_DATA_REDIS_PASSWORD` 없음(이름 오타·BOM 포함) | 4장 "환경변수 파일" 의 확인·추가 명령 |
| `app` 재시작 반복 + `database "moa" does not exist` | RDS 에 `moa` 데이터베이스 없음 | 4장 "RDS 데이터베이스 만들기" |
| `app` 재시작 반복 + `Connect timed out` (DB) | EC2 → RDS 5432 막힘 | RDS 보안 그룹 인바운드 5432 에 EC2 보안 그룹 허용 |
| CloudFront **504** | 원본 프로토콜·포트가 틀림, 또는 보안 그룹이 3000·8080 을 막음 | 원본 "HTTP만 해당" + 3000/8080, 보안 그룹 `3000-8080` ← CloudFront 접두사 목록 |
| 백엔드 경로가 전부 **401** (로그인 필요 없는 주소까지) | 원본의 **오리진 경로** 에 값이 들어감 | 오리진 경로 비우기 |
| 백엔드 경로가 Next 404 화면 | 그 경로의 동작이 없음 | 3장 동작 6개 추가 |
| `ERR_SSL_VERSION_OR_CIPHER_MISMATCH` / CloudFront 403 | 그 도메인이 대체 도메인·인증서에 없음 | 인증서 발급 후 대체 도메인 추가 |
| `DNS_PROBE_FINISHED_NXDOMAIN` (`moayo.site`) | 루트 도메인 레코드 없음 | Route 53 A 별칭 → CloudFront |
| `ERR_CONNECTION_REFUSED` (`127.0.0.1`) | 노트북 브라우저에서 서버 주소로 착각 | `curl 127.0.0.1` 은 서버 터미널에서 실행 |
| `Package 'docker.io' has no installation candidate` | 새 서버에서 `apt update` 전 | 공식 스크립트로 설치 |

---

## 업데이트
```bash
cd ~/Moa_Backend && git pull && cd ../Moa_Front && git pull && cd ../Moa_Python && git pull
```
```bash
cd ~/Moa_Backend && BUILDX_BAKE_ENTITLEMENTS_FS=0 docker compose -f docker-compose.prod.yml up -d --build
```
- env 파일만 바꿨으면 빌드 없이 `docker compose -f docker-compose.prod.yml up -d` 로 다시 띄운다.
- 단, `.front.env` 의 `NEXT_PUBLIC_*` 는 빌드할 때 화면 코드에 박히므로 바꾸면 `--build` 로 프론트를 다시 빌드해야 반영된다.

## 운영 명령
```bash
docker compose -f docker-compose.prod.yml logs -f app
```
원인 줄만 보기:
```bash
docker compose -f docker-compose.prod.yml logs app 2>&1 | sed 's/\x1b\[[0-9;]*m//g' | grep -E "Caused by|Description:|Action:|Application run failed" | grep -v "^\s*at " | sort | uniq -c | sort -rn | head -15
```
메모리 사용량:
```bash
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}"
```
```bash
docker compose -f docker-compose.prod.yml restart front
```
중지 (`down -v` 는 Redis 데이터를 지우므로 쓰지 않는다):
```bash
docker compose -f docker-compose.prod.yml down
```
인스턴스를 껐다 켜도 컨테이너는 `restart: unless-stopped` 라 자동으로 다시 뜬다.

## 비용 (무료 플랜 크레딧)
- 크레딧은 **결제 및 비용 관리 → 크레딧** 에서 본다. 크레딧이 다 떨어지면 무료 플랜 계정은 서비스가 멈춘다.
- **예산(Budgets)** 으로 월 비용 알림(예: 50%·80%)을 걸어 둔다.
- 8GB(`m7i-flex.large`)는 2GB(`t3.small`)보다 몇 배 비싸다. 평소에는 `t3.small` 로 두고 필요할 때만 올린다. 오래 안 쓰면 인스턴스를 중지한다.

## 주의
- **서버 한 대 기준**: STOMP 는 인메모리 브로커라 앱 컨테이너를 여러 개로 늘리면 실시간 전송이 인스턴스 사이에 전달되지 않는다.
- **OAuth 콜백 주소**: CloudFront 뒤에서는 Spring 이 자기 공개 주소를 알 수 없어 `.backend.env` 에 직접 지정한다(`SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_*_REDIRECTURI`). 도메인을 바꾸면 이 값과 각 콘솔을 함께 바꾼다.
- **8080 직접 접속**은 평문 HTTP 라 `COOKIE_SECURE=true` 인 refresh 쿠키가 동작하지 않는다. 점검용으로만 쓴다.
- **SSE 타임아웃**: CloudFront 원본 응답 제한 시간(최대 60초, 더 늘리려면 AWS 한도 증가 요청)을 넘겨 데이터가 끊기면 스트림이 잘린다. AI 서버 마감이 25초라 보통은 걸리지 않는다.
- **프론트 목 모드**: 화면에 `isMock = true` 가 보이면 `.front.env` 의 `NEXT_PUBLIC_USE_MOCK` 이 `false` 가 아닌 것이다. 실서버 로그인 흐름이 준비된 뒤 FE 와 정해서 바꾼다.
