FROM openjdk:17-alpine

ENV APP_JAR="ui-service-0.0.1-SNAPSHOT.jar"

COPY target/${APP_JAR} /opt/application.jar

CMD ["java", "-jar", "/opt/application.jar"]