# Demo 4: Multi-tenancy & Scaling

**Development Sprint**: Tenant isolation + load distribution  
**Estimated Duration**: 6-8 hours  
**Target**: Multi-instance deployment + load balancing  
**Agents**: nova-service-gen, nova-release-mgr, nova-ops-monitor, nova-async-comm

---

## Business Context

The platform should scale horizontally to serve multiple independent banks, each with separate data. This demo shows multi-instance deployment with load balancing.

---

## Task Breakdown

### nova-service-gen (Backend Services)

**Objective**: Add tenant context isolation

**Must deliver**:

1. **Tenant Context**
   - [ ] Add `tenant_id` column to GUARANTEE, AMENDMENT, CLAIM tables
   - [ ] Extract tenant from JWT token or `X-Tenant-ID` header in all requests
   - [ ] Store in `RequestContext` or `ThreadLocal` for current request

2. **Repository Changes**
   - [ ] Modify all queries: add `.where(guarantee.tenant_id = currentTenant)`
   - [ ] Example: `repository.findByStatusAndTenant(status, tenantId)`
   - [ ] Prevent data leakage: queries always filter by tenant

3. **Audit Logging**
   - [ ] Log all guarantee access: who (userId), when, tenantId, action
   - [ ] Store in AUDIT_LOG table: userId, tenantId, action, guaranteeId, timestamp

**Success Criteria**:
- ✅ All queries auto-filter by tenant_id
- ✅ No cross-tenant data visible
- ✅ Audit logs show all access

---

### nova-release-mgr (Docker & CI/CD)

**Objective**: Configure multi-instance deployment

**Must deliver**:

1. **Docker Compose Multi-Instance Setup**
   - [ ] Update docker-compose.yml to run 3 backend instances (ports 8080, 8081, 8082)
   - [ ] Each instance: separate container, same image, different port mapping
   - [ ] Shared database: All 3 instances read/write to same H2 (or PostgreSQL)
   - [ ] Example:
     ```yaml
     backend-1:
       build: ./guarantees-service
       ports:
         - "8080:8080"
     backend-2:
       build: ./guarantees-service
       ports:
         - "8081:8080"
     backend-3:
       build: ./guarantees-service
       ports:
         - "8082:8080"
     ```

2. **Nginx Load Balancer**
   - [ ] Add nginx service to docker-compose
   - [ ] Configure: upstream block with 3 backend servers
   - [ ] Strategy: round-robin or least connections
   - [ ] Forward requests: `http://localhost:8080` → distributed to backends
   - [ ] Config file example:
     ```nginx
     upstream backend {
       least_conn;
       server backend-1:8080;
       server backend-2:8080;
       server backend-3:8080;
     }
     server {
       listen 80;
       location / {
         proxy_pass http://backend;
       }
     }
     ```

3. **Health Checks**
   - [ ] Configure healthcheck in docker-compose for each backend
   - [ ] Endpoint: GET /actuator/health
   - [ ] Interval: 10s, Timeout: 5s, Retries: 3

4. **Zero-Downtime Deployment**
   - [ ] Document blue-green strategy: start 2 new instances, switch LB, kill old ones
   - [ ] Add deployment script (bash/shell) that automates the process

**Success Criteria**:
- ✅ `docker compose up` starts 3 backends + nginx
- ✅ All instances healthy
- ✅ Nginx distributes requests evenly
- ✅ Deployment script works

---

### nova-ops-monitor (Infrastructure & Observability)

**Objective**: Monitor multi-instance deployment

**Must deliver**:

1. **Per-Instance Metrics**
   - [ ] Prometheus scrape config: monitor all 3 backends
   - [ ] Metrics: requests/sec per instance, latency per instance, error rate per instance
   - [ ] Targets: localhost:8080/actuator/prometheus, :8081/actuator/prometheus, :8082/actuator/prometheus

2. **Grafana Dashboard**
   - [ ] Dashboard: "Multi-Instance Health"
   - [ ] Panels:
     - Request distribution: pie chart showing % requests per instance
     - Response time per instance: line graph
     - CPU/Memory per instance: bar chart
     - Error rate per instance: gauge

3. **Load Balancer Metrics**
   - [ ] Monitor nginx: upstream response times, connection counts
   - [ ] Detect if any instance is slow (outlier detection)

4. **Alerts**
   - [ ] Alert: "Instance X down" (healthcheck failed)
   - [ ] Alert: "Uneven load distribution" (one instance >80% of traffic)
   - [ ] Alert: "High error rate on instance Y" (>1% errors)

**Success Criteria**:
- ✅ All 3 instances appear in Prometheus targets
- ✅ Grafana dashboard shows per-instance metrics
- ✅ Load distribution is balanced (±20% variance acceptable)

---

### nova-async-comm (Messaging & Events)

**Objective**: Coordinate events across multi-instance deployment

**Must deliver**:

1. **RabbitMQ Cluster** (or single broker for now)
   - [ ] All 3 backend instances connect to same RabbitMQ
   - [ ] Publish guarantee events from any instance
   - [ ] All consumers subscribe to same queue

2. **Tenant-Specific Topics**
   - [ ] Event routing: `guarantees.bank-a.events`, `guarantees.bank-b.events`
   - [ ] Each tenant's instance publishes to its own topic
   - [ ] Consumers subscribe to tenant-specific topic

**Success Criteria**:
- ✅ Events published from instance-1 visible in RabbitMQ Management UI
- ✅ Events from instance-2 also appear in queue
- ✅ Tenant routing works (events segregated by tenant)

---

## Verification Checklist

- [ ] `docker compose up` — 3 backends + nginx start successfully
- [ ] `curl http://localhost:8080/api/v1/guarantees` — requests load-balanced (check nginx logs)
- [ ] Create guarantee on instance-1, query from instance-2 — data consistent
- [ ] Kill instance-1, requests still succeed (routed to 2, 3)
- [ ] Prometheus shows metrics from all 3 instances
- [ ] Grafana shows load distribution ≈33% each
- [ ] Events published from each instance reach queue

**Definition of Done for Demo 4**: 
All agents complete, 3-instance cluster stable, load balanced, data consistent.
