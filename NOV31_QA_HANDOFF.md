# NOV-31 SUB-3: QA Handoff — Calendar View Implementation

## Executive Summary

Frontend implementation for NOV-31 SUB-3 (Vista /calendar con heatmap de vencimientos y badges en vivo) is **COMPLETE** and merged to `main` branch (commit: 8100609).

**Status**: ✅ **READY FOR QA TESTING**

---

## What Was Implemented

### 1. Calendar View Component
- **Route**: `/calendar`
- **File**: `guarantees-ui/src/app/features/calendar/calendar.component.ts`
- **Features**:
  - 7x6 CSS Grid monthly calendar with month navigation (prev/next)
  - Color-coded day cells by risk level (green→amber→red)
  - Day tooltips showing total amount and per-currency breakdown
  - Side panel with guarantee details per selected day
  - Risk level legend with 5 categories and descriptions
  - Loading/error states with mock data fallback
  - SSE integration for real-time expiration updates

### 2. Expiry Badges in Guarantee List
- **Route**: `/guarantees` (existing page)
- **Feature**: New "Expiry Status" column in guarantee table
- **Badge Info**:
  - Color-coded by risk level
  - Text: "Expired" | "Expires Today" | "Expires Tomorrow" | "Expires in N days"
  - Updates live via SSE when guarantees expire

### 3. Navigation Integration
- Added "Calendar" button to main toolbar (between Dashboard and Guarantees)
- Accessible from any page with single click

### 4. Type-Safe Implementation
- New TypeScript interfaces: `ExpiryCalendar`, `ExpiryCalendarDay`, `ExpiryGuarantee`
- Service method: `GuaranteeService.getExpiryCalendar(month: string)`
- No `any` types; full TypeScript strict mode compliance

### 5. Testing
- 13 unit tests in `calendar.component.spec.ts`
- Tests cover: creation, data loading, navigation, color mapping, grid structure, badges, error handling

---

## How to Test

### Quick Start (Local)

```bash
# 1. Start the entire system
cd /path/to/CasoUsoNova
docker compose up --build

# Wait ~30s for backend health check

# 2. Open browser
http://localhost:4200

# 3. Navigate to Calendar
Click "Calendar" button in toolbar

# 4. Test flows
- Navigate months with < and > buttons
- Click on colored days to see details
- Hover over days to see tooltips
- Watch guarantees list for expiry badges
- Wait ~30s for scheduler to run and trigger SSE updates
```

### Full Test Plan

See **`TEST_PLAN_NOV31.md`** for comprehensive test cases covering:
1. Calendar rendering and grid structure
2. Risk level color mapping
3. Tooltips and data display
4. Side panel details
5. Month navigation
6. Expiry badges
7. Live SSE updates
8. Error handling and edge cases
9. TypeScript strict mode compliance
10. Unit test execution

### Key Test Scenarios

**Basic Flow**:
1. ✅ Open calendar → see current month with colored days
2. ✅ Click colored day → side panel shows guarantees
3. ✅ Click guarantee reference → navigates to detail view
4. ✅ Close panel → side panel closes

**Badge Testing**:
1. ✅ Go to /guarantees → see "Expiry Status" column
2. ✅ Verify badge color matches risk level
3. ✅ Verify text shows correct days remaining

**Live Updates** (SSE Integration):
1. ✅ Keep calendar + list open
2. ✅ Wait ~30s for backend scheduler to run
3. ✅ When guarantee expires (ISSUED → EXPIRED):
   - Calendar should auto-reload
   - List badges should update
   - No page refresh needed

**Error Handling**:
1. ✅ Simulate backend unavailable (stop docker) → mock data appears
2. ✅ Test with no expirations → empty calendar with helpful message
3. ✅ Test responsive design → resize to mobile width

---

## Dependencies

### Backend (Already Implemented ✅)

- **Endpoint**: `GET /api/v1/guarantees/expiry-calendar?month=YYYY-MM`
  - Returns: `ExpiryCalendarDTO` with days grouped by expiration date
  - DTOs: ExpiryCalendarDTO, ExpiryCalendarDayDTO, ExpiryCalendarGuaranteeDTO
  - Location: `guarantees-service/src/main/.../dto/ExpiryCalendar*.java`
  - Service: `ExpiryCalendarService.java`
  - Controller: `GuaranteeController.getExpiryCalendar()`

- **SSE Channel**: `guarantee-events` (already operational)
  - Event type: `expiration-auto`
  - Payload includes: guaranteeId, reference, status, expiryDate, expiredAt

- **Scheduler**: `@Scheduled(fixedRate = 30000)` in demo profile
  - Runs every 30 seconds
  - Transitions ISSUED/AMENDED → EXPIRED if expiryDate <= today
  - Emits SSE event on transition

### Frontend (Newly Implemented ✅)

All frontend code complete and committed:
- CalendarComponent with template and styles
- ExpiryGuarantee models
- GuaranteeService.getExpiryCalendar() method
- Guarantee list expiry badges
- Navigation button
- Unit tests

---

## Acceptance Criteria Checklist

**Visual Requirements**:
- [ ] Calendar displays 7x6 grid (7 columns for weekdays, 6 rows for weeks)
- [ ] Day cells show: day number + expiration count + color by riskLevel
- [ ] Risk legend shows all 5 levels (none, low, medium, high, critical)
- [ ] Side panel displays on click, closes on second click or X button
- [ ] Month navigation works (< and > buttons)

**Data Requirements**:
- [ ] Calendar day shows correct number of expirations
- [ ] Tooltips display total amount and per-currency breakdown
- [ ] Side panel lists all guarantees for selected day
- [ ] Each guarantee shows: reference, beneficiary, amount, days remaining, risk badge
- [ ] Guarantee list has "Expiry Status" column with colored badges
- [ ] Badge text matches: "Expired", "Expires Today", "Expires Tomorrow", "Expires in N days"

**Live Update Requirements**:
- [ ] Calendar auto-reloads when guarantee expires (SSE event received)
- [ ] List badges update when guarantee expires
- [ ] No page refresh required for updates

**Code Quality**:
- [ ] No TypeScript errors: `ng build --configuration production` succeeds
- [ ] All 13 unit tests pass: `ng test --include="**/calendar.component.spec.ts"`
- [ ] No console errors when navigating and using calendar

**Error Handling**:
- [ ] Backend unavailable → mock data appears, calendar still usable
- [ ] No expirations in month → empty message shown
- [ ] Network disconnection → graceful error, no console warnings

**Responsive Design**:
- [ ] Mobile (320px) → calendar responsive, side panel stacks below
- [ ] Tablet (768px) → side-by-side layout readable
- [ ] Desktop (1024px+) → optimal two-column layout

---

## Known Issues / Limitations

**None identified**. Implementation is production-ready.

**Optional Future Enhancements** (not in scope for this PR):
- Bulk claim all guarantees expiring on a specific day
- Export calendar as PDF
- Advanced filtering (by type, amount, applicant)
- Custom risk level thresholds per user

---

## Rollback Plan

If issues discovered during QA:

1. **Minor UI/UX issues**: Cherry-pick fixes in new PR
2. **Critical blocker**: `git revert 8100609` (reverts calendar component)
3. **Backend incompatibility**: Coordinate with backend team for API revision

**Git Status**: All changes on `main` branch (merged commit 8100609)

---

## Next Steps

### QA Team
1. **Execute Test Plan**: Follow TEST_PLAN_NOV31.md for all 10 sections
2. **Report Findings**: Document any bugs/issues with:
   - Steps to reproduce
   - Expected vs. actual behavior
   - Screenshots if applicable
3. **Sign-off**: Confirm all acceptance criteria met

### Backend Team
- Monitor SSE channel and scheduler during QA
- Verify endpoint returns correct data for test months
- Check logs for any errors during expiration events

### Architect / Release Manager
- Review QA findings and approve for production deployment
- Update release notes with new calendar feature
- Coordinate frontend/backend version alignment

---

## Contact & Support

**Frontend Implementation**: Frontend Generator (Claude Code)
- Component: CalendarComponent
- Service method: GuaranteeService.getExpiryCalendar()
- Tests: calendar.component.spec.ts

**Backend Counterpart**: Backend Service Team
- Endpoint: GET /api/v1/guarantees/expiry-calendar
- SSE: guarantee-events channel, expiration-auto event
- Scheduler: @Scheduled job in demo profile

---

## Documentation References

- **Contract**: NOV-28-CONTRACTS.md (sections 5-6)
- **Implementation Summary**: NOV31_COMPLETION_SUMMARY.md
- **Test Plan**: TEST_PLAN_NOV31.md
- **Code**: guarantees-ui/src/app/features/calendar/
- **Models**: guarantees-ui/src/app/models/guarantee.model.ts
- **Service**: guarantees-ui/src/app/services/guarantee.service.ts

---

## Sign-off

- [x] Frontend implementation complete
- [x] Code merged to main (commit 8100609)
- [x] Unit tests written and passing
- [x] TypeScript strict mode compliance verified
- [x] Navigation integrated
- [x] Documentation provided
- [ ] QA testing complete (awaiting QA team)
- [ ] Production deployment (awaiting approval)

**Ready for QA**: ✅ 2026-07-19

---

**Test Plan Execution**: Start with TEST_PLAN_NOV31.md section 1 (Calendar Component Rendering)
