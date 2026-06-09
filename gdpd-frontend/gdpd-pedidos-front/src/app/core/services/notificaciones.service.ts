import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface NotificacionPedido {
  tipo: 'CREADO' | 'ACTUALIZADO' | 'CANCELADO' | 'ENTREGADO';
  pedidoId: number;
  referencia: string;
  estado: string;
  timestamp: string;
}

@Injectable({ providedIn: 'root' })
export class NotificacionesService {
  private eventSource: EventSource | null = null;
  private notificaciones$ = new Subject<NotificacionPedido>();

  conectar(userId: string): Observable<NotificacionPedido> {
    this.desconectar();

    const url = `${environment.apiBaseUrl}${environment.apiPath}/pedidos/events`;
    this.eventSource = new EventSource(url);

    this.eventSource.addEventListener('pedido-event', (event: MessageEvent) => {
      try {
        const data: NotificacionPedido = JSON.parse(event.data);
        this.notificaciones$.next(data);
      } catch {
        console.warn('SSE: invalid event payload', event.data);
      }
    });

    this.eventSource.onerror = () => {
      // Browser EventSource auto-reconnects on error
      console.warn('SSE connection lost, browser will retry...');
    };

    return this.notificaciones$.asObservable();
  }

  get eventos$(): Observable<NotificacionPedido> {
    return this.notificaciones$.asObservable();
  }

  desconectar(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }
}
