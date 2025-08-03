FROM openjdk:21

EXPOSE 8762

ADD ../wait-for-it.sh /wait-for-it.sh

WORKDIR /app

COPY ../target/api-gateway-1.0.jar /app

ENTRYPOINT ["java", "-jar", "api-gateway-1.0.jar"]