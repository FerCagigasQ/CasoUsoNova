import { ComponentFixture, TestBed } from '@angular/core/testing';
import { GuaranteeListComponent } from './guarantee-list.component';
import { GuaranteeService } from '../../services/guarantee.service';
import { ExportService } from '../../services/export.service';
import { GuaranteeEventsService } from '../../services/guarantee-events.service';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { Guarantee } from '../../models/guarantee.model';

describe('GuaranteeListComponent', () => {
  let component: GuaranteeListComponent;
  let fixture: ComponentFixture<GuaranteeListComponent>;
  let guaranteeService: jasmine.SpyObj<GuaranteeService>;
  let exportService: jasmine.SpyObj<ExportService>;
  let eventsService: jasmine.SpyObj<GuaranteeEventsService>;

  const mockGuarantees: Guarantee[] = [
    {
      id: 1,
      reference: 'REF-001',
      type: 'PERFORMANCE',
      amount: 50000,
      currency: 'EUR',
      issueDate: '2026-01-01',
      expiryDate: '2026-07-15',
      status: 'ISSUED',
      applicant: {
        id: 1,
        firstName: 'John',
        lastName: 'Doe',
        taxId: '12345678A',
        email: 'john@example.com',
        phone: '123456789',
        address: 'Street 1',
        country: 'Spain'
      },
      beneficiary: {
        id: 2,
        firstName: 'Jane',
        lastName: 'Smith',
        taxId: '87654321B',
        email: 'jane@example.com',
        phone: '987654321',
        address: 'Street 2',
        country: 'Spain'
      },
      issuingBank: {
        id: 1,
        name: 'Bank A',
        bic: 'BICA123',
        country: 'Spain'
      },
      amendments: [],
      claims: []
    }
  ];

  beforeEach(async () => {
    const guaranteeServiceSpy = jasmine.createSpyObj('GuaranteeService', ['getAll', 'delete']);
    const exportServiceSpy = jasmine.createSpyObj('ExportService', ['startExcelExport']);
    const eventsServiceSpy = jasmine.createSpyObj('GuaranteeEventsService', ['exportReady', 'guaranteeEvents']);

    await TestBed.configureTestingModule({
      imports: [GuaranteeListComponent, MatSnackBarModule],
      providers: [
        { provide: GuaranteeService, useValue: guaranteeServiceSpy },
        { provide: ExportService, useValue: exportServiceSpy },
        { provide: GuaranteeEventsService, useValue: eventsServiceSpy }
      ]
    }).compileComponents();

    guaranteeService = TestBed.inject(GuaranteeService) as jasmine.SpyObj<GuaranteeService>;
    exportService = TestBed.inject(ExportService) as jasmine.SpyObj<ExportService>;
    eventsService = TestBed.inject(GuaranteeEventsService) as jasmine.SpyObj<GuaranteeEventsService>;

    guaranteeService.getAll.and.returnValue(of(mockGuarantees));
    exportService.startExcelExport.and.returnValue(of({ jobId: 'job-123' }));
    eventsService.exportReady.and.returnValue(of());
    eventsService.guaranteeEvents.and.returnValue(of());

    fixture = TestBed.createComponent(GuaranteeListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load guarantees on init', () => {
    expect(guaranteeService.getAll).toHaveBeenCalled();
    expect(component.guarantees).toEqual(mockGuarantees);
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

  it('should reload guarantees on expiration-auto event', () => {
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

    guaranteeService.getAll.and.returnValue(of(mockGuarantees));
    eventsService.guaranteeEvents.and.returnValue(of(expirationEvent));

    const initialCallCount = guaranteeService.getAll.calls.count();
    component.ngOnInit();

    expect(guaranteeService.getAll.calls.count()).toBeGreaterThan(initialCallCount);
  });

  it('should unsubscribe on destroy', () => {
    component.ngOnDestroy();
    // Component should clean up subscriptions without errors
    expect(component).toBeTruthy();
  });

  it('should calculate days until expiry correctly', () => {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    const expiryDate = new Date(today);
    expiryDate.setDate(expiryDate.getDate() + 5);

    const days = component.getDaysUntilExpiry(expiryDate.toISOString().split('T')[0]);
    expect(days).toBeGreaterThanOrEqual(4);
    expect(days).toBeLessThanOrEqual(5);
  });

  it('should get correct expiry risk level', () => {
    expect(component.getExpiryRiskLevel(-1, 100000)).toBe('critical');
    expect(component.getExpiryRiskLevel(0, 100000)).toBe('critical');
    expect(component.getExpiryRiskLevel(5, 30000)).toBe('low');
    expect(component.getExpiryRiskLevel(15, 100000)).toBe('medium');
    expect(component.getExpiryRiskLevel(45, 250000)).toBe('high');
    expect(component.getExpiryRiskLevel(100, 30000)).toBe('none');
  });

  it('should get correct expiry badge color', () => {
    expect(component.getExpiryBadgeColor(-1, 100000)).toBe('#f44336');
    expect(component.getExpiryBadgeColor(5, 30000)).toBe('#8bc34a');
    expect(component.getExpiryBadgeColor(15, 100000)).toBe('#ffc107');
    expect(component.getExpiryBadgeColor(45, 250000)).toBe('#ff9800');
    expect(component.getExpiryBadgeColor(100, 30000)).toBe('#4caf50');
  });

  it('should get correct expiry badge text', () => {
    expect(component.getExpiryBadgeText(-1)).toBe('Expired');
    expect(component.getExpiryBadgeText(0)).toBe('Expires Today');
    expect(component.getExpiryBadgeText(1)).toBe('Expires Tomorrow');
    expect(component.getExpiryBadgeText(5)).toBe('Expires in 5 days');
  });

  it('should get correct status color', () => {
    expect(component.statusColor('DRAFT')).toBe('draft');
    expect(component.statusColor('ISSUED')).toBe('issued');
    expect(component.statusColor('AMENDED')).toBe('amended');
    expect(component.statusColor('CLAIMED')).toBe('claimed');
    expect(component.statusColor('EXPIRED')).toBe('expired');
    expect(component.statusColor('CANCELLED')).toBe('cancelled');
  });
});
