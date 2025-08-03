FROM openjdk:21

EXPOSE 8761

ADD ../wait-for-it.sh /wait-for-it.sh

WORKDIR /app

COPY ../target/service-discovery-1.0.jar /app

ENTRYPOINT ["java", "-jar", "service-discovery-1.0.jar"]