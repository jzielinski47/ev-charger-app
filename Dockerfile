FROM amazoncorretto:21-alpine3.23

WORKDIR /app

COPY build/libs/evcm-app.jar /app/evcm-app.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/evcm-app.jar"]