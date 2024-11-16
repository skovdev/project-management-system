FROM openjdk:latest

EXPOSE 8082

ADD ../wait-for-it.sh /wait-for-it.sh

WORKDIR /app

COPY ../target/project-service-1.0.jar /app

ENTRYPOINT ["java", "-jar", "project-service-1.0.jar"]

