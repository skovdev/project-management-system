FROM openjdk:20

EXPOSE 8085

ADD ../wait-for-it.sh /wait-for-it.sh

WORKDIR /app

COPY ../target/ai-service-1.0.jar /app

ENTRYPOINT ["java", "-jar", "ai-service-1.0.jar"]