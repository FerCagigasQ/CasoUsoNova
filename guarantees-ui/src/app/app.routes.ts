import { Routes } from '@angular/router';
import { GuaranteeListComponent } from './features/guarantee-list/guarantee-list.component';
import { GuaranteeDetailComponent } from './features/guarantee-detail/guarantee-detail.component';
import { GuaranteeFormComponent } from './features/guarantee-form/guarantee-form.component';

export const routes: Routes = [
  { path: '', redirectTo: '/guarantees', pathMatch: 'full' },
  { path: 'guarantees', component: GuaranteeListComponent },
  { path: 'guarantees/new', component: GuaranteeFormComponent },
  { path: 'guarantees/:id/edit', component: GuaranteeFormComponent },
  { path: 'guarantees/:id', component: GuaranteeDetailComponent },
];
