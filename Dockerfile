# Stage 1 - build
FROM maven:3.9-eclipse-temurin-21 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

#Stage 2 - runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
LABEL maintainer="shubrato1@gmail.com"
LABEL description="Job Tracker API - Spring Boot REST API for job application tracking"
COPY --from=build /app/target/job-tracker-api-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]