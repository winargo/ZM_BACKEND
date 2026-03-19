FROM openjdk:8-alpine

COPY target/billermanagement-*.jar /billermanagement.jar

CMD ["java", "-jar", "/billermanagement.jar"]