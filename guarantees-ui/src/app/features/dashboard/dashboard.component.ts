import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Subject, takeUntil } from 'rxjs';
import { GuaranteeEventsService } from '../../services/guarantee-events.service';
import { MetricsService } from '../../services/metrics.service';
import { MetricsDTO } from '../../models/metrics.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent implements OnInit, OnDestroy {
  metrics: MetricsDTO | null = null;
  loading = true;
  error = false;
  private readonly destroy$ = new Subject<void>();

  constructor(
    private metricsService: MetricsService,
    private guaranteeEventsService: GuaranteeEventsService
  ) {}

  ngOnInit(): void {
    this.loadMetrics();
    this.guaranteeEventsService.changes()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.loadMetrics(),
        error: () => undefined
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadMetrics(): void {
    this.metricsService.getMetrics().subscribe({
      next: data => {
        this.metrics = data;
        this.loading = false;
        this.error = false;
      },
      error: () => {
        this.error = true;
        this.loading = false;
      }
    });
  }

  get statusEntries(): { key: string; value: number; color: string }[] {
    if (!this.metrics) return [];
    const colors: Record<string, string> = {
      ISSUED: '#4caf50',
      AMENDED: '#2196f3',
      CLAIMED: '#ff9800',
      EXPIRED: '#9e9e9e',
      DRAFT: '#607d8b',
      CANCELLED: '#f44336'
    };
    return Object.entries(this.metrics.byStatus).map(([key, value]) => ({
      key,
      value: Number(value),
      color: colors[key] ?? '#90caf9'
    }));
  }

  get typeEntries(): { key: string; value: number; color: string }[] {
    if (!this.metrics) return [];
    const colors: Record<string, string> = {
      PERFORMANCE: '#3f51b5',
      ADVANCE_PAYMENT: '#e91e63',
      BID_BOND: '#009688',
      WARRANTY: '#ff5722'
    };
    return Object.entries(this.metrics.byType).map(([key, value]) => ({
      key,
      value: Number(value),
      color: colors[key] ?? '#ce93d8'
    }));
  }

  get maxMonthCount(): number {
    if (!this.metrics?.byMonth?.length) return 1;
    return Math.max(...this.metrics.byMonth.map(m => m.count));
  }

  barHeightPercent(count: number): number {
    return Math.round((count / this.maxMonthCount) * 100);
  }

  donutSegments(entries: { key: string; value: number; color: string }[]): { path: string; color: string; key: string }[] {
    const total = entries.reduce((s, e) => s + e.value, 0);
    if (total === 0) return [];

    const cx = 50;
    const cy = 50;
    const r = 35;
    const innerR = 20;
    const segments: { path: string; color: string; key: string }[] = [];
    let startAngle = -Math.PI / 2;

    for (const entry of entries) {
      const angle = (entry.value / total) * 2 * Math.PI;
      const endAngle = startAngle + angle;

      const x1o = cx + r * Math.cos(startAngle);
      const y1o = cy + r * Math.sin(startAngle);
      const x2o = cx + r * Math.cos(endAngle);
      const y2o = cy + r * Math.sin(endAngle);
      const x1i = cx + innerR * Math.cos(endAngle);
      const y1i = cy + innerR * Math.sin(endAngle);
      const x2i = cx + innerR * Math.cos(startAngle);
      const y2i = cy + innerR * Math.sin(startAngle);
      const large = angle > Math.PI ? 1 : 0;

      const path = `M ${x1o} ${y1o} A ${r} ${r} 0 ${large} 1 ${x2o} ${y2o} L ${x1i} ${y1i} A ${innerR} ${innerR} 0 ${large} 0 ${x2i} ${y2i} Z`;
      segments.push({ path, color: entry.color, key: entry.key });
      startAngle = endAngle;
    }
    return segments;
  }

  get statusSegments() { return this.donutSegments(this.statusEntries); }
  get typeSegments() { return this.donutSegments(this.typeEntries); }

  formatStatusLabel(key: string): string {
    return key.charAt(0) + key.slice(1).toLowerCase().replace('_', ' ');
  }
}
