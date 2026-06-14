import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { GuaranteeService } from '../../services/guarantee.service';
import { CreateGuaranteeRequest, Applicant, Beneficiary, IssuingBank } from '../../models/guarantee.model';

@Component({
  selector: 'app-guarantee-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatToolbarModule,
    MatCardModule,
    MatSnackBarModule
  ],
  templateUrl: './guarantee-form.component.html',
  styleUrls: ['./guarantee-form.component.scss']
})
export class GuaranteeFormComponent implements OnInit {
  form: FormGroup;
  loading = false;
  applicants: Applicant[] = [];
  beneficiaries: Beneficiary[] = [];
  issuingBanks: IssuingBank[] = [];

  guaranteeTypes = ['PERFORMANCE', 'ADVANCE_PAYMENT', 'BID_BOND', 'WARRANTY'];
  currencies = ['EUR', 'USD', 'GBP'];

  constructor(
    private fb: FormBuilder,
    private guaranteeService: GuaranteeService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      reference: ['', Validators.required],
      type: ['', Validators.required],
      amount: ['', [Validators.required, Validators.min(1)]],
      currency: ['EUR', Validators.required],
      issueDate: ['', Validators.required],
      expiryDate: ['', Validators.required],
      applicantId: ['', Validators.required],
      beneficiaryId: ['', Validators.required],
      issuingBankId: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadReferenceData();
  }

  loadReferenceData(): void {
    // Preload from existing guarantees to populate dropdowns
    this.guaranteeService.getAll().subscribe({
      next: (guarantees) => {
        const seen = { app: new Set<number>(), ben: new Set<number>(), bank: new Set<number>() };
        guarantees.forEach(g => {
          if (g.applicant && !seen.app.has(g.applicant.id)) {
            seen.app.add(g.applicant.id);
            this.applicants.push(g.applicant);
          }
          if (g.beneficiary && !seen.ben.has(g.beneficiary.id)) {
            seen.ben.add(g.beneficiary.id);
            this.beneficiaries.push(g.beneficiary);
          }
          if (g.issuingBank && !seen.bank.has(g.issuingBank.id)) {
            seen.bank.add(g.issuingBank.id);
            this.issuingBanks.push(g.issuingBank);
          }
        });
      },
      error: (e) => console.error('Error loading reference data', e)
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;
    this.loading = true;
    const request: CreateGuaranteeRequest = this.form.value;
    this.guaranteeService.create(request).subscribe({
      next: (g) => {
        this.snackBar.open('Garantia creada correctamente', 'OK', { duration: 3000 });
        this.router.navigate(['/guarantees', g.id]);
      },
      error: () => {
        this.snackBar.open('Error al crear la garantia', 'OK', { duration: 3000 });
        this.loading = false;
      }
    });
  }
}
