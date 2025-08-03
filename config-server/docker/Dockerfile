FROM openjdk:21

EXPOSE 8888

WORKDIR /app

COPY ../target/config-server-1.0.jar /app

ENTRYPOINT ["java", "-jar", "config-server-1.0.jar"]