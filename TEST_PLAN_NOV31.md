# Test Plan: NOV-31 SUB-3 — Calendar View with Expiry Heatmap

## Objective
Verify the expiry calendar view implementation meets all contract requirements (NOV-28-CONTRACTS.md sections 5-6).

## Environment Setup

### Prerequisites
- Backend running: `docker compose up --build` or local Docker Compose
- Frontend running: `ng serve` (or npm start)
- Access URLs:
  - Frontend: http://localhost:4200
  - Backend API: http://localhost:8080/api/v1
  - Swagger: http://localhost:8080/swagger-ui.html
  - H2 Console: http://localhost:8080/h2-console

### Test Data
- Backend H2 in-memory database with 6 seed guarantees in various states (ISSUED, AMENDED, CLAIMED, EXPIRED, DRAFT)
- Guarantee amounts: 50k, 100k, 150k, 200k, 250k, 300k EUR/USD
- Expiry dates spread across current and future months

## Test Cases

### 1. Calendar Component Rendering

#### T1.1: Navigate to /calendar
- **Steps**:
  1. Open http://localhost:4200
  2. Click "Calendar" button in navigation toolbar
- **Expected**:
  - URL changes to /calendar
  - Calendar view loads with CSS Grid heatmap
  - Monthly calendar displays with current month

#### T1.2: Calendar grid structure
- **Steps**:
  1. Observe calendar grid
  2. Count grid columns (should be 7 for weekdays)
  3. Count grid rows (should be 6 + header for full month)
  4. Verify week day headers (Sun, Mon, Tue, Wed, Thu, Fri, Sat)
- **Expected**:
  - 7 columns, 6 rows for calendar days
  - Previous month's trailing days and next month's leading days shown as empty
  - Current month days correctly positioned

#### T1.3: Loading state
- **Steps**:
  1. Open calendar with slow 3G throttling (Chrome DevTools)
  2. Observe loading indicator
- **Expected**:
  - Loading spinner appears while fetching calendar data
  - Spinner disappears when data loads

### 2. Heatmap Color Coding

#### T2.1: Risk level color mapping
- **Steps**:
  1. Navigate to calendar
  2. Observe day cell colors
  3. Cross-reference with risk legend
- **Expected**:
  - Green (#4caf50) = none (no expirations)
  - Light green (#8bc34a) = low (≤7 days AND amount < 50k)
  - Amber (#ffc107) = medium (8-30 days OR amount 50k-200k)
  - Orange (#ff9800) = high (31-60 days OR amount > 200k)
  - Red (#f44336) = critical (>60 days OR very high amount / already expired)

#### T2.2: Risk legend display
- **Steps**:
  1. Look for risk legend on calendar
  2. Verify all 5 levels displayed
  3. Check legend descriptions match contract
- **Expected**:
  - Legend shows all 5 risk levels with color squares
  - Each level has description matching NOV-28-CONTRACTS.md

#### T2.3: Aggregate risk calculation
- **Steps**:
  1. Click on day with multiple guarantees of different risk levels
  2. Check cell background color
  3. Verify it matches the highest risk level among that day's guarantees
- **Expected**:
  - Day cell background color = highest risk level among all guarantees expiring that day

### 3. Calendar Day Tooltips

#### T3.1: Tooltip with amount and currency
- **Steps**:
  1. Hover over a colored day cell (day with guarantees)
  2. Wait for tooltip to appear
- **Expected**:
  - Tooltip shows total amount for that day
  - Tooltip displays per-currency breakdown (e.g., "EUR: 100000, USD: 50000")

#### T3.2: Empty days have no tooltip
- **Steps**:
  1. Hover over day with no expirations (white cell)
- **Expected**:
  - No tooltip appears

### 4. Side Panel — Day Detail View

#### T4.1: Open side panel by clicking day
- **Steps**:
  1. Click on a colored calendar day (e.g., a day with 3 guarantees)
- **Expected**:
  - Right side panel appears (or stacks below on mobile)
  - Panel shows: day number, month/year, and list of guarantees
  - Close button (X) visible in panel header

#### T4.2: Side panel displays guarantees for selected day
- **Steps**:
  1. Click on day with known guarantees (from test data)
  2. Examine side panel list
- **Expected**:
  - Panel title: "{day} — {month} {year}" (e.g., "5 — July 2026")
  - List of guarantees expiring on that day
  - Each row shows: reference, beneficiary name, amount + currency, days remaining, risk badge

#### T4.3: Days until expiry labels
- **Steps**:
  1. Look at "days remaining" column in side panel list
  2. For guarantees with different expiryDate values, verify labels:
     - Negative days → "Expired"
     - 0 days → "Today"
     - 1 day → "Tomorrow"
     - N days → "N days"
- **Expected**:
  - Correct label for each guarantee based on calculation from backend

#### T4.4: Close side panel
- **Steps**:
  1. Click X button in panel header
  2. Click same day again
- **Expected**:
  - Panel closes
  - Clicking same day re-opens panel
  - Clicking different day shows that day's details instead

#### T4.5: Navigate to guarantee detail from panel
- **Steps**:
  1. Click on guarantee row in side panel
  2. Verify navigation
- **Expected**:
  - Navigates to /guarantees/{id} detail view
  - Shows full guarantee details

### 5. Month Navigation

#### T5.1: Previous month button
- **Steps**:
  1. Click left arrow (Previous month button)
- **Expected**:
  - Calendar updates to previous month
  - Month/year in header changes
  - Calendar grid reloads with previous month's data
  - Side panel closes

#### T5.2: Next month button
- **Steps**:
  1. Click right arrow (Next month button)
  2. Click multiple times to span several months
- **Expected**:
  - Calendar updates to next month each click
  - Days and expiries update correctly
  - Month/year header reflects current view

#### T5.3: Month format in header
- **Steps**:
  1. Navigate through several months
  2. Observe header display
- **Expected**:
  - Format is "Month Year" (e.g., "July 2026")
  - Displayed using locale-aware formatting

### 6. Guarantee List Expiry Badges

#### T6.1: Navigate to /guarantees list
- **Steps**:
  1. Click "Guarantees" button in toolbar
- **Expected**:
  - Table displays with guarantees
  - New "Expiry Status" column visible (positioned after "Expiry Date")

#### T6.2: Badge color coding
- **Steps**:
  1. Observe each row's "Expiry Status" badge
  2. Compare to guarantee's expiryDate and amount
- **Expected**:
  - Each badge color matches risk level calculation
  - Green = low risk
  - Amber = medium risk
  - Orange = high risk
  - Red = critical/expired

#### T6.3: Badge text formatting
- **Steps**:
  1. Look at badge text for different guarantees:
     - Already expired: should show "Expired"
     - Expiring today: should show "Expires Today"
     - Expiring tomorrow: should show "Expires Tomorrow"
     - Future expiries: should show "Expires in N days"
- **Expected**:
  - Correct text format for each case

### 7. Live Updates via SSE (Server-Sent Events)

#### T7.1: SSE connection established
- **Steps**:
  1. Open browser DevTools → Network tab
  2. Navigate to /calendar
  3. Look for EventSource connection to SSE endpoint
- **Expected**:
  - Network tab shows GET request to `/api/v1/guarantee-events` or similar
  - Status shows 200 (long-lived connection)
  - No errors in Console tab

#### T7.2: Auto-refresh on guarantee expiration
- **Prerequisite**: Backend scheduler running (checks every 30 seconds for expired guarantees)
- **Steps**:
  1. Keep calendar and list open side-by-side
  2. Wait for backend scheduler to run (or trigger manually if available)
  3. Guarantee status transitions from ISSUED → EXPIRED
  4. Backend emits SSE event: { type: "expiration-auto", guaranteeId, reference, status: "EXPIRED", expiryDate, expiredAt }
- **Expected**:
  - Calendar auto-reloads without page refresh
  - List table auto-reloads without page refresh
  - Expiry badges update to show "Expired" with red background
  - Calendar cell colors update if that day was the expiry date
  - No console errors

#### T7.3: SSE connection resilience
- **Steps**:
  1. Simulate network disconnection (DevTools → throttle, then disable)
  2. Wait 30+ seconds
  3. Re-enable network
- **Expected**:
  - Console shows connection error (graceful)
  - On network restore, SSE reconnects automatically
  - Browser's EventSource API handles reconnection

### 8. Error States and Edge Cases

#### T8.1: Backend unavailable (no /expiry-calendar endpoint)
- **Steps**:
  1. Stop backend or simulate 503 error
  2. Navigate to /calendar
- **Expected**:
  - Error message displays: "Failed to load calendar. Using mock data."
  - Mock calendar renders with sample data for day 5, 15, 25
  - Calendar is functional (navigation, side panel work)

#### T8.2: Empty calendar (no expirations in month)
- **Steps**:
  1. Navigate to a past month (e.g., 2 years ago) with no guarantees
- **Expected**:
  - Calendar grid shows all white cells (no colored days)
  - Tooltip shows empty message for any day
  - Side panel shows "No guarantees expiring on this day"

#### T8.3: Single day with many guarantees
- **Steps**:
  1. Navigate to day with 10+ guarantees expiring
  2. Click side panel
- **Expected**:
  - Side panel scrollable if list exceeds visible height
  - All guarantees visible with proper scrolling
  - Panel layout remains responsive

#### T8.4: Responsiveness on mobile
- **Steps**:
  1. Open DevTools → Device toolbar
  2. Set to mobile (iPhone 12) or tablet (iPad)
  3. Navigate calendar
- **Expected**:
  - Calendar grid responsive (may wrap to fewer columns)
  - Side panel stacks below calendar on narrow screens
  - Touch interactions work (click on day, close button)
  - No horizontal scrolling needed

### 9. TypeScript Strict Mode & No `any` Types

#### T9.1: Type safety check
- **Steps**:
  1. Run TypeScript compiler: `ng build --configuration production`
  2. Check for any TS errors in calendar component
- **Expected**:
  - Build succeeds with no TypeScript errors
  - No warnings about `any` types in calendar.component.ts
  - ExpiryCalendar and ExpiryGuarantee types used throughout

### 10. Unit Tests

#### T10.1: Run calendar component tests
- **Steps**:
  1. Run `ng test --include="**/calendar.component.spec.ts"`
  2. Observe test output
- **Expected**:
  - All 13 tests pass
  - Coverage includes:
    - Component creation
    - Loading calendar data
    - Month navigation
    - Day selection
    - Risk color mapping
    - Mock data fallback
    - Calendar grid structure

## Acceptance Criteria

✅ All calendar elements render correctly:
- 7x6 CSS Grid with navigation
- Day numbers + count of expirations
- Color-coded backgrounds by riskLevel
- Tooltips with amounts and currencies

✅ Side panel functional:
- Displays guarantees for selected day
- Shows reference, beneficiary, amount, days remaining
- Risk badge color-coded
- Can navigate to detail view

✅ Expiry badges in list:
- Column added to table
- Badge color matches risk level
- Text shows days until expiry or "Expired"/"Today"/"Tomorrow"

✅ Live updates:
- SSE subscription active
- Calendar/list auto-reload on expiration events
- Badges update in real-time

✅ Code quality:
- TypeScript strict mode, no `any`
- Unit tests cover main functionality
- Responsive design works on mobile

## Sign-off

- **Frontend QA**: Verify all test cases pass
- **Integration QA**: Verify end-to-end with backend
- **Architect**: Approve for merge to main

---

**Contract Reference**: NOV-28-CONTRACTS.md sections 5-6
**Issue**: NOV-31 SUB-3
**Component**: CalendarComponent, expiry badges in GuaranteeListComponent
