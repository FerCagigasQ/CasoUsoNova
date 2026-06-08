import { Component, OnInit, OnDestroy } from '@angular/core';
import { GdpdPedidosService } from '../../core/api/gdpd-pedidos.service';
import { PedidoDTO } from '../../core/api/models';
import { PedidoEvent } from './pedido-event.model';

@Component({
  selector: 'app-pedidos-list',
  templateUrl: './pedidos-list.component.html',
  styleUrls: ['./pedidos-list.component.scss']
})
export class PedidosListComponent implements OnInit, OnDestroy {

  pedidos: PedidoDTO[] = [];
  loading = false;

  pedidoEventos: PedidoEvent[] = [];
  sseConexionActiva = false;
  sseError: string | null = null;

  private eventSource: EventSource | null = null;
  readonly SSE_URL = '/api/pedidos/events';

  constructor(private pedidosApi: GdpdPedidosService) {}

  ngOnInit(): void {
    this.loadPedidos();
    this.suscribirSSE();
  }

  ngOnDestroy(): void {
    this.cerrarSSE();
  }

  loadPedidos(): void {
    this.loading = true;
    this.pedidosApi.listarPedidos().subscribe({
      next: (result: any) => { this.pedidos = result.content || result; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  private suscribirSSE(): void {
    if (this.eventSource) { this.cerrarSSE(); }
    this.eventSource = new EventSource(this.SSE_URL);

    this.eventSource.addEventListener('pedido-evento', (event: MessageEvent) => {
      try {
        const pedidoEvent: PedidoEvent = JSON.parse(event.data);
        this.pedidoEventos = [pedidoEvent, ...this.pedidoEventos].slice(0, 50);
        this.sseConexionActiva = true;
        this.sseError = null;
        if (pedidoEvent.tipo === 'CREACION' || pedidoEvent.tipo === 'ELIMINACION') {
          this.loadPedidos();
        }
      } catch (e) {
        console.error('Error parseando evento SSE:', e);
      }
    });

    this.eventSource.onopen = () => { this.sseConexionActiva = true; this.sseError = null; };
    this.eventSource.onerror = () => {
      this.sseConexionActiva = false;
      this.sseError = 'Conexion SSE interrumpida. Reconectando...';
    };
  }

  cerrarSSE(): void {
    if (this.eventSource) { this.eventSource.close(); this.eventSource = null; this.sseConexionActiva = false; }
  }

  reconectarSSE(): void { this.sseError = null; this.suscribirSSE(); }
  limpiarEventos(): void { this.pedidoEventos = []; }
}
