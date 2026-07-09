# 빌드 스테이지 (빌드 캐시에만 남음)
# 빌드 시 사용되는 임시 공간이라고 보면 된다 - 때문에 빌드하는 공간에 그만큼의 임시 여유공간이 필요하다.
FROM eclipse-temurin:26-jdk-jammy AS builder

WORKDIR /app

# 의존성 캐시
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon || true

# 소스 복사 후 빌드 (테스트 제외)
COPY src src
RUN ./gradlew clean bootJar -x test --no-daemon

# 실행 스테이지 (빌드 도구 제외)
FROM eclipse-temurin:26-jre-jammy

# non-root 사용자 생성
RUN groupadd -g 1000 spring && \
    useradd -u 1000 -g spring -s /usr/sbin/nologin -M spring

ENV TZ=Etc/UTC
WORKDIR /app

# 빌드 스테이지에서 jar만 가져옴
# CMD 경로 일치를 위해 이름 지정
COPY --from=builder /app/build/libs/*.jar app.jar

# 권한 한번에 부여
RUN chmod 400 app.jar && \
    chown spring:spring /app && \
    chmod 500 /app

USER spring
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]