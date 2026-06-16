import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'guarantees',
    pathMatch: 'full'
  },
  {
    path: 'guarantees',
    loadComponent: () => import('./features/guarantees/guarantees.component').then(m => m.GuaranteesComponent)
  },
  {
    path: '**',
    redirectTo: 'guarantees'
  }
];
