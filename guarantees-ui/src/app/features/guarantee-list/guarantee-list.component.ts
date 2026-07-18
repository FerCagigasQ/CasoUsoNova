import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatChipsModule } from '@angular/material/chips';
import { MatCardModule } from '@angular/material/card';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatMenuModule } from '@angular/material/menu';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatBadgeModule } from '@angular/material/badge';
import { FormsModule } from '@angular/forms';
import { Subscription } from 'rxjs';
import { GuaranteeService } from '../../services/guarantee.service';
import { ExportService } from '../../services/export.service';
import { GuaranteeEventsService } from '../../services/guarantee-events.service';
import { Guarantee } from '../../models/guarantee.model';

@Component({
  selector: 'app-guarantee-list',
  standalone: true,
  imports: [
    CommonModule, RouterLink, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule,
    MatSelectModule, MatFormFieldModule, MatChipsModule,
    MatCardModule, MatProgressSpinnerModule,
    MatMenuModule, MatSnackBarModule, MatBadgeModule
  ],
  templateUrl: './guarantee-list.component.html',
  styleUrl: './guarantee-list.component.scss'
})
export class GuaranteeListComponent implements OnInit, OnDestroy {
  displayedColumns = ['reference', 'type', 'amount', 'issueDate', 'expiryDate', 'applicant', 'beneficiary', 'status', 'actions'];
  guarantees: Guarantee[] = [];
  loading = false;

  selectedStatus = '';
  selectedType = '';

  exportingExcel = false;
  excelJobId: string | null = null;

  private eventSubscription: Subscription | null = null;

  readonly statuses = ['', 'DRAFT', 'ISSUED', 'AMENDED', 'CLAIMED', 'EXPIRED', 'CANCELLED'];
  readonly types = ['', 'PERFORMANCE', 'ADVANCE_PAYMENT', 'BID_BOND', 'WARRANTY'];

  constructor(
    private svc: GuaranteeService,
    private router: Router,
    private exportService: ExportService,
    private snackBar: MatSnackBar,
    private eventsService: GuaranteeEventsService
  ) {}

  ngOnInit(): void {
    this.load();

    this.eventSubscription = this.eventsService.exportReady().subscribe({
      next: event => {
        this.exportingExcel = false;
        this.excelJobId = null;
        this.snackBar.open('Excel ready! Downloading...', 'OK', { duration: 4000 });
        this.triggerDownload(event.downloadUrl);
      },
      error: () => {
        // SSE connection errors are non-fatal; exportingExcel state is preserved
      }
    });
  }

  ngOnDestroy(): void {
    this.eventSubscription?.unsubscribe();
  }

  load(): void {
    this.loading = true;
    this.svc.getAll(this.selectedStatus || undefined, this.selectedType || undefined)
      .subscribe({
        next: data => { this.guarantees = data; this.loading = false; },
        error: () => { this.loading = false; }
      });
  }

  onFilterChange(): void {
    this.load();
  }

  delete(id: number): void {
    if (confirm('Delete this guarantee?')) {
      this.svc.delete(id).subscribe(() => this.load());
    }
  }

  statusColor(status: string): string {
    const map: Record<string, string> = {
      DRAFT: 'draft', ISSUED: 'issued', AMENDED: 'amended',
      CLAIMED: 'claimed', EXPIRED: 'expired', CANCELLED: 'cancelled'
    };
    return map[status] || '';
  }

  exportCSV(): void {
    const BOM = '﻿';
    const headers = [
      'Reference', 'Type', 'Status', 'Amount', 'Currency',
      'Issue Date', 'Expiry Date', 'Applicant', 'Beneficiary', 'Issuing Bank'
    ];

    const rows = this.guarantees.map(g => [
      g.reference,
      g.type,
      g.status,
      String(g.amount),
      g.currency,
      g.issueDate,
      g.expiryDate,
      `${g.applicant.firstName} ${g.applicant.lastName}`,
      `${g.beneficiary.firstName} ${g.beneficiary.lastName}`,
      g.issuingBank.name
    ].map(cell => `"${cell.replace(/"/g, '""')}"`).join(','));

    const csvContent = BOM + [headers.join(','), ...rows].join('\r\n');
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const today = new Date().toISOString().slice(0, 10);
    this.downloadBlob(blob, `guarantees-${today}.csv`);
  }

  exportExcel(): void {
    if (this.exportingExcel) {
      return;
    }

    const filters = {
      status: this.selectedStatus || undefined,
      type: this.selectedType || undefined
    };

    this.exportService.startExcelExport(filters).subscribe({
      next: data => {
        this.exportingExcel = true;
        this.excelJobId = data.jobId;
      },
      error: () => {
        this.snackBar.open('Error starting Excel export. Please try again.', 'Close', { duration: 5000 });
      }
    });
  }

  private triggerDownload(url: string): void {
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = '';
    document.body.appendChild(anchor);
    anchor.click();
    document.body.removeChild(anchor);
  }

  private downloadBlob(blob: Blob, filename: string): void {
    const url = URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = filename;
    document.body.appendChild(anchor);
    anchor.click();
    document.body.removeChild(anchor);
    URL.revokeObjectURL(url);
  }
}
