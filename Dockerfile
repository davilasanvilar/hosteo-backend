FROM openjdk:17-jdk-alpine
COPY target/hosteoapi-0.0.1-SNAPSHOT.jar hosteoapi-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/hosteoapi-0.0.1-SNAPSHOT.jar"]
