# Integración GDPD - Documentación

## Descripción General

Esta documentación describe la integración de la API de Pedidos GDPD con la plataforma NOVA. La integración incluye:

- Cliente Java generado desde OpenAPI/Swagger
- Tipos TypeScript para el cliente frontend
- Configuración del API Gateway
- Servidor Mock para testing local

## Flujo de Integración

```
┌─────────────────────┐
│  Frontend Angular   │
│  (TypeScript)       │
└──────────┬──────────┘
           │
           │ HTTP Requests
           │
┌──────────▼──────────┐
│  API Gateway        │
│  (Puerto 24000)     │
└──────────┬──────────┘
           │
           │ Routing /api/pedidos/**
           │ -> lb://gdpd-pedidos-api
           │
┌──────────▼──────────┐
│  Backend Service    │
│  (Puerto 8080)      │
│  Mock Server        │
└─────────────────────┘
```

## Endpoints Disponibles

### Gestión de Pedidos

#### GET /api/v1/pedidos
**Descripción:** Listar pedidos con paginación

**Parámetros:**
- `estado` (query, opcional): Filtrar por estado (BORRADOR, CONFIRMADO, EN_PROCESO, ENVIADO, ENTREGADO, CANCELADO)
- `fechaDesde` (query, opcional): Fecha inicio del filtro
- `fechaHasta` (query, opcional): Fecha fin del filtro
- `page` (query, default: 0): Número de página
- `size` (query, default: 20): Tamaño de página

**Respuesta (200):**
```json
{
  "content": [
    {
      "id": 1,
      "numeroPedido": "PED-2024-000001",
      "clienteId": "CLI-001",
      "estado": "CONFIRMADO",
      "fechaCreacion": "2024-01-15T10:30:00Z",
      "fechaActualizacion": "2024-01-15T10:30:00Z",
      "importeTotal": 450.50,
      "lineas": [...]
    }
  ],
  "totalElements": 10,
  "totalPages": 1,
  "number": 0,
  "size": 20
}
```

#### POST /api/v1/pedidos
**Descripción:** Crear nuevo pedido

**Body:**
```json
{
  "clienteId": "CLI-001",
  "observaciones": "Entrega urgente",
  "lineas": [
    {
      "productoId": "PROD-001",
      "descripcion": "Descripción del producto",
      "cantidad": 2,
      "precioUnitario": 125.25
    }
  ]
}
```

**Respuesta (201):** Pedido creado con todos los campos

#### GET /api/v1/pedidos/:id
**Descripción:** Obtener pedido por ID

**Respuesta (200):** Detalle del pedido

#### PUT /api/v1/pedidos/:id
**Descripción:** Actualizar observaciones de un pedido

**Body:**
```json
{
  "observaciones": "Notas actualizadas"
}
```

#### DELETE /api/v1/pedidos/:id
**Descripción:** Eliminar pedido (solo estado BORRADOR)

**Respuesta (204):** Sin contenido

#### PATCH /api/v1/pedidos/:id/estado
**Descripción:** Cambiar estado del pedido

**Body:**
```json
{
  "estado": "CONFIRMADO",
  "motivo": "Aprobado por gerente"
}
```

### Gestión de Líneas de Pedido

#### GET /api/v1/pedidos/:id/lineas
**Descripción:** Listar líneas de un pedido

**Respuesta (200):**
```json
[
  {
    "id": 1,
    "productoId": "PROD-001",
    "descripcion": "Producto de prueba",
    "cantidad": 2,
    "precioUnitario": 125.25,
    "importeLinea": 250.50
  }
]
```

#### POST /api/v1/pedidos/:id/lineas
**Descripción:** Añadir línea a pedido

**Body:**
```json
{
  "productoId": "PROD-002",
  "descripcion": "Nuevo producto",
  "cantidad": 1,
  "precioUnitario": 200.00
}
```

**Respuesta (201):** Línea creada

## Configuración del API Gateway

El API Gateway está configurado en `gateway/application.yml` con las siguientes características:

### Ruta de Pedidos
```yaml
- id: gdpd-pedidos-api
  uri: lb://gdpd-pedidos-api
  predicates:
    - Path=/api/pedidos/**
  filters:
    - RewritePath=/api/pedidos/(?<segment>.*), /api/v1/pedidos/$\{segment}
```

**Detalles:**
- **Puerto:** 24000
- **Path base:** `/api/pedidos/**`
- **Target:** `lb://gdpd-pedidos-api`
- **Rewrite:** `/api/pedidos/*` → `/api/v1/pedidos/*`

### Límites de Rate Limiting
- **replenish Rate:** 10 requests/sec
- **burst Capacity:** 20 requests/sec

## Clientes Generados

### Cliente Java

**Ubicación:** `api-generated/java-client/`

**Dependencia en Maven:**
```xml
<dependency>
    <groupId>com.gdpd.pedidos</groupId>
    <artifactId>gdpd-pedidos-api-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Uso básico:**
```java
import com.gdpd.pedidos.api.client.GdpdPedidosApiClient;
import com.gdpd.pedidos.api.client.model.*;

@Autowired
private GdpdPedidosApiClient apiClient;

// Crear pedido
CrearPedidoRequest request = new CrearPedidoRequest();
request.setClienteId("CLI-001");

apiClient.crearPedido(request)
    .subscribe(pedido -> System.out.println("Pedido creado: " + pedido.getId()));
```

### Cliente TypeScript

**Ubicación:** `gdpd-pedidos-front/src/app/core/api/`

**Tipos disponibles:**
- `PedidoDTO`
- `LineaPedidoDTO`
- `CrearPedidoRequest`
- `CrearLineaRequest`
- `EstadoPedido`

**Uso en Angular:**
```typescript
import { GdpdPedidosService } from './gdpd-pedidos.service';

export class PedidosComponent {
  constructor(private pedidosService: GdpdPedidosService) {}

  crearPedido() {
    this.pedidosService.crearPedido({
      clienteId: 'CLI-001',
      observaciones: 'Pedido de prueba'
    }).subscribe(pedido => {
      console.log('Pedido creado:', pedido);
    });
  }
}
```

## Mock Server

### Inicio

```bash
cd mock-server
npm install
npm start
```

**Puerto:** 8080
**Base URL:** `http://localhost:8080`

### Datos de Prueba

El mock server incluye 2 pedidos de prueba con las siguientes características:

1. **Pedido 1** (ID: 1)
   - Número: PED-2024-000001
   - Cliente: CLI-001
   - Estado: CONFIRMADO
   - Importe: $450.50
   - 2 líneas de pedido

2. **Pedido 2** (ID: 2)
   - Número: PED-2024-000002
   - Cliente: CLI-002
   - Estado: BORRADOR
   - Importe: $150.00
   - 1 línea de pedido

## Esquemas de Datos

### PedidoDTO
```typescript
interface PedidoDTO {
  id: number;
  numeroPedido: string;
  clienteId: string;
  estado: EstadoPedido;
  fechaCreacion: Date;
  fechaActualizacion: Date;
  importeTotal: number;
  lineas: LineaPedidoDTO[];
}
```

### LineaPedidoDTO
```typescript
interface LineaPedidoDTO {
  id: number;
  productoId: string;
  descripcion: string;
  cantidad: number;
  precioUnitario: number;
  importeLinea: number;
}
```

### EstadoPedido (Enum)
- BORRADOR
- CONFIRMADO
- EN_PROCESO
- ENVIADO
- ENTREGADO
- CANCELADO

## Seguridad

### Autenticación
Se utiliza **Bearer Token (JWT)** para autenticación en todos los endpoints.

**Header requerido:**
```
Authorization: Bearer <JWT_TOKEN>
```

### Validaciones
- `clienteId` es obligatorio para crear pedidos
- `cantidad` mínima: 1
- `precioUnitario` mínimo: 0
- Los pedidos solo se pueden eliminar en estado BORRADOR

## Testing

### Ejemplos con cURL

**Listar pedidos:**
```bash
curl -X GET http://localhost:24000/api/pedidos \
  -H "Authorization: Bearer TOKEN"
```

**Crear pedido:**
```bash
curl -X POST http://localhost:24000/api/pedidos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{
    "clienteId": "CLI-001",
    "lineas": [{
      "productoId": "PROD-001",
      "cantidad": 2,
      "precioUnitario": 125.25
    }]
  }'
```

**Obtener pedido:**
```bash
curl -X GET http://localhost:24000/api/pedidos/1 \
  -H "Authorization: Bearer TOKEN"
```

## Troubleshooting

### Error 404 Not Found
- Verificar que el API Gateway está corriendo en puerto 24000
- Verificar que el endpoint existe en el backend

### Error 400 Bad Request
- Validar que todos los campos requeridos están presentes
- Verificar el formato de los datos

### Error 401 Unauthorized
- Verificar que el token JWT es válido
- Verificar que el header Authorization está correctamente formado

## Referencias

- Especificación Swagger: `swagger/gdpd-pedidos-api.yaml`
- Cliente Java: `api-generated/java-client/`
- Configuración Gateway: `gateway/application.yml`
- Mock Server: `mock-server/index.js`
