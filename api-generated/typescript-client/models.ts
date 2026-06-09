export interface Pedido {
  id: string;
  referencia: string;
  descripcion?: string;
  estado: EstadoPedido;
  fechaCreacion: string;
  importe: number;
}

export interface CreatePedidoRequest {
  referencia: string;
  descripcion?: string;
  importe?: number;
}

export enum EstadoPedido {
  PENDIENTE = 'PENDIENTE',
  CONFIRMADO = 'CONFIRMADO',
  ENVIADO = 'ENVIADO',
  ENTREGADO = 'ENTREGADO',
  CANCELADO = 'CANCELADO'
}

export interface UpdateEstadoRequest {
  estado: EstadoPedido;
}

export interface ErrorResponse {
  codigo: string;
  mensaje: string;
  timestamp: string;
}

export interface SseEvent {
  type: 'pedido-creado' | 'pedido-actualizado' | 'pedido-eliminado';
  data: Pedido;
}
