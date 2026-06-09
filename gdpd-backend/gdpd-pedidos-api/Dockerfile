# Stage 1: Build
FROM maven:3.8-openjdk-11 AS builder

WORKDIR /build

# Copy pom.xml
COPY pom.xml .

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:11-jre-slim

WORKDIR /app

# Metadata
LABEL maintainer="GDPD Release Team"
LABEL version="1.0.0"
LABEL description="GDPD Pedidos API - Core service for order management"

# Copy JAR from builder
COPY --from=builder /build/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
