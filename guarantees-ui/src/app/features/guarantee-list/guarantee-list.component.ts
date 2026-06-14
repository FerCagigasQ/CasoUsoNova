import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatToolbarModule } from '@angular/material/toolbar';
import { GuaranteeService } from '../../services/guarantee.service';
import { Guarantee, GuaranteeStatus, GuaranteeType } from '../../models/guarantee.model';

@Component({
  selector: 'app-guarantee-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatInputModule,
    MatFormFieldModule,
    MatSelectModule,
    MatProgressSpinnerModule,
    MatChipsModule,
    MatToolbarModule
  ],
  template: `
    <mat-toolbar color="primary">
      <span>Trade Finance — Bank Guarantees (URDG 758)</span>
      <span class="spacer"></span>
      <button mat-raised-button color="accent" [routerLink]="['/guarantees/new']">
        + Nueva Garantía
      </button>
    </mat-toolbar>

    <div class="container">
      <div class="filters">
        <mat-form-field>
          <mat-label>Estado</mat-label>
          <mat-select [(ngModel)]="filterStatus" (selectionChange)="loadGuarantees()">
            <mat-option value="">Todos</mat-option>
            <mat-option value="DRAFT">DRAFT</mat-option>
            <mat-option value="ISSUED">ISSUED</mat-option>
            <mat-option value="AMENDED">AMENDED</mat-option>
            <mat-option value="CLAIMED">CLAIMED</mat-option>
            <mat-option value="EXPIRED">EXPIRED</mat-option>
            <mat-option value="CANCELLED">CANCELLED</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field>
          <mat-label>Tipo</mat-label>
          <mat-select [(ngModel)]="filterType" (selectionChange)="loadGuarantees()">
            <mat-option value="">Todos</mat-option>
            <mat-option value="PERFORMANCE">PERFORMANCE</mat-option>
            <mat-option value="ADVANCE_PAYMENT">ADVANCE PAYMENT</mat-option>
            <mat-option value="BID_BOND">BID BOND</mat-option>
            <mat-option value="WARRANTY">WARRANTY</mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div *ngIf="loading" class="spinner-container">
        <mat-spinner></mat-spinner>
      </div>

      <table mat-table [dataSource]="guarantees" class="mat-elevation-z4 full-width" *ngIf="!loading">
        <ng-container matColumnDef="reference">
          <th mat-header-cell *matHeaderCellDef>Referencia</th>
          <td mat-cell *matCellDef="let element">{{ element.reference }}</td>
        </ng-container>

        <ng-container matColumnDef="type">
          <th mat-header-cell *matHeaderCellDef>Tipo</th>
          <td mat-cell *matCellDef="let element">{{ element.type }}</td>
        </ng-container>

        <ng-container matColumnDef="amount">
          <th mat-header-cell *matHeaderCellDef>Importe</th>
          <td mat-cell *matCellDef="let element">{{ element.amount | number:'1.2-2' }} {{ element.currency }}</td>
        </ng-container>

        <ng-container matColumnDef="beneficiary">
          <th mat-header-cell *matHeaderCellDef>Beneficiario</th>
          <td mat-cell *matCellDef="let element">{{ element.beneficiary.name }}</td>
        </ng-container>

        <ng-container matColumnDef="status">
          <th mat-header-cell *matHeaderCellDef>Estado</th>
          <td mat-cell *matCellDef="let element">
            <mat-chip [class]="'status-chip status-' + element.status.toLowerCase()">
              {{ element.status }}
            </mat-chip>
          </td>
        </ng-container>

        <ng-container matColumnDef="expiryDate">
          <th mat-header-cell *matHeaderCellDef>Vencimiento</th>
          <td mat-cell *matCellDef="let element">{{ element.expiryDate }}</td>
        </ng-container>

        <ng-container matColumnDef="actions">
          <th mat-header-cell *matHeaderCellDef>Acciones</th>
          <td mat-cell *matCellDef="let element">
            <button mat-icon-button [routerLink]="['/guarantees', element.id]">
              <mat-icon>visibility</mat-icon>
            </button>
          </td>
        </ng-container>

        <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
        <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
      </table>
    </div>
  `,
  styles: [`
    .spacer { flex: 1; }
    .container { padding: 24px; }
    .filters { display: flex; gap: 16px; margin-bottom: 16px; }
    .full-width { width: 100%; }
    .spinner-container { display: flex; justify-content: center; padding: 40px; }
    .status-chip { font-size: 11px; font-weight: 600; }
    .status-draft { background-color: #e3f2fd; color: #1565c0; }
    .status-issued { background-color: #e8f5e9; color: #2e7d32; }
    .status-amended { background-color: #fff3e0; color: #e65100; }
    .status-claimed { background-color: #fce4ec; color: #c62828; }
    .status-expired { background-color: #f5f5f5; color: #616161; }
    .status-cancelled { background-color: #fafafa; color: #9e9e9e; }
  `]
})
export class GuaranteeListComponent implements OnInit {
  guarantees: Guarantee[] = [];
  displayedColumns = ['reference', 'type', 'amount', 'beneficiary', 'status', 'expiryDate', 'actions'];
  loading = false;
  filterStatus = '';
  filterType = '';

  constructor(private guaranteeService: GuaranteeService) {}

  ngOnInit(): void {
    this.loadGuarantees();
  }

  loadGuarantees(): void {
    this.loading = true;
    this.guaranteeService.getAll(this.filterStatus || undefined, this.filterType || undefined).subscribe({
      next: (data) => {
        this.guarantees = data;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading guarantees:', error);
        this.loading = false;
      }
    });
  }
}
