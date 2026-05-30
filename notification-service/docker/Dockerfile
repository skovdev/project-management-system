FROM eclipse-temurin:21-jre

EXPOSE 8084

ADD ../wait-for-it.sh /wait-for-it.sh

WORKDIR /app

COPY ../target/notification-service-1.0.jar /app

ENTRYPOINT ["java", "-jar", "notification-service-1.0.jar"]
