# Demo 3: Real-time Dashboard & Filtering

**Duration**: 30 minutes | **Complexity**: Beginner  
**Target Audience**: Finance teams, portfolio managers, executives  
**Agents**: nova-frontend-gen, nova-api-integr, nova-ops-monitor

---

## Overview

This demo showcases **portfolio management capabilities**: filtering guarantees by status and type, tracking expiry dates, and visualizing key metrics (total exposure, guarantees by status, etc.).

### What You'll Show
- Advanced filtering (multi-select status, type, date range)
- Sorting and pagination
- KPI cards (total exposure, active guarantees, expiring soon)
- Real-time query performance (H2 Console)
- Dashboard layout (existing or prototype future dashboard)

### Business Value
- **Portfolio visibility**: See all guarantees at a glance
- **Risk management**: Track expiring guarantees and their status
- **Decision support**: Filter by type/status to analyze trends
- **Operational efficiency**: Instantly find problematic guarantees (DRAFT, EXPIRED, CLAIMED)

---

## Prerequisites (2 minutes)

### Services Running
```bash
docker compose ps  # Verify healthy
```

### Data Availability
Use the 6 seed guarantees from previous demos, or add more (see Demo 1).

---

## Step-by-Step Guide (28 minutes)

### Step 1: Overview the Guarantee List (3 min)

**In Frontend** (http://localhost:4200):
1. Navigate to **Guarantees list**
2. Show the **full table** with all guarantees:
   - Reference (BG-2026-001, etc.)
   - Type (PERFORMANCE, BID_BOND, etc.)
   - Amount
   - Status (ISSUED, AMENDED, CLAIMED, EXPIRED, DRAFT)
   - Issue Date, Expiry Date
3. Count total rows → Shows portfolio size

**Explain**:
- Each row is a guarantee
- Status determines what actions are available (can't claim an EXPIRED guarantee)
- Expiry Date is critical for risk management

### Step 2: Filter by Status (5 min)

**In Frontend**:
1. Click **Status filter** dropdown
2. Uncheck "All", then select:
   - ☑ ISSUED
   - ☑ AMENDED
3. Click **Apply** (or auto-filter)

**Expected Result**:
- List now shows only **active guarantees** (ISSUED and AMENDED)
- DRAFT, EXPIRED, CLAIMED guarantees disappear
- Shows count: e.g., "4 of 6 active"

**In Swagger UI** (show API equivalent):
1. Click **GET /api/v1/guarantees**
2. Add query parameter: `status=ISSUED&status=AMENDED` (or single value)
3. Execute → Shows filtered JSON response

**Explain**:
- Frontend sends `?status=ISSUED` query parameter
- Backend repository filters: `.findByStatus(status)` or `.findByStatusIn(statuses)`
- Database index on `status` column makes this fast

### Step 3: Filter by Type (5 min)

**In Frontend**:
1. Click **Type filter** dropdown
2. Uncheck "All", then select:
   - ☑ PERFORMANCE
   - ☑ BID_BOND
3. Combined with previous status filter

**Expected Result**:
- List shows **performance bonds and bid bonds** that are also ISSUED or AMENDED
- Further narrows the portfolio

**In H2 Console** (http://localhost:8080/h2-console):
1. Run combined query:
   ```sql
   SELECT reference, type, status, amount FROM GUARANTEE 
   WHERE status IN ('ISSUED', 'AMENDED') 
   AND type IN ('PERFORMANCE', 'BID_BOND');
   ```
2. Show same results as frontend

### Step 4: Filter by Expiry Date (5 min)

**Show upcoming expiry**:
1. In Frontend, add **date range filter** (or show as proposal):
   - Start: Today
   - End: 30 days from today
2. This shows **guarantees expiring within 30 days** (risk indicators)

**In H2 Console**:
```sql
SELECT reference, expiry_date, status 
FROM GUARANTEE 
WHERE expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + 30;
```

**Explain**:
- Risk teams need to know which guarantees are expiring soon
- Expiring guarantees may need renewal or amendment

### Step 5: Sorting (3 min)

**In Frontend**:
1. Click **Amount** column header twice to sort (ascending → descending)
2. Show guarantees sorted by amount (largest first)
3. Click **Expiry Date** header to sort by urgency

**In H2 Console**:
```sql
SELECT reference, amount, expiry_date FROM GUARANTEE 
ORDER BY amount DESC;
```

**Explain**:
- Sorting by amount shows **highest-exposure guarantees**
- Sorting by expiry date shows **most urgent renewals**

### Step 6: Dashboard KPI Cards (5 min)

**Demonstrate or propose**:
1. **Total Exposure**: Sum of all guarantee amounts
   ```sql
   SELECT SUM(amount) FROM GUARANTEE;
   ```
2. **Active Guarantees**: Count with status ISSUED or AMENDED
   ```sql
   SELECT COUNT(*) FROM GUARANTEE WHERE status IN ('ISSUED', 'AMENDED');
   ```
3. **Expiring Soon**: Count expiring in 30 days
   ```sql
   SELECT COUNT(*) FROM GUARANTEE 
   WHERE expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + 30;
   ```
4. **Under Review**: Count of guarantees with claims in UNDER_REVIEW status
   ```sql
   SELECT COUNT(DISTINCT g.id) FROM GUARANTEE g 
   JOIN CLAIM c ON g.id = c.guarantee_id 
   WHERE c.status = 'UNDER_REVIEW';
   ```

**In Frontend**:
- Show these as cards with color-coded numbers
- Refresh to see live updates when data changes

### Step 7: Pagination (2 min)

**In Frontend**:
1. Show **pagination controls** (Page 1 of 2, etc.)
2. Navigate between pages
3. Show items per page selector (10, 25, 50)

**Explain**:
- With 100,000+ guarantees, pagination is essential
- Backend `Pageable` support handles `?page=0&size=20`

---

## Discussion Points

### For Finance & Operations
1. **Portfolio Dashboard**: "Can we see all guarantees and their risk exposure in one view?"
   - Answer: Yes, with filters, KPIs, and color-coding
2. **Expiry Management**: "How do we track guarantees expiring soon?"
   - Answer: Date range filter + email alerts (future improvement)
3. **Regulatory Reporting**: "Can we export this data for compliance?"
   - Answer: Yes, CSV export (future improvement)

### For Technical Teams
1. **Query Performance**: "How fast is filtering with 100K guarantees?"
   - Answer: Sub-second with database indexes on status, type, expiry_date
2. **Pagination Strategy**: "Why paginate instead of load all?"
   - Answer: Memory efficiency; browser can't render 100K rows

---

## Technical Details

### Backend Filtering Logic

**Repository Methods**:
```java
List<Guarantee> findByStatus(GuaranteeStatus status);
List<Guarantee> findByType(GuaranteeType type);
List<Guarantee> findByStatusAndType(GuaranteeStatus status, GuaranteeType type);
Page<Guarantee> findAll(Pageable pageable);
```

**Controller**:
```java
@GetMapping
public List<GuaranteeDTO> list(
    @RequestParam(required = false) GuaranteeStatus status,
    @RequestParam(required = false) GuaranteeType type) {
    if (status != null && type != null) {
        return repository.findByStatusAndType(status, type).stream()
            .map(mapper::toDTO).collect(toList());
    }
    // ... other combinations
}
```

### Database Indexes

```sql
CREATE INDEX idx_guarantee_status ON GUARANTEE(status);
CREATE INDEX idx_guarantee_type ON GUARANTEE(type);
CREATE INDEX idx_guarantee_expiry ON GUARANTEE(expiry_date);
CREATE INDEX idx_guarantee_status_type ON GUARANTEE(status, type);
```

### Frontend Filtering

```typescript
// In GuaranteeListComponent
filters = {
  status: [],
  type: [],
  dateRange: { start: null, end: null }
};

applyFilters() {
  const params = new HttpParams()
    .set('status', this.filters.status.join(','))
    .set('type', this.filters.type.join(','));
  
  this.guaranteeService.list(params).subscribe(data => {
    this.guarantees = data;
  });
}
```

---

## Proposed Improvements (for Delegation)

### nova-frontend-gen (UI)
- [ ] **Dashboard with charts** (NgCharts): Pie chart of guarantees by status, Bar chart by type
- [ ] **Calendar heatmap** for expiry dates (visual risk indicator)
- [ ] **Advanced filter UI** with date range picker, multi-select dropdowns
- [ ] **Export to CSV/PDF** for reporting

### nova-api-integr (Integration)
- [ ] **Elasticsearch integration** for full-text search on reference, applicant name
- [ ] **GraphQL endpoint** for flexible querying (alternative to REST filters)
- [ ] **OpenAPI 3.0 spec** with complex query parameter documentation

### nova-ops-monitor (Infrastructure)
- [ ] **Grafana dashboard** showing KPIs in real-time
- [ ] **Prometheus metrics** for query latency and result counts
- [ ] **Database query optimization** (EXPLAIN ANALYZE)

### nova-async-comm (Messaging)
- [ ] **Expiry alerts** via RabbitMQ (daily report of guarantees expiring in 7 days)
- [ ] **Webhook for external systems** (e.g., SAP notification when guarantee expires)

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Filter returns 0 results | Check data exists in H2 Console; filters are cumulative (all must match) |
| Sorting not working | Refresh browser; check that sortable column is enabled in frontend |
| Pagination broken | Verify backend returns `Pageable` response; check frontend handles `Page` object |

---

**Previous Demo**: [Demo 2: Claims Processing Workflow](./demo-02-claims-workflow.md)  
**Next Demo**: [Demo 4: Multi-tenancy & Scaling](./demo-04-multitenancy-scaling.md)
