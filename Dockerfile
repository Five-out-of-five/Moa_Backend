# 운영 이미지. 서버에서 docker compose -f docker-compose.prod.yml up --build 로 만든다. (docs/deploy.md)
# 빌드와 실행을 나눠 실행 이미지에는 JRE 와 jar 만 남긴다. 오라클 ARM(Ampere)·x86 모두 같은 파일로 빌드된다.

FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# 의존성 내려받기를 소스 복사보다 먼저 두어, 소스만 바뀌면 이 단계는 캐시를 쓴다.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
# Windows 에서 체크아웃해 CRLF 가 섞여도 실행되도록 줄바꿈을 정리한다.
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null

COPY src src
RUN ./gradlew --no-daemon bootJar -x test && cp build/libs/*.jar app.jar


FROM eclipse-temurin:25-jre
WORKDIR /app

RUN groupadd --system moa && useradd --system --gid moa --home /app moa \
    && mkdir -p /app/logs && chown -R moa:moa /app
COPY --from=build --chown=moa:moa /workspace/app.jar app.jar
USER moa

# 저장 시각은 UTC 로 통일한다. (응답의 KST 변환은 애플리케이션이 한다)
ENV TZ=UTC
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Duser.timezone=UTC", "-jar", "/app/app.jar"]
