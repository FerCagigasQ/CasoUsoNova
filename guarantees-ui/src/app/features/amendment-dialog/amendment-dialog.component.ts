import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

@Component({
  selector: 'app-amendment-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatInputModule, MatButtonModule,
    MatDatepickerModule, MatNativeDateModule
  ],
  templateUrl: './amendment-dialog.component.html'
})
export class AmendmentDialogComponent {
  form: FormGroup;

  constructor(private fb: FormBuilder, private dialogRef: MatDialogRef<AmendmentDialogComponent>) {
    this.form = this.fb.group({
      newAmount: [null, [Validators.required, Validators.min(1)]],
      newExpiryDate: [null, [Validators.required]],
      description: ['', [Validators.required]]
    });
  }

  submit(): void {
    if (this.form.invalid) return;
    const val = this.form.value;
    const date = val.newExpiryDate instanceof Date
      ? val.newExpiryDate.toISOString().split('T')[0]
      : val.newExpiryDate;
    this.dialogRef.close({ ...val, newExpiryDate: date });
  }

  cancel(): void {
    this.dialogRef.close();
  }
}
