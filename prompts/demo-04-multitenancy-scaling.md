# Demo 4: Multi-tenancy & Scaling

**Duration**: 40 minutes | **Complexity**: Advanced  
**Target Audience**: Enterprise architects, DevOps, CTOs  
**Agents**: nova-service-gen, nova-release-mgr, nova-ops-monitor, nova-async-comm

---

## Overview

This demo shows how the NOVA platform scales to support **multiple independent banks**, each managing their own guarantee portfolio without cross-tenant data leakage. Demonstrates Docker Compose scaling, multi-instance deployment, and load distribution.

### What You'll Show
- Running multiple backend instances
- Load balancing across instances
- Separate data isolation per tenant
- Database connection pooling
- Container resource constraints
- Health monitoring across instances

### Business Value
- **Multi-bank SaaS model**: BBVA can offer NOVA as a platform service to other banks
- **Horizontal scalability**: Add instances to handle increased load
- **Zero-downtime deployment**: Blue-green updates with load balancer
- **Cost optimization**: Scale up/down based on demand

---

## Prerequisites (5 minutes)

### Scale Up Docker Compose

**Modify docker-compose.yml to run 3 backend instances**:
```yaml
version: '3.8'
services:
  backend-1:
    build: ./guarantees-service
    environment:
      - SERVER_PORT=8080
    ports:
      - "8080:8080"
  
  backend-2:
    build: ./guarantees-service
    environment:
      - SERVER_PORT=8080
    ports:
      - "8081:8080"
  
  backend-3:
    build: ./guarantees-service
    environment:
      - SERVER_PORT=8080
    ports:
      - "8082:8080"
  
  nginx:
    image: nginx:alpine
    ports:
      - "8080:80"
    volumes:
      - ./nginx-lb.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - backend-1
      - backend-2
      - backend-3
```

**Start scaled environment**:
```bash
docker-compose up --scale backend=3 --build
# Or manually edit docker-compose.yml and run:
docker-compose up --build
```

---

## Step-by-Step Guide (35 minutes)

### Step 1: Verify Multi-Instance Deployment (5 min)

**Check running containers**:
```bash
docker ps | grep guarantees-service
# Expected output: 3 backend containers (backend-1, backend-2, backend-3)
```

**Check health of each instance**:
```bash
curl http://localhost:8080/actuator/health  # Instance 1
curl http://localhost:8081/actuator/health  # Instance 2
curl http://localhost:8082/actuator/health  # Instance 3
```

**Expected**: All return `{"status":"UP"}`

**In H2 Console**:
- Each instance has its own in-memory H2 database
- Verify: Run `SELECT COUNT(*) FROM GUARANTEE;` on each instance
- Each shows 6 seed guarantees (separate databases)

### Step 2: Load Balancer Configuration (5 min)

**Create nginx-lb.conf** for round-robin load balancing:
```nginx
upstream backend {
    least_conn;  # or round_robin
    server backend-1:8080;
    server backend-2:8080;
    server backend-3:8080;
}

server {
    listen 80;
    location / {
        proxy_pass http://backend;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

**Test load balancer** (requests routed to different instances):
```bash
for i in {1..6}; do
  curl http://localhost:8080/api/v1/guarantees | grep "id" | head -1
done
# Each request may hit a different backend instance
```

**Explain**:
- Nginx balances incoming requests across 3 backends
- Load distribution: round-robin or least connections
- No single point of failure (one instance down, others serve requests)

### Step 3: Data Isolation (Per-Tenant) (5 min)

**Concept**: In a true multi-tenant SaaS, each bank has separate data.

**Current limitation**: Each instance has separate H2 database (no shared state)

**Future improvement**: Shared PostgreSQL with `tenant_id` column:
```sql
ALTER TABLE GUARANTEE ADD COLUMN tenant_id VARCHAR(50);
CREATE INDEX idx_guarantee_tenant ON GUARANTEE(tenant_id);

-- Query for specific bank
SELECT * FROM GUARANTEE WHERE tenant_id = 'bbva-es';
```

**In Frontend**:
1. Create guarantee on instance 1 (Instance 1 database)
2. Create guarantee on instance 2 (Instance 2 database)
3. Each database has its own 7 guarantees (separate isolation)

**Explain**:
- Real SaaS would use shared DB with row-level security (RLS)
- Tenant ID in every query ensures data isolation
- Audit logs tied to tenant_id for compliance

### Step 4: Stress Test & Scalability (8 min)

**Generate load** (multiple concurrent requests):
```bash
# Using Apache Bench
ab -n 1000 -c 100 http://localhost:8080/api/v1/guarantees

# Or using GNU Parallel
seq 1 100 | parallel -j 10 'curl http://localhost:8080/api/v1/guarantees'
```

**Monitor response times**:
- With 1 instance: latency increases as load increases
- With 3 instances: latency stays stable (load distributed)

**In Prometheus/Grafana** (future):
- Track request latency per instance
- Show CPU/memory usage per container

### Step 5: Zero-Downtime Deployment (8 min)

**Blue-Green Strategy**:
1. Keep 3 instances running (blue)
2. Start 2 new instances with updated code (green)
3. Switch load balancer to green
4. Terminate blue instances

**Simulate**:
```bash
# Terminal 1: Current deployment (blue)
docker-compose up -d

# Terminal 2: New deployment (green, different port)
docker-compose -f docker-compose-v2.yml up -d

# Update nginx load balancer to route to green instances
# curl http://localhost:8080 now hits green instances

# Terminate blue instances
docker-compose down
```

**Result**: No requests dropped, seamless transition

**In Frontend**:
- During deployment, requests still succeed (routed to healthy instances)
- Users see no downtime

### Step 6: Container Resource Constraints (4 min)

**Set resource limits** in docker-compose.yml:
```yaml
backend-1:
  build: ./guarantees-service
  deploy:
    resources:
      limits:
        cpus: '0.5'
        memory: 512M
      reservations:
        cpus: '0.25'
        memory: 256M
```

**Monitor resource usage**:
```bash
docker stats --no-stream  # CPU, memory, network I/O
```

**Explain**:
- Guarantees each instance doesn't consume excessive resources
- Auto-scaling: If CPU > 80%, start new instance
- Cost control: Tight resource limits reduce infrastructure cost

### Step 7: Health Checks & Auto-Recovery (5 min)

**Kubernetes-style health check**:
```yaml
backend-1:
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
    interval: 10s
    timeout: 5s
    retries: 3
    start_period: 30s
```

**Simulate failure**:
1. Stop one backend instance
2. Load balancer removes it from pool
3. Nginx continues routing to healthy instances
4. Restart instance → automatically re-added to pool

```bash
docker-compose kill backend-1
# curl now only hits backend-2 and backend-3

docker-compose start backend-1
# curl now includes backend-1 again
```

---

## Discussion Points

### For Enterprise Architects
1. **Multi-tenancy**: "How do we prevent one bank from seeing another's data?"
   - Answer: Row-level security (tenant_id filter on all queries)
2. **Compliance**: "Can each tenant have their own compliance domain?"
   - Answer: Yes, with separate clusters or database schemas per tenant
3. **Cost**: "How much does running 3 instances cost vs. 1?"
   - Answer: 3x infrastructure, but handles 10x load → better cost-per-transaction

### For DevOps / SREs
1. **Deployment**: "How do we roll out updates without downtime?"
   - Answer: Blue-green or canary deployments with load balancer switching
2. **Monitoring**: "What metrics matter for scaling decisions?"
   - Answer: CPU, memory, request latency, error rate per instance
3. **Auto-scaling**: "How do we automatically scale based on load?"
   - Answer: Kubernetes HPA (Horizontal Pod Autoscaler) watches metrics

---

## Technical Details

### Horizontal Scaling Architecture

```
┌─────────────────────────────────────────┐
│         Nginx Load Balancer             │
│   (round-robin / least connections)     │
└──────┬──────────────────┬───────────────┘
       │                  │
       ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Backend 1   │  │  Backend 2   │  │  Backend 3   │
│  :8080       │  │  :8081       │  │  :8082       │
│  H2 DB       │  │  H2 DB       │  │  H2 DB       │
└──────────────┘  └──────────────┘  └──────────────┘
```

**With Shared DB** (Production):
```
┌─────────────────────────────────────────┐
│         Nginx Load Balancer             │
└──────┬──────────────────┬───────────────┘
       │                  │
       ▼                  ▼
┌──────────────┐  ┌──────────────┐  ┌──────────────┐
│  Backend 1   │  │  Backend 2   │  │  Backend 3   │
│  :8080       │  │  :8081       │  │  :8082       │
└──────────────┘  └──────────────┘  └──────────────┘
       │                  │                  │
       └──────────────────┴──────────────────┘
                    ▼
           ┌──────────────────────┐
           │   PostgreSQL DB      │
           │  (shared, tenant_id) │
           └──────────────────────┘
```

### Tenant Isolation Query

```java
// In GuaranteeRepository
List<Guarantee> findByTenantIdAndStatus(String tenantId, GuaranteeStatus status);

// In GuaranteeService (ALL queries include tenant_id)
public List<GuaranteeDTO> list(String tenantId, GuaranteeStatus status) {
    return repository.findByTenantIdAndStatus(tenantId, status).stream()
        .map(mapper::toDTO)
        .collect(toList());
}
```

### Docker Resource Constraints

```yaml
deploy:
  resources:
    limits:
      cpus: '0.5'      # Max 50% of 1 CPU
      memory: 512M     # Max 512 MB RAM
    reservations:
      cpus: '0.25'     # Guaranteed 25% of 1 CPU
      memory: 256M     # Guaranteed 256 MB RAM
```

---

## Proposed Improvements (for Delegation)

### nova-service-gen (Backend)
- [ ] **Tenant context filter** — Extract tenant_id from JWT/header, auto-filter all queries
- [ ] **Audit logging per tenant** — Log who accessed what guarantee, when
- [ ] **Tenant-specific workflows** — Different approval chains per bank

### nova-release-mgr (Docker & CI/CD)
- [ ] **Helm charts** for Kubernetes (replicas, scaling policies)
- [ ] **Blue-green deployment automation** (GitHub Actions / Jenkins)
- [ ] **SonarQube** for multi-instance quality gates

### nova-ops-monitor (Infrastructure)
- [ ] **Prometheus** for instance-level metrics (requests/sec, latency)
- [ ] **Grafana dashboard** for multi-instance health
- [ ] **Jaeger** for distributed tracing across instances

### nova-async-comm (Messaging)
- [ ] **RabbitMQ cluster** for multi-instance event coordination
- [ ] **Tenant-specific event topics** (separate queue per bank)

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Load balancer returns 502 | Check if all backends are healthy: `docker ps` |
| Instances have different data | Expected (separate H2 DB); use shared DB for true multi-tenancy |
| High latency with 3 instances | Check CPU/memory constraints; increase resources |

---

**Previous Demo**: [Demo 3: Dashboard & Filtering](./demo-03-dashboard-filtering.md)  
**Next Demo**: [Demo 5: Async Notification System](./demo-05-async-notifications.md)
