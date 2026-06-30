FROM eclipse-temurin:26-jre-jammy

ENV TZ=Asia/Seoul

WORKDIR /app

COPY build/libs/amumal-0.0.2.jar app.jar

CMD ["java", "-jar", "-Dspring.profiles.active=dev8080", "app.jar"]
