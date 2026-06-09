const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();
const PORT = process.env.PORT || 8080;

app.use(cors());
app.use(bodyParser.json());

let pedidos = [
  {
    id: 'pedido-001',
    referencia: 'REF-2024-001',
    descripcion: 'Pedido de prueba 1',
    estado: 'CONFIRMADO',
    fechaCreacion: new Date('2024-06-01T10:00:00Z').toISOString(),
    importe: 250.50
  },
  {
    id: 'pedido-002',
    referencia: 'REF-2024-002',
    descripcion: 'Pedido de prueba 2',
    estado: 'PENDIENTE',
    fechaCreacion: new Date('2024-06-05T14:30:00Z').toISOString(),
    importe: 150.00
  }
];

let sseClients = [];

app.get('/api/v1/pedidos', (req, res) => {
  res.json(pedidos);
});

app.post('/api/v1/pedidos', (req, res) => {
  const { referencia, descripcion, importe } = req.body;

  if (!referencia) {
    return res.status(400).json({
      codigo: 'VALIDATION_ERROR',
      mensaje: 'referencia es requerido',
      timestamp: new Date().toISOString()
    });
  }

  const newPedido = {
    id: `pedido-${Date.now()}`,
    referencia,
    descripcion: descripcion || '',
    estado: 'PENDIENTE',
    fechaCreacion: new Date().toISOString(),
    importe: importe || 0
  };

  pedidos.push(newPedido);
  broadcastEvent('pedido-creado', newPedido);
  res.status(201).json(newPedido);
});

app.get('/api/v1/pedidos/:id', (req, res) => {
  const pedido = pedidos.find(p => p.id === req.params.id);
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
  const pedido = pedidos.find(p => p.id === req.params.id);
  if (!pedido) {
    return res.status(404).json({
      codigo: 'NOT_FOUND',
      mensaje: 'Pedido no encontrado',
      timestamp: new Date().toISOString()
    });
  }

  if (req.body.referencia) pedido.referencia = req.body.referencia;
  if (req.body.descripcion) pedido.descripcion = req.body.descripcion;
  if (req.body.importe !== undefined) pedido.importe = req.body.importe;

  broadcastEvent('pedido-actualizado', pedido);
  res.json(pedido);
});

app.delete('/api/v1/pedidos/:id', (req, res) => {
  const index = pedidos.findIndex(p => p.id === req.params.id);
  if (index === -1) {
    return res.status(404).json({
      codigo: 'NOT_FOUND',
      mensaje: 'Pedido no encontrado',
      timestamp: new Date().toISOString()
    });
  }

  const pedido = pedidos[index];
  pedidos.splice(index, 1);
  broadcastEvent('pedido-eliminado', pedido);
  res.status(204).send();
});

app.patch('/api/v1/pedidos/:id/estado', (req, res) => {
  const pedido = pedidos.find(p => p.id === req.params.id);
  if (!pedido) {
    return res.status(404).json({
      codigo: 'NOT_FOUND',
      mensaje: 'Pedido no encontrado',
      timestamp: new Date().toISOString()
    });
  }

  const estadosValidos = ['PENDIENTE', 'CONFIRMADO', 'ENVIADO', 'ENTREGADO', 'CANCELADO'];
  if (!req.body.estado || !estadosValidos.includes(req.body.estado)) {
    return res.status(400).json({
      codigo: 'VALIDATION_ERROR',
      mensaje: 'Estado inválido',
      timestamp: new Date().toISOString()
    });
  }

  pedido.estado = req.body.estado;
  broadcastEvent('pedido-actualizado', pedido);
  res.json(pedido);
});

app.get('/api/v1/pedidos/eventos', (req, res) => {
  res.setHeader('Content-Type', 'text/event-stream');
  res.setHeader('Cache-Control', 'no-cache');
  res.setHeader('Connection', 'keep-alive');
  res.setHeader('Access-Control-Allow-Origin', '*');

  const clientId = Date.now().toString();
  sseClients.push({ id: clientId, res });

  res.write(':\n\n');

  req.on('close', () => {
    sseClients = sseClients.filter(client => client.id !== clientId);
  });
});

function broadcastEvent(eventType, pedido) {
  const message = `event: ${eventType}\ndata: ${JSON.stringify(pedido)}\n\n`;
  sseClients.forEach(client => {
    try {
      client.res.write(message);
    } catch (e) {
      console.error('Error writing to client:', e);
    }
  });
}

app.listen(PORT, () => {
  console.log(`Mock server running on http://localhost:${PORT}`);
  console.log(`Available endpoints:`);
  console.log(`  GET    /api/v1/pedidos`);
  console.log(`  POST   /api/v1/pedidos`);
  console.log(`  GET    /api/v1/pedidos/:id`);
  console.log(`  PUT    /api/v1/pedidos/:id`);
  console.log(`  DELETE /api/v1/pedidos/:id`);
  console.log(`  PATCH  /api/v1/pedidos/:id/estado`);
  console.log(`  GET    /api/v1/pedidos/eventos (Server-Sent Events)`);
});
