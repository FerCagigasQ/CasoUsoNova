import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatToolbarModule } from '@angular/material/toolbar';
import { GuaranteeService } from '../../services/guarantee.service';
import { Guarantee } from '../../models/guarantee.model';

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
    MatChipsModule,
    MatProgressSpinnerModule,
    MatTableModule,
    MatDialogModule,
    MatSnackBarModule,
    MatToolbarModule
  ],
  templateUrl: './guarantee-detail.component.html',
  styleUrls: ['./guarantee-detail.component.scss']
})
export class GuaranteeDetailComponent implements OnInit {
  guarantee: Guarantee | null = null;
  loading = false;
  amendmentColumns = ['date', 'description', 'newAmount'];
  claimColumns = ['date', 'amount', 'status', 'reason'];

  constructor(
    private route: ActivatedRoute,
    private guaranteeService: GuaranteeService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadGuarantee(+id);
    }
  }

  loadGuarantee(id: number): void {
    this.loading = true;
    this.guaranteeService.getById(id).subscribe({
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

  issueGuarantee(): void {
    if (!this.guarantee) return;
    this.guaranteeService.issue(this.guarantee.id).subscribe({
      next: (data) => {
        this.guarantee = data;
        this.snackBar.open('Garantia emitida correctamente', 'OK', { duration: 3000 });
      },
      error: () => this.snackBar.open('Error al emitir la garantia', 'OK', { duration: 3000 })
    });
  }

  openAmendmentDialog(): void {
    const description = window.prompt('Descripcion de la enmienda:');
    if (!description || !this.guarantee) return;
    this.guaranteeService.addAmendment(this.guarantee.id, {
      description,
      amendmentDate: new Date().toISOString().split('T')[0]
    } as any).subscribe({
      next: (data) => {
        this.guarantee = data;
        this.snackBar.open('Enmienda registrada', 'OK', { duration: 3000 });
      },
      error: () => this.snackBar.open('Error al registrar enmienda', 'OK', { duration: 3000 })
    });
  }

  openClaimDialog(): void {
    const reason = window.prompt('Motivo de la reclamacion:');
    if (!reason || !this.guarantee) return;
    this.guaranteeService.addClaim(this.guarantee.id, {
      reason,
      claimDate: new Date().toISOString().split('T')[0],
      claimedAmount: this.guarantee.amount,
      status: 'SUBMITTED'
    } as any).subscribe({
      next: (data) => {
        this.guarantee = data;
        this.snackBar.open('Reclamacion registrada', 'OK', { duration: 3000 });
      },
      error: () => this.snackBar.open('Error al registrar reclamacion', 'OK', { duration: 3000 })
    });
  }
}
