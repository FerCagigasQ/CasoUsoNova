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
LABEL description="GDPD Batch Reportes - Spring Batch job for report generation"
COPY --from=builder /build/target/*.jar app.jar
HEALTHCHECK --interval=60s --timeout=10s --start-period=40s --retries=3 \
  CMD [ -f /app/health.flag ] && echo "OK" || exit 1
ENTRYPOINT ["java", "-Xmx1024m", "-Xms512m", "-jar", "app.jar"]
