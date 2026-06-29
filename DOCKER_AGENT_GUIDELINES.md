# Docker Agent Guidelines — NOV-3 Implementation Standard

**Issue**: NOV-3 — Necesidad de que devops este listo para construir bien el docker y probarlo  
**Audience**: All agents (backend, frontend, QA, DevOps)  
**Purpose**: Unified Docker standards for all services in CasoUsoNova  
**Version**: 1.0

---

## 🎯 What This Means: "DevOps Ready"

All services must be built and deployed **entirely through Docker**. No external databases, no manual setup, no environment-specific workarounds. When a user runs `docker compose up --build`, everything should work.

**Key Principle**: *"If it doesn't work in Docker, it doesn't work."*

---

## ✅ Checklist: Before You Submit Code

### 1. **Your Service Has a Dockerfile**

Every service (backend, frontend, supporting tool) must have a `Dockerfile`.

**Backend (Java/Spring Boot)**:
```dockerfile
# Multi-stage:
# Stage 1: Maven builder
FROM maven:3.9-eclipse-temurin-17 AS builder
# ... build JAR ...

# Stage 2: Lightweight runtime
FROM eclipse-temurin:17-jre-alpine
# ... copy JAR and run ...
```

**Frontend (Node.js)**:
```dockerfile
# Multi-stage:
# Stage 1: Node builder
FROM node:20-alpine AS build
# ... build artifacts ...

# Stage 2: Nginx server
FROM nginx:alpine
# ... serve and proxy ...
```

**Other Services**:
- Database service? Use `docker:latest` for your DB language
- Python service? Use `python:3.11-alpine`
- Go service? Use `golang:1.21-alpine`

### 2. **Zero External Dependencies**

Your Docker image must include **everything** it needs to run.

✅ **OK**:
- Database: H2 (in-memory) or PostgreSQL service in docker-compose
- Cache: Redis service in docker-compose
- Message Queue: RabbitMQ service in docker-compose

❌ **NOT OK**:
- "Expects MySQL running on localhost:3306"
- "Requires environment variable `DATABASE_URL` pointing to external cloud DB"
- "Install [tool] manually before running"

**If you need external services**: Add them to `docker-compose.yml`:

```yaml
postgres:
  image: postgres:15-alpine
  environment:
    POSTGRES_PASSWORD: password
  networks:
    - guarantees-network
```

Then your service connects via `postgres:5432` on the Docker network.

### 3. **Docker Compose Entry**

Your service must be defined in `docker-compose.yml`:

```yaml
my-service:
  build:
    context: ./my-service       # Path to your service directory
    dockerfile: Dockerfile      # Must exist
  container_name: my-service
  ports:
    - "8081:8080"              # External:Internal
  environment:
    ENV_VAR: value             # Any config needed
    DATABASE_HOST: postgres    # Reference other services by name
  depends_on:
    postgres:                  # If this service needs postgres
      condition: service_healthy
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
    interval: 10s
    timeout: 5s
    retries: 5
  networks:
    - guarantees-network       # Must join the network
```

### 4. **Health Check Endpoint**

Every service must expose a health check endpoint so orchestration can wait for readiness.

**Backend (Spring Boot)**:
```bash
GET http://service:8080/actuator/health
```
→ Returns `{ status: "UP" }` when ready.

**Frontend (Nginx)**:
```bash
GET http://service:80/
```
→ Returns 200 (HTML) when ready.

**Custom Service**:
```bash
GET http://service:port/health
```
→ Returns 200 when ready.

**In docker-compose.yml**:
```yaml
healthcheck:
  test: ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health || exit 1"]
  interval: 10s
  timeout: 5s
  retries: 5
```

### 5. **Port Configuration**

Declare ports in both Dockerfile and docker-compose:

**Dockerfile**:
```dockerfile
EXPOSE 8080
```

**docker-compose.yml**:
```yaml
ports:
  - "8080:8080"    # External port (for user) : Internal port (in container)
```

**Conflict Resolution**: If port 8080 is taken:
- ✅ Assign a different external port: `"8081:8080"` (container still uses 8080)
- ❌ Don't change the internal port in the service (breaks other dependencies)

### 6. **Configuration (No Hardcoded Secrets)**

All configuration must be environment variables or config files (not hardcoded).

✅ **OK**:
```bash
docker run -e DATABASE_URL="postgres://host:5432/db" my-service
# or in docker-compose.yml:
environment:
  DATABASE_URL: postgres://postgres:5432/db
```

❌ **NOT OK**:
```java
String dbUrl = "jdbc:mysql://prod-server.com:3306/db";  // Hardcoded!
```

### 7. **Image Optimization**

Docker images should be small for faster CI/CD and deployment.

✅ **Good**:
- `eclipse-temurin:17-jre-alpine` (140 MB JRE-only)
- `node:20-alpine` (180 MB Node)
- Multi-stage builds (drop build tools from final image)
- `.dockerignore` file (exclude node_modules, .git, test files)

❌ **Avoid**:
- `ubuntu:latest` as base (4+ GB)
- `openjdk:17` (includes full JDK, 300+ MB)
- Copying `node_modules` into final image
- Including test files, source code in production image

### 8. **Environment Variables (3 Levels)**

```yaml
# Level 1: .env file (local overrides, NOT checked in)
POSTGRES_PASSWORD=dev-password

# Level 2: docker-compose.yml (defaults for all developers)
environment:
  LOG_LEVEL: INFO
  PROFILE: docker

# Level 3: Dockerfile (hardcoded defaults)
ENV JAVA_OPTS="-Xmx512m"
```

### 9. **Service Dependencies (Startup Order)**

Use `depends_on` with `condition: service_healthy`:

```yaml
frontend:
  depends_on:
    backend:
      condition: service_healthy  # Wait for backend health check
  # ... rest of config ...
```

This ensures:
1. Backend starts first
2. Frontend waits for backend health check to pass
3. No race conditions or "connection refused" errors

### 10. **Logging (Visible in `docker compose logs`)**

All logs must go to **stdout/stderr**, not files.

✅ **Good**:
```java
System.out.println("Service started");  // Goes to docker logs
logger.info("Service started");          // Goes to docker logs
```

❌ **NOT OK**:
```java
FileWriter fw = new FileWriter("/app/logs/app.log");  // Invisible in docker logs
```

**Why**: `docker compose logs` only captures stdout/stderr. Logs written to files inside the container are invisible to the outside world.

---

## 🚀 Testing Your Changes (Before Commit)

### Local Docker Test (5 minutes)

```bash
# Navigate to project root
cd CasoUsoNova

# Build all services
docker compose build

# Start all services (will timeout after ~1 min if healthchecks fail)
docker compose up --build

# In another terminal, verify:
curl http://localhost:8080/actuator/health    # Backend
curl http://localhost/                         # Frontend
docker compose logs backend                    # Check logs
docker compose logs frontend

# Stop when done
docker compose down
```

**Expected Result**:
- ✅ Both services start without errors
- ✅ Health checks pass (no "unhealthy" status)
- ✅ Logs show successful startup
- ✅ Can access endpoints without errors

### Quick Smoke Test

```bash
# API health
curl http://localhost:8080/actuator/health

# Frontend loads
curl -I http://localhost/  # Should return 200, not 502

# API endpoint works
curl http://localhost:8080/api/v1/guarantees | jq .  # Should return JSON

# Swagger
curl -s http://localhost:8080/swagger-ui.html | grep -q "Swagger" && echo "✓ Swagger works"
```

---

## 🔍 Debugging: When Something Breaks

### "Service failed to start"

```bash
# Check logs
docker compose logs backend
docker compose logs frontend

# Look for:
# - Port already in use → Change docker-compose.yml port mapping
# - Cannot find file → Check COPY paths in Dockerfile
# - Connection refused → Check depends_on and healthcheck
```

### "Container is unhealthy"

```bash
# Check healthcheck command
docker compose ps

# Run healthcheck manually inside container
docker exec guarantees-backend wget -qO- http://localhost:8080/actuator/health

# If it fails:
# - Service not ready yet (wait longer)
# - Endpoint doesn't exist (check application.yml)
# - Port mismatch (check Dockerfile EXPOSE vs service binding)
```

### "Cannot connect backend from frontend"

```bash
# Verify network
docker network ls  # Should show guarantees-network

# Test connection inside frontend container
docker exec guarantees-frontend curl http://guarantees-backend:8080/actuator/health

# If fails:
# - Service name mismatch (use container_name or service name)
# - Not on same network (check docker-compose.yml networks section)
# - Firewall (unlikely in Docker)
```

### "Docker image is huge"

```bash
# Check size
docker images | grep -i "my-service"

# Reduce:
# 1. Use alpine base images
# 2. Multi-stage build (drop build tools from final stage)
# 3. Create .dockerignore (exclude unnecessary files)
# 4. Clean package managers: RUN apt-get clean && rm -rf /var/lib/apt/lists/*
```

---

## 📋 Pre-Commit Checklist

Before you push your code:

- [ ] Dockerfile exists and builds without errors
- [ ] docker-compose.yml entry added for your service
- [ ] `docker compose build` succeeds
- [ ] `docker compose up --build` starts your service
- [ ] Health check endpoint exists and returns 200/UP
- [ ] All config is via environment variables (not hardcoded)
- [ ] No external database or tool required (or added to docker-compose)
- [ ] Logs appear in `docker compose logs <service>`
- [ ] `docker compose down` stops gracefully
- [ ] No secrets in git (passwords, API keys, connection strings)

---

## 🎓 Examples: Template Services

### Backend Template (Spring Boot)

```dockerfile
# Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -B -DskipTests

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache wget
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```yaml
# docker-compose.yml entry
backend:
  build:
    context: ./guarantees-service
    dockerfile: Dockerfile
  container_name: guarantees-backend
  ports:
    - "8080:8080"
  environment:
    SPRING_PROFILES_ACTIVE: docker
  healthcheck:
    test: ["CMD-SHELL", "wget -qO- http://localhost:8080/actuator/health || exit 1"]
    interval: 10s
    timeout: 5s
    retries: 5
  networks:
    - guarantees-network
```

### Frontend Template (Angular + Nginx)

```dockerfile
# Dockerfile
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci --legacy-peer-deps
COPY . .
RUN npm run build

FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/guarantees-ui/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

```yaml
# docker-compose.yml entry
frontend:
  build:
    context: ./guarantees-ui
    dockerfile: Dockerfile
  container_name: guarantees-frontend
  ports:
    - "80:80"
  depends_on:
    backend:
      condition: service_healthy
  networks:
    - guarantees-network
```

---

## 📞 Questions?

Refer to `DOCKER_READINESS_CHECKLIST.md` for complete deployment info, troubleshooting, and service templates.

**This is the standard for all code in CasoUsoNova.** If your service doesn't follow these rules, it won't be mergeable.

---

**Status**: ✅ **APPROVED FOR ALL AGENTS**  
**Effective Date**: 2026-06-29  
**Next Review**: After Phase 2 implementation
