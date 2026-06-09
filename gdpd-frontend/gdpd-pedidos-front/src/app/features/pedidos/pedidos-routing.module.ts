import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PedidosListComponent } from './components/pedidos-list/pedidos-list.component';
import { PedidoDetailComponent } from './components/pedido-detail/pedido-detail.component';
import { PedidoCreateComponent } from './components/pedido-create/pedido-create.component';

const routes: Routes = [
  { path: '',          component: PedidosListComponent },
  { path: 'nuevo',     component: PedidoCreateComponent },
  { path: ':id',       component: PedidoDetailComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PedidosRoutingModule {}
