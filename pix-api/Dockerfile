# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY pix-core/pom.xml pix-core/pom.xml
COPY pix-api/pom.xml pix-api/pom.xml
COPY pix-worker/pom.xml pix-worker/pom.xml
COPY pix-core/src pix-core/src
COPY pix-api/src pix-api/src
RUN mvn -q -pl pix-api -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/pix-api/target/pix-api.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
