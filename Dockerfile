FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY /build/libs/dcs-0.0.1-SNAPSHOT.jar dcs.jar

EXPOSE 8093

ENV DB_HOST=placeholder
ENV DB_SCHEMA=placeholder
ENV DB_USER=placeholder
ENV DB_PASSWORD=placeholder
ENV DB_PORT=placeholder
ENV REDIS_HOST=placeholder

ENTRYPOINT ["java", "-jar", "dcs.jar", "--spring.profiles.active=local"]