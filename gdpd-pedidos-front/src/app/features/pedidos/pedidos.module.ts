import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Routes } from '@angular/router';
import { PedidosListComponent } from './pedidos-list.component';
import { PedidoDetailComponent } from './pedido-detail.component';
import { PedidoCreateComponent } from './pedido-create.component';

const routes: Routes = [
  { path: '', component: PedidosListComponent },
  { path: 'nuevo', component: PedidoCreateComponent },
  { path: ':id', component: PedidoDetailComponent }
];

@NgModule({
  declarations: [PedidosListComponent, PedidoDetailComponent, PedidoCreateComponent],
  imports: [CommonModule, FormsModule, RouterModule.forChild(routes)]
})
export class PedidosModule {}
