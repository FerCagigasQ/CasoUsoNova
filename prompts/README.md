# NOVA Guarantees Demo Prompts

This folder contains **full-stack demo scenarios** for the NOVA Bank Guarantees platform, designed for **30-40 minute presentations**. Each prompt covers a specific workflow and leverages the complete NOVA agent ecosystem.

## Purpose

These prompts guide architects, engineers, and stakeholders through realistic use cases of the NOVA platform. They are:
- **Self-contained**: Each can run independently in 30-40 minutes
- **Full-stack**: Demonstrate backend REST APIs, frontend UI, state transitions, and data persistence
- **Agent-driven**: Delegate specialized tasks to NOVA agents (service-gen, frontend-gen, api-integr, async-comm, release-mgr, ops-monitor)
- **Demo-first**: Show business value first, then technical implementation details

## Demo Scenarios

### 1. [Demo 1: Guarantee Lifecycle (Create → Issue → Amend)](./demo-01-guarantee-lifecycle.md)
**Duration**: 35 minutes | **Complexity**: Beginner  
**Scenario**: Walk through creating a bank guarantee, issuing it, and amending its terms.  
**Agents**: nova-service-gen, nova-frontend-gen, nova-api-integr

### 2. [Demo 2: Claims Processing Workflow](./demo-02-claims-workflow.md)
**Duration**: 40 minutes | **Complexity**: Intermediate  
**Scenario**: Submit a claim on an active guarantee, track its status, and view the audit trail.  
**Agents**: nova-service-gen, nova-frontend-gen, nova-async-comm

### 3. [Demo 3: Real-time Dashboard & Filtering](./demo-03-dashboard-filtering.md)
**Duration**: 30 minutes | **Complexity**: Beginner  
**Scenario**: Filter guarantees by status/type, track expiry dates, and visualize portfolio metrics.  
**Agents**: nova-frontend-gen, nova-api-integr, nova-ops-monitor

### 4. [Demo 4: Multi-tenancy & Scaling](./demo-04-multitenancy-scaling.md)
**Duration**: 40 minutes | **Complexity**: Advanced  
**Scenario**: Deploy multiple bank instances, manage separate guarantee portfolios, and scale under load.  
**Agents**: nova-service-gen, nova-release-mgr, nova-ops-monitor, nova-async-comm

### 5. [Demo 5: Async Notification System](./demo-05-async-notifications.md)
**Duration**: 35 minutes | **Complexity**: Intermediate  
**Scenario**: Set up event-driven notifications for guarantee expiry, amendments, and claims.  
**Agents**: nova-async-comm, nova-service-gen, nova-frontend-gen

---

## How to Use These Prompts

### For Architects
1. Pick a scenario matching your audience (beginner/intermediate/advanced)
2. Read the **Overview** section
3. Follow **Step-by-Step Guide** to show the workflow
4. Use **Discussion Points** to engage stakeholders
5. Refer to **Technical Details** if deep-dive questions arise

### For Demos
1. Clone the repo and run `docker compose up --build` (5 min)
2. Open the guide in your editor
3. Follow the UI steps on http://localhost:4200
4. Watch API calls in Swagger UI (http://localhost:8080/swagger-ui.html)
5. Use H2 Console for real-time data inspection (http://localhost:8080/h2-console)

### For Agent Delegation
1. Each demo lists **Responsible Agents**
2. Review the **Proposed Improvements** section
3. Create sub-issues (max 5 per demo) with clear acceptance criteria
4. Link this README in the epic description for context

---

## Improvement Proposals by Agent

### nova-service-gen (Backend Services)
- [ ] **Async guarantee status updates** via RabbitMQ (KAFKA alternative)
- [ ] **Event sourcing** for guarantee amendments
- [ ] **Bulk guarantee import** API endpoint (CSV upload)
- [ ] **Guarantee expiry notifications** (scheduled job)
- [ ] **API versioning** (v2 with backward compatibility)

### nova-frontend-gen (Angular UI)
- [ ] **Real-time status updates** using WebSocket / Server-Sent Events (SSE)
- [ ] **Chart library integration** (NgCharts) for portfolio metrics
- [ ] **Advanced filtering panel** with date range & amount filters
- [ ] **Mobile-responsive design** (Material breakpoints)
- [ ] **Dark mode support** (Angular Material theme switching)

### nova-api-integr (Service Integration)
- [ ] **OpenAPI 3.0 spec** validation & client code generation
- [ ] **Feign client** for multi-service calls
- [ ] **Circuit breaker** (Hystrix/Resilience4j) for downstream services
- [ ] **API gateway** pattern (Spring Cloud Gateway)
- [ ] **Request/Response logging** middleware

### nova-async-comm (Messaging & Events)
- [ ] **RabbitMQ broker** for guarantee events
- [ ] **Dead-letter queue** handling for failed notifications
- [ ] **Event schema** (AsyncAPI) for guarantee topics
- [ ] **Email notifications** (Spring Mail) on claim submission
- [ ] **Webhook integrations** for external systems

### nova-release-mgr (Docker & CI/CD)
- [ ] **Helm charts** for Kubernetes deployment
- [ ] **Multi-stage CI/CD pipeline** (GitHub Actions / Jenkins)
- [ ] **Blue-green deployment** strategy
- [ ] **SonarQube integration** for code quality gates
- [ ] **Container scanning** (Trivy) for vulnerabilities

### nova-ops-monitor (Infrastructure & Observability)
- [ ] **Prometheus metrics** for guarantee processing latency
- [ ] **Grafana dashboard** for SLA monitoring
- [ ] **Distributed tracing** (Spring Cloud Sleuth + Jaeger)
- [ ] **Log aggregation** (ELK stack or similar)
- [ ] **Kubernetes resource management** (CPU/memory limits)

---

## Demo Architecture

```
┌─────────────────────────────────────────────┐
│         NOVA Guarantees Platform            │
├─────────────────────────────────────────────┤
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │    Angular 17 UI (Material Design)   │  │
│  │  - Guarantee CRUD                    │  │
│  │  - Amendment/Claim Forms             │  │
│  │  - Real-time Dashboard (TODO)        │  │
│  └──────────────────────────────────────┘  │
│                    ↓↑                       │
│            REST API Gateway                │
│                    ↓↑                       │
│  ┌──────────────────────────────────────┐  │
│  │    Spring Boot 3.2.x Backend         │  │
│  │  - Guarantee CRUD Service            │  │
│  │  - State Machine (DRAFT→ISSUED...)   │  │
│  │  - Amendment & Claim Processing      │  │
│  │  - Swagger UI & Actuator             │  │
│  └──────────────────────────────────────┘  │
│                    ↓↑                       │
│     H2 Database (or PostgreSQL TODO)       │
│                                             │
│  ┌──────────────────────────────────────┐  │
│  │   RabbitMQ Broker (TODO)             │  │
│  │  - Guarantee Events                  │  │
│  │  - Notification Queue                │  │
│  └──────────────────────────────────────┘  │
│                                             │
└─────────────────────────────────────────────┘
```

---

## Quick Reference: API Endpoints

| Method | Endpoint | Purpose |
|--------|----------|---------|
| GET | `/api/v1/guarantees` | List all guarantees (with filters) |
| POST | `/api/v1/guarantees` | Create new guarantee |
| GET | `/api/v1/guarantees/{id}` | Get guarantee detail |
| PUT | `/api/v1/guarantees/{id}` | Update guarantee |
| POST | `/api/v1/guarantees/{id}/issue` | Issue guarantee (DRAFT → ISSUED) |
| POST | `/api/v1/guarantees/{id}/amendments` | Add amendment |
| POST | `/api/v1/guarantees/{id}/claims` | Submit claim |
| GET | `/api/v1/guarantees/{id}/claims` | List claims for guarantee |
| GET | `/api/v1/applicants` | Reference data |
| GET | `/api/v1/beneficiaries` | Reference data |
| GET | `/api/v1/issuing-banks` | Reference data |

---

## Running a Demo

### Prerequisites
- Docker & Docker Compose (or Java 17 + Node.js 18+)
- 10-15 min to build + start services

### Quick Start
```bash
# Clone repo (if needed)
git clone https://github.com/FerCagigasQ/CasoUsoNova.git
cd CasoUsoNova

# Start all services (backend + frontend)
docker compose up --build

# Or use local script (no Docker required)
./run-local.sh          # Linux/Mac
.\run-local.ps1         # Windows
```

### Access Points
- **Frontend**: http://localhost:4200 (dev) or http://localhost (Docker)
- **Backend API**: http://localhost:8080/api/v1/guarantees
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console (user: `sa`, password: empty)

---

## Feedback & Improvements

These prompts are living documents. After each demo:
1. Collect feedback on clarity, pace, and technical accuracy
2. Propose improvements using the **Improvement Proposals** checklist
3. Create GitHub issues with demo-related PRs
4. Update this README with lessons learned

**Estimated demo content refresh**: Quarterly  
**Last updated**: 2026-06-29
