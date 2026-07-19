import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatListModule } from '@angular/material/list';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { RouterLink } from '@angular/router';
import { GuaranteeService } from '../../services/guarantee.service';
import { GuaranteeEventsService } from '../../services/guarantee-events.service';
import { ExpiryCalendar, ExpiryCalendarDay, ExpiryGuarantee } from '../../models/guarantee.model';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [
    CommonModule, RouterLink,
    MatCardModule, MatButtonModule, MatIconModule,
    MatTooltipModule, MatProgressSpinnerModule,
    MatListModule, MatBadgeModule, MatDividerModule, MatSnackBarModule
  ],
  templateUrl: './calendar.component.html',
  styleUrl: './calendar.component.scss'
})
export class CalendarComponent implements OnInit, OnDestroy {
  calendar: ExpiryCalendar | null = null;
  loading = false;
  error: string | null = null;

  currentMonth: Date = new Date();
  weekDays = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  selectedDay: ExpiryCalendarDay | null = null;

  private eventSubscription: Subscription | null = null;
  private expirationSubscription: Subscription | null = null;

  constructor(
    private guaranteeService: GuaranteeService,
    private eventsService: GuaranteeEventsService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.load();
    this.subscribeToExpirationEvents();
  }

  ngOnDestroy(): void {
    this.eventSubscription?.unsubscribe();
    this.expirationSubscription?.unsubscribe();
  }

  load(): void {
    this.loading = true;
    this.error = null;

    const monthStr = this.formatMonth(this.currentMonth);
    this.guaranteeService.getExpiryCalendar(monthStr).subscribe({
      next: data => {
        this.calendar = data;
        this.loading = false;
      },
      error: err => {
        console.error('Error loading expiry calendar:', err);
        this.error = 'Failed to load calendar. Using mock data.';
        this.calendar = this.getMockCalendar();
        this.loading = false;
      }
    });
  }

  private subscribeToExpirationEvents(): void {
    this.expirationSubscription = this.eventsService.guaranteeEvents().subscribe({
      next: event => {
        if (event.type === 'expiration-auto') {
          this.snackBar.open(`Guarantee ${event.reference} has expired`, 'OK', { duration: 5000 });
          this.load();
        }
      },
      error: () => {
        console.warn('SSE connection error for expiration events');
      }
    });
  }

  previousMonth(): void {
    this.currentMonth = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() - 1, 1);
    this.selectedDay = null;
    this.load();
  }

  nextMonth(): void {
    this.currentMonth = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() + 1, 1);
    this.selectedDay = null;
    this.load();
  }

  private formatMonth(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    return `${year}-${month}`;
  }

  getDaysInMonth(): number {
    return new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() + 1, 0).getDate();
  }

  getFirstDayOfMonth(): number {
    return new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth(), 1).getDay();
  }

  getCalendarDays(): (number | null)[] {
    const days: (number | null)[] = [];
    const firstDay = this.getFirstDayOfMonth();
    const daysInMonth = this.getDaysInMonth();

    for (let i = 0; i < firstDay; i++) {
      days.push(null);
    }
    for (let i = 1; i <= daysInMonth; i++) {
      days.push(i);
    }
    return days;
  }

  getDayData(dayNum: number | null): ExpiryCalendarDay | null {
    if (!dayNum || !this.calendar) return null;
    return this.calendar.days.find(d => d.day === dayNum) || null;
  }

  getRiskColor(riskLevel: string): string {
    const map: Record<string, string> = {
      'none': '#4caf50',
      'low': '#8bc34a',
      'medium': '#ffc107',
      'high': '#ff9800',
      'critical': '#f44336'
    };
    return map[riskLevel] || '#e0e0e0';
  }

  getRiskLevelLabel(level: string): string {
    const labels: Record<string, string> = {
      'none': 'None',
      'low': 'Low',
      'medium': 'Medium',
      'high': 'High',
      'critical': 'Critical'
    };
    return labels[level] || level;
  }

  selectDay(day: ExpiryCalendarDay): void {
    this.selectedDay = this.selectedDay?.day === day.day ? null : day;
  }

  onGuaranteeClick(id: string): void {
    const numId = parseInt(id, 10);
    window.location.href = `/guarantees/${numId}`;
  }

  daysUntilExpiry(guarantee: ExpiryGuarantee): string {
    const days = guarantee.daysUntilExpiry;
    if (days < 0) return 'Expired';
    if (days === 0) return 'Today';
    if (days === 1) return 'Tomorrow';
    return `${days} days`;
  }

  private getMockCalendar(): ExpiryCalendar {
    const month = this.formatMonth(this.currentMonth);
    return {
      month,
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
        },
        {
          day: 15,
          guarantees: [
            {
              id: '2',
              reference: 'REF-002',
              beneficiary: { firstName: 'Jane', lastName: 'Smith' },
              amount: 150000,
              currency: 'USD',
              expiryDate: '2026-07-15',
              daysUntilExpiry: 15,
              riskLevel: 'high'
            }
          ],
          totalByAmount: 150000,
          totalByCurrency: { USD: 150000 },
          aggregateRiskLevel: 'high'
        },
        {
          day: 25,
          guarantees: [
            {
              id: '3',
              reference: 'REF-003',
              beneficiary: { firstName: 'Bob', lastName: 'Johnson' },
              amount: 300000,
              currency: 'EUR',
              expiryDate: '2026-07-25',
              daysUntilExpiry: 25,
              riskLevel: 'critical'
            }
          ],
          totalByAmount: 300000,
          totalByCurrency: { EUR: 300000 },
          aggregateRiskLevel: 'critical'
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
  }
}
