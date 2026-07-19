import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CalendarComponent } from './calendar.component';
import { GuaranteeService } from '../../services/guarantee.service';
import { GuaranteeEventsService } from '../../services/guarantee-events.service';
import { MatSnackBarModule, MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { ExpiryCalendar } from '../../models/guarantee.model';

describe('CalendarComponent', () => {
  let component: CalendarComponent;
  let fixture: ComponentFixture<CalendarComponent>;
  let guaranteeService: jasmine.SpyObj<GuaranteeService>;
  let eventsService: jasmine.SpyObj<GuaranteeEventsService>;

  const mockCalendar: ExpiryCalendar = {
    month: '2026-07',
    days: [
      {
        day: 5,
        guarantees: [
          {
            id: '1',
            reference: 'REF-001',
            beneficiary: { firstName: 'John', lastName: 'Doe' },
            amount: 50000,
            currency: 'EUR',
            expiryDate: '2026-07-05',
            daysUntilExpiry: 5,
            riskLevel: 'medium'
          }
        ],
        totalByAmount: 50000,
        totalByCurrency: { EUR: 50000 },
        aggregateRiskLevel: 'medium'
      }
    ],
    riskCatalog: {
      'none': 'No expirations',
      'low': '≤7 days AND amount < 50k',
      'medium': '8-30 days OR amount 50k-200k',
      'high': '31-60 days OR amount > 200k',
      'critical': '>60 days OR very high amount'
    }
  };

  beforeEach(async () => {
    const guaranteeServiceSpy = jasmine.createSpyObj('GuaranteeService', ['getExpiryCalendar']);
    const eventsServiceSpy = jasmine.createSpyObj('GuaranteeEventsService', ['guaranteeEvents']);

    await TestBed.configureTestingModule({
      imports: [CalendarComponent, MatSnackBarModule],
      providers: [
        { provide: GuaranteeService, useValue: guaranteeServiceSpy },
        { provide: GuaranteeEventsService, useValue: eventsServiceSpy }
      ]
    }).compileComponents();

    guaranteeService = TestBed.inject(GuaranteeService) as jasmine.SpyObj<GuaranteeService>;
    eventsService = TestBed.inject(GuaranteeEventsService) as jasmine.SpyObj<GuaranteeEventsService>;

    guaranteeService.getExpiryCalendar.and.returnValue(of(mockCalendar));
    eventsService.guaranteeEvents.and.returnValue(of());

    fixture = TestBed.createComponent(CalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load calendar on init', () => {
    expect(guaranteeService.getExpiryCalendar).toHaveBeenCalled();
    expect(component.calendar).toEqual(mockCalendar);
  });

  it('should navigate to previous month', () => {
    const initialMonth = new Date(component.currentMonth);
    component.previousMonth();
    expect(component.currentMonth.getMonth()).toBe(initialMonth.getMonth() - 1);
  });

  it('should navigate to next month', () => {
    const initialMonth = new Date(component.currentMonth);
    component.nextMonth();
    expect(component.currentMonth.getMonth()).toBe(initialMonth.getMonth() + 1);
  });

  it('should get correct days in current month', () => {
    const days = component.getDaysInMonth();
    expect(days).toBeGreaterThan(0);
    expect(days).toBeLessThanOrEqual(31);
  });

  it('should get correct first day of month', () => {
    const firstDay = component.getFirstDayOfMonth();
    expect(firstDay).toBeGreaterThanOrEqual(0);
    expect(firstDay).toBeLessThan(7);
  });

  it('should get day data from calendar', () => {
    const dayData = component.getDayData(5);
    expect(dayData).toEqual(mockCalendar.days[0]);
  });

  it('should return null for non-existent day', () => {
    const dayData = component.getDayData(99);
    expect(dayData).toBeNull();
  });

  it('should return correct risk color for each level', () => {
    expect(component.getRiskColor('none')).toBe('#4caf50');
    expect(component.getRiskColor('low')).toBe('#8bc34a');
    expect(component.getRiskColor('medium')).toBe('#ffc107');
    expect(component.getRiskColor('high')).toBe('#ff9800');
    expect(component.getRiskColor('critical')).toBe('#f44336');
  });

  it('should select/deselect day on click', () => {
    const day = mockCalendar.days[0];
    component.selectDay(day);
    expect(component.selectedDay).toEqual(day);
    component.selectDay(day);
    expect(component.selectedDay).toBeNull();
  });

  it('should calculate days until expiry correctly', () => {
    const guarantee = mockCalendar.days[0].guarantees[0];
    expect(component.daysUntilExpiry(guarantee)).toContain('days');
  });

  it('should provide expiry badge text', () => {
    const expiredGuarantee = { ...mockCalendar.days[0].guarantees[0], daysUntilExpiry: -1 };
    expect(component.daysUntilExpiry(expiredGuarantee)).toBe('Expired');

    const todayGuarantee = { ...mockCalendar.days[0].guarantees[0], daysUntilExpiry: 0 };
    expect(component.daysUntilExpiry(todayGuarantee)).toBe('Today');

    const tomorrowGuarantee = { ...mockCalendar.days[0].guarantees[0], daysUntilExpiry: 1 };
    expect(component.daysUntilExpiry(tomorrowGuarantee)).toBe('Tomorrow');
  });

  it('should provide mock calendar on error', () => {
    guaranteeService.getExpiryCalendar.and.returnValue(
      new Promise((resolve, reject) => reject('Error'))
    );
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.error).toBeDefined();
  });

  it('should have correct calendar grid structure', () => {
    const days = component.getCalendarDays();
    expect(days.length).toBeLessThanOrEqual(42); // 6 weeks * 7 days
    expect(days.length).toBeGreaterThanOrEqual(35); // 5 weeks * 7 days
  });

  it('should show snackbar notification on expiration-auto event', () => {
    const snackBar = TestBed.inject(MatSnackBar);
    spyOn(snackBar, 'open');

    const expirationEvent = {
      type: 'expiration-auto',
      eventType: 'expiration-auto',
      guaranteeId: '1',
      reference: 'REF-001',
      status: 'EXPIRED',
      expiryDate: '2026-07-01',
      reason: 'Auto-expired by scheduler',
      expiredAt: '2026-07-19T12:00:00Z'
    };

    eventsService.guaranteeEvents.and.returnValue(of(expirationEvent));
    component.ngOnInit();

    expect(snackBar.open).toHaveBeenCalledWith('Guarantee REF-001 has expired', 'OK', { duration: 5000 });
  });

  it('should reload calendar data on expiration-auto event', () => {
    const expirationEvent = {
      type: 'expiration-auto',
      eventType: 'expiration-auto',
      guaranteeId: '1',
      reference: 'REF-001',
      status: 'EXPIRED',
      expiryDate: '2026-07-01',
      reason: 'Auto-expired by scheduler',
      expiredAt: '2026-07-19T12:00:00Z'
    };

    guaranteeService.getExpiryCalendar.and.returnValue(of(mockCalendar));
    eventsService.guaranteeEvents.and.returnValue(of(expirationEvent));

    const initialCallCount = guaranteeService.getExpiryCalendar.calls.count();
    component.ngOnInit();

    expect(guaranteeService.getExpiryCalendar.calls.count()).toBeGreaterThan(initialCallCount);
  });
});
