FROM openjdk:13-jdk-slim
LABEL maintainer="ricardo.sanchez@globant.com"
VOLUME /tmp
EXPOSE 8004
ARG JAR_FILE=build/libs/ThinkGearReader-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} ThinkGearReader-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/ThinkGearReader-0.0.1-SNAPSHOT.jar"]
