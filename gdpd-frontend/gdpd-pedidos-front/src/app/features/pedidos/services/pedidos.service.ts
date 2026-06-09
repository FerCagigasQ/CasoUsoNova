import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import {
  Pedido,
  PageResponse,
  CreatePedidoRequest,
  UpdatePedidoRequest,
  PedidoFilter
} from '../models/pedido.model';

@Injectable({ providedIn: 'root' })
export class PedidosService {
  private readonly baseUrl = `${environment.apiBaseUrl}${environment.apiPath}/pedidos`;

  constructor(private http: HttpClient) {}

  getPedidos(filter?: PedidoFilter): Observable<PageResponse<Pedido>> {
    let params = new HttpParams();
    if (filter?.estado) params = params.set('estado', filter.estado);
    if (filter?.page !== undefined) params = params.set('page', filter.page.toString());
    if (filter?.size !== undefined) params = params.set('size', filter.size.toString());
    if (filter?.sort) params = params.set('sort', filter.sort);

    return this.http.get<PageResponse<Pedido>>(this.baseUrl, { params });
  }

  getPedido(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(`${this.baseUrl}/${id}`);
  }

  createPedido(data: CreatePedidoRequest): Observable<Pedido> {
    return this.http.post<Pedido>(this.baseUrl, data);
  }

  updatePedido(id: number, data: UpdatePedidoRequest): Observable<Pedido> {
    return this.http.put<Pedido>(`${this.baseUrl}/${id}`, data);
  }

  deletePedido(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
