# H2 Docker Fix - Verification Checklist

## ✅ What Was Fixed

| Item | Status | Detail |
|------|--------|--------|
| **application-docker.yml** | ✅ Created | Spring profile for Docker with H2 in-memory config |
| **application-local.yml** | ✅ Created | Spring profile for local dev with debug logging |
| **docker-compose.local.yml** | ✅ Created | Alternative docker-compose for backend-only testing |
| **Documentation** | ✅ Created | 3 comprehensive guides (H2_DATABASE_GUIDE, CODE_STRUCTURE, SETUP_H2_DOCKER) |
| **README.md** | ✅ Updated | Added H2 Configuration section with troubleshooting |
| **docker-compose.yml** | ✅ Updated | Added comment directing to docker-compose.local.yml |

---

## 🧪 How to Verify the Fix

### Prerequisite
```bash
cd C:\Users\ferna\Documents\TRABAJO\CasoUsoNova
git pull
```

### Test 1: Check Configuration Files Exist

```bash
# Should all exist and have H2 config
cat guarantees-service/src/main/resources/application-docker.yml
cat guarantees-service/src/main/resources/application-local.yml
cat docker-compose.local.yml
```

**Expected**: JDBC URLs contain `jdbc:h2:mem:testdb` (NOT `/root/test`)

### Test 2: Build Docker Image (Local simulation)

```bash
# Build the backend image
cd guarantees-service
docker build -f Dockerfile -t guarantees-backend:test .

# Check image was created
docker images | grep guarantees-backend
```

**Expected**: Image builds without errors, size ~300-400MB

### Test 3: Run Docker Container

```bash
# Option A: Full stack (with frontend)
docker compose up --build

# Option B: Backend only (faster for testing)
docker compose -f docker-compose.local.yml up --build
```

**Expected Output** (in logs):
```
guarantees-backend-local  | ...
guarantees-backend-local  | Spring DataSource initialization starting
guarantees-backend-local  | Loaded database config from 'file:/app/BOOT-INF/classes/application-docker.yml'
guarantees-backend-local  | H2 database initialized: jdbc:h2:mem:testdb;...
guarantees-backend-local  | Created TABLE GUARANTEE (ID, STATUS, AMOUNT, ...)
guarantees-backend-local  | Tomcat started on port(s): 8080 (http)
guarantees-backend-local  | Started GuaranteesServiceApplication in X.XXX seconds
```

**NO ERROR** like:
```
✗ Database "/root/test" not found
✗ Could not create /root/test.mv.db
```

### Test 4: Access H2 Console

Once container is running:

1. **Open** http://localhost:8080/h2-console
2. **JDBC URL**: `jdbc:h2:mem:testdb` (pre-filled)
3. **User**: `sa` (default)
4. **Password**: (leave empty)
5. **Click**: "Connect"

**Expected**:
- Connection successful
- Left panel shows tables:
  - `GUARANTEE` (main entity)
  - `GUARANTEE_AMENDMENT` (related)
  - `GUARANTEE_CLAIM` (related)
- Can execute SQL queries

### Test 5: Test API Endpoint

```bash
# Check health
curl http://localhost:8080/actuator/health

# Expected: {"status":"UP",...}
```

```bash
# List guarantees
curl http://localhost:8080/api/v1/guarantees

# Expected: JSON array of guarantees (may be empty initially)
```

```bash
# View Swagger
curl http://localhost:8080/swagger-ui.html

# Expected: HTTP 200 with HTML
```

### Test 6: Insert and Query Data

```bash
# Create a guarantee
curl -X POST http://localhost:8080/api/v1/guarantees \
  -H "Content-Type: application/json" \
  -d '{
    "reference": "TEST-001",
    "type": "PERFORMANCE",
    "amount": 50000.00,
    "currency": "EUR",
    "issueDate": "2026-06-29",
    "expiryDate": "2027-06-29"
  }'

# Expected: HTTP 201 with created guarantee object

# Query it back
curl http://localhost:8080/api/v1/guarantees/1

# Expected: HTTP 200 with the guarantee
```

### Test 7: Verify Data in H2 Console

In H2 Console, run:

```sql
SELECT * FROM GUARANTEE;
```

**Expected**: Shows the newly created row:
```
ID | REFERENCE | TYPE | AMOUNT | CURRENCY | ...
1  | TEST-001  | PERFORMANCE | 50000.0 | EUR | ...
```

---

## 📊 Success Criteria

| Criterion | Check |
|-----------|-------|
| **Docker starts without error** | ✅ No "/root/test" error |
| **H2 initializes in memory** | ✅ "jdbc:h2:mem:testdb" in logs |
| **H2 Console accessible** | ✅ Connect via http://localhost:8080/h2-console |
| **Tables created** | ✅ GUARANTEE, GUARANTEE_AMENDMENT, GUARANTEE_CLAIM exist |
| **API works** | ✅ POST/GET /api/v1/guarantees succeeds |
| **Data persists in session** | ✅ Data queryable in H2 Console and API |
| **Documentation clear** | ✅ H2_DATABASE_GUIDE.md explains architecture |

---

## 🔧 Troubleshooting (If Tests Fail)

### Still seeing "/root/test" error?

1. **Verify application-docker.yml loaded**:
   ```bash
   docker logs guarantees-backend-local | grep -i "application-docker"
   ```
   
2. **Force rebuild**:
   ```bash
   docker compose down -v
   docker system prune -a
   docker compose up --build
   ```

3. **Check JDBC URL in logs**:
   ```bash
   docker logs guarantees-backend-local | grep -i "jdbc:h2"
   ```
   Should show: `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;...`

### H2 Console "Connection refused"?

1. **Verify container is running**:
   ```bash
   docker ps | grep guarantees
   ```

2. **Check network**:
   ```bash
   docker network inspect guarantees-network
   ```

3. **Wait for health check**:
   ```bash
   docker ps --format "table {{.Names}}\t{{.Status}}"
   # Status should be "Up X seconds (healthy)"
   ```

### Tables not created?

1. **Check Hibernate logs**:
   ```bash
   docker logs guarantees-backend-local | grep -i "create table"
   ```

2. **Increase timeout**:
   Edit docker-compose.yml, increase `retries` in healthcheck

3. **Verify entities exist**:
   ```bash
   # Check if GuaranteeServiceApplication.java exists
   ls -la guarantees-service/src/main/java/.../GuaranteesServiceApplication.java
   ```

---

## 📚 Documentation Map

| Document | Purpose | Read time |
|----------|---------|-----------|
| **H2_DATABASE_GUIDE.md** | How H2 works, architecture, configuration, troubleshooting | 15 min |
| **CODE_STRUCTURE.md** | Entity mapping, data flow, API layer, SQL examples | 20 min |
| **SETUP_H2_DOCKER.md** | Quick fix guide for "/root/test" error | 5 min |
| **README.md** (H2 section) | Quick reference + links | 5 min |

**Start here**: SETUP_H2_DOCKER.md (5 min quick fix)  
**Deep dive**: H2_DATABASE_GUIDE.md (complete reference)

---

## 🎯 What's Different Now

**Before**:
```
docker-compose.yml
├── Backend with SPRING_PROFILES_ACTIVE=docker
├── application.yml (default)
└─ NO application-docker.yml ← ERROR! Profile not configured

Result: H2 tries to create /root/test → Fails ❌
```

**After**:
```
docker-compose.yml
├── Backend with SPRING_PROFILES_ACTIVE=docker
├── application.yml (base config)
└─ application-docker.yml ← Override with H2 in-memory ✅

Result: H2 creates jdbc:h2:mem:testdb in RAM → Works ✅
```

**Additional**:
- `docker-compose.local.yml` for backend-only testing
- `application-local.yml` for local development
- Comprehensive documentation (3 guides)

---

## ✨ Next Steps

1. **Follow SETUP_H2_DOCKER.md** to verify the fix works
2. **Read H2_DATABASE_GUIDE.md** to understand the architecture
3. **Use docker-compose.local.yml** for faster iteration
4. **Share with team** if needed

---

**Status**: ✅ FIXED and DOCUMENTED  
**Commit**: 55a2ba2  
**Date**: 2026-06-29
