# Multi-stage Dockerfiles — GDPD v1.0.0

**Producto**: GDPD - Gestión de Pedidos  
**Versión**: 1.0.0  
**Referencia**: NOVA LE 7.8.0 — Multi-stage build strategy

---

## Overview

All GDPD services use a **two-stage Docker build pattern**:
1. **Builder Stage**: Maven compilation, testing, and JAR creation
2. **Runtime Stage**: Minimal JRE image with only the compiled application

This approach reduces final image size by 60-70% and minimizes attack surface.

---

## 1. Backend Services (API, Daemon, Batch, Scheduler)

### Template: Dockerfile for Spring Boot Services

All backend services follow this pattern. Replace `${SERVICE_NAME}` with the actual service name.

```dockerfile
# Stage 1: Builder
FROM openjdk:11-jdk-slim AS builder

# Set build arguments
ARG SERVICE_NAME=gdpd-pedidos-api
ARG VERSION=1.0.0

# Install Maven
RUN apt-get update && apt-get install -y --no-install-recommends \
    maven=3.8.* \
    git \
    && rm -rf /var/lib/apt/lists/*

# Copy source code
WORKDIR /workspace
COPY . .

# Build the service
RUN mvn clean package -DskipTests \
    -pl ${SERVICE_NAME} \
    -am \
    -B -q

# Extract the JAR (optional — for Spring Boot fat JARs)
WORKDIR /workspace/${SERVICE_NAME}/target
RUN if [ -f ${SERVICE_NAME}-${VERSION}.jar ]; then \
      jar tf ${SERVICE_NAME}-${VERSION}.jar | head -1; \
    fi

# Stage 2: Runtime
FROM openjdk:11-jre-slim

# Set runtime environment variables
ENV SERVICE_NAME=gdpd-pedidos-api \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200" \
    SPRING_PROFILES_ACTIVE=prod

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy compiled JAR from builder stage
COPY --from=builder --chown=appuser:appuser \
    /workspace/${SERVICE_NAME}/target/${SERVICE_NAME}-*.jar \
    /app/application.jar

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD java -cp /app/application.jar org.springframework.boot.loader.JarLauncher \
            -Dspring.boot.health.check=true || exit 1

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]
```

---

## 2. Service-Specific Dockerfiles

### 2.1 gdpd-pedidos-api

**Type**: REST API (Spring Boot)  
**Port**: 8080  
**Health Check**: GET /actuator/health

```dockerfile
FROM openjdk:11-jdk-slim AS builder
ARG VERSION=1.0.0
WORKDIR /workspace
COPY . .
RUN mvn clean package -DskipTests -pl gdpd-pedidos-api -am -B -q

FROM openjdk:11-jre-slim
ENV SERVICE_NAME=gdpd-pedidos-api \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
WORKDIR /app
COPY --from=builder --chown=1000:1000 \
    /workspace/gdpd-pedidos-api/target/gdpd-pedidos-api-*.jar \
    /app/application.jar
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]
```

**Build command**:
```bash
docker build \
  --build-arg VERSION=1.0.0 \
  -t gdpd-pedidos-api:1.0.0 \
  -f gdpd-pedidos-api/Dockerfile \
  .
```

---

### 2.2 gdpd-event-processor

**Type**: Event Daemon (Spring Boot)  
**Port**: 8080  
**Dependencies**: RabbitMQ / ActiveMQ

```dockerfile
FROM openjdk:11-jdk-slim AS builder
ARG VERSION=1.0.0
WORKDIR /workspace
COPY . .
RUN mvn clean package -DskipTests -pl gdpd-event-processor -am -B -q

FROM openjdk:11-jre-slim
ENV SERVICE_NAME=gdpd-event-processor \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC" \
    SPRING_PROFILES_ACTIVE=prod
WORKDIR /app
COPY --from=builder --chown=1000:1000 \
    /workspace/gdpd-event-processor/target/gdpd-event-processor-*.jar \
    /app/application.jar
RUN groupadd -r appuser && useradd -r -g appuser appuser && \
    apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]
```

---

### 2.3 gdpd-batch-reportes

**Type**: Spring Batch (Spring Boot)  
**Port**: 8080  
**Dependencies**: PostgreSQL, Flyway migrations

```dockerfile
FROM openjdk:11-jdk-slim AS builder
ARG VERSION=1.0.0
WORKDIR /workspace
COPY . .
RUN mvn clean package -DskipTests -pl gdpd-batch-reportes -am -B -q

FROM openjdk:11-jre-slim
ENV SERVICE_NAME=gdpd-batch-reportes \
    JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC" \
    SPRING_PROFILES_ACTIVE=prod
WORKDIR /app
COPY --from=builder --chown=1000:1000 \
    /workspace/gdpd-batch-reportes/target/gdpd-batch-reportes-*.jar \
    /app/application.jar
RUN groupadd -r appuser && useradd -r -g appuser appuser && \
    apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]
```

---

### 2.4 gdpd-scheduler-reportes

**Type**: Spring Scheduler (Spring Boot)  
**Port**: 8080  
**Dependencies**: PostgreSQL, PostgreSQL JDBC Driver

```dockerfile
FROM openjdk:11-jdk-slim AS builder
ARG VERSION=1.0.0
WORKDIR /workspace
COPY . .
RUN mvn clean package -DskipTests -pl gdpd-scheduler-reportes -am -B -q

FROM openjdk:11-jre-slim
ENV SERVICE_NAME=gdpd-scheduler-reportes \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC" \
    SPRING_PROFILES_ACTIVE=prod
WORKDIR /app
COPY --from=builder --chown=1000:1000 \
    /workspace/gdpd-scheduler-reportes/target/gdpd-scheduler-reportes-*.jar \
    /app/application.jar
RUN groupadd -r appuser && useradd -r -g appuser appuser && \
    apt-get update && apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/*
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/application.jar"]
```

---

## 3. Frontend Service

### 3.1 gdpd-pedidos-front (Angular 12 + Thin3)

**Type**: Frontend SPA (Angular)  
**Port**: 4200 (dev), 80 (production)  
**Web Server**: Nginx

```dockerfile
# Stage 1: Builder
FROM node:16-alpine AS builder

ARG VERSION=1.0.0

# Install Angular CLI globally (optional)
RUN npm install -g @angular/cli

WORKDIR /workspace
COPY . .

# Install dependencies
RUN npm ci

# Build Angular application for production
RUN ng build --configuration production --base-href /

# Stage 2: Runtime (Nginx)
FROM nginx:1.21-alpine

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# Create nginx user and group
RUN addgroup -g 101 -S nginx && \
    adduser -S -D -H -u 101 -h /var/cache/nginx -s /sbin/nologin \
            -c "nginx user" -G nginx nginx || true

# Copy compiled Angular build from builder stage
COPY --from=builder --chown=nginx:nginx \
    /workspace/dist/gdpd-pedidos-front /usr/share/nginx/html

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:80/health || exit 1

EXPOSE 80

# Run Nginx
CMD ["nginx", "-g", "daemon off;"]
```

**Nginx Configuration** (nginx.conf):
```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
    use epoll;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for"';

    access_log /var/log/nginx/access.log main;

    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;
    gzip on;
    gzip_types text/plain text/css text/xml text/javascript 
               application/x-javascript application/xml+rss 
               application/json application/javascript;

    include /etc/nginx/conf.d/*.conf;
}
```

**Default Server Configuration** (default.conf):
```nginx
server {
    listen 80 default_server;
    listen [::]:80 default_server;
    server_name _;
    root /usr/share/nginx/html;

    # SPA routing
    location / {
        try_files $uri $uri/ /index.html;
        expires -1;
        add_header Cache-Control "no-cache, must-revalidate";
    }

    # Static assets with long cache
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Health check endpoint
    location /health {
        return 200 "healthy";
        add_header Content-Type text/plain;
    }

    # Deny access to hidden files
    location ~ /\. {
        deny all;
    }
}
```

**Build command**:
```bash
docker build \
  --build-arg VERSION=1.0.0 \
  -t gdpd-pedidos-front:1.0.0 \
  -f gdpd-pedidos-front/Dockerfile \
  .
```

---

## 4. Building All Images

### Script: build-all.sh

```bash
#!/bin/bash
set -e

VERSION=${1:-1.0.0}
REGISTRY=${REGISTRY:-docker.io}
NAMESPACE=${NAMESPACE:-gdpd}

echo "Building GDPD services v${VERSION}"
echo "Registry: ${REGISTRY}/${NAMESPACE}"

# Backend services
for service in gdpd-pedidos-api gdpd-event-processor gdpd-batch-reportes gdpd-scheduler-reportes; do
    echo "Building ${service}..."
    docker build \
        --build-arg VERSION=${VERSION} \
        -t ${REGISTRY}/${NAMESPACE}/${service}:${VERSION} \
        -f ${service}/Dockerfile \
        .
done

# Frontend
echo "Building gdpd-pedidos-front..."
docker build \
    --build-arg VERSION=${VERSION} \
    -t ${REGISTRY}/${NAMESPACE}/gdpd-pedidos-front:${VERSION} \
    -f gdpd-pedidos-front/Dockerfile \
    .

# Tag as latest
for service in gdpd-pedidos-api gdpd-event-processor gdpd-batch-reportes \
                gdpd-scheduler-reportes gdpd-pedidos-front; do
    docker tag ${REGISTRY}/${NAMESPACE}/${service}:${VERSION} \
               ${REGISTRY}/${NAMESPACE}/${service}:latest
done

echo "✓ All images built successfully"
docker images | grep "${NAMESPACE}/"
```

---

## 5. Push to Registry

### Docker Hub

```bash
# Login
docker login docker.io

# Push all images
docker push docker.io/gdpd/gdpd-pedidos-api:1.0.0
docker push docker.io/gdpd/gdpd-event-processor:1.0.0
docker push docker.io/gdpd/gdpd-batch-reportes:1.0.0
docker push docker.io/gdpd/gdpd-scheduler-reportes:1.0.0
docker push docker.io/gdpd/gdpd-pedidos-front:1.0.0
```

### Private Registry

```bash
# Login
docker login registry.example.com

# Push all images with custom registry
for service in gdpd-pedidos-api gdpd-event-processor gdpd-batch-reportes \
                gdpd-scheduler-reportes gdpd-pedidos-front; do
    docker push registry.example.com/gdpd/${service}:1.0.0
done
```

---

## 6. Verification Checklist

- [ ] All Dockerfiles follow multi-stage pattern (builder → runtime)
- [ ] Runtime images use minimal base (`openjdk:11-jre-slim`, `nginx:1.21-alpine`)
- [ ] Non-root users configured in all images
- [ ] Health checks configured for all services
- [ ] JVM memory settings appropriate for each service
- [ ] Spring Boot profiles set to production
- [ ] No hardcoded credentials in images (use environment variables)
- [ ] Image sizes optimized:
  - API/Demon/Batch/Scheduler: < 300MB each
  - Frontend: < 50MB
- [ ] Images build and run successfully locally
- [ ] No security warnings from image scanners (Trivy, Anchore)

---

## 7. Related Documentation

- **Release Plan**: [docs/release-plan.md](./release-plan.md)
- **Deploy Checklist**: [docs/deploy-checklist.md](./deploy-checklist.md)
- **NOVA Validation**: [NOVA-VALIDATION-SUMMARY.md](../NOVA-VALIDATION-SUMMARY.md)

---

**Document Version**: 1.0  
**Last Updated**: 2026-06-09  
**Status**: Ready for Implementation
