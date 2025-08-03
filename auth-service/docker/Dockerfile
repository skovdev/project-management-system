FROM openjdk:21

EXPOSE 8080

ADD ../wait-for-it.sh /wait-for-it.sh

WORKDIR /app

COPY ../target/auth-service-1.0.jar /app

ENTRYPOINT ["java", "-jar", "auth-service-1.0.jar"]