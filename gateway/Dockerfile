# Stage 1 - build
FROM maven:3.9-eclipse-temurin-21 as build
WORKDIR /app
COPY pom.xml .
# jwt-common (dependency)
COPY jwt-common/pom.xml ./jwt-common/
COPY jwt-common/src ./jwt-common/src
COPY application-service/pom.xml ./application-service/
COPY user-service/pom.xml ./user-service/

# gateway-service
COPY gateway/pom.xml ./gateway/
COPY gateway/src ./gateway/src
RUN mvn -pl gateway -am clean package -DskipTests

#Stage 2 - runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
LABEL maintainer="shubrato1@gmail.com"
LABEL description="Gateway Service application"
COPY --from=build /app/gateway/target/gateway-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]