# Demo 2: Claims Processing Workflow

**Duration**: 40 minutes | **Complexity**: Intermediate  
**Target Audience**: Financial operations, compliance teams, developers  
**Agents**: nova-service-gen, nova-frontend-gen, nova-async-comm

---

## Overview

This demo shows the **complete claims lifecycle**: submitting a claim on an active guarantee, tracking its status through multiple stages (SUBMITTED → UNDER_REVIEW → PAID/REJECTED), and viewing the audit trail.

### What You'll Show
- Viewing active guarantees eligible for claims
- Submitting a claim through the Angular UI
- Tracking claim status in real-time
- Filtering claims by status
- Database audit trail (H2 Console)
- Async notification setup (future enhancement)

### Business Value
- **Claims automation**: Streamline claim submission and tracking
- **Transparency**: Beneficiaries see claim status in real-time
- **Compliance**: Every claim is timestamped and auditable
- **SLA management**: Can track days-to-resolution per claim

---

## Prerequisites (2 minutes)

### Services Running
Ensure demo 1's environment is still running:
```bash
docker compose ps  # Should show backend + frontend healthy
```

Or restart:
```bash
docker compose up --build
```

### Guarantees for Claims
The demo uses existing guaranteed guarantees from the seed data:
- `BG-2026-001` (ISSUED, Performance Bond)
- `BG-2026-002` (ISSUED, Bid Bond)

**Alternatively**, create a fresh guarantee first (see Demo 1, Steps 1-4).

---

## Step-by-Step Guide (38 minutes)

### Step 1: Identify Claimable Guarantees (3 min)

**In Frontend** (http://localhost:4200):
1. Navigate to **Guarantees list**
2. Look for guarantees with **status: ISSUED or AMENDED** (can only claim on active guarantees)
3. Click on one that says **ISSUED** (e.g., `BG-2026-001`)

**In Swagger UI** (http://localhost:8080/swagger-ui.html):
1. Click **GET /api/v1/guarantees**
2. Filter by **status=ISSUED**
3. Copy the ID of an ISSUED guarantee (for API verification)

**Business Rule**:
> Claims can only be submitted on guarantees in ISSUED or AMENDED status. Claims on DRAFT, EXPIRED, or CANCELLED guarantees are rejected by the backend.

### Step 2: View Guarantee Details (2 min)

**In Frontend**:
1. Click the guarantee (e.g., `BG-2026-001` in the list)
2. Open **detail view**
3. Show tabs:
   - **General**: guarantee info (amount, dates, applicant, beneficiary)
   - **Amendments**: any amendments (empty if just issued)
   - **Claims**: claims list (initially empty)

### Step 3: Submit a Claim (8 min)

**In Frontend**:
1. Click the **Claims** tab
2. Click **Submit Claim** button
3. Fill in the form:
   - **Claimed Amount**: 25000 (half of guarantee amount 50000)
   - **Reason**: "Performance not completed as per contract terms"
   - Click **Submit Claim**

**Expected Result**:
- UI shows: "Claim submitted successfully"
- New claim appears in the Claims tab list with status **SUBMITTED**
- Claim date is today's date
- Backend automatically sets status to SUBMITTED

### Step 4: Verify in Backend API (5 min)

**In Swagger UI**:
1. Click **GET /api/v1/guarantees/{id}/claims**
2. Enter guarantee ID (e.g., `1`)
3. Click **Execute**
4. Show the response:
   ```json
   [
     {
       "id": 1,
       "claimDate": "2026-06-29",
       "claimedAmount": 25000,
       "reason": "Performance not completed as per contract terms",
       "status": "SUBMITTED"
     }
   ]
   ```

**In H2 Console** (http://localhost:8080/h2-console):
1. Run: `SELECT * FROM CLAIM;`
2. Show new record:
   - `guarantee_id`: (references the guarantee)
   - `claimed_amount`: 25000
   - `reason`: "Performance not completed..."
   - `status`: SUBMITTED
   - `claim_date`: TODAY

### Step 5: Simulate Claim Review (7 min)

**Explain the workflow** (before updating):
1. Claim submitted → Beneficiary notified (async, future feature)
2. Issuing bank reviews claim → Status: UNDER_REVIEW
3. Bank approves/rejects → Status: PAID or REJECTED

**In Swagger UI**:
1. Click **PUT /api/v1/guarantees/{id}/claims/{claimId}**
2. Enter guarantee ID and claim ID
3. Provide request body:
   ```json
   {
     "status": "UNDER_REVIEW",
     "claimedAmount": 25000,
     "reason": "Performance not completed as per contract terms"
   }
   ```
4. Click **Execute**
5. Show response with updated `"status": "UNDER_REVIEW"`

**In Frontend** (refresh Claims tab):
1. Press F5 or click refresh
2. Claim now shows **status: UNDER_REVIEW**

### Step 6: Approve the Claim (8 min)

**In Swagger UI**:
1. Click **PUT /api/v1/guarantees/{id}/claims/{claimId}** again
2. Update request body:
   ```json
   {
     "status": "PAID",
     "claimedAmount": 25000,
     "reason": "Approved: Payment issued to beneficiary account"
   }
   ```
3. Click **Execute**

**In Frontend** (refresh):
1. Claim now shows **status: PAID**
2. Guarantee status might change to **CLAIMED** (see step 7)

**In H2 Console**:
1. Run: `SELECT claim_date, status, claimed_amount FROM CLAIM;`
2. Verify: Status is now PAID

### Step 7: Check Guarantee Status Update (3 min)

**Business Rule**:
> When a claim is submitted on a guarantee, the guarantee status changes to CLAIMED.

**In Frontend**:
1. Go back to **Guarantees list**
2. Find the guarantee (e.g., `BG-2026-001`)
3. Its status should now be **CLAIMED** (changed from ISSUED)

**In H2 Console**:
1. Run: `SELECT reference, status FROM GUARANTEE WHERE id = 1;`
2. Verify: Status changed to **CLAIMED**

### Step 8: Submit Additional Claims (5 min)

**Show claim accumulation**:
1. Go back to guarantee **Claims tab**
2. Click **Submit Claim** again
3. Fill in:
   - **Claimed Amount**: 15000 (partial claim on the remaining amount)
   - **Reason**: "Additional damages due to non-performance"
4. Click **Submit**

**Explain**:
- Multiple claims can exist on a single guarantee
- Each claim has its own status lifecycle
- Total claimed amount can exceed original guarantee (for demonstration)

**In H2 Console**:
1. Run: `SELECT COUNT(*) FROM CLAIM WHERE guarantee_id = 1;`
2. Should show 2 claims now

---

## Discussion Points

### For Operations & Compliance Teams
1. **SLA Tracking**: "How long should a claim review take?"
   - Answer: Status field enables SLA monitoring; Grafana dashboard can track time in each stage
2. **Audit Trail**: "Can we prove who approved the claim and when?"
   - Answer: Timestamp + status history in H2 Console; future: Event Sourcing for complete audit
3. **Multiple Claims**: "What if 10 claims come in on the same guarantee?"
   - Answer: All are stored; system can track total exposure per guarantee

### For Technical Teams
1. **State Transitions**: "What are valid claim status transitions?"
   - Answer: SUBMITTED → UNDER_REVIEW → (PAID or REJECTED)
2. **Guarantee Status Link**: "Why does submitting a claim change the guarantee status?"
   - Answer: Domain rule; a guarantee with open claims is in active use
3. **API Consistency**: "Why is PUT used to update claim status, not PATCH?"
   - Answer: Simplicity; PATCH would require partial updates, PUT is full replace

---

## Technical Details

### Backend State Machine

**Claim Status Transitions**:
```
SUBMITTED
  ├─ [bank reviews]
  ├─ [bank approves] → PAID
  └─ [bank rejects] → REJECTED
```

**Guarantee Status Update**:
```
ISSUED/AMENDED
  ├─ [claim submitted]
  └─ → CLAIMED
```

**API Endpoints Used**:
- `POST /api/v1/guarantees/{id}/claims` — Submit claim
- `PUT /api/v1/guarantees/{id}/claims/{claimId}` — Update claim status
- `GET /api/v1/guarantees/{id}/claims` — List claims for guarantee

### Data Models

**Claim Entity**:
```java
@Entity
@Table(name = "CLAIM")
public class Claim {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Guarantee guarantee;
    
    private LocalDate claimDate;
    private BigDecimal claimedAmount;
    private String reason;
    
    @Enumerated(EnumType.STRING)
    private ClaimStatus status; // SUBMITTED, UNDER_REVIEW, PAID, REJECTED
}
```

**ClaimStatus Enum**:
```java
public enum ClaimStatus {
    SUBMITTED,
    UNDER_REVIEW,
    PAID,
    REJECTED
}
```

### Frontend Integration

**Components**:
- `ClaimsTabComponent`: Shows claims list for a guarantee
- `ClaimDialogComponent`: Modal for submitting claims
- `ClaimListComponent`: Table with claim details and status

**Service**:
```typescript
// In GuaranteeService
submitClaim(guaranteeId: number, claimDto: ClaimDTO): Observable<ClaimDTO> {
  return this.http.post(`/api/v1/guarantees/${guaranteeId}/claims`, claimDto);
}

updateClaimStatus(guaranteeId: number, claimId: number, claimDto: ClaimDTO) {
  return this.http.put(`/api/v1/guarantees/${guaranteeId}/claims/${claimId}`, claimDto);
}

getClaimsForGuarantee(guaranteeId: number): Observable<ClaimDTO[]> {
  return this.http.get(`/api/v1/guarantees/${guaranteeId}/claims`);
}
```

### Database Schema

```sql
CLAIM
├── id (PK)
├── guarantee_id (FK) → GUARANTEE
├── claim_date (DATE)
├── claimed_amount (DECIMAL)
├── reason (VARCHAR)
└── status (ENUM: SUBMITTED, UNDER_REVIEW, PAID, REJECTED)

-- Index for performance
CREATE INDEX idx_claim_guarantee_status ON CLAIM(guarantee_id, status);
```

---

## Proposed Improvements (for Delegation)

### nova-service-gen (Backend)
- [ ] **Claim rejection reasons** — Add structured rejection reasons (e.g., "Missing documentation", "Amount exceeds limit")
- [ ] **Claim amendments** — Allow beneficiary to revise claimed amount before approval
- [ ] **Automatic expiry transitions** — Job that marks guarantees/claims expired after due date

### nova-frontend-gen (UI)
- [ ] **Claim status badge** — Color-coded badges (red=SUBMITTED, yellow=UNDER_REVIEW, green=PAID, gray=REJECTED)
- [ ] **Claim history timeline** — Visual timeline of status transitions with timestamps
- [ ] **Claim PDF export** — Generate claim statement PDF for download

### nova-async-comm (Messaging)
- [ ] **Claim notification events** — Publish to RabbitMQ when claim is submitted/approved/rejected
- [ ] **Email notifications** — Send email to applicant/beneficiary on claim status change
- [ ] **Webhook integration** — Allow external systems to subscribe to claim events

### nova-api-integr (Integration)
- [ ] **Feign client for claims** — Generate Feign client for multi-service claim queries
- [ ] **Request validation middleware** — Validate claim amounts against guarantee balance
- [ ] **Circuit breaker** — Add resilience for downstream claim notification services

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Cannot submit claim on DRAFT guarantee" | Ensure guarantee status is ISSUED or AMENDED (see Demo 1) |
| Claim not appearing after submission | Refresh browser (F5) or restart Docker |
| PUT request returns 404 | Verify guarantee ID and claim ID exist (check H2 Console) |
| H2 data empty | Run seed data: `docker compose restart` |

---

## Notes for Future Runs

- **Reusable guarantees**: Don't delete guarantees between demos; create new claims instead
- **Claim accumulation**: Each run, claims accumulate in H2; reset with `DELETE FROM CLAIM;` if needed
- **Browser DevTools**: Network tab shows API responses in detail
- **Swagger responses**: Copy the claim ID from POST response for PUT operations

---

**Previous Demo**: [Demo 1: Guarantee Lifecycle](./demo-01-guarantee-lifecycle.md)  
**Next Demo**: [Demo 3: Real-time Dashboard & Filtering](./demo-03-dashboard-filtering.md)
