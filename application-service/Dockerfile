# Stage 1 - build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY application-service/pom.xml ./application-service/
COPY application-service/src ./application-service/src
# Still need other module pom.xml files so Maven doesn't complain
COPY jwt-common/pom.xml ./jwt-common/
COPY user-service/pom.xml ./user-service/
COPY gateway/pom.xml ./gateway/
RUN mvn -pl application-service -am clean package -DskipTests

#Stage 2 - runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system appgroup && useradd --system --gid appgroup --create-home appuser
LABEL maintainer="shubrato1@gmail.com"
LABEL description="Application Service Application"
COPY --from=build --chown=appuser:appgroup /app/application-service/target/application-service-0.0.1-SNAPSHOT.jar app.jar
USER appuser
ENTRYPOINT ["java", "-jar", "app.jar"]