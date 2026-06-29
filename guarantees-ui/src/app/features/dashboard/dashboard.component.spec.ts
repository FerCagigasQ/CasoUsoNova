import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { MetricsService } from '../../services/metrics.service';
import { MetricsDTO } from '../../models/metrics.model';
import { of, throwError } from 'rxjs';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let metricsService: jasmine.SpyObj<MetricsService>;

  const mockMetrics: MetricsDTO = {
    total: 6,
    byStatus: {
      ISSUED: 2,
      AMENDED: 1,
      CLAIMED: 1,
      EXPIRED: 1,
      DRAFT: 1
    },
    byType: {
      PERFORMANCE: 2,
      ADVANCE_PAYMENT: 2,
      BID_BOND: 1,
      WARRANTY: 1
    },
    byMonth: [
      { month: '2024-01', count: 1 },
      { month: '2024-02', count: 1 },
      { month: '2024-03', count: 1 },
      { month: '2024-04', count: 1 },
      { month: '2024-05', count: 1 },
      { month: '2024-11', count: 1 }
    ]
  };

  beforeEach(async () => {
    const metricsServiceSpy = jasmine.createSpyObj('MetricsService', ['getMetrics']);

    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        { provide: MetricsService, useValue: metricsServiceSpy }
      ]
    }).compileComponents();

    metricsService = TestBed.inject(MetricsService) as jasmine.SpyObj<MetricsService>;
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
  });

  describe('Loading State', () => {
    it('should display loading spinner initially', () => {
      metricsService.getMetrics.and.returnValue(of(mockMetrics));
      fixture.detectChanges();

      const loadingElement = fixture.nativeElement.querySelector('.loading-state');
      expect(component.loading).toBe(true);
      expect(loadingElement).toBeTruthy();
    });

    it('should show loading message', () => {
      metricsService.getMetrics.and.returnValue(of(mockMetrics));
      const compiled = fixture.nativeElement;

      metricsService.getMetrics.and.returnValue(new Promise(() => {})); // Never resolves
      fixture.detectChanges();

      const loadingMsg = compiled.querySelector('.loading-state p');
      expect(loadingMsg?.textContent).toContain('Loading metrics');
    });
  });

  describe('Data Rendering', () => {
    beforeEach(() => {
      metricsService.getMetrics.and.returnValue(of(mockMetrics));
    });

    it('should load metrics on init', () => {
      component.ngOnInit();
      expect(metricsService.getMetrics).toHaveBeenCalled();
    });

    it('should set metrics data on successful load', (done) => {
      component.ngOnInit();
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        expect(component.metrics).toEqual(mockMetrics);
        expect(component.loading).toBe(false);
        expect(component.error).toBe(false);
        done();
      });
    });

    it('should display KPI total card with correct value', (done) => {
      component.ngOnInit();
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        fixture.detectChanges();
        const totalCard = fixture.nativeElement.querySelector('.kpi-total .kpi-value');
        expect(totalCard?.textContent).toContain('6');
        done();
      });
    });

    it('should display KPI cards for each status', (done) => {
      component.ngOnInit();
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        fixture.detectChanges();
        const statusCards = fixture.nativeElement.querySelectorAll('.kpi-card');
        // 1 total card + 5 status cards = 6
        expect(statusCards.length).toBeGreaterThanOrEqual(5);
        done();
      });
    });

    it('should generate bar chart data for months', (done) => {
      component.ngOnInit();
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        expect(component.metrics?.byMonth.length).toBe(6);
        const barWrappers = fixture.nativeElement.querySelectorAll('.bar-wrapper');
        expect(barWrappers.length).toBe(6);
        done();
      });
    });

    it('should generate donut segments for status', (done) => {
      component.ngOnInit();
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        fixture.detectChanges();
        const statusSegments = component.statusSegments;
        expect(statusSegments.length).toBe(5); // 5 unique statuses
        done();
      });
    });

    it('should generate donut segments for type', (done) => {
      component.ngOnInit();
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        fixture.detectChanges();
        const typeSegments = component.typeSegments;
        expect(typeSegments.length).toBe(4); // 4 unique types
        done();
      });
    });
  });

  describe('Error State', () => {
    it('should display error message when service fails', (done) => {
      metricsService.getMetrics.and.returnValue(throwError(() => new Error('API Error')));
      component.ngOnInit();
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        fixture.detectChanges();
        expect(component.error).toBe(true);
        expect(component.loading).toBe(false);
        const errorElement = fixture.nativeElement.querySelector('.empty-state');
        expect(errorElement).toBeTruthy();
        done();
      });
    });
  });

  describe('Empty State', () => {
    it('should display empty state when no data available', (done) => {
      const emptyMetrics: MetricsDTO = {
        total: 0,
        byStatus: {},
        byType: {},
        byMonth: []
      };
      metricsService.getMetrics.and.returnValue(of(emptyMetrics));
      component.ngOnInit();
      fixture.detectChanges();

      fixture.whenStable().then(() => {
        fixture.detectChanges();
        expect(component.metrics?.total).toBe(0);
        const emptyStateMsg = fixture.nativeElement.querySelector('.empty-state p');
        expect(emptyStateMsg?.textContent).toContain('No data available');
        done();
      });
    });
  });

  describe('Utility Methods', () => {
    beforeEach(() => {
      metricsService.getMetrics.and.returnValue(of(mockMetrics));
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should calculate correct bar height percentage', () => {
      const maxCount = 1;
      const heightPercent = component.barHeightPercent(1);
      expect(heightPercent).toBe(100);
    });

    it('should format status labels correctly', () => {
      expect(component.formatStatusLabel('ISSUED')).toBe('Issued');
      expect(component.formatStatusLabel('ADVANCE_PAYMENT')).toBe('Advance payment');
    });

    it('should generate donut segments with correct paths', () => {
      const segments = component.donutSegments(component.statusEntries);
      expect(segments.length).toBeGreaterThan(0);
      segments.forEach(seg => {
        expect(seg.path).toBeTruthy();
        expect(seg.color).toBeTruthy();
        expect(seg.key).toBeTruthy();
      });
    });

    it('should calculate max month count correctly', () => {
      const maxCount = component.maxMonthCount;
      expect(maxCount).toBe(1); // All months have count=1
    });
  });

  describe('Responsive Design', () => {
    beforeEach(() => {
      metricsService.getMetrics.and.returnValue(of(mockMetrics));
      component.ngOnInit();
      fixture.detectChanges();
    });

    it('should render dashboard container', (done) => {
      fixture.whenStable().then(() => {
        fixture.detectChanges();
        const container = fixture.nativeElement.querySelector('.dashboard-container');
        expect(container).toBeTruthy();
        done();
      });
    });

    it('should have responsive grid for KPI cards', (done) => {
      fixture.whenStable().then(() => {
        fixture.detectChanges();
        const kpiGrid = fixture.nativeElement.querySelector('.kpi-grid');
        expect(kpiGrid).toBeTruthy();
        const computedStyle = window.getComputedStyle(kpiGrid);
        expect(computedStyle.display).toMatch(/grid|flex/); // Should use grid or flex
        done();
      });
    });
  });
});
