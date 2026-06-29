# Docker Quick Reference — CasoUsoNova Commands

**Purpose**: Fast command reference for common DevOps tasks  
**Quick Links**: [Full Checklist](./DOCKER_READINESS_CHECKLIST.md) | [Agent Guidelines](./DOCKER_AGENT_GUIDELINES.md) | [Verification](./DOCKER_VERIFICATION_SUMMARY.md)

---

## 🚀 Start Everything (1 Command)

```bash
docker compose up --build
```

**What it does**:
- Builds backend (Maven compile)
- Builds frontend (npm build)
- Starts backend on port 8080
- Starts frontend on port 80
- Waits for backend health check
- Then starts frontend

**Access**:
- Frontend: http://localhost
- Backend API: http://localhost:8080/api/v1/guarantees
- Swagger: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console

---

## 🛑 Stop Everything

```bash
docker compose down
```

Or gracefully with network cleanup:
```bash
docker compose down -v
```

---

## 🔨 Build (Without Starting)

```bash
docker compose build
```

Specific service:
```bash
docker compose build backend
docker compose build frontend
```

---

## 📋 Check Status

```bash
# See running containers
docker compose ps

# See service logs
docker compose logs -f

# Specific service
docker compose logs -f backend
docker compose logs -f frontend

# Follow logs only
docker compose logs -f --tail=50
```

---

## 🧪 Test Endpoints

### Backend Health
```bash
curl http://localhost:8080/actuator/health
# Expected: { "status": "UP" }
```

### Backend API
```bash
curl http://localhost:8080/api/v1/guarantees | jq .
# Expected: JSON array of guarantees
```

### Frontend
```bash
curl -I http://localhost/
# Expected: HTTP 200
```

### Swagger UI
```bash
curl -s http://localhost:8080/swagger-ui.html | grep -q "swagger" && echo "✓ Works"
```

---

## 🔍 Debug Specific Service

### Backend Not Starting?
```bash
docker compose logs backend

# Or run in foreground to see errors
docker compose run --rm backend
```

### Frontend Shows 502?
```bash
docker compose logs frontend

# Test backend connectivity from frontend
docker exec guarantees-frontend curl http://guarantees-backend:8080/actuator/health
```

### Health Check Failing?
```bash
# See healthcheck attempts
docker compose ps

# Manual healthcheck inside container
docker exec guarantees-backend wget -qO- http://localhost:8080/actuator/health

# Check port listening
docker exec guarantees-backend ss -ln | grep 8080
```

---

## 🐚 Interactive Shell Inside Container

```bash
# Backend (Java)
docker exec -it guarantees-backend /bin/sh

# Frontend (Node/Nginx)
docker exec -it guarantees-frontend /bin/sh
```

Then inside:
```bash
# Backend: test health endpoint
wget -qO- http://localhost:8080/actuator/health

# Frontend: test reverse proxy
curl http://guarantees-backend:8080/actuator/health
```

---

## 🏗️ Rebuild One Service

```bash
# Backend
docker compose build --no-cache backend
docker compose up backend

# Frontend
docker compose build --no-cache frontend
docker compose up frontend
```

---

## 🗑️ Clean Up Unused Images

```bash
# Remove unused images
docker image prune

# Remove unused volumes
docker volume prune

# Complete cleanup (careful!)
docker system prune -a
```

---

## 📊 Check Image Sizes

```bash
docker images | grep -E "guarantees|backend|frontend"
```

---

## 🔗 Network Debugging

```bash
# See Docker network
docker network ls

# Inspect our network
docker network inspect guarantees-network

# Test connectivity between services
docker exec guarantees-frontend ping -c 1 guarantees-backend
```

---

## 💾 Save/Load Images

```bash
# Save backend image to file
docker save guarantees-backend -o backend.tar

# Save frontend image
docker save guarantees-frontend -o frontend.tar

# Load from file
docker load -i backend.tar
docker load -i frontend.tar
```

---

## 🌍 Push to Registry (If Using Docker Hub/AWS/GCP)

```bash
# Tag images
docker tag guarantees-backend myregistry.azurecr.io/guarantees-backend:1.0
docker tag guarantees-frontend myregistry.azurecr.io/guarantees-frontend:1.0

# Login (example: Azure Container Registry)
az acr login --name myregistry

# Push
docker push myregistry.azurecr.io/guarantees-backend:1.0
docker push myregistry.azurecr.io/guarantees-frontend:1.0
```

---

## 📝 View Dockerfile

```bash
# Backend
cat guarantees-service/Dockerfile

# Frontend
cat guarantees-ui/Dockerfile
```

---

## ⚙️ Check docker-compose.yml

```bash
# Validate syntax
docker compose config

# See resolved configuration (with interpolated env vars)
docker compose config --resolve-image-digests
```

---

## 🚨 Common Errors & Fixes

### "Port 8080 already in use"
```bash
# Change port in docker-compose.yml
# From: "8080:8080"
# To: "8081:8080"  (external:internal)

# Then rebuild
docker compose up --build
```

### "Service unhealthy"
```bash
# Wait longer (Spring Boot startup takes 30-40s)
# Or check logs: docker compose logs backend

# Increase healthcheck retries in docker-compose.yml
healthcheck:
  retries: 10  # from 5
```

### "Cannot reach backend from frontend"
```bash
# Verify network connectivity
docker exec guarantees-frontend nslookup guarantees-backend

# Verify port
docker exec guarantees-backend netstat -ln | grep 8080
```

### "Maven/npm download errors"
```bash
# Clear Docker build cache
docker compose build --no-cache

# Or manually
docker builder prune
docker compose build
```

---

## 📱 Local Development (Without Docker)

```bash
# Terminal 1: Backend
cd guarantees-service
./mvnw spring-boot:run
# Runs on http://localhost:8080

# Terminal 2: Frontend
cd guarantees-ui
npm install
npm start
# Runs on http://localhost:4200 (with proxy to :8080)
```

Or use script:
```bash
# Linux/macOS
./run-local.sh

# Windows
.\run-local.ps1
```

---

## 🔄 Restart Single Service

```bash
docker compose restart backend
docker compose restart frontend
```

---

## 📤 Export Logs

```bash
# All logs to file
docker compose logs > logs.txt

# Specific service
docker compose logs backend > backend.log
docker compose logs frontend > frontend.log

# With timestamps
docker compose logs --timestamps
```

---

## 🎯 One-Liner: Full Cycle

```bash
# Build, start, test, show logs
docker compose build && docker compose up -d && sleep 10 && curl http://localhost && docker compose logs -f backend
```

---

## 📚 More Info

- Full documentation: [`DOCKER_READINESS_CHECKLIST.md`](./DOCKER_READINESS_CHECKLIST.md)
- Agent guidelines: [`DOCKER_AGENT_GUIDELINES.md`](./DOCKER_AGENT_GUIDELINES.md)
- Verification details: [`DOCKER_VERIFICATION_SUMMARY.md`](./DOCKER_VERIFICATION_SUMMARY.md)

---

**Last Updated**: 2026-06-29  
**Status**: ✅ Ready for Production
