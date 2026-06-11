import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, MatToolbarModule, MatIconModule],
  template: `
    <mat-toolbar color="primary">
      <span class="logo">
        <mat-icon>verified_user</mat-icon>
        <span class="app-title">Guarantees Management</span>
      </span>
    </mat-toolbar>
    <router-outlet></router-outlet>
  `,
  styles: [`:host { display: block; height: 100%; } .logo { display: flex; align-items: center; gap: 10px; } .app-title { font-size: 1.2em; font-weight: 500; }`]
})
export class AppComponent {
  title = 'guarantees-ui';
}
