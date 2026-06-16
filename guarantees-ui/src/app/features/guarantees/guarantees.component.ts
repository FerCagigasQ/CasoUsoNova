import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-guarantees',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule],
  template: `
    <div class="guarantees-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Guarantees</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <p>Guarantees management module</p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .guarantees-container {
      max-width: 1200px;
      margin: 0 auto;
    }
  `]
})
export class GuaranteesComponent {}
