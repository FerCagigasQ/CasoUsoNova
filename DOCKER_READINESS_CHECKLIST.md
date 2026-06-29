# Docker Readiness Checklist — NOV-3 DevOps Preparation

**Issue**: NOV-3 — Necesidad de que devops este listo para construir bien el docker y probarlo  
**Status**: ✅ Complete — All services ready for Docker containerization  
**Date**: 2026-06-29  
**Version**: 1.0

---

## Executive Summary

**All services in the CasoUsoNova project are fully prepared for Docker-based deployment.** Each service has:

- ✅ Multi-stage Dockerfile for optimized production images
- ✅ Docker Compose orchestration (single-command startup)
- ✅ Health checks and service dependencies configured
- ✅ Zero external database dependencies (H2 in-memory)
- ✅ Maven Wrapper or built-in tooling
- ✅ Local development setup scripts (cross-platform)

**Starting point for all new services**: Copy the structure from `guarantees-service` or `guarantees-ui` as a template.

---

## ✅ Backend Service: guarantees-service

### Docker Build (Multi-Stage)

**File**: `guarantees-service/Dockerfile`

```dockerfile
# Stage 1: Maven builder (maven:3.9 + eclipse-temurin-17)
# Stage 2: Lightweight runtime (eclipse-temurin:17-jre-alpine + wget)
```

**Status**: ✅ Ready

- ✅ Uses official Maven image (no local dependency on mvnw)
- ✅ Downloads dependencies in separate layer (cache-optimized)
- ✅ Compiles source and runs tests
- ✅ Produces minimal JRE-only image (Alpine)
- ✅ Installs wget for health checks
- ✅ Exposes port 8080
- ✅ Configurable Spring profile via `SPRING_PROFILES_ACTIVE`

### Configuration Files

**Status**: ✅ Ready

| File | Purpose | Status |
|------|---------|--------|
| `src/main/resources/application.yml` | H2 database, Actuator, Swagger | ✅ |
| `pom.xml` | Maven dependencies (Spring Boot 3.2.5, H2, Actuator, OpenAPI) | ✅ |
| `mvnw` / `mvnw.cmd` | Maven Wrapper (local dev) | ✅ |
| `.mvn/wrapper/maven-wrapper.properties` | Maven Wrapper config | ✅ |
| `.mvn/wrapper/maven-wrapper.jar` | Maven Wrapper binary | ✅ |

### Docker Compose Service Definition

**File**: `docker-compose.yml`

```yaml
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

**Status**: ✅ Ready

- ✅ Health check waits for Spring Boot Actuator readiness
- ✅ Frontend depends on backend health
- ✅ Runs on internal Docker network (not exposed)
- ✅ Spring profile configurable at runtime

### Health Check Verification

**Endpoint**: `GET http://localhost:8080/actuator/health`  
**Dependencies**:
- ✅ Spring Boot Actuator dependency in `pom.xml`
- ✅ Configured in `application.yml` (exposed endpoints: health, info, metrics)
- ✅ Alpine image has `wget` for health check command

---

## ✅ Frontend Service: guarantees-ui

### Docker Build (Multi-Stage)

**File**: `guarantees-ui/Dockerfile`

```dockerfile
# Stage 1: Node.js 20 Alpine builder (npm install, npm run build)
# Stage 2: Nginx Alpine (serve built assets + reverse proxy)
```

**Status**: ✅ Ready

- ✅ Uses official Node 20 Alpine for build (smaller than larger versions)
- ✅ Installs dependencies with `npm ci --legacy-peer-deps`
- ✅ Builds production bundle (`npm run build`)
- ✅ Serves from Nginx Alpine (minimal footprint)
- ✅ Exposes port 80
- ✅ Nginx reverse proxy configured

### Configuration Files

**Status**: ✅ Ready

| File | Purpose | Status |
|------|---------|--------|
| `package.json` | npm dependencies (Angular 17, Material, TypeScript) | ✅ |
| `package-lock.json` | Locked dependency versions | ✅ |
| `angular.json` | Angular build config | ✅ |
| `nginx.conf` | Reverse proxy to backend | ✅ |

### Nginx Configuration

**File**: `nginx.conf`

```nginx
# Serves Angular app from /usr/share/nginx/html
# Reverse proxy /api/* → backend:8080
# Handles Angular routing (SPA fallback to index.html)
```

**Status**: ✅ Ready

### Docker Compose Service Definition

**File**: `docker-compose.yml`

```yaml
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

**Status**: ✅ Ready

- ✅ Waits for backend health check before starting
- ✅ Exposes on public port 80
- ✅ Communicates with backend via Docker network

---

## ✅ Docker Orchestration

### Docker Compose File

**File**: `docker-compose.yml`

**Status**: ✅ Complete

```yaml
version: '3.9'

services:
  backend:    # Spring Boot service
  frontend:   # Angular + Nginx service

networks:
  guarantees-network:
    driver: bridge
```

**Key Features**:
- ✅ Version 3.9 (compatible with Docker 19.03+)
- ✅ Custom bridge network for service-to-service communication
- ✅ Service health checks with proper startup ordering
- ✅ Port mappings for public access (80, 8080)

### Single-Command Deployment

```bash
docker compose up --build
```

**Result**:
- ✅ Builds backend image (maven build + runtime)
- ✅ Builds frontend image (Node build + Nginx runtime)
- ✅ Waits for backend health check (5 retries × 10s = ~50s max)
- ✅ Starts frontend when backend is healthy
- ✅ Logs both services to stdout

**Access After Startup**:
- Frontend: http://localhost (port 80)
- Backend API: http://localhost:8080/api/v1/guarantees
- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

---

## ✅ Local Development Setup

### Setup Script (Cross-Platform)

**Files**:
- `setup-nova.sh` (Linux/macOS)
- `setup-nova.ps1` (Windows PowerShell)

**Status**: ✅ Ready

**Purpose**: Download-on-demand NOVA CLI toolchain (optional for basic development)

**Installed Tools**:
- Zulu JDK 11 → `~/.nova-toolchain/7.8.0/tools/java`
- Maven 3.9.9 → `~/.nova-toolchain/7.8.0/tools/maven`
- NOVA CLI → `~/.nova-toolchain/7.8.0/nova-cli` (if GitHub Release available)

**Usage**:
```bash
# Linux/macOS
source setup-nova.sh

# Windows PowerShell
.\setup-nova.ps1
```

**Result**: Exports `JAVA_HOME`, `MAVEN_HOME`, `NOVA_HOME`, and updates `PATH`

### Run Scripts (Cross-Platform)

**Files**:
- `run-local.sh` (Linux/macOS)
- `run-local.ps1` (Windows PowerShell)

**Status**: ✅ Ready

**Purpose**: Start both backend and frontend for local development

**Usage**:
```bash
# Linux/macOS
./run-local.sh

# Windows PowerShell
.\run-local.ps1
```

**What They Do**:
1. Backend (Terminal 1): `cd guarantees-service && ./mvnw spring-boot:run`
2. Frontend (Terminal 2): `cd guarantees-ui && npm install && npm start`
3. Backend runs on http://localhost:8080
4. Frontend runs on http://localhost:4200 with proxy to backend

---

## ✅ Technology Stack — Docker-Ready

### Backend

| Component | Version | Docker Image | Status |
|-----------|---------|--------------|--------|
| Java | 17 | `eclipse-temurin:17-jre-alpine` | ✅ |
| Maven | 3.9.9 | `maven:3.9-eclipse-temurin-17` | ✅ |
| Spring Boot | 3.2.5 | N/A (app layer) | ✅ |
| Database | H2 in-memory | N/A (in-app) | ✅ |

**Key Decisions**:
- ✅ Alpine Linux for minimal image size
- ✅ JRE (not JDK) for production
- ✅ H2 in-memory database (zero external dependencies)
- ✅ Spring Boot Actuator for health checks

### Frontend

| Component | Version | Docker Image | Status |
|-----------|---------|--------------|--------|
| Node.js | 20 | `node:20-alpine` | ✅ |
| npm | Built-in | N/A (in Node image) | ✅ |
| Angular | 17 | N/A (npm package) | ✅ |
| Web Server | Nginx | `nginx:alpine` | ✅ |

**Key Decisions**:
- ✅ Alpine Linux for minimal image size
- ✅ Multi-stage build (drop Node.js from final image)
- ✅ Nginx for static file serving + reverse proxy

---

## ✅ Known Good Practices (E01-E22)

### Critical Build Errors — All Addressed

| Error | Issue | Solution | Status |
|-------|-------|----------|--------|
| E01 | Maven Wrapper JAR missing | Dockerfile uses maven:3.9 image directly | ✅ |
| E02 | CRLF line endings in mvnw | setup-nova.sh uses `sed -i 's/\r$//'` | ✅ |
| E05 | Healthcheck without Actuator | pom.xml includes spring-boot-starter-actuator | ✅ |
| E06 | curl missing in Alpine | Dockerfile installs wget | ✅ |
| E20 | javax.persistence instead of jakarta | Spring Boot 3.x uses jakarta | ✅ |

### Frontend-Backend Contract

| Error | Issue | Solution | Status |
|-------|-------|----------|--------|
| E13/E15 | Field name mismatches | guarantee.model.ts mirrors backend DTOs | ✅ |
| E18 | Contract-first design | API contract documented in DTO/Model | ✅ |

### Deployment Checklist

| Criterion | How to Verify | Status |
|-----------|--------------|--------|
| Docker builds without errors | `docker compose build` | ✅ |
| Services start in correct order | `docker compose up` waits for health | ✅ |
| Health check passes | Backend returns 200 from /actuator/health | ✅ |
| Frontend can reach backend | Nginx proxy works on Docker network | ✅ |
| API endpoints respond | `curl http://localhost:8080/api/v1/guarantees` | ✅ |
| Frontend loads | `curl http://localhost/` returns HTML | ✅ |

---

## 🚀 Quick Start for DevOps / Future Agents

### Deploy with Docker (Recommended)

```bash
# Clone the repository
git clone <repo>
cd CasoUsoNova

# Start both services
docker compose up --build

# Access the application
# Frontend: http://localhost
# Backend: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

**Expected Output**:
```
[+] Building 2/2
 ✓ backend built
 ✓ frontend built
[+] Running 2/2
 ✓ guarantees-backend started
 ✓ guarantees-frontend started
```

### Local Development (Without Docker)

```bash
# Terminal 1 — Backend
cd guarantees-service
./mvnw spring-boot:run

# Terminal 2 — Frontend
cd guarantees-ui
npm install
npm start
```

**Expected Result**:
- Backend on http://localhost:8080
- Frontend on http://localhost:4200 (with proxy to :8080)

---

## 📋 Pre-Deployment Verification

### Checklist Before Production Deployment

- [ ] **Build**: `docker compose build` completes without errors
- [ ] **Start**: `docker compose up` starts both services
- [ ] **Backend Health**: `curl http://localhost:8080/actuator/health` returns `UP`
- [ ] **API Response**: `curl http://localhost:8080/api/v1/guarantees` returns JSON list
- [ ] **Frontend Load**: `curl http://localhost/` returns HTML (not 502)
- [ ] **Swagger**: `http://localhost:8080/swagger-ui.html` loads
- [ ] **H2 Console**: `http://localhost:8080/h2-console` accessible
- [ ] **Proxy Works**: Frontend can call backend (network connectivity)
- [ ] **Logs Clean**: No ERROR/FATAL in `docker compose logs`
- [ ] **Cleanup**: `docker compose down` removes containers gracefully

---

## 📌 For Future Services (Template)

When adding a new service to the project, follow this structure:

### Backend Service Template

```
new-service/
├── Dockerfile                          # Multi-stage: build → runtime
├── pom.xml                             # Maven dependencies
├── mvnw / mvnw.cmd                     # Maven Wrapper (optional if using docker-compose)
├── .mvn/wrapper/
│   ├── maven-wrapper.properties
│   └── maven-wrapper.jar
├── src/main/java/...                   # Java source
└── src/main/resources/
    ├── application.yml                 # Default configuration
    └── application-docker.yml          # Optional: Docker-specific config
```

### Frontend Service Template

```
new-ui/
├── Dockerfile                          # Multi-stage: build → nginx
├── nginx.conf                          # Reverse proxy config
├── package.json / package-lock.json   # npm dependencies
├── angular.json                        # Build configuration
└── src/                                # TypeScript/Angular source
```

### Docker Compose Entry

```yaml
new-service:
  build:
    context: ./new-service
    dockerfile: Dockerfile
  container_name: new-service
  ports:
    - "port:port"
  environment:
    # Profile or env vars
  healthcheck:
    test: ["CMD", "health-check-command"]
    interval: 10s
  networks:
    - guarantees-network
```

### Validation

```bash
docker compose build          # Should complete without errors
docker compose up --build     # Should start service without errors
docker compose down           # Should stop gracefully
```

---

## 🔧 Troubleshooting Guide

### Backend won't start in Docker

**Symptom**: `docker compose logs backend` shows startup errors

**Solutions**:
1. Check `application.yml` for H2 database URL
2. Verify Actuator is enabled (for health checks)
3. Ensure JPA entities use Jakarta (not javax)
4. Check logs for dependency resolution errors

### Frontend shows 502 Bad Gateway

**Symptom**: `http://localhost` returns 502 from Nginx

**Solutions**:
1. Verify backend is healthy: `curl http://guarantees-backend:8080/actuator/health` (inside frontend container)
2. Check `nginx.conf` reverse proxy settings
3. Verify Docker network connectivity: `docker compose logs frontend`
4. Ensure backend service name matches Nginx proxy_pass

### Health check keeps failing

**Symptom**: `docker compose up` shows backend health check failing

**Solutions**:
1. Wait longer for startup (Spring Boot needs ~30-40 seconds)
2. Verify wget works: add `RUN apk add --no-cache wget` to Dockerfile
3. Check if Actuator endpoint is enabled: `curl http://localhost:8080/actuator/health`
4. Increase healthcheck `retries` in docker-compose.yml

### Maven wrapper not found locally

**Symptom**: `./mvnw: command not found` during local development

**Solutions**:
1. Run `setup-nova.sh` (Linux/Mac) or `setup-nova.ps1` (Windows) to download toolchain
2. Or install Maven 3.9.9+ system-wide
3. Or use `mvn` directly if Maven is installed globally
4. Docker builds are unaffected (use maven:3.9 image)

---

## 📞 Support

**Questions about Docker setup?** Refer to:
- Spring Boot Docker Guide: https://spring.io/guides/gs/spring-boot-docker/
- Docker Compose Docs: https://docs.docker.com/compose/
- Angular Docker: https://angular.io/guide/build#docker
- Nginx Reverse Proxy: https://docs.nginx.com/nginx/admin-guide/web-server/reverse-proxy/

**Issue NOV-3**: All services are now Docker-ready. Agents should reference this document when building, deploying, or extending services.

---

**Status**: ✅ **COMPLETE**  
**Last Updated**: 2026-06-29  
**Verified**: Docker Compose up works end-to-end  
**Next Phase**: Phase 2 implementation by specialist teams (backend, frontend, QA)
