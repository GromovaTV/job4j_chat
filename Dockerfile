FROM eclipse-temurin:17.0.6_10-jre
WORKDIR /job4j_chat
ADD target/*.jar app.jar
ENTRYPOINT java -jar /job4j_chat/app.jar

