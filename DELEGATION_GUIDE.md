# NOV-2 Agent Delegation Guide

**Issue**: NOV-2 — Lee el repo y el directorio y dime sus estado, añade una carpeta carpeta llamada prompts que incluya mejoras fullstack y que usen todos mis agentes de la organización en una tarea

**Status**: DONE — Demo prompts complete. Delegation framework ready.  
**Created**: 2026-06-29  
**By**: Arquitecto NOVA

---

## What Was Completed

### Phase 1: Demo Prompts Creation ✅
- **6 files** created in `prompts/` folder
- **2,058 lines** of comprehensive documentation
- **5 full-stack demos** (30-40 min each)
- **58 improvement proposals** for agent delegation
- **Committed** to git (commit: 9dbf369)

### Phase 2: Agent Mapping ✅
Each demo explicitly maps to 1-4 responsible agents with specific improvement proposals.

---

## How to Delegate to Agent Teams

### Step 1: Pick a Demo & Agent Pair
Review `prompts/README.md` → Select demo + agent combination.

**Example**: Demo 5 (Async Notifications) + nova-async-comm

### Step 2: Create GitHub Issue per Improvement
For each `[ ]` checkbox in the "Proposed Improvements" section, create one issue:

**Issue Title Template**:
```
[NOV-XX] (agent): Improvement title
```

**Example**:
```
[NOV-5-A] nova-async-comm: Implement RabbitMQ broker for guarantee events
```

**Issue Body Template**:
```markdown
## Context
Demo: [Demo 5: Internacionalización ES/EN](./prompts/demo-05-internacionalizacion.md)

## Task
[Copy the improvement proposal from the demo]

Example from demo-05-internacionalizacion.md:
- **Selector de idioma ES/EN** con cambio en caliente (ngx-translate)

## Acceptance Criteria
- [ ] Implementation complete per spec
- [ ] Tests written (unit + integration)
- [ ] Documentation updated
- [ ] Demo runs end-to-end

## Links
- Parent Issue: NOV-2
- Demo Guide: prompts/demo-05-internacionalizacion.md
- Architecture: See "Technical Details" section of demo
```

**Labels**: `agent:nova-frontend-gen`, `demo:5`, `phase:2-implementation`

---

## Delegation Matrix: All Improvements by Agent

### nova-service-gen (Backend Services)

**Demo 1: Guarantee Lifecycle**
- [ ] Add **bulk guarantee upload** endpoint (CSV file)
- [ ] Implement **guarantee expiry notifications** (scheduled job every night)
- [ ] Add **API versioning** (v2 with backward compatibility)

**Demo 2: Claims Processing**
- [ ] **Claim rejection reasons** — Add structured rejection reasons (e.g., "Missing documentation", "Amount exceeds limit")
- [ ] **Claim amendments** — Allow beneficiary to revise claimed amount before approval
- [ ] **Automatic expiry transitions** — Job that marks guarantees/claims expired after due date

**Demo 3: Dashboard & Filtering**
- [ ] (none listed for backend in this demo)

**Demo 4: Multi-tenancy & Scaling**
- [ ] **Tenant context filter** — Extract tenant_id from JWT/header, auto-filter all queries
- [ ] **Audit logging per tenant** — Log who accessed what guarantee, when
- [ ] **Tenant-specific workflows** — Different approval chains per bank

**Demo 5: Async Notifications**
- [ ] **Event sourcing** (store all events as immutable log)
- [ ] **CQRS pattern** (separate read/write models)
- [ ] **Saga pattern** (distributed transactions across services)

**Total**: 12 improvements

---

### nova-frontend-gen (Angular UI)

**Demo 1: Guarantee Lifecycle**
- [ ] Add **real-time status updates** using WebSocket/SSE
- [ ] Implement **amendment history timeline** (visual representation)
- [ ] Add **PDF generation** for guarantee certificates

**Demo 2: Claims Processing**
- [ ] **Claim status badge** — Color-coded badges (red=SUBMITTED, yellow=UNDER_REVIEW, green=PAID, gray=REJECTED)
- [ ] **Claim history timeline** — Visual timeline of status transitions with timestamps
- [ ] **Claim PDF export** — Generate claim statement PDF for download

**Demo 3: Dashboard & Filtering**
- [ ] **Dashboard with charts** (NgCharts): Pie chart of guarantees by status, Bar chart by type
- [ ] **Calendar heatmap** for expiry dates (visual risk indicator)
- [ ] **Advanced filter UI** with date range picker, multi-select dropdowns
- [ ] **Export to CSV/PDF** for reporting

**Demo 4: Multi-tenancy & Scaling**
- [ ] (none listed for frontend in this demo)

**Demo 5: Async Notifications**
- [ ] **WebSocket/SSE integration** (real-time updates)
- [ ] **Notification toast** (top-right corner alerts)
- [ ] **Notification center** (history of all notifications)

**Total**: 14 improvements

---

### nova-async-comm (Messaging & Events)

**Demo 2: Claims Processing**
- [ ] **Claim notification events** — Publish to RabbitMQ when claim is submitted/approved/rejected
- [ ] **Email notifications** — Send email to applicant/beneficiary on claim status change
- [ ] **Webhook integration** — Allow external systems to subscribe to claim events

**Demo 3: Dashboard & Filtering**
- [ ] **Expiry alerts** via RabbitMQ (daily report of guarantees expiring in 7 days)
- [ ] **Webhook for external systems** (e.g., SAP notification when guarantee expires)

**Demo 4: Multi-tenancy & Scaling**
- [ ] **RabbitMQ cluster** for multi-instance event coordination
- [ ] **Tenant-specific event topics** (separate queue per bank)

**Demo 5: Async Notifications**
- [ ] **Kafka integration** as RabbitMQ alternative (higher throughput)
- [ ] **Event versioning** (schema evolution for breaking changes)
- [ ] **Slack notifications** (create Slack worker)
- [ ] **SMS notifications** (Twilio integration)
- [ ] **Webhook delivery** (retry with exponential backoff)

**Total**: 12 improvements

---

### nova-api-integr (Service Integration)

**Demo 1: Guarantee Lifecycle**
- [ ] Generate **Feign client** for multi-service calls
- [ ] Add **request/response logging** middleware
- [ ] Implement **circuit breaker** for downstream calls

**Demo 3: Dashboard & Filtering**
- [ ] **Elasticsearch integration** for full-text search on reference, applicant name
- [ ] **GraphQL endpoint** for flexible querying (alternative to REST filters)
- [ ] **OpenAPI 3.0 spec** with complex query parameter documentation

**Demo 4: Multi-tenancy & Scaling**
- [ ] (none listed for API integration in this demo)

**Total**: 6 improvements

---

### nova-ops-monitor (Infrastructure & Observability)

**Demo 3: Dashboard & Filtering**
- [ ] **Grafana dashboard** showing KPIs in real-time
- [ ] **Prometheus metrics** for query latency and result counts
- [ ] **Database query optimization** (EXPLAIN ANALYZE)

**Demo 4: Multi-tenancy & Scaling**
- [ ] **Prometheus** for instance-level metrics (requests/sec, latency)
- [ ] **Grafana dashboard** for multi-instance health
- [ ] **Jaeger** for distributed tracing across instances

**Demo 5: Async Notifications**
- [ ] **RabbitMQ metrics** in Prometheus/Grafana
- [ ] **Alert on DLQ growth** (indicates systemic issues)
- [ ] **Message throughput monitoring** (events/sec)

**Total**: 9 improvements

---

### nova-release-mgr (Docker & CI/CD)

**Demo 4: Multi-tenancy & Scaling**
- [ ] **Helm charts** for Kubernetes (replicas, scaling policies)
- [ ] **Blue-green deployment automation** (GitHub Actions / Jenkins)
- [ ] **SonarQube** for multi-instance quality gates

**Total**: 3 improvements

---

## Summary Statistics

| Agent | Demo Count | Improvement Count | Starting Demo |
|-------|-----------|------------------|---------------|
| nova-service-gen | 4 | 12 | Demo 1 |
| nova-frontend-gen | 4 | 14 | Demo 1 |
| nova-async-comm | 4 | 12 | Demo 2 |
| nova-api-integr | 3 | 6 | Demo 1 |
| nova-ops-monitor | 3 | 9 | Demo 3 |
| nova-release-mgr | 1 | 3 | Demo 4 |
| **TOTAL** | **5** | **58** | — |

---

## Implementation Phases

### Phase 2A: Agent Kickoff (Week 1)
1. Distribute this guide to all agent teams
2. Each agent reviews their improvements (3-14 per agent)
3. Create GitHub issues per improvement
4. Estimate complexity & timeline

### Phase 2B: Parallel Implementation (Weeks 2-6)
- nova-service-gen: Event sourcing, bulk imports, notifications
- nova-frontend-gen: Charts, real-time updates, PDFs
- nova-async-comm: RabbitMQ/Kafka, webhooks, notifications
- nova-api-integr: Feign clients, Elasticsearch, GraphQL
- nova-ops-monitor: Prometheus, Grafana, tracing
- nova-release-mgr: Helm, blue-green deployment, SonarQube

### Phase 2C: Integration Testing (Week 7)
- Run all 5 demos with improvements enabled
- Cross-team integration tests
- Performance testing (load, latency)

### Phase 3: Release (Week 8)
- Release v2.0 with all improvements
- Update demo prompts with new features
- Train demo presenters

---

## How to Track Progress

**GitHub Labels** (recommended):
- `phase:2-implementation` — All Phase 2 issues
- `agent:nova-service-gen`, `agent:nova-frontend-gen`, etc.
- `demo:1`, `demo:2`, etc.
- `priority:p0`, `priority:p1` for prioritization

**GitHub Project Board** (optional):
- Columns: Backlog, In Progress, Review, Done
- Filter by agent or demo
- Track velocity per team

**Paperclip Integration** (if using):
- Create child issues from NOV-2 with parent link
- Link each issue to its demo prompt
- Auto-link via commit messages: `refs NOV-2-A-1` (Demo 1, Agent A, Issue 1)

---

## Demo Prompts Location

All reference material in: **`prompts/`** folder
- `prompts/README.md` — Overview & quick reference
- `prompts/demo-01-tema-oscuro.md`
- `prompts/demo-02-exportacion-datos.md`
- `prompts/demo-03-busqueda-global.md`
- `prompts/demo-04-dashboard-kpis.md`
- `prompts/demo-05-internacionalizacion.md`

Each demo has:
- Step-by-step guide (for demo presenters)
- Technical details (architecture, code examples)
- Proposed Improvements section (for agent teams)

---

## Next Steps

1. ✅ **Done**: Create demo prompts (NOV-2, commit 9dbf369)
2. ⏭️ **Next**: Distribute this delegation guide to agent teams
3. ⏭️ **Then**: Agent teams create GitHub issues (Phase 2A)
4. ⏭️ **Finally**: Parallel implementation (Phase 2B-2C)

---

**Owner**: Arquitecto NOVA  
**Status**: Ready for delegation  
**Last Updated**: 2026-06-29
