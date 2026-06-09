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
  observaciones?: string;
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

export interface CambiarEstadoRequest {
  estado: EstadoPedido;
  motivo?: string;
}
