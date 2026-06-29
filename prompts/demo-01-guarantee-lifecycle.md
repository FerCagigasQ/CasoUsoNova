# Demo 1: Guarantee Lifecycle (Create → Issue → Amend)

**Duration**: 35 minutes | **Complexity**: Beginner  
**Target Audience**: Business stakeholders, product managers, new engineers  
**Agents**: nova-service-gen, nova-frontend-gen, nova-api-integr

---

## Overview

This demo walks through the **complete lifecycle of a bank guarantee**, from creation in draft status through issuance to actual use (amendments). It shows how the NOVA platform manages the business process and validates state transitions.

### What You'll Show
- Creating a new guarantee in the Angular UI
- Viewing it in the backend API (Swagger)
- Issuing the guarantee (state transition: DRAFT → ISSUED)
- Amending its terms (adding new expiry date, amount adjustment)
- Real-time database view (H2 Console)

### Business Value
- **Automation**: End-to-end guarantee workflow in one platform
- **Audit trail**: Every state change is tracked and queryable
- **Data validation**: Guarantee amounts, dates, and parties are validated before issuance
- **ICC URDG 758 compliance**: State transitions follow banking standards

---

## Prerequisites (5 minutes)

### Environment Setup
```bash
# Terminal 1: Start backend + frontend
docker compose up --build

# OR (no Docker)
./run-local.sh          # Linux/Mac
.\run-local.ps1         # Windows
```

**Wait for**:
- Backend health check: `GET /actuator/health` returns `{"status":"UP"}`
- Frontend console shows: `Application bundle generated successfully`

### Open in Browser
- **Frontend**: http://localhost:4200 (dev) or http://localhost (Docker)
- **Swagger UI** (for API docs): http://localhost:8080/swagger-ui.html
- **H2 Console** (database viewer): http://localhost:8080/h2-console

---

## Step-by-Step Guide (30 minutes)

### Step 1: Show Initial Data (2 min)

**In Frontend** (http://localhost:4200):
1. Navigate to **Guarantees** (main page loads)
2. Show the **list of 6 seed guarantees** (status: ISSUED, AMENDED, CLAIMED, EXPIRED, DRAFT)
3. Point out various **states** (DRAFT, ISSUED, AMENDED, etc.)

**In Swagger UI** (http://localhost:8080/swagger-ui.html):
1. Click **GET /api/v1/guarantees**
2. Click **Try it out** → **Execute**
3. Show the JSON response with all 6 guarantees
4. Explain the **status** and **type** fields

**In H2 Console** (http://localhost:8080/h2-console):
1. Login with username `sa` (no password)
2. Run SQL: `SELECT COUNT(*) FROM GUARANTEE;` → shows 6 records
3. Explain the table structure: `id, reference, type, amount, status, ...`

### Step 2: Create a New Guarantee (5 min)

**In Frontend**:
1. Click **Create Guarantee** button (top-right)
2. Fill in the form:
   - **Reference**: `BG-2026-DEMO-001`
   - **Type**: PERFORMANCE
   - **Amount**: 50000
   - **Currency**: EUR
   - **Issue Date**: Today's date
   - **Expiry Date**: 30 days from today
   - **Applicant**: Select "Acme Corp"
   - **Beneficiary**: Select "BBVA Projects Inc"
   - **Issuing Bank**: Select "Deutsche Bank"
3. Click **Create** button

**Expected Result**:
- UI shows success message: "Guarantee created successfully"
- New guarantee appears in the list with status **DRAFT**
- Refresh H2 Console to verify: 7 records total now

### Step 3: Verify in Backend API (5 min)

**In Swagger UI**:
1. Click **GET /api/v1/guarantees**
2. Execute
3. **Scroll** to find your newly created guarantee (search for `BG-2026-DEMO-001`)
4. Copy the guarantee **ID** (e.g., `7`)

**In H2 Console**:
1. Run: `SELECT * FROM GUARANTEE WHERE REFERENCE = 'BG-2026-DEMO-001';`
2. Show the record:
   - `status`: DRAFT
   - `amount`: 50000
   - `currency`: EUR
   - `applicant_id`: (references Applicant)

### Step 4: Issue the Guarantee (State Transition) (5 min)

**In Frontend**:
1. Click the newly created guarantee in the list (row with `BG-2026-DEMO-001`)
2. Open the **detail view**
3. Click **Issue Guarantee** button

**Expected Result**:
- UI message: "Guarantee issued successfully"
- Status changes from **DRAFT** → **ISSUED**
- Button changes or becomes disabled (already issued)

**In Swagger UI** (verify via API):
1. Click **GET /api/v1/guarantees/{id}** 
2. Enter the guarantee ID (e.g., `7`)
3. Click **Execute**
4. Show the response:
   - `"status": "ISSUED"`
   - Timestamp updated (if using `issuedDate` field)

**In H2 Console**:
1. Run: `SELECT reference, status FROM GUARANTEE WHERE reference = 'BG-2026-DEMO-001';`
2. Verify: `BG-2026-DEMO-001 | ISSUED`

### Step 5: Add an Amendment (State Transition) (8 min)

**In Frontend**:
1. Stay on the guarantee detail view (BG-2026-DEMO-001)
2. Click **Amendments** tab
3. Click **Add Amendment** button
4. Fill in the form:
   - **Description**: "Increase amount due to scope expansion"
   - **New Amount**: 75000 (increase from 50000)
   - **New Expiry Date**: 60 days from today (extend from 30)
5. Click **Submit Amendment**

**Expected Result**:
- Amendment appears in the **Amendments** tab list
- Guarantee status changes from **ISSUED** → **AMENDED** (in the list view)
- Shows amendment details (date, description, new amount)

**In H2 Console**:
1. Run: `SELECT * FROM AMENDMENT WHERE guarantee_id = 7;`
2. Show the new record:
   - `description`: "Increase amount due to scope expansion"
   - `new_amount`: 75000
   - `new_expiry_date`: (60 days out)
3. Run: `SELECT reference, status FROM GUARANTEE WHERE reference = 'BG-2026-DEMO-001';`
4. Verify: Status is now **AMENDED**

### Step 6: Query with Filters (3 min)

**In Frontend**:
1. Go back to **Guarantees list**
2. Use the **filters** (Status dropdown, Type dropdown)
3. Filter by:
   - **Status**: AMENDED
   - **Type**: PERFORMANCE
4. Show that the list now shows **only amended performance guarantees** (including your new one)

**In Swagger UI**:
1. Click **GET /api/v1/guarantees**
2. Click **Try it out**
3. Add query parameters:
   - **status**: AMENDED
   - **type**: PERFORMANCE
4. Click **Execute**
5. Show filtered results

---

## Discussion Points

### For Business Stakeholders
1. **Process Efficiency**: "How does automating this workflow reduce manual errors?"
   - Answer: No paper forms, automatic state validation, audit trail built-in
2. **Compliance**: "How does NOVA track ICC URDG 758 compliance?"
   - Answer: State machine enforces valid transitions; amendments are timestamped
3. **Scalability**: "What if we have 100,000 guarantees?"
   - Answer: Database is optimized for queries; filtering is instant

### For Technical Teams
1. **State Machine**: "How are state transitions validated?"
   - Code: `GuaranteeService.issue()` checks `status == DRAFT` before updating
2. **Data Consistency**: "Can a guarantee be amended after expiry?"
   - Answer: Frontend/backend validation prevents invalid state transitions
3. **API Design**: "Why are amendments separate from guarantees?"
   - Answer: Domain-driven design; amendments are first-class events with audit trail

---

## Technical Details

### Backend Architecture

**State Machine** (in `GuaranteeService`):
```
DRAFT
  ↓ [issue()]
ISSUED
  ├─ [addAmendment()] → AMENDED
  ├─ [addClaim()] → CLAIMED
  └─ [expiry date reached] → EXPIRED
```

**API Endpoints Used**:
- `POST /api/v1/guarantees` — Create (backend sets status=DRAFT)
- `POST /api/v1/guarantees/{id}/issue` — Issue (status: DRAFT → ISSUED)
- `POST /api/v1/guarantees/{id}/amendments` — Add amendment (status: ISSUED → AMENDED)
- `GET /api/v1/guarantees?status=AMENDED&type=PERFORMANCE` — Filter

**Data Models**:
- `Guarantee`: Main entity, has status, amount, dates, parties
- `Amendment`: OneToMany relationship to Guarantee, has description, new_amount, new_expiry_date
- `Applicant`, `Beneficiary`, `IssuingBank`: Reference data

### Frontend Architecture

**Components Used**:
- `GuaranteeListComponent`: Shows list with filters
- `GuaranteeDetailComponent`: Shows single guarantee with tabs
- `GuaranteeFormComponent`: Create/edit form
- `AmendmentDialogComponent`: Modal for adding amendments

**Service Integration**:
- `GuaranteeService`: HTTP calls to backend, handles filters
- Reactive Forms for validation

### Database Schema

```sql
GUARANTEE
├── id (PK)
├── reference (UNIQUE)
├── type (enum: PERFORMANCE, ADVANCE_PAYMENT, BID_BOND, WARRANTY)
├── amount (DECIMAL)
├── currency (VARCHAR: EUR, USD, GBP, etc.)
├── status (enum: DRAFT, ISSUED, AMENDED, CLAIMED, EXPIRED, CANCELLED)
├── issue_date (DATE)
├── expiry_date (DATE)
├── applicant_id (FK)
├── beneficiary_id (FK)
└── issuing_bank_id (FK)

AMENDMENT
├── id (PK)
├── guarantee_id (FK)
├── amendment_date (DATE)
├── description (VARCHAR)
├── new_amount (DECIMAL, nullable)
└── new_expiry_date (DATE, nullable)

APPLICANT
├── id (PK)
├── first_name, last_name
├── tax_id, email, phone
├── address, country

BENEFICIARY
├── id (PK)
├── first_name, last_name
├── tax_id, email, phone
├── address, country

ISSUING_BANK
├── id (PK)
├── name
├── bic (SWIFT)
└── country
```

---

## Proposed Improvements (for Delegation)

### nova-service-gen (Backend)
- [ ] Add **bulk guarantee upload** endpoint (CSV file)
- [ ] Implement **guarantee expiry notifications** (scheduled job every night)
- [ ] Add **API versioning** (v2 with backward compatibility)

### nova-frontend-gen (UI)
- [ ] Add **real-time status updates** using WebSocket/SSE
- [ ] Implement **amendment history timeline** (visual representation)
- [ ] Add **PDF generation** for guarantee certificates

### nova-api-integr (Integration)
- [ ] Generate **Feign client** for multi-service calls
- [ ] Add **request/response logging** middleware
- [ ] Implement **circuit breaker** for downstream calls

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Frontend won't load | Check Docker logs: `docker logs guarantees-ui` |
| Backend API not responding | Verify health: `curl http://localhost:8080/actuator/health` |
| H2 Console login fails | Username: `sa`, Password: (empty) |
| New guarantee not appearing | Refresh browser (F5) and check H2 Console for DB record |
| Amendment button disabled | Ensure guarantee status is ISSUED (check H2 Console) |

---

## Notes for Future Runs

- **Timing**: The 5-minute setup is one-time. Subsequent demos reuse the running environment.
- **Reset data**: To clear all guarantees, restart Docker or run SQL: `DELETE FROM GUARANTEE;`
- **Browser DevTools**: Open Network tab to see API calls in real-time
- **Swagger UI**: Great for showing API responses in detail; use alongside UI for contrast

---

**Next Demo**: [Demo 2: Claims Processing Workflow](./demo-02-claims-workflow.md)
