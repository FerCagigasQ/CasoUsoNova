FROM maven:3.8-openjdk-11 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:11-jre-slim
WORKDIR /app
LABEL maintainer="GDPD Release Team"
LABEL version="1.0.0"
LABEL description="GDPD Event Processor - Asynchronous event processing daemon"
COPY --from=builder /build/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
