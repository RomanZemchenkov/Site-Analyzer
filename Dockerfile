FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/SentenceSearcher-1.0-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
ENTRYPOINT ["java","-jar","/application.jar"]