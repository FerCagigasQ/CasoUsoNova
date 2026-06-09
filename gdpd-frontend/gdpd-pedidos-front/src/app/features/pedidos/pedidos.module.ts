import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { PedidosRoutingModule } from './pedidos-routing.module';
import { SharedModule } from '../../shared/shared.module';
import { PedidosListComponent } from './components/pedidos-list/pedidos-list.component';
import { PedidoDetailComponent } from './components/pedido-detail/pedido-detail.component';
import { PedidoCreateComponent } from './components/pedido-create/pedido-create.component';

@NgModule({
  declarations: [
    PedidosListComponent,
    PedidoDetailComponent,
    PedidoCreateComponent
  ],
  imports: [
    SharedModule,
    PedidosRoutingModule
  ],
  // CUSTOM_ELEMENTS_SCHEMA allows bbva-web-* custom elements in templates
  schemas: [CUSTOM_ELEMENTS_SCHEMA]
})
export class PedidosModule {}
