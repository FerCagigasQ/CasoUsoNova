const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(bodyParser.json());

let pedidos = [
  {
    id: 1,
    numeroPedido: 'PED-2024-000001',
    clienteId: 'CLI-001',
    estado: 'CONFIRMADO',
    fechaCreacion: new Date().toISOString(),
    fechaActualizacion: new Date().toISOString(),
    importeTotal: 450.50,
    lineas: [
      {
        id: 1,
        productoId: 'PROD-001',
        descripcion: 'Producto de prueba 1',
        cantidad: 2,
        precioUnitario: 125.25,
        importeLinea: 250.50
      },
      {
        id: 2,
        productoId: 'PROD-002',
        descripcion: 'Producto de prueba 2',
        cantidad: 1,
        precioUnitario: 200.00,
        importeLinea: 200.00
      }
    ]
  },
  {
    id: 2,
    numeroPedido: 'PED-2024-000002',
    clienteId: 'CLI-002',
    estado: 'BORRADOR',
    fechaCreacion: new Date().toISOString(),
    fechaActualizacion: new Date().toISOString(),
    importeTotal: 150.00,
    lineas: [
      {
        id: 3,
        productoId: 'PROD-003',
        descripcion: 'Producto de prueba 3',
        cantidad: 3,
        precioUnitario: 50.00,
        importeLinea: 150.00
      }
    ]
  }
];

let nextPedidoId = 3;
let nextLineaId = 4;

app.get('/api/v1/pedidos', (req, res) => {
  const { estado, page = 0, size = 20 } = req.query;
  let filtered = pedidos;

  if (estado) {
    filtered = pedidos.filter(p => p.estado === estado);
  }

  const start = parseInt(page) * parseInt(size);
  const end = start + parseInt(size);
  const paginatedPedidos = filtered.slice(start, end);

  res.json({
    content: paginatedPedidos,
    totalElements: filtered.length,
    totalPages: Math.ceil(filtered.length / parseInt(size)),
    number: parseInt(page),
    size: parseInt(size)
  });
});

app.post('/api/v1/pedidos', (req, res) => {
  const { clienteId, observaciones, lineas } = req.body;

  if (!clienteId) {
    return res.status(400).json({
      codigo: 'VALIDATION_ERROR',
      mensaje: 'clienteId es requerido',
      timestamp: new Date().toISOString()
    });
  }

  const nuevoPedido = {
    id: nextPedidoId++,
    numeroPedido: `PED-2024-${String(nextPedidoId).padStart(6, '0')}`,
    clienteId,
    estado: 'BORRADOR',
    fechaCreacion: new Date().toISOString(),
    fechaActualizacion: new Date().toISOString(),
    importeTotal: lineas ? lineas.reduce((sum, l) => sum + (l.cantidad * l.precioUnitario), 0) : 0,
    lineas: (lineas || []).map(l => ({
      id: nextLineaId++,
      productoId: l.productoId,
      descripcion: l.descripcion,
      cantidad: l.cantidad,
      precioUnitario: l.precioUnitario,
      importeLinea: l.cantidad * l.precioUnitario
    }))
  };

  pedidos.push(nuevoPedido);
  res.status(201).json(nuevoPedido);
});

app.get('/api/v1/pedidos/:id', (req, res) => {
  const pedido = pedidos.find(p => p.id === parseInt(req.params.id));
  if (!pedido) {
    return res.status(404).json({
      codigo: 'NOT_FOUND',
      mensaje: 'Pedido no encontrado',
      timestamp: new Date().toISOString()
    });
  }
  res.json(pedido);
});

app.put('/api/v1/pedidos/:id', (req, res) => {
  const pedido = pedidos.find(p => p.id === parseInt(req.params.id));
  if (!pedido) {
    return res.status(404).json({
      codigo: 'NOT_FOUND',
      mensaje: 'Pedido no encontrado',
      timestamp: new Date().toISOString()
    });
  }

  if (req.body.observaciones) {
    pedido.observaciones = req.body.observaciones;
  }
  pedido.fechaActualizacion = new Date().toISOString();

  res.json(pedido);
});

app.delete('/api/v1/pedidos/:id', (req, res) => {
  const index = pedidos.findIndex(p => p.id === parseInt(req.params.id));
  if (index === -1) {
    return res.status(404).json({
      codigo: 'NOT_FOUND',
      mensaje: 'Pedido no encontrado',
      timestamp: new Date().toISOString()
    });
  }

  const pedido = pedidos[index];
  if (pedido.estado !== 'BORRADOR') {
    return res.status(409).json({
      codigo: 'CONFLICT',
      mensaje: 'No se puede eliminar un pedido que no está en estado BORRADOR',
      timestamp: new Date().toISOString()
    });
  }

  pedidos.splice(index, 1);
  res.status(204).send();
});

app.patch('/api/v1/pedidos/:id/estado', (req, res) => {
  const pedido = pedidos.find(p => p.id === parseInt(req.params.id));
  if (!pedido) {
    return res.status(404).json({
      codigo: 'NOT_FOUND',
      mensaje: 'Pedido no encontrado',
      timestamp: new Date().toISOString()
    });
  }

  const estadosValidos = ['BORRADOR', 'CONFIRMADO', 'EN_PROCESO', 'ENVIADO', 'ENTREGADO', 'CANCELADO'];
  if (!req.body.estado || !estadosValidos.includes(req.body.estado)) {
    return res.status(400).json({
      codigo: 'VALIDATION_ERROR',
      mensaje: 'Estado inválido',
      timestamp: new Date().toISOString()
    });
  }

  pedido.estado = req.body.estado;
  pedido.fechaActualizacion = new Date().toISOString();
  res.json(pedido);
});

app.get('/api/v1/pedidos/:id/lineas', (req, res) => {
  const pedido = pedidos.find(p => p.id === parseInt(req.params.id));
  if (!pedido) {
    return res.status(404).json({
      codigo: 'NOT_FOUND',
      mensaje: 'Pedido no encontrado',
      timestamp: new Date().toISOString()
    });
  }
  res.json(pedido.lineas);
});

app.post('/api/v1/pedidos/:id/lineas', (req, res) => {
  const pedido = pedidos.find(p => p.id === parseInt(req.params.id));
  if (!pedido) {
    return res.status(404).json({
      codigo: 'NOT_FOUND',
      mensaje: 'Pedido no encontrado',
      timestamp: new Date().toISOString()
    });
  }

  const { productoId, cantidad, precioUnitario, descripcion } = req.body;
  if (!productoId || !cantidad || !precioUnitario) {
    return res.status(400).json({
      codigo: 'VALIDATION_ERROR',
      mensaje: 'Campos requeridos: productoId, cantidad, precioUnitario',
      timestamp: new Date().toISOString()
    });
  }

  const novaLinea = {
    id: nextLineaId++,
    productoId,
    descripcion: descripcion || '',
    cantidad: parseInt(cantidad),
    precioUnitario: parseFloat(precioUnitario),
    importeLinea: parseInt(cantidad) * parseFloat(precioUnitario)
  };

  pedido.lineas.push(novaLinea);
  pedido.importeTotal = pedido.lineas.reduce((sum, l) => sum + l.importeLinea, 0);

  res.status(201).json(novaLinea);
});

app.listen(PORT, () => {
  console.log(`Mock server running on http://localhost:${PORT}`);
  console.log(`Available endpoints:`);
  console.log(`  GET    /api/v1/pedidos`);
  console.log(`  POST   /api/v1/pedidos`);
  console.log(`  GET    /api/v1/pedidos/:id`);
  console.log(`  PUT    /api/v1/pedidos/:id`);
  console.log(`  DELETE /api/v1/pedidos/:id`);
  console.log(`  PATCH  /api/v1/pedidos/:id/estado`);
  console.log(`  GET    /api/v1/pedidos/:id/lineas`);
  console.log(`  POST   /api/v1/pedidos/:id/lineas`);
});
