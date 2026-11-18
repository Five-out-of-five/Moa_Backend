# syntax=docker/dockerfile:1
# Next.js 프론트 운영 이미지. (deploy/cdn/docker-compose.yml 의 front)
#
# 프론트 소스는 프론트 저장소(Moa_Front)를 그대로 쓰고, 이 파일만 백엔드 저장소에 둔다.
# compose 가 프론트 저장소 폴더를 이름 있는 컨텍스트 `front_src` 로 넘겨준다.
#
# NEXT_PUBLIC_* 값은 빌드할 때 화면 코드에 박힌다. 그래서 env/front.env 를 빌드 비밀(front_env)로
# 잠깐 .env.production 으로 붙여 빌드하고, 이미지에는 남기지 않는다. 서버 전용 값은 실행할 때 env_file 로 받는다.

FROM node:22-alpine AS build
WORKDIR /app

# 의존성 설치를 소스 복사보다 먼저 두어, 소스만 바뀌면 이 단계는 캐시를 쓴다.
COPY --from=front_src package.json package-lock.json ./
RUN npm ci

# 서버에 남아 있을 수 있는 node_modules·.next·로컬 env 파일은 빼고 소스만 가져온다.
COPY --from=front_src . /src
RUN cd /src \
    && tar --exclude=./node_modules --exclude=./.next --exclude='./.env*' --exclude=./.git -cf - . | (cd /app && tar -xf -) \
    && rm -rf /src

ENV NEXT_TELEMETRY_DISABLED=1
RUN --mount=type=secret,id=front_env,target=/app/.env.production npm run build


FROM node:22-alpine
WORKDIR /app

ENV NODE_ENV=production \
    NEXT_TELEMETRY_DISABLED=1 \
    PORT=3000 \
    HOSTNAME=0.0.0.0

# next.config.ts 를 실행할 때 읽으므로 개발 의존성까지 포함한 node_modules 를 그대로 옮긴다.
COPY --from=build --chown=node:node /app ./
USER node

EXPOSE 3000
CMD ["npm", "start"]
