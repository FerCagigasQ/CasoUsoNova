import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { GuaranteeService } from '../../services/guarantee.service';
import { Guarantee, GuaranteeStatus, GuaranteeType } from '../../models/guarantee.model';

@Component({
  selector: 'app-guarantee-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatInputModule,
    MatFormFieldModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatToolbarModule,
    MatSelectModule,
    MatTooltipModule
  ],
  template: `
    <mat-toolbar color="primary">
      <h1>Guarantees</h1>
      <button mat-raised-button color="accent" routerLink="/guarantees/new">
        <mat-icon>add</mat-icon> New Guarantee
      </button>
    </mat-toolbar>

    <div class="guarantee-list-container">
      <div class="filters">
        <mat-form-field>
          <mat-label>Status</mat-label>
          <mat-select [(ngModel)]="selectedStatus" (selectionChange)="onFilterChange()">
            <mat-option [value]="">All</mat-option>
            <mat-option value="DRAFT">Draft</mat-option>
            <mat-option value="ISSUED">Issued</mat-option>
            <mat-option value="AMENDED">Amended</mat-option>
            <mat-option value="CLAIMED">Claimed</mat-option>
            <mat-option value="EXPIRED">Expired</mat-option>
            <mat-option value="CANCELLED">Cancelled</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field>
          <mat-label>Type</mat-label>
          <mat-select [(ngModel)]="selectedType" (selectionChange)="onFilterChange()">
            <mat-option [value]="">All</mat-option>
            <mat-option value="PERFORMANCE">Performance</mat-option>
            <mat-option value="ADVANCE_PAYMENT">Advance Payment</mat-option>
            <mat-option value="BID_BOND">Bid Bond</mat-option>
            <mat-option value="WARRANTY">Warranty</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div class="table-container">
        <table mat-table [dataSource]="guarantees" class="guarantee-table">
          <ng-container matColumnDef="reference">
            <th mat-header-cell *matHeaderCellDef>Reference</th>
            <td mat-cell *matCellDef="let element">{{ element.reference }}</td>
          </ng-container>

          <ng-container matColumnDef="type">
            <th mat-header-cell *matHeaderCellDef>Type</th>
            <td mat-cell *matCellDef="let element">{{ element.type }}</td>
          </ng-container>

          <ng-container matColumnDef="amount">
            <th mat-header-cell *matHeaderCellDef>Amount</th>
            <td mat-cell *matCellDef="let element">{{ element.amount | number:'1.2-2' }} {{ element.currency }}</td>
          </ng-container>

          <ng-container matColumnDef="beneficiary">
            <th mat-header-cell *matHeaderCellDef>Beneficiary</th>
            <td mat-cell *matCellDef="let element">{{ element.beneficiary?.firstName }} {{ element.beneficiary?.lastName }}</td>
          </ng-container>

          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let element">
              <span [class]="'status-' + element.status.toLowerCase()">
                {{ element.status }}
              </span>
            </td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let element">
              <button mat-icon-button [routerLink]="['/guarantees', element.id]" matTooltip="View details">
                <mat-icon>visibility</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
      </div>

      <mat-paginator
        [length]="totalItems"
        [pageSize]="pageSize"
        [pageSizeOptions]="pageSizeOptions"
        (page)="onPageChange($event)">
      </mat-paginator>

      <div *ngIf="loading" class="loading">
        <mat-spinner></mat-spinner>
      </div>
    </div>
  `,
  styles: [`
    .guarantee-list-container {
      padding: 20px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;
    }

    .filters {
      margin-bottom: 20px;
    }

    .table-container {
      overflow-x: auto;
      background: white;
      border-radius: 4px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .guarantee-table {
      width: 100%;
    }

    .status-active {
      color: #4caf50;
      font-weight: bold;
    }

    .status-expired {
      color: #ff9800;
      font-weight: bold;
    }

    .status-cancelled {
      color: #f44336;
      font-weight: bold;
    }

    .loading {
      display: flex;
      justify-content: center;
      padding: 40px;
    }
  `]
})
export class GuaranteeListComponent implements OnInit {
  guarantees: Guarantee[] = [];
  displayedColumns = ['reference', 'type', 'amount', 'beneficiary', 'status', 'actions'];
  selectedStatus: GuaranteeStatus | '' = '';
  selectedType: GuaranteeType | '' = '';
  searchTerm = '';
  page = 0;
  pageSize = 10;
  pageSizeOptions = [5, 10, 25, 50];
  totalItems = 0;
  loading = false;

  constructor(private guaranteeService: GuaranteeService) {}

  ngOnInit(): void {
    this.loadGuarantees();
  }

  loadGuarantees(): void {
    this.loading = true;
    const status = this.selectedStatus || undefined;
    const type = this.selectedType || undefined;
    this.guaranteeService.getAll(status, type).subscribe({
      next: (data) => {
        this.guarantees = data;
        this.totalItems = data.length;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading guarantees:', error);
        this.loading = false;
      }
    });
  }

  onFilterChange(): void {
    this.page = 0;
    this.loadGuarantees();
  }

  onPageChange(event: PageEvent): void {
    this.page = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadGuarantees();
  }
}
