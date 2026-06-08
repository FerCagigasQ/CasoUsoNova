import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { GdpdPedidosService } from '../../core/api/gdpd-pedidos.service';
import { CrearPedidoRequest, CrearLineaRequest } from '../../core/api/models';

@Component({
  selector: 'app-pedido-create',
  templateUrl: './pedido-create.component.html'
})
export class PedidoCreateComponent {

  clienteId = '';
  observaciones = '';
  enviando = false;
  error: string | null = null;

  lineas: CrearLineaRequest[] = [];

  constructor(
    private router: Router,
    private pedidosApi: GdpdPedidosService
  ) {}

  anadirLinea(): void {
    this.lineas.push({ productoId: '', descripcion: '', cantidad: 1, precioUnitario: 0 });
  }

  quitarLinea(index: number): void {
    this.lineas.splice(index, 1);
  }

  importeTotal(): number {
    return this.lineas.reduce((sum, l) => sum + (l.cantidad * l.precioUnitario), 0);
  }

  crear(): void {
    if (!this.clienteId.trim()) {
      this.error = 'El ID de cliente es obligatorio.';
      return;
    }
    this.enviando = true;
    this.error = null;

    const request: CrearPedidoRequest = {
      clienteId: this.clienteId.trim(),
      observaciones: this.observaciones.trim() || undefined,
      lineas: this.lineas.length > 0 ? this.lineas : undefined
    };

    this.pedidosApi.crearPedido(request).subscribe({
      next: (pedido) => {
        this.enviando = false;
        this.router.navigate(['/pedidos', pedido.id]);
      },
      error: () => {
        this.error = 'Error al crear el pedido. Inténtelo de nuevo.';
        this.enviando = false;
      }
    });
  }

  cancelar(): void {
    this.router.navigate(['/pedidos']);
  }
}
