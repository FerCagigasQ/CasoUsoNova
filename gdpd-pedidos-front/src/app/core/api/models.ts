// Generated from swagger/gdpd-pedidos-api.yaml

export type EstadoPedido =
  | 'BORRADOR'
  | 'CONFIRMADO'
  | 'EN_PROCESO'
  | 'ENVIADO'
  | 'ENTREGADO'
  | 'CANCELADO';

export interface LineaPedidoDTO {
  id?: number;
  productoId?: string;
  descripcion?: string;
  cantidad?: number;
  precioUnitario?: number;
  importeLinea?: number;
}

export interface PedidoDTO {
  id?: number;
  numeroPedido?: string;
  clienteId?: string;
  estado?: EstadoPedido;
  fechaCreacion?: string;
  fechaActualizacion?: string;
  importeTotal?: number;
  lineas?: LineaPedidoDTO[];
}

export interface PedidoPage {
  content?: PedidoDTO[];
  totalElements?: number;
  totalPages?: number;
  number?: number;
  size?: number;
}

export interface CrearLineaRequest {
  productoId: string;
  descripcion?: string;
  cantidad: number;
  precioUnitario: number;
}

export interface CrearPedidoRequest {
  clienteId: string;
  observaciones?: string;
  lineas?: CrearLineaRequest[];
}

export interface ActualizarPedidoRequest {
  observaciones?: string;
}

export interface CambiarEstadoRequest {
  estado: EstadoPedido;
  motivo?: string;
}

export interface ActualizarLineaRequest {
  cantidad?: number;
  precioUnitario?: number;
}

export interface ErrorResponse {
  codigo?: string;
  mensaje?: string;
  timestamp?: string;
}
