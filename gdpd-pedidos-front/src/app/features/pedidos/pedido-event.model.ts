export interface PedidoEvent {
  pedidoId: string;
  tipo: 'CREACION' | 'ACTUALIZACION_ESTADO' | 'ELIMINACION';
  estado: string;
  codigoCliente: string;
  importe: number | null;
  timestamp: string;
  origen: string;
}
