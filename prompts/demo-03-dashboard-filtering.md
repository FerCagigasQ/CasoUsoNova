# Demo 3: Real-time Dashboard & Filtering

**Development Sprint**: Advanced filtering + KPI dashboard  
**Estimated Duration**: 3-4 hours  
**Target**: Query optimization + UI dashboard with metrics  
**Agents**: nova-frontend-gen, nova-api-integr, nova-ops-monitor

---

## Business Context

Portfolio managers need to filter guarantees by status, type, and date range, and see KPI metrics (total exposure, active count, expiring soon). The system must handle large datasets efficiently.

---

## Task Breakdown

### nova-frontend-gen (Angular UI)

**Objective**: Build advanced filter UI + KPI dashboard

**Must deliver**:

1. **Filter Panel**
   - [ ] Add filter sidebar above guarantee table
   - [ ] Status filter: multiselect dropdown (DRAFT, ISSUED, AMENDED, CLAIMED, EXPIRED, CANCELLED)
   - [ ] Type filter: multiselect dropdown (PERFORMANCE, ADVANCE_PAYMENT, BID_BOND, WARRANTY)
   - [ ] Date range filter: "From" and "To" date pickers (for expiryDate)
   - [ ] Amount range filter: "Min" and "Max" text inputs
   - [ ] Button: "Apply Filters" (or auto-apply on change)
   - [ ] Button: "Clear Filters" (reset all)
   - [ ] Display active filter count badge

2. **Table Sorting**
   - [ ] Click column header to sort ascending/descending
   - [ ] Visual indicator (up/down arrow) on sorted column
   - [ ] Persist sort state in URL params

3. **Pagination**
   - [ ] Page selector (1, 2, 3, ... max page)
   - [ ] Items-per-page dropdown (10, 20, 50, 100)
   - [ ] Show "Page X of Y" and "Total: Z items"

4. **KPI Dashboard Cards**
   - [ ] Card 1: "Total Exposure" — sum of all guarantee amounts (colored box)
   - [ ] Card 2: "Active Guarantees" — count where status IN (ISSUED, AMENDED)
   - [ ] Card 3: "Expiring Soon" — count where expiryDate ≤ today + 30 days
   - [ ] Card 4: "Under Review" — count of guarantees with open claims
   - [ ] Auto-refresh KPIs every 5 seconds (or on filter change)

5. **Responsive Design**
   - [ ] Mobile: Stack filters vertically, hide non-essential columns
   - [ ] Tablet: 2-column filter layout
   - [ ] Desktop: Full sidebar

**Success Criteria**:
- ✅ Filters apply correctly (table updates)
- ✅ KPI cards show correct values
- ✅ Sorting works on all columns
- ✅ Pagination works (navigate between pages)
- ✅ Responsive on mobile/tablet/desktop

---

### nova-api-integr (Service Integration)

**Objective**: Ensure filter queries are performant and properly documented

**Must deliver**:

1. **Query Parameter Support**
   - [ ] Backend already supports: `?status=ISSUED&type=PERFORMANCE&page=0&size=20&sort=amount,desc`
   - [ ] Add support for: `?expiryDateFrom=2026-07-01&expiryDateTo=2026-07-31&amountMin=10000&amountMax=100000`
   - [ ] Verify query parameters in Swagger documentation

2. **Database Query Optimization**
   - [ ] Create composite index: (status, type, expiryDate, amount) for fast filtering
   - [ ] Verify index usage with EXPLAIN ANALYZE
   - [ ] Test query performance with 10,000+ records (should return <500ms)

3. **Pageable Response Format**
   - [ ] Response includes: `content: [...]`, `totalElements: 12345`, `totalPages: 617`, `currentPage: 0`, `pageSize: 20`, `hasNext: true`
   - [ ] Verify Spring Data returns `Page<GuaranteeDTO>` which Jackson serializes correctly

4. **OpenAPI Documentation**
   - [ ] Document all filter parameters in Swagger
   - [ ] Show example queries: `/api/v1/guarantees?status=ISSUED&expiryDateTo=2026-07-31`
   - [ ] Document response format

**Success Criteria**:
- ✅ Filter queries execute in <500ms with large datasets
- ✅ Swagger shows all filter parameters
- ✅ Frontend receives paginated response in expected format
- ✅ Index created (verify with database admin tools)

---

### nova-ops-monitor (Infrastructure & Observability)

**Objective**: Monitor query performance + set up KPI dashboards

**Must deliver**:

1. **Prometheus Metrics**
   - [ ] Enable Spring Boot Micrometer: add micrometer-registry-prometheus dependency
   - [ ] Expose metrics at http://localhost:8080/actuator/prometheus
   - [ ] Capture: request_count, request_duration_ms, database_query_time_ms per endpoint

2. **Grafana Dashboard**
   - [ ] Add Prometheus data source: http://localhost:9090
   - [ ] Create dashboard: "Guarantee Portfolio KPIs"
   - [ ] Panels:
     - Query latency (histogram): /api/v1/guarantees endpoint
     - Request throughput (counter): requests/sec
     - Database query time: slow query detection (>500ms)
     - Cache hit ratio (if caching added)

3. **Performance Baseline**
   - [ ] Measure query response time with 1K, 10K, 100K records
   - [ ] Document baseline: "GET /api/v1/guarantees with filters executes in 250-400ms with 100K records"
   - [ ] Set SLA: "All queries <500ms"

4. **Alerting (Optional)**
   - [ ] Create alert: "Query latency > 1000ms" — fires if 5 consecutive slow requests
   - [ ] Create alert: "Database index missing" — based on EXPLAIN ANALYZE output

**Success Criteria**:
- ✅ Prometheus metrics exposed at actuator/prometheus
- ✅ Grafana dashboard shows query latency trends
- ✅ Baseline documented
- ✅ Alerts configured

---

## Verification Checklist

- [ ] Apply filters via UI → table updates with correct data
- [ ] KPI cards show correct totals
- [ ] Sorting works on amount, expiryDate
- [ ] Pagination navigates correctly
- [ ] Query takes <500ms with filters applied
- [ ] Swagger shows all filter parameters
- [ ] Grafana dashboard displays query metrics

**Definition of Done for Demo 3**: 
All agents complete, filters working, KPIs accurate, performance baseline documented.
