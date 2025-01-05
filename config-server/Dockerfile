FROM openjdk:21
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} config-server.jar
ENTRYPOINT ["java","-jar","/config-server.jar"]
EXPOSE port 8088