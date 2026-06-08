import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  PedidoDTO,
  PedidoPage,
  LineaPedidoDTO,
  CrearPedidoRequest,
  ActualizarPedidoRequest,
  CambiarEstadoRequest,
  CrearLineaRequest,
  ActualizarLineaRequest,
  EstadoPedido,
} from './models';

// Generado por: nova generate-api-code --swagger swagger/gdpd-pedidos-api.yaml --flavour angular
// UUAA: GDPD | Servicio: gdpd-pedidos-api | Versión: 1.0.0

@Injectable({ providedIn: 'root' })
export class GdpdPedidosApiService {

  constructor(private http: HttpClient) {}

  private url(path: string): string {
    return `/gdpd/pedidos-api/1.0.0${path}`;
  }

  listarPedidos(params?: {
    estado?: EstadoPedido;
    fechaDesde?: string;
    fechaHasta?: string;
    page?: number;
    size?: number;
  }): Observable<PedidoPage> {
    let httpParams = new HttpParams();
    if (params?.estado) httpParams = httpParams.set('estado', params.estado);
    if (params?.fechaDesde) httpParams = httpParams.set('fechaDesde', params.fechaDesde);
    if (params?.fechaHasta) httpParams = httpParams.set('fechaHasta', params.fechaHasta);
    if (params?.page !== undefined) httpParams = httpParams.set('page', String(params.page));
    if (params?.size !== undefined) httpParams = httpParams.set('size', String(params.size));
    return this.http.get<PedidoPage>(this.url('/api/v1/pedidos'), { params: httpParams });
  }

  crearPedido(body: CrearPedidoRequest): Observable<PedidoDTO> {
    return this.http.post<PedidoDTO>(this.url('/api/v1/pedidos'), body);
  }

  obtenerPedido(id: number): Observable<PedidoDTO> {
    return this.http.get<PedidoDTO>(this.url(`/api/v1/pedidos/${id}`));
  }

  actualizarPedido(id: number, body: ActualizarPedidoRequest): Observable<PedidoDTO> {
    return this.http.put<PedidoDTO>(this.url(`/api/v1/pedidos/${id}`), body);
  }

  eliminarPedido(id: number): Observable<void> {
    return this.http.delete<void>(this.url(`/api/v1/pedidos/${id}`));
  }

  cambiarEstado(id: number, body: CambiarEstadoRequest): Observable<PedidoDTO> {
    return this.http.patch<PedidoDTO>(this.url(`/api/v1/pedidos/${id}/estado`), body);
  }

  listarLineas(pedidoId: number): Observable<LineaPedidoDTO[]> {
    return this.http.get<LineaPedidoDTO[]>(this.url(`/api/v1/pedidos/${pedidoId}/lineas`));
  }

  anadirLinea(pedidoId: number, body: CrearLineaRequest): Observable<LineaPedidoDTO> {
    return this.http.post<LineaPedidoDTO>(this.url(`/api/v1/pedidos/${pedidoId}/lineas`), body);
  }

  actualizarLinea(pedidoId: number, lineaId: number, body: ActualizarLineaRequest): Observable<LineaPedidoDTO> {
    return this.http.put<LineaPedidoDTO>(this.url(`/api/v1/pedidos/${pedidoId}/lineas/${lineaId}`), body);
  }

  eliminarLinea(pedidoId: number, lineaId: number): Observable<void> {
    return this.http.delete<void>(this.url(`/api/v1/pedidos/${pedidoId}/lineas/${lineaId}`));
  }
}
