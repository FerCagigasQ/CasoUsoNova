import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { GdpdPedidosService } from '../../core/api/gdpd-pedidos.service';
import { PedidoDTO, EstadoPedido } from '../../core/api/models';

@Component({
  selector: 'app-pedido-detail',
  templateUrl: './pedido-detail.component.html'
})
export class PedidoDetailComponent implements OnInit {

  pedido: PedidoDTO | null = null;
  loading = false;
  error: string | null = null;
  cambiandoEstado = false;

  estadosDisponibles: EstadoPedido[] = ['BORRADOR', 'CONFIRMADO', 'EN_PROCESO', 'ENVIADO', 'ENTREGADO', 'CANCELADO'];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private pedidosApi: GdpdPedidosService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (id) {
      this.cargarPedido(id);
    }
  }

  cargarPedido(id: number): void {
    this.loading = true;
    this.pedidosApi.obtenerPedido(id).subscribe({
      next: (pedido) => {
        this.pedido = pedido;
        this.loading = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el pedido.';
        this.loading = false;
      }
    });
  }

  cambiarEstado(nuevoEstado: EstadoPedido): void {
    if (!this.pedido?.id) { return; }
    this.cambiandoEstado = true;
    this.pedidosApi.cambiarEstado(this.pedido.id, { estado: nuevoEstado }).subscribe({
      next: (updated) => {
        this.pedido = updated;
        this.cambiandoEstado = false;
      },
      error: () => {
        this.error = 'Error al cambiar estado.';
        this.cambiandoEstado = false;
      }
    });
  }

  eliminar(): void {
    if (!this.pedido?.id || !confirm('¿Eliminar este pedido?')) { return; }
    this.pedidosApi.eliminarPedido(this.pedido.id).subscribe({
      next: () => this.router.navigate(['/pedidos']),
      error: () => { this.error = 'Error al eliminar el pedido.'; }
    });
  }

  volver(): void {
    this.router.navigate(['/pedidos']);
  }
}
