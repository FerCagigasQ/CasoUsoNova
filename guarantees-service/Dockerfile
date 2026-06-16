# Multi-stage build for guarantees-service

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /build

# Copy POM and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

RUN apk add --no-cache wget

WORKDIR /app

# Copy the JAR from builder stage
COPY --from=build /build/target/guarantees-service-*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
