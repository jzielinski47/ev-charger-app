FROM amazoncorretto:21-alpine3.23 AS build

WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
COPY src src

RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon bootJar

FROM amazoncorretto:21-alpine3.23

WORKDIR /app

COPY --from=build /workspace/build/libs/evcm-app.jar /app/evcm-app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/evcm-app.jar"]