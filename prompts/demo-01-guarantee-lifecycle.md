# Demo 1: Guarantee Lifecycle (Create → Issue → Amend)

**Development Sprint**: Guarantee CRUD + State Transitions  
**Estimated Duration**: 4-6 hours (full implementation + testing)  
**Target**: Complete guarantee lifecycle endpoints + UI + API documentation  
**Agents**: nova-service-gen, nova-frontend-gen, nova-api-integr

---

## Business Context

Bank guarantees flow through a lifecycle: created in DRAFT status, issued (transitioned to ISSUED), amended (status → AMENDED), and eventually claimed or expired. The system must validate state transitions and maintain audit trail per ICC URDG 758.

---

## Task Breakdown

### nova-service-gen (Backend Services)

**Objective**: Implement complete guarantee CRUD + lifecycle management

**Must deliver**:

1. **Guarantee Entity & JPA Mapping**
   - [ ] Create `Guarantee` JPA entity with fields: id, reference (UNIQUE), type (enum), amount (BigDecimal), currency, issueDate, expiryDate, status (enum), applicant_id (FK), beneficiary_id (FK), issuing_bank_id (FK)
   - [ ] Create `GuaranteeStatus` enum: DRAFT, ISSUED, AMENDED, CLAIMED, EXPIRED, CANCELLED
   - [ ] Create `GuaranteeType` enum: PERFORMANCE, ADVANCE_PAYMENT, BID_BOND, WARRANTY
   - [ ] Set up relationships: ManyToOne to Applicant, Beneficiary, IssuingBank (all EAGER)
   - [ ] Create `Amendment` and `Claim` entities with OneToMany back-references

2. **Repository Layer**
   - [ ] Create `GuaranteeRepository extends JpaRepository<Guarantee, Long>`
   - [ ] Add filter methods: `findByStatus(GuaranteeStatus)`, `findByType(GuaranteeType)`, `findByStatusAndType(...)`
   - [ ] Add database indexes on status, type, and composite (status, type)

3. **REST Endpoints**
   - [ ] `GET /api/v1/guarantees` — List all (support `?status=ISSUED&type=PERFORMANCE` filters)
   - [ ] `GET /api/v1/guarantees/{id}` — Get single guarantee with all relationships
   - [ ] `POST /api/v1/guarantees` — Create new (status auto-set to DRAFT)
   - [ ] `PUT /api/v1/guarantees/{id}` — Update guarantee (amount, dates, parties)
   - [ ] `DELETE /api/v1/guarantees/{id}` — Delete (only in DRAFT status)
   - [ ] `POST /api/v1/guarantees/{id}/issue` — State transition DRAFT → ISSUED
   - [ ] `POST /api/v1/guarantees/{id}/amendments` — Add amendment, auto-transition to AMENDED
   - [ ] Response format: GuaranteeDTO with nested Applicant, Beneficiary, IssuingBank objects

4. **Validation & State Machine**
   - [ ] Before issuance: validate amount > 0, expiryDate > issueDate, parties non-null
   - [ ] State machine: Only allow ISSUED guarantees to be amended/claimed
   - [ ] Amendment must not exceed 3 per guarantee (business rule)
   - [ ] Return 400 Bad Request if state transition invalid, 404 if guarantee not found

5. **Seed Data**
   - [ ] Create `DataSeeder` component that runs on startup
   - [ ] Insert 6 test guarantees in various states (DRAFT, ISSUED, AMENDED, CLAIMED, EXPIRED)
   - [ ] Create 3 Applicants, 3 Beneficiaries, 3 IssuingBanks

6. **DTOs**
   - [ ] Create `GuaranteeDTO`, `ApplicantDTO`, `BeneficiaryDTO`, `IssuingBankDTO`, `AmendmentDTO`, `ClaimDTO`
   - [ ] Ensure field names match frontend contract exactly (reference, issueDate, expiryDate, NOT startDate/endDate)

**Success Criteria**:
- ✅ All 9 endpoints implemented and return correct HTTP status codes (201 CREATE, 200 OK, 400 Bad Request, 404 Not Found)
- ✅ State transitions validated (reject invalid transitions with error message)
- ✅ Seed data loads on startup
- ✅ Unit tests for repository, service, controller (>80% coverage)
- ✅ Swagger documentation auto-generated (OpenAPI 3.0)

---

### nova-frontend-gen (Angular UI)

**Objective**: Build guarantee management UI with forms, list, detail view

**Must deliver**:

1. **Guarantee List Component**
   - [ ] Display table: reference, type, amount, currency, status, issueDate, expiryDate
   - [ ] Add status badge (color-coded: blue=DRAFT, green=ISSUED, orange=AMENDED, red=CLAIMED, gray=EXPIRED)
   - [ ] Add type badge (color variations)
   - [ ] Support sorting by clicking column headers
   - [ ] Support pagination (20 items/page default, configurable)
   - [ ] Add status filter dropdown (multiselect: DRAFT, ISSUED, AMENDED, etc.)
   - [ ] Add type filter dropdown (multiselect)
   - [ ] Click row to navigate to detail view

2. **Guarantee Detail Component**
   - [ ] Show guarantee info in read-only format
   - [ ] Tabs: General, Amendments, Claims
   - [ ] General tab: reference, type, amount, currency, dates, applicant (firstName, lastName, email), beneficiary (same), issuing bank (name, bic, country)
   - [ ] Button: "Issue Guarantee" (DRAFT→ISSUED transition, visible only if status=DRAFT)
   - [ ] Button: "Edit" (disabled if not DRAFT)
   - [ ] Button: "Delete" (only if DRAFT)

3. **Guarantee Form (Create/Edit)**
   - [ ] Modal/page with form fields: reference, type (dropdown), amount, currency (dropdown), issueDate (date picker), expiryDate, applicant (autocomplete or dropdown), beneficiary (dropdown), issuing bank (dropdown)
   - [ ] Validation: amount > 0, expiryDate > issueDate, all fields required
   - [ ] On submit: POST /api/v1/guarantees (create) or PUT /api/v1/guarantees/{id} (update)
   - [ ] Show success/error toast notification

4. **Amendment Dialog**
   - [ ] Modal triggered from Amendments tab
   - [ ] Form fields: description (textarea), newAmount (decimal), newExpiryDate (date picker)
   - [ ] On submit: POST /api/v1/guarantees/{id}/amendments
   - [ ] Auto-refresh guarantee detail after success

5. **Material Design**
   - [ ] Use Angular Material table, buttons, forms, dialogs
   - [ ] Apply indigo-pink theme
   - [ ] Responsive layout (breakpoints for mobile/tablet)
   - [ ] Consistent spacing and typography

6. **HTTP Integration**
   - [ ] Create `GuaranteeService` with methods: list(), getById(), create(), update(), delete(), issue(), addAmendment()
   - [ ] Use `HttpClient` with proper error handling
   - [ ] Auto-convert JSON responses to TypeScript models

**Success Criteria**:
- ✅ All UI components render without console errors
- ✅ Forms submit data in correct shape (match backend DTO expectations)
- ✅ Guarantees can be created, viewed, issued, amended through UI
- ✅ Responsive design works on mobile/desktop
- ✅ TypeScript strict mode passes (no `any` types)
- ✅ Unit tests for services and components (>70% coverage)

---

### nova-api-integr (Service Integration)

**Objective**: Ensure backend/frontend contract integrity and API documentation

**Must deliver**:

1. **OpenAPI/Swagger Documentation**
   - [ ] Auto-generate Swagger UI from Spring Boot annotations (@Operation, @ApiResponse, etc.)
   - [ ] Verify endpoint at http://localhost:8080/swagger-ui.html
   - [ ] Verify OpenAPI JSON at http://localhost:8080/v3/api-docs
   - [ ] Document all query parameters, request bodies, responses
   - [ ] Include example values (e.g., BG-2026-001, EUR, 50000)

2. **Request/Response Contract Validation**
   - [ ] Verify field names match exactly: reference (not refNumber), issueDate (not startDate), expiryDate (not endDate)
   - [ ] Verify nested objects returned: applicant {firstName, lastName, email, ...}, not just applicantId string
   - [ ] Verify enum values (DRAFT, ISSUED, etc.) returned as strings, not numbers
   - [ ] Add validation layer: validate incoming requests against constraints (amount > 0, etc.)

3. **CORS Configuration**
   - [ ] Allow frontend (http://localhost:4200) to call backend (http://localhost:8080)
   - [ ] Configure in `WebConfig` or `SecurityConfig`

4. **Error Handling**
   - [ ] Return 400 Bad Request with error message for invalid transitions
   - [ ] Return 404 Not Found when guarantee not found
   - [ ] Return 409 Conflict if concurrent update detected
   - [ ] Consistent error response format: `{error: "message", status: 400}`

5. **Integration Testing**
   - [ ] Test create → issue → amend flow end-to-end
   - [ ] Verify frontend receives correct response shape
   - [ ] Test filter queries (?status=ISSUED&type=PERFORMANCE)

**Success Criteria**:
- ✅ Swagger UI shows all 9 endpoints with correct parameters
- ✅ Frontend can successfully call all endpoints (no 400/409 errors from contract mismatch)
- ✅ CORS allows cross-origin requests
- ✅ Error responses include helpful messages

---

## Verification Checklist

**After all agents complete their tasks**:

- [ ] Run `docker compose up --build` — both services start successfully
- [ ] Navigate to http://localhost:4200 → Guarantee list loads (6 seed guarantees visible)
- [ ] Create a new guarantee via UI → POST /api/v1/guarantees succeeds → new guarantee appears in list
- [ ] Issue the guarantee via UI → status changes DRAFT → ISSUED
- [ ] Add amendment → status changes ISSUED → AMENDED
- [ ] View in H2 Console → 7 guarantees total, 1 in AMENDED status
- [ ] Filter by status=AMENDED → shows only amended guarantee
- [ ] Delete attempt on AMENDED guarantee → returns error (not allowed)
- [ ] Swagger UI shows all endpoints

**Definition of Done for Demo 1**: 
All 3 agents complete their tasks, system passes verification checklist, commits pushed to `main`.

---

## References

- **Backend API Spec**: REST endpoints defined above
- **Frontend Models**: `Guarantee`, `Applicant`, `Beneficiary`, `IssuingBank` interfaces
- **Database Schema**: H2 (in-memory) with tables: GUARANTEE, APPLICANT, BENEFICIARY, ISSUING_BANK, AMENDMENT, CLAIM
- **Architecture**: Spring Boot 3.2.x + Angular 17 + Docker Compose
