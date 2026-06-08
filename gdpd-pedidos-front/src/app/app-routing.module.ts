import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'pedidos',
    loadChildren: () =>
      import('./features/pedidos/pedidos.module').then(m => m.PedidosModule)
  },
  { path: '', redirectTo: 'pedidos', pathMatch: 'full' },
  { path: '**', redirectTo: 'pedidos' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
