# syntax=docker/dockerfile:1

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY pix-core/pom.xml pix-core/pom.xml
COPY pix-api/pom.xml pix-api/pom.xml
COPY pix-worker/pom.xml pix-worker/pom.xml
COPY pix-core/src pix-core/src
COPY pix-worker/src pix-worker/src
RUN mvn -q -pl pix-worker -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/pix-worker/target/pix-worker.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
