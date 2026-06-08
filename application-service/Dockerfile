# Stage 1 - build
FROM maven:3.9-eclipse-temurin-21 as build
WORKDIR /app
COPY pom.xml .
COPY application-service/pom.xml ./application-service/
COPY application-service/src ./application-service/src
# Still need other module pom.xml files so Maven doesn't complain
COPY jwt-common/pom.xml ./jwt-common/
COPY user-service/pom.xml ./user-service/
COPY gateway/pom.xml ./gateway/
RUN mvn -pl application-service clean package -DskipTests

#Stage 2 - runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
LABEL maintainer="subratokha@gmail.com"
LABEL description="Application Service Application"
COPY --from=build /app/application-service/target/application-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]