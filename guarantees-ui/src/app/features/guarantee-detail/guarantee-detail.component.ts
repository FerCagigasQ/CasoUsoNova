import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDialog } from '@angular/material/dialog';
import { GuaranteeService } from '../../services/guarantee.service';
import { Guarantee } from '../../models/guarantee.model';
import { AmendmentDialogComponent } from './amendment-dialog/amendment-dialog.component';
import { ClaimDialogComponent } from './claim-dialog/claim-dialog.component';

@Component({
  selector: 'app-guarantee-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    MatButtonModule,
    MatTabsModule,
    MatTableModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './guarantee-detail.component.html',
  styleUrls: ['./guarantee-detail.component.scss']
})
export class GuaranteeDetailComponent implements OnInit {
  guarantee: Guarantee | null = null;
  loading = false;

  constructor(
    private route: ActivatedRoute,
    private guaranteeService: GuaranteeService,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      const id = params['id'];
      if (id) {
        this.loadGuarantee(id);
      }
    });
  }

  loadGuarantee(id: string): void {
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

  openAmendmentDialog(): void {
    if (!this.guarantee) return;
    this.dialog.open(AmendmentDialogComponent, {
      width: '500px',
      data: { guaranteeId: this.guarantee.id }
    });
  }

  openClaimDialog(): void {
    if (!this.guarantee) return;
    this.dialog.open(ClaimDialogComponent, {
      width: '500px',
      data: { guaranteeId: this.guarantee.id }
    });
  }
}
