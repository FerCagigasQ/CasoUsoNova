import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Pedido, CreatePedidoRequest, UpdateEstadoRequest, EstadoPedido } from './models';

@Injectable({providedIn: 'root'})
export class GdpdPedidosApiClient {
  private baseUrl: string = 'http://localhost:24000/api';

  constructor(private http: HttpClient) {}

  listPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(\/pedidos).pipe(catchError(this.handleError));
  }

  getPedido(id: string): Observable<Pedido> {
    return this.http.get<Pedido>(\/pedidos/\).pipe(catchError(this.handleError));
  }

  createPedido(request: CreatePedidoRequest): Observable<Pedido> {
    return this.http.post<Pedido>(\/pedidos, request).pipe(catchError(this.handleError));
  }

  updatePedido(id: string, updates: Partial<CreatePedidoRequest>): Observable<Pedido> {
    return this.http.put<Pedido>(\/pedidos/\, updates).pipe(catchError(this.handleError));
  }

  deletePedido(id: string): Observable<void> {
    return this.http.delete<void>(\/pedidos/\).pipe(catchError(this.handleError));
  }

  updatePedidoEstado(id: string, estado: EstadoPedido): Observable<Pedido> {
    return this.http.patch<Pedido>(\/pedidos/\/estado, { estado }).pipe(catchError(this.handleError));
  }

  subscribeToEvents(): Observable<MessageEvent> {
    return new Observable(observer => {
      const eventSource = new EventSource(\/pedidos/eventos);
      eventSource.onmessage = (event: MessageEvent) => observer.next(event);
      eventSource.onerror = (error: any) => {observer.error(error); eventSource.close();};
      return () => eventSource.close();
    });
  }

  private handleError(error: HttpErrorResponse) {
    const msg = error.error?.mensaje || error.message;
    console.error(msg);
    return throwError(msg);
  }
}
