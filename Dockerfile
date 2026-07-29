# Build stage using a modern, officially supported Eclipse Temurin JDK image
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage using a lightweight Eclipse Temurin JRE image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/ticket-triage-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
