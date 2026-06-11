# Multi-stage build for guarantees-service

# Stage 1: Build
FROM eclipse-temurin:17-jdk-focal AS builder

WORKDIR /app

# Copy Maven wrapper and POM
COPY .mvn .mvn
COPY mvnw* ./
COPY pom.xml ./

# Copy source code
COPY src ./src

# Build the application
RUN chmod +x ./mvnw && \
    ./mvnw clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-focal

WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /app/target/guarantees-service-*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
