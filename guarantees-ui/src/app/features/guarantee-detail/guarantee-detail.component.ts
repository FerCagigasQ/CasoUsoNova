import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatDividerModule } from '@angular/material/divider';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { GuaranteeService } from '../../services/guarantee.service';
import { Guarantee } from '../../models/guarantee.model';
import { AmendmentDialogComponent } from '../amendment-dialog/amendment-dialog.component';
import { ClaimDialogComponent } from '../claim-dialog/claim-dialog.component';

@Component({
  selector: 'app-guarantee-detail',
  standalone: true,
  imports: [
    CommonModule, RouterLink,
    MatCardModule, MatButtonModule, MatIconModule, MatTabsModule,
    MatTableModule, MatDividerModule, MatDialogModule, MatProgressSpinnerModule
  ],
  templateUrl: './guarantee-detail.component.html',
  styleUrl: './guarantee-detail.component.scss'
})
export class GuaranteeDetailComponent implements OnInit {
  guarantee: Guarantee | null = null;
  loading = false;
  amendmentColumns = ['amendmentDate', 'description', 'newAmount', 'newExpiryDate'];
  claimColumns = ['claimDate', 'claimedAmount', 'status', 'reason'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private svc: GuaranteeService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
  }

  load(id: number): void {
    this.loading = true;
    this.svc.getById(id).subscribe({
      next: g => { this.guarantee = g; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  issue(): void {
    if (!this.guarantee) return;
    this.svc.issue(this.guarantee.id).subscribe(g => this.guarantee = g);
  }

  openAmendment(): void {
    if (!this.guarantee) return;
    const ref = this.dialog.open(AmendmentDialogComponent, { width: '500px' });
    ref.afterClosed().subscribe(result => {
      if (result && this.guarantee) {
        this.svc.addAmendment(this.guarantee.id, result).subscribe(g => this.guarantee = g);
      }
    });
  }

  openClaim(): void {
    if (!this.guarantee) return;
    const ref = this.dialog.open(ClaimDialogComponent, { width: '500px' });
    ref.afterClosed().subscribe(result => {
      if (result && this.guarantee) {
        this.svc.addClaim(this.guarantee.id, result).subscribe(g => this.guarantee = g);
      }
    });
  }

  canIssue(): boolean {
    return this.guarantee?.status === 'DRAFT';
  }

  canAmend(): boolean {
    return this.guarantee?.status === 'ISSUED' || this.guarantee?.status === 'AMENDED';
  }

  canClaim(): boolean {
    return this.guarantee?.status === 'ISSUED' || this.guarantee?.status === 'AMENDED';
  }
}
