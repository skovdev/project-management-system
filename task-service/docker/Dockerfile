FROM openjdk:20

EXPOSE 8084

ADD ../wait-for-it.sh /wait-for-it.sh

WORKDIR /app

COPY ../target/task-service-1.0.jar /app

ENTRYPOINT ["java", "-jar", "task-service-1.0.jar"]