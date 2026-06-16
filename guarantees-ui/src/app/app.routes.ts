import { Routes } from '@angular/router';
import { GuaranteeListComponent } from './features/guarantee-list/guarantee-list.component';
import { GuaranteeFormComponent } from './features/guarantee-form/guarantee-form.component';
import { GuaranteeDetailComponent } from './features/guarantee-detail/guarantee-detail.component';

export const routes: Routes = [
  { path: '', redirectTo: '/guarantees', pathMatch: 'full' },
  { path: 'guarantees', component: GuaranteeListComponent },
  { path: 'guarantees/new', component: GuaranteeFormComponent },
  { path: 'guarantees/:id', component: GuaranteeDetailComponent },
];
