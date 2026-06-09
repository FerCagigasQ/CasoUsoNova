import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PedidosListComponent } from './pedidos-list.component';

@NgModule({
  declarations: [PedidosListComponent],
  imports: [CommonModule],
  exports: [PedidosListComponent]
})
export class PedidosModule {}
