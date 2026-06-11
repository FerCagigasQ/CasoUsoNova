import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatCardModule } from '@angular/material/card';
import { GuaranteeService } from '../../services/guarantee.service';

@Component({
  selector: 'app-guarantee-form',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule,RouterLink,MatFormFieldModule,MatInputModule,MatButtonModule,MatIconModule,MatDatepickerModule,MatNativeDateModule,MatSnackBarModule,MatCardModule],
  template: `<div class="form-container"><div class="header"><button mat-icon-button routerLink="/"><mat-icon>arrow_back</mat-icon></button><h1>New Guarantee</h1></div><mat-card class="form-card"><mat-card-content><form [formGroup]="form" (ngSubmit)="onSubmit()"><mat-form-field><mat-label>Number</mat-label><input matInput formControlName="number" required><mat-error *ngIf="form.get('number')?.hasError('required')">Number is required</mat-error></mat-form-field><mat-form-field><mat-label>Description</mat-label><textarea matInput formControlName="description" required rows="4"></textarea><mat-error *ngIf="form.get('description')?.hasError('required')">Description is required</mat-error></mat-form-field><mat-form-field><mat-label>Beneficiary</mat-label><input matInput formControlName="beneficiary" required><mat-error *ngIf="form.get('beneficiary')?.hasError('required')">Beneficiary is required</mat-error></mat-form-field><mat-form-field><mat-label>Amount</mat-label><input matInput formControlName="amount" type="number" required><mat-error *ngIf="form.get('amount')?.hasError('required')">Amount is required</mat-error><mat-error *ngIf="form.get('amount')?.hasError('min')">Amount must be greater than 0</mat-error></mat-form-field><mat-form-field><mat-label>Start Date</mat-label><input matInput formControlName="startDate" [matDatepicker]="startPicker" required><mat-datepicker-toggle matSuffix [for]="startPicker"></mat-datepicker-toggle><mat-datepicker #startPicker></mat-datepicker><mat-error *ngIf="form.get('startDate')?.hasError('required')">Start date is required</mat-error></mat-form-field><mat-form-field><mat-label>End Date</mat-label><input matInput formControlName="endDate" [matDatepicker]="endPicker" required><mat-datepicker-toggle matSuffix [for]="endPicker"></mat-datepicker-toggle><mat-datepicker #endPicker></mat-datepicker><mat-error *ngIf="form.get('endDate')?.hasError('required')">End date is required</mat-error></mat-form-field><div class="actions"><button mat-raised-button type="button" [routerLink]="['/guarantees']">Cancel</button><button mat-raised-button color="primary" type="submit" [disabled]="!form.valid||loading"><span *ngIf="!loading">Create Guarantee</span><span *ngIf="loading">Creating...</span></button></div></form></mat-card-content></mat-card></div>`,
  styles: [`.form-container{padding:20px;max-width:600px;margin:0 auto}.header{display:flex;align-items:center;gap:20px;margin-bottom:30px}h1{margin:0}.form-card{background:#fff;box-shadow:0 2px 4px rgba(0,0,0,0.1)}form{display:flex;flex-direction:column;gap:15px}mat-form-field{width:100%}.actions{display:flex;gap:10px;justify-content:flex-end;margin-top:20px}button{min-width:120px}`]
})
export class GuaranteeFormComponent {
  form: FormGroup;
  loading = false;

  constructor(private fb: FormBuilder, private guaranteeService: GuaranteeService, private router: Router, private snackBar: MatSnackBar) {
    this.form = this.fb.group({
      number: ['',Validators.required],
      description: ['',Validators.required],
      beneficiary: ['',Validators.required],
      amount: ['',[Validators.required,Validators.min(0.01)]],
      startDate: ['',Validators.required],
      endDate: ['',Validators.required]
    });
  }

  onSubmit(): void {
    if(this.form.valid) {
      this.loading = true;
      const formValue = this.form.value;
      const request = {...formValue, startDate: this.formatDate(formValue.startDate), endDate: this.formatDate(formValue.endDate)};
      this.guaranteeService.create(request).subscribe({
        next: (result) => {
          this.snackBar.open('Guarantee created successfully','Close',{duration:3000});
          this.router.navigate(['/guarantees',result.id]);
          this.loading = false;
        },
        error: (error) => {
          this.snackBar.open('Error creating guarantee','Close',{duration:3000});
          console.error('Error:',error);
          this.loading = false;
        }
      });
    }
  }

  private formatDate(date: any): string {
    if(!date) return '';
    if(typeof date === 'string') return date;
    return new Date(date).toISOString().split('T')[0];
  }
}
