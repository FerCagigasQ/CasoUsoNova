import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-claim-dialog',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule,MatDialogModule,MatFormFieldModule,MatInputModule,MatButtonModule,MatDatepickerModule,MatNativeDateModule,MatSelectModule],
  template: `<h2 mat-dialog-title>Add Claim</h2><mat-dialog-content><form [formGroup]="form"><mat-form-field><mat-label>Date</mat-label><input matInput formControlName="date" [matDatepicker]="picker"><mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle><mat-datepicker #picker></mat-datepicker></mat-form-field><mat-form-field><mat-label>Amount</mat-label><input matInput formControlName="amount" type="number" required></mat-form-field><mat-form-field><mat-label>Status</mat-label><mat-select formControlName="status"><mat-option value="PENDING">Pending</mat-option><mat-option value="APPROVED">Approved</mat-option><mat-option value="REJECTED">Rejected</mat-option></mat-select></mat-form-field><mat-form-field><mat-label>Description</mat-label><textarea matInput formControlName="description" required rows="4"></textarea></mat-form-field></form></mat-dialog-content><mat-dialog-actions><button mat-button (click)="onCancel()">Cancel</button><button mat-raised-button color="primary" (click)="onSave()" [disabled]="!form.valid">Save</button></mat-dialog-actions>`,
  styles: ['mat-form-field{width:100%;margin-bottom:15px}']
})
export class ClaimDialogComponent {
  form: FormGroup;
  constructor(private fb: FormBuilder, private dialogRef: MatDialogRef<ClaimDialogComponent>) {
    this.form = this.fb.group({date: ['',Validators.required],amount: ['',[Validators.required,Validators.min(0)]],status: ['PENDING',Validators.required],description: ['',Validators.required]});
  }
  onCancel(): void { this.dialogRef.close(); }
  onSave(): void { if(this.form.valid) this.dialogRef.close(this.form.value); }
}
