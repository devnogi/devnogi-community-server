# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace

COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src ./src

RUN chmod +x gradlew
RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jdk
WORKDIR /app

COPY --from=builder /workspace/build/libs/*.jar dcs.jar

EXPOSE 8093

ENV DB_HOST=placeholder \
    DB_SCHEMA=placeholder \
    DB_USER=placeholder \
    DB_PASSWORD=placeholder \
    DB_PORT=placeholder \
    REDIS_HOST=placeholder

ENTRYPOINT ["java", "-jar", "dcs.jar"]
