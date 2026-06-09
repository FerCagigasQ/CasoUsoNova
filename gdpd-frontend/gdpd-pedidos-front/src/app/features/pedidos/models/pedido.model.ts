export type EstadoPedido =
  | 'PENDIENTE'
  | 'PROCESANDO'
  | 'ENVIADO'
  | 'ENTREGADO'
  | 'CANCELADO';

export interface HistorialEstado {
  estado: EstadoPedido;
  fechaCambio: string;
  observacion?: string;
}

export interface Pedido {
  id: number;
  referencia: string;
  descripcion: string;
  importe: number;
  estado: EstadoPedido;
  fechaCreacion: string;
  fechaActualizacion: string;
  historialEstados: HistorialEstado[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface CreatePedidoRequest {
  referencia: string;
  descripcion: string;
  importe: number;
}

export interface UpdatePedidoRequest {
  estado?: EstadoPedido;
  descripcion?: string;
  importe?: number;
}

export interface PedidoFilter {
  estado?: EstadoPedido;
  page?: number;
  size?: number;
  sort?: string;
}
