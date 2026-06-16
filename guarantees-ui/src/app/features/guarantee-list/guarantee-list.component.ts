import { Component, OnInit } from '@angular/core';
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
import { FormsModule } from '@angular/forms';
import { GuaranteeService } from '../../services/guarantee.service';
import { Guarantee } from '../../models/guarantee.model';

@Component({
  selector: 'app-guarantee-list',
  standalone: true,
  imports: [
    CommonModule, RouterLink, FormsModule,
    MatTableModule, MatButtonModule, MatIconModule,
    MatSelectModule, MatFormFieldModule, MatChipsModule,
    MatCardModule, MatProgressSpinnerModule
  ],
  templateUrl: './guarantee-list.component.html',
  styleUrl: './guarantee-list.component.scss'
})
export class GuaranteeListComponent implements OnInit {
  displayedColumns = ['reference', 'type', 'amount', 'issueDate', 'expiryDate', 'applicant', 'beneficiary', 'status', 'actions'];
  guarantees: Guarantee[] = [];
  loading = false;

  selectedStatus = '';
  selectedType = '';

  readonly statuses = ['', 'DRAFT', 'ISSUED', 'AMENDED', 'CLAIMED', 'EXPIRED', 'CANCELLED'];
  readonly types = ['', 'PERFORMANCE', 'ADVANCE_PAYMENT', 'BID_BOND', 'WARRANTY'];

  constructor(private svc: GuaranteeService, private router: Router) {}

  ngOnInit(): void {
    this.load();
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
}
