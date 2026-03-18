FROM eclipse-temurin:21-jre

EXPOSE 8082

ADD ../wait-for-it.sh /wait-for-it.sh

WORKDIR /app

COPY ../target/user-service-1.0.jar /app

ENTRYPOINT ["java", "-jar", "user-service-1.0.jar"]