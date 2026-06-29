# Demo 2: Claims Processing Workflow

**Development Sprint**: Claim submission + Status tracking  
**Estimated Duration**: 3-4 hours (implementation + testing)  
**Target**: Complete claims lifecycle (SUBMITTED → UNDER_REVIEW → PAID/REJECTED)  
**Agents**: nova-service-gen, nova-frontend-gen, nova-async-comm

---

## Business Context

Claims are submitted against active guarantees. The system tracks claim status through workflow stages and publishes events for notifications. Only ISSUED or AMENDED guarantees can receive claims.

---

## Task Breakdown

### nova-service-gen (Backend Services)

**Objective**: Implement claim submission + state management

**Must deliver**:

1. **Claim Entity & State Management**
   - [ ] Create `Claim` JPA entity: id, guarantee_id (FK), claimDate (LocalDate), claimedAmount (BigDecimal), reason (String), status (enum)
   - [ ] Create `ClaimStatus` enum: SUBMITTED, UNDER_REVIEW, PAID, REJECTED
   - [ ] Add ManyToOne relationship to Guarantee

2. **REST Endpoints**
   - [ ] `POST /api/v1/guarantees/{guaranteeId}/claims` — Submit new claim (auto-set status=SUBMITTED, auto-set guarantee status=CLAIMED)
   - [ ] `PUT /api/v1/guarantees/{guaranteeId}/claims/{claimId}` — Update claim status (SUBMITTED→UNDER_REVIEW, UNDER_REVIEW→PAID/REJECTED)
   - [ ] `GET /api/v1/guarantees/{guaranteeId}/claims` — List all claims for a guarantee
   - [ ] `GET /api/v1/guarantees/{guaranteeId}/claims/{claimId}` — Get single claim

3. **Validation**
   - [ ] Only allow claims on ISSUED or AMENDED guarantees (return 400 if DRAFT/EXPIRED/CLAIMED)
   - [ ] Validate claimedAmount > 0 and ≤ guarantee amount (warning if exceeds, but allow)
   - [ ] Validate reason is non-empty

4. **State Transitions**
   - [ ] On claim submission: auto-update guarantee.status to CLAIMED
   - [ ] Valid transitions: SUBMITTED→UNDER_REVIEW, SUBMITTED→REJECTED, UNDER_REVIEW→PAID, UNDER_REVIEW→REJECTED
   - [ ] Block invalid transitions (return 400 with error message)

5. **Audit Trail**
   - [ ] Store claimDate automatically on creation
   - [ ] Support query: list all claims for a guarantee with timestamps

**Success Criteria**:
- ✅ All 4 endpoints implemented and tested
- ✅ Guarantee status auto-updates to CLAIMED when claim submitted
- ✅ State transitions validated
- ✅ Unit tests with >80% coverage

---

### nova-frontend-gen (Angular UI)

**Objective**: Build claim submission + tracking UI

**Must deliver**:

1. **Claims Tab in Guarantee Detail**
   - [ ] Add "Claims" tab to guarantee detail view (alongside General, Amendments)
   - [ ] Show table: claimDate, claimedAmount, reason, status (badge-colored)
   - [ ] Status badges: gray=SUBMITTED, yellow=UNDER_REVIEW, green=PAID, red=REJECTED

2. **Claim Submission Form**
   - [ ] Button: "Submit Claim" (visible if guarantee status is ISSUED or AMENDED)
   - [ ] Modal form: claimedAmount (decimal input), reason (textarea)
   - [ ] Validation: amount > 0, reason non-empty
   - [ ] On submit: POST /api/v1/guarantees/{id}/claims
   - [ ] Auto-refresh guarantee detail and claims list

3. **Claim Status Updates**
   - [ ] In claims table, add action buttons (only visible to admin/reviewer)
   - [ ] Button: "Mark Under Review" (transitions SUBMITTED→UNDER_REVIEW)
   - [ ] Button: "Pay Claim" (transitions UNDER_REVIEW→PAID)
   - [ ] Button: "Reject Claim" (transitions UNDER_REVIEW→REJECTED)
   - [ ] On click: PUT /api/v1/guarantees/{id}/claims/{claimId} with new status

4. **Real-time Feedback**
   - [ ] Toast notification on claim submission success/failure
   - [ ] Display error message if guarantee not eligible (status != ISSUED/AMENDED)

**Success Criteria**:
- ✅ Claims can be submitted via UI
- ✅ Claim status can be updated via action buttons
- ✅ Guarantee status updates to CLAIMED when claim submitted
- ✅ Form validation works
- ✅ No console errors

---

### nova-async-comm (Messaging & Events)

**Objective**: Publish claim events for notifications

**Must deliver**:

1. **Event Publishing**
   - [ ] Create `ClaimSubmittedEvent` class: claimId, guaranteeId, applicantEmail, beneficiaryEmail, claimedAmount, timestamp
   - [ ] Create `ClaimStatusChangedEvent` class: claimId, guaranteeId, oldStatus, newStatus, timestamp
   - [ ] In `ClaimService.submitClaim()`: publish ClaimSubmittedEvent
   - [ ] In `ClaimService.updateStatus()`: publish ClaimStatusChangedEvent
   - [ ] Use `RabbitTemplate.convertAndSend()` to publish to `guarantees.events` exchange

2. **RabbitMQ Configuration**
   - [ ] Configure Spring AMQP: host=localhost, port=5672, user/pass=guest/guest
   - [ ] Create queue: `claim.submissions`
   - [ ] Bind to exchange: `guarantees.events` with routing key: `claim.*`
   - [ ] Event payload format: JSON (includes all fields above)

3. **Event Versioning**
   - [ ] Include version field in events (v1, for future upgrades)
   - [ ] Backward compatible: old consumers can ignore new fields

**Success Criteria**:
- ✅ RabbitMQ broker running (docker compose includes it)
- ✅ Events published to queue when claims submitted/updated
- ✅ Queue accumulates messages (visible in RabbitMQ Management UI)
- ✅ Event payload includes all required fields

---

## Verification Checklist

**After all agents complete**:

- [ ] Submit claim via UI → message appears in RabbitMQ queue (`claim.submissions`)
- [ ] Check guarantee status changed to CLAIMED
- [ ] Update claim status via UI → new event published
- [ ] View in RabbitMQ Management UI: 2+ messages in queue
- [ ] All endpoints return correct HTTP status codes

**Definition of Done for Demo 2**: 
All 3 agents complete tasks, verification checklist passes, commits pushed.
