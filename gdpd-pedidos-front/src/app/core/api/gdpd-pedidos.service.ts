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

@Injectable({ providedIn: 'root' })
export class GdpdPedidosService {
  private readonly baseUrl = '/api/pedidos';

  constructor(private http: HttpClient) {}

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
    if (params?.page != null) httpParams = httpParams.set('page', params.page.toString());
    if (params?.size != null) httpParams = httpParams.set('size', params.size.toString());
    return this.http.get<PedidoPage>(this.baseUrl, { params: httpParams });
  }

  crearPedido(request: CrearPedidoRequest): Observable<PedidoDTO> {
    return this.http.post<PedidoDTO>(this.baseUrl, request);
  }

  obtenerPedido(id: number): Observable<PedidoDTO> {
    return this.http.get<PedidoDTO>(`${this.baseUrl}/${id}`);
  }

  actualizarPedido(id: number, request: ActualizarPedidoRequest): Observable<PedidoDTO> {
    return this.http.put<PedidoDTO>(`${this.baseUrl}/${id}`, request);
  }

  eliminarPedido(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  cambiarEstado(id: number, request: CambiarEstadoRequest): Observable<PedidoDTO> {
    return this.http.patch<PedidoDTO>(`${this.baseUrl}/${id}/estado`, request);
  }

  listarLineas(pedidoId: number): Observable<LineaPedidoDTO[]> {
    return this.http.get<LineaPedidoDTO[]>(`${this.baseUrl}/${pedidoId}/lineas`);
  }

  anadirLinea(pedidoId: number, request: CrearLineaRequest): Observable<LineaPedidoDTO> {
    return this.http.post<LineaPedidoDTO>(`${this.baseUrl}/${pedidoId}/lineas`, request);
  }

  actualizarLinea(pedidoId: number, lineaId: number, request: ActualizarLineaRequest): Observable<LineaPedidoDTO> {
    return this.http.put<LineaPedidoDTO>(`${this.baseUrl}/${pedidoId}/lineas/${lineaId}`, request);
  }

  eliminarLinea(pedidoId: number, lineaId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${pedidoId}/lineas/${lineaId}`);
  }
}
