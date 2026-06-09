import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { PedidosListComponent } from './pedidos-list.component';
import { PedidoDetailComponent } from './pedido-detail.component';
import { PedidoCreateComponent } from './pedido-create.component';

const routes: Routes = [
  { path: '', component: PedidosListComponent },
  { path: 'nuevo', component: PedidoCreateComponent },
  { path: ':id', component: PedidoDetailComponent }
];

@NgModule({ imports: [RouterModule.forChild(routes)], exports: [RouterModule] })
export class PedidosRoutingModule {}
