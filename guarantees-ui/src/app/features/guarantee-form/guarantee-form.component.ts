import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { GuaranteeService } from '../../services/guarantee.service';
import { Applicant, Beneficiary, IssuingBank } from '../../models/guarantee.model';

@Component({
  selector: 'app-guarantee-form',
  standalone: true,
  imports: [
    CommonModule, RouterLink, ReactiveFormsModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    MatButtonModule, MatIconModule, MatCardModule,
    MatDatepickerModule, MatNativeDateModule, MatProgressSpinnerModule
  ],
  templateUrl: './guarantee-form.component.html',
  styleUrl: './guarantee-form.component.scss'
})
export class GuaranteeFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  editId: number | null = null;
  loading = false;
  saving = false;

  applicants: Applicant[] = [];
  beneficiaries: Beneficiary[] = [];
  issuingBanks: IssuingBank[] = [];

  readonly types = ['PERFORMANCE', 'ADVANCE_PAYMENT', 'BID_BOND', 'WARRANTY'];
  readonly currencies = ['EUR', 'USD', 'GBP', 'CHF', 'JPY'];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private svc: GuaranteeService
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      reference: ['', [Validators.required]],
      type: ['PERFORMANCE', [Validators.required]],
      amount: [null, [Validators.required, Validators.min(1)]],
      currency: ['EUR', [Validators.required]],
      issueDate: ['', [Validators.required]],
      expiryDate: ['', [Validators.required]],
      applicantId: [null, [Validators.required]],
      beneficiaryId: [null, [Validators.required]],
      issuingBankId: [null, [Validators.required]]
    });

    this.loading = true;
    this.svc.getApplicants().subscribe(a => this.applicants = a);
    this.svc.getBeneficiaries().subscribe(b => this.beneficiaries = b);
    this.svc.getIssuingBanks().subscribe(b => { this.issuingBanks = b; this.loading = false; });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.editId = Number(id);
      this.svc.getById(this.editId).subscribe(g => {
        this.form.patchValue({
          reference: g.reference,
          type: g.type,
          amount: g.amount,
          currency: g.currency,
          issueDate: g.issueDate,
          expiryDate: g.expiryDate,
          applicantId: g.applicant.id,
          beneficiaryId: g.beneficiary.id,
          issuingBankId: g.issuingBank.id
        });
      });
    }
  }

  save(): void {
    if (this.form.invalid) return;
    this.saving = true;
    const val = this.form.value;
    const req = {
      ...val,
      issueDate: this.formatDate(val.issueDate),
      expiryDate: this.formatDate(val.expiryDate)
    };

    const obs = this.isEdit && this.editId
      ? this.svc.update(this.editId, req)
      : this.svc.create(req);

    obs.subscribe({
      next: g => { this.saving = false; this.router.navigate(['/guarantees', g.id]); },
      error: () => { this.saving = false; }
    });
  }

  private formatDate(value: string | Date): string {
    if (!value) return '';
    if (value instanceof Date) {
      return value.toISOString().split('T')[0];
    }
    return value;
  }
}
