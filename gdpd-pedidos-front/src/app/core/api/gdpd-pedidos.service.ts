import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { PedidoDTO, PedidoPage, CrearPedidoRequest, CambiarEstadoRequest } from './models';

@Injectable({ providedIn: 'root' })
export class GdpdPedidosService {
  private readonly base = `${environment.apiBaseUrl}/pedidos`;

  constructor(private http: HttpClient) {}

  listarPedidos(page = 0, size = 20): Observable<PedidoPage | PedidoDTO[]> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PedidoPage | PedidoDTO[]>(this.base, { params });
  }

  obtenerPedido(id: number): Observable<PedidoDTO> {
    return this.http.get<PedidoDTO>(`${this.base}/${id}`);
  }

  crearPedido(request: CrearPedidoRequest): Observable<PedidoDTO> {
    return this.http.post<PedidoDTO>(this.base, request);
  }

  cambiarEstado(id: number, req: CambiarEstadoRequest): Observable<PedidoDTO> {
    return this.http.post<PedidoDTO>(`${this.base}/${id}/estado`, req);
  }

  eliminarPedido(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
