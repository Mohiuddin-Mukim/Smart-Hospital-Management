# Build stage
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk-jammy
COPY --from=build target/hospital-management-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]