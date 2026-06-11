import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { GuaranteeService } from '../../services/guarantee.service';
import { Guarantee } from '../../models/guarantee.model';
import { AmendmentDialogComponent } from './amendment-dialog/amendment-dialog.component';
import { ClaimDialogComponent } from './claim-dialog/claim-dialog.component';

@Component({
  selector: 'app-guarantee-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatTableModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatDialogModule
  ],
  template: `
    <div class="detail-container">
      <div class="header">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h1>Guarantee Details</h1>
      </div>

      <div *ngIf="loading" class="loading">
        <mat-spinner></mat-spinner>
      </div>

      <div *ngIf="!loading && guarantee" class="guarantee-detail">
        <mat-card class="detail-card">
          <mat-card-content>
            <div class="detail-grid">
              <div class="detail-field">
                <label>Number</label>
                <span>{{ guarantee.number }}</span>
              </div>
              <div class="detail-field">
                <label>Status</label>
                <mat-chip [color]="getStatusColor(guarantee.status)" selected>
                  {{ guarantee.status }}
                </mat-chip>
              </div>
              <div class="detail-field">
                <label>Amount</label>
                <span>{{ guarantee.amount | currency }}</span>
              </div>
              <div class="detail-field">
                <label>Description</label>
                <span>{{ guarantee.description }}</span>
              </div>
              <div class="detail-field">
                <label>Beneficiary</label>
                <span>{{ guarantee.beneficiary }}</span>
              </div>
              <div class="detail-field">
                <label>Start Date</label>
                <span>{{ guarantee.startDate | date:'short' }}</span>
              </div>
              <div class="detail-field">
                <label>End Date</label>
                <span>{{ guarantee.endDate | date:'short' }}</span>
              </div>
            </div>
          </mat-card-content>
        </mat-card>

        <div class="actions">
          <button mat-raised-button color="accent" (click)="openAmendmentDialog()">
            <mat-icon>edit</mat-icon> Amendment
          </button>
          <button mat-raised-button color="warn" (click)="openClaimDialog()">
            <mat-icon>report</mat-icon> Claim
          </button>
        </div>

        <mat-tab-group>
          <mat-tab label="Amendments">
            <div class="tab-content">
              <table mat-table [dataSource]="guarantee.amendments" class="amendments-table">
                <ng-container matColumnDef="date">
                  <th mat-header-cell *matHeaderCellDef>Date</th>
                  <td mat-cell *matCellDef="let element">{{ element.date | date:'short' }}</td>
                </ng-container>
                <ng-container matColumnDef="description">
                  <th mat-header-cell *matHeaderCellDef>Description</th>
                  <td mat-cell *matCellDef="let element">{{ element.description }}</td>
                </ng-container>
                <ng-container matColumnDef="type">
                  <th mat-header-cell *matHeaderCellDef>Type</th>
                  <td mat-cell *matCellDef="let element">{{ element.type }}</td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="['date', 'description', 'type']"></tr>
                <tr mat-row *matRowDef="let row; columns: ['date', 'description', 'type'];"></tr>
              </table>
              <p *ngIf="!guarantee.amendments || guarantee.amendments.length === 0" class="empty-message">
                No amendments yet
              </p>
            </div>
          </mat-tab>

          <mat-tab label="Claims">
            <div class="tab-content">
              <table mat-table [dataSource]="guarantee.claims" class="claims-table">
                <ng-container matColumnDef="date">
                  <th mat-header-cell *matHeaderCellDef>Date</th>
                  <td mat-cell *matCellDef="let element">{{ element.date | date:'short' }}</td>
                </ng-container>
                <ng-container matColumnDef="amount">
                  <th mat-header-cell *matHeaderCellDef>Amount</th>
                  <td mat-cell *matCellDef="let element">{{ element.amount | currency }}</td>
                </ng-container>
                <ng-container matColumnDef="status">
                  <th mat-header-cell *matHeaderCellDef>Status</th>
                  <td mat-cell *matCellDef="let element">
                    <mat-chip [color]="getClaimStatusColor(element.status)" selected>
                      {{ element.status }}
                    </mat-chip>
                  </td>
                </ng-container>
                <ng-container matColumnDef="description">
                  <th mat-header-cell *matHeaderCellDef>Description</th>
                  <td mat-cell *matCellDef="let element">{{ element.description }}</td>
                </ng-container>
                <tr mat-header-row *matHeaderRowDef="['date', 'amount', 'status', 'description']"></tr>
                <tr mat-row *matRowDef="let row; columns: ['date', 'amount', 'status', 'description'];"></tr>
              </table>
              <p *ngIf="!guarantee.claims || guarantee.claims.length === 0" class="empty-message">
                No claims yet
              </p>
            </div>
          </mat-tab>
        </mat-tab-group>
      </div>

      <div *ngIf="!loading && !guarantee" class="not-found">
        <p>Guarantee not found</p>
        <button mat-raised-button [routerLink]="['/guarantees']">
          Back to List
        </button>
      </div>
    </div>
  `,
  styles: [`
    .detail-container {
      padding: 20px;
      max-width: 1000px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      align-items: center;
      gap: 20px;
      margin-bottom: 30px;
    }

    h1 {
      margin: 0;
    }

    .loading {
      display: flex;
      justify-content: center;
      padding: 40px;
    }

    .detail-card {
      background: white;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      margin-bottom: 20px;
    }

    .detail-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 20px;
    }

    .detail-field {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .detail-field label {
      font-weight: bold;
      color: #666;
      font-size: 12px;
      text-transform: uppercase;
    }

    .detail-field span {
      font-size: 16px;
      color: #333;
    }

    .actions {
      display: flex;
      gap: 10px;
      margin-bottom: 30px;
    }

    .tab-content {
      padding: 20px;
    }

    .amendments-table,
    .claims-table {
      width: 100%;
    }

    .empty-message {
      text-align: center;
      color: #999;
      padding: 40px 20px;
    }

    .not-found {
      text-align: center;
      padding: 40px;
    }
  `]
})
export class GuaranteeDetailComponent implements OnInit {
  guarantee: Guarantee | null = null;
  loading = true;
  guaranteeId: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private guaranteeService: GuaranteeService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.guaranteeId = params['id'];
      if (this.guaranteeId) {
        this.loadGuarantee();
      }
    });
  }

  loadGuarantee(): void {
    if (!this.guaranteeId) return;

    this.loading = true;
    this.guaranteeService.getById(this.guaranteeId).subscribe({
      next: (data) => {
        this.guarantee = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading guarantee:', error);
        this.loading = false;
      }
    });
  }

  openAmendmentDialog(): void {
    const dialogRef = this.dialog.open(AmendmentDialogComponent, {
      width: '500px',
      data: { guaranteeId: this.guaranteeId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadGuarantee();
      }
    });
  }

  openClaimDialog(): void {
    const dialogRef = this.dialog.open(ClaimDialogComponent, {
      width: '500px',
      data: { guaranteeId: this.guaranteeId }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.loadGuarantee();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/guarantees']);
  }

  getStatusColor(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'primary';
      case 'EXPIRED':
        return 'warn';
      case 'CANCELLED':
        return 'error';
      default:
        return 'accent';
    }
  }

  getClaimStatusColor(status: string): string {
    switch (status) {
      case 'APPROVED':
        return 'primary';
      case 'REJECTED':
        return 'warn';
      case 'PENDING':
        return 'accent';
      default:
        return 'accent';
    }
  }
}
