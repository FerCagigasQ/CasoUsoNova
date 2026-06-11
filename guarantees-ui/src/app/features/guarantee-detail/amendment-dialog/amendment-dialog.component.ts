import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

@Component({
  selector: 'app-amendment-dialog',
  standalone: true,
  imports: [CommonModule,ReactiveFormsModule,MatDialogModule,MatFormFieldModule,MatInputModule,MatButtonModule,MatDatepickerModule,MatNativeDateModule],
  template: `<h2 mat-dialog-title>Add Amendment</h2><mat-dialog-content><form [formGroup]="form"><mat-form-field><mat-label>Date</mat-label><input matInput formControlName="date" [matDatepicker]="picker"><mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle><mat-datepicker #picker></mat-datepicker></mat-form-field><mat-form-field><mat-label>Type</mat-label><input matInput formControlName="type" required></mat-form-field><mat-form-field><mat-label>Description</mat-label><textarea matInput formControlName="description" required rows="4"></textarea></mat-form-field></form></mat-dialog-content><mat-dialog-actions><button mat-button (click)="onCancel()">Cancel</button><button mat-raised-button color="primary" (click)="onSave()" [disabled]="!form.valid">Save</button></mat-dialog-actions>`,
  styles: ['mat-form-field{width:100%;margin-bottom:15px}']
})
export class AmendmentDialogComponent {
  form: FormGroup;
  constructor(private fb: FormBuilder, private dialogRef: MatDialogRef<AmendmentDialogComponent>) {
    this.form = this.fb.group({date: ['',Validators.required],type: ['',Validators.required],description: ['',Validators.required]});
  }
  onCancel(): void { this.dialogRef.close(); }
  onSave(): void { if(this.form.valid) this.dialogRef.close(this.form.value); }
}
