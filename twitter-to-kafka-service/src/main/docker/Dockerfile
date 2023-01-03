FROM openjdk:17-alpine
LABEL maintainer="AlMLand"
COPY twitter-to-kafka-service-0.0.1-SNAPSHOT.jar twitter-to-kafka-service-0.0.1-SNAPSHOT.jar
CMD ["java","-jar", "--add-opens=java.base/java.nio=ALL-UNNAMED", "twitter-to-kafka-service-0.0.1-SNAPSHOT.jar"]