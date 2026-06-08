# Stage 1 - build
FROM maven:3.9-eclipse-temurin-21 as build
WORKDIR /app
COPY pom.xml .
# jwt-common (dependency)
COPY jwt-common/pom.xml ./jwt-common/
COPY jwt-common/src ./jwt-common/src
COPY application-service/pom.xml ./application-service/
COPY gateway/pom.xml ./gateway/
# user-service
COPY user-service/pom.xml ./user-service/
COPY user-service/src ./user-service/src
RUN mvn -pl user-service -am clean package -DskipTests

#Stage 2 - runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
LABEL maintainer="subratokha@gmail.com"
LABEL description="Job Tracker API - Spring Boot REST API for job application tracking"
COPY --from=build /app/user-service/target/user-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]