FROM adoptopenjdk/openjdk11:alpine-slim
EXPOSE 8083
ADD target/transactions.jar app.jar
ENTRYPOINT ["java", "-jar","/app.jar"]