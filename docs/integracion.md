# Integración GDPD - Documentación

## Descripción General

Esta documentación describe la integración de la API de Pedidos GDPD con la plataforma NOVA. La integración incluye:

- Especificación OpenAPI/Swagger completa
- Cliente Java generado desde OpenAPI
- Tipos TypeScript para el cliente frontend
- Soporte para Server-Sent Events (SSE) para streaming de eventos
- Configuración del API Gateway
- Servidor Mock para testing local

## Flujo de Integración

```
┌─────────────────────┐
│  Frontend Angular   │
│  (TypeScript)       │
└──────────┬──────────┘
           │
           │ HTTP Requests + SSE
           │
┌──────────▼──────────┐
│  API Gateway        │
│  (Puerto 24000)     │
└──────────┬──────────┘
           │
           │ Routing /api/pedidos/**
           │ -> /api/v1/pedidos/**
           │
┌──────────▼──────────┐
│  Backend Service    │
│  (Puerto 8080)      │
│  Mock Server        │
└─────────────────────┘
```

## Especificación OpenAPI

La especificación completa de la API está disponible en `swagger/gdpd-pedidos-api.yaml`. Incluye:

- Endpoints CRUD para gestión de pedidos
- Modelos y esquemas JSON
- Validaciones de entrada
- Códigos de respuesta HTTP
- Endpoint SSE para eventos en tiempo real

## Endpoints Disponibles

### Gestión de Pedidos

#### GET /api/v1/pedidos
**Descripción:** Listar todos los pedidos

**Respuesta (200):**
```json
[
  {
    "id": "pedido-001",
    "referencia": "REF-2024-001",
    "descripcion": "Pedido de prueba",
    "estado": "CONFIRMADO",
    "fechaCreacion": "2024-06-01T10:00:00Z",
    "importe": 250.50
  }
]
```

#### POST /api/v1/pedidos
**Descripción:** Crear nuevo pedido

**Body:**
```json
{
  "referencia": "REF-2024-003",
  "descripcion": "Nuevo pedido",
  "importe": 150.00
}
```

**Respuesta (201):** Pedido creado con todos los campos

#### GET /api/v1/pedidos/{id}
**Descripción:** Obtener pedido por ID

**Parámetros:**
- `id` (path, requerido): Identificador único del pedido

**Respuesta (200):** Detalle del pedido

#### PUT /api/v1/pedidos/{id}
**Descripción:** Actualizar un pedido

**Body:**
```json
{
  "referencia": "REF-2024-003-UPD",
  "descripcion": "Pedido actualizado",
  "importe": 175.00
}
```

**Respuesta (200):** Pedido actualizado

#### DELETE /api/v1/pedidos/{id}
**Descripción:** Eliminar un pedido

**Respuesta (204):** Sin contenido

#### PATCH /api/v1/pedidos/{id}/estado
**Descripción:** Cambiar estado del pedido

**Body:**
```json
{
  "estado": "CONFIRMADO"
}
```

**Respuesta (200):** Pedido con nuevo estado

### Eventos en Tiempo Real

#### GET /api/v1/pedidos/eventos
**Descripción:** Stream de eventos SSE para cambios de pedidos

**Respuesta:** Server-Sent Events

**Eventos disponibles:**
- `pedido-creado`: Se envía cuando se crea un nuevo pedido
- `pedido-actualizado`: Se envía cuando se actualiza un pedido
- `pedido-eliminado`: Se envía cuando se elimina un pedido

**Ejemplo de evento:**
```
event: pedido-creado
data: {"id":"pedido-001","referencia":"REF-2024-001","descripcion":"Nuevo","estado":"PENDIENTE","fechaCreacion":"2024-06-09T14:00:00Z","importe":250.50}

```

**Uso en TypeScript:**
```typescript
this.pedidosService.streamPedidosEventos().subscribe(evento => {
  console.log(`Evento: ${evento.event}`, evento.data);
});

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

**Generado con:** `nova generate-api-code` desde `swagger/gdpd-pedidos-api.yaml`

**Dependencia en Maven:**
```xml
<dependency>
    <groupId>com.gdpd.pedidos</groupId>
    <artifactId>gdpd-pedidos-api-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

**Clases disponibles:**
- `GdpdPedidosApiClient` - Cliente principal
- `PedidoDTO` - Modelo de datos
- `CreatePedidoRequest` - Solicitud de creación
- `UpdatePedidoRequest` - Solicitud de actualización
- `UpdateEstadoRequest` - Solicitud de cambio de estado

**Uso básico:**
```java
import com.gdpd.pedidos.api.client.GdpdPedidosApiClient;
import com.gdpd.pedidos.api.client.model.*;

@Autowired
private GdpdPedidosApiClient apiClient;

// Crear pedido
CreatePedidoRequest request = new CreatePedidoRequest();
request.setReferencia("REF-2024-003");
request.setDescripcion("Nuevo pedido");
request.setImporte(150.00);

apiClient.createPedido(request)
    .subscribe(pedido -> System.out.println("Pedido creado: " + pedido.getId()));

// Listar pedidos
apiClient.listPedidos()
    .subscribe(pedidos -> System.out.println("Total: " + pedidos.size()));

// Obtener pedido
apiClient.getPedido("pedido-001")
    .subscribe(pedido -> System.out.println("Pedido: " + pedido));

// Cambiar estado
UpdateEstadoRequest estadoRequest = new UpdateEstadoRequest();
estadoRequest.setEstado("CONFIRMADO");
apiClient.updatePedidoEstado("pedido-001", estadoRequest)
    .subscribe(pedido -> System.out.println("Estado actualizado"));
```

### Cliente TypeScript

**Ubicación:** `gdpd-pedidos-front/src/app/core/api/`

**Tipos disponibles:**
- `Pedido` - Modelo de pedido
- `CreatePedidoRequest` - Solicitud de creación
- `UpdatePedidoRequest` - Solicitud de actualización
- `UpdateEstadoRequest` - Solicitud de cambio de estado
- `EstadoPedido` - Estados disponibles
- `PedidoEvento` - Evento SSE

**Servicio:** `GdpdPedidosService`

**Uso en Angular:**
```typescript
import { GdpdPedidosService } from './gdpd-pedidos.service';

export class PedidosComponent implements OnInit {
  constructor(private pedidosService: GdpdPedidosService) {}

  ngOnInit() {
    // Listar pedidos
    this.pedidosService.listPedidos().subscribe(pedidos => {
      console.log('Pedidos:', pedidos);
    });

    // Escuchar eventos en tiempo real
    this.pedidosService.streamPedidosEventos().subscribe(evento => {
      console.log(`Evento: ${evento.event}`, evento.data);
    });
  }

  crearPedido() {
    this.pedidosService.createPedido({
      referencia: 'REF-2024-003',
      descripcion: 'Nuevo pedido',
      importe: 150.00
    }).subscribe(pedido => {
      console.log('Pedido creado:', pedido);
    });
  }

  actualizarEstado(id: string) {
    this.pedidosService.updatePedidoEstado(id, {
      estado: 'CONFIRMADO'
    }).subscribe(pedido => {
      console.log('Estado actualizado:', pedido);
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

### Características

- Endpoints CRUD completamente funcionales
- Soporte para Server-Sent Events (SSE)
- Broadcast automático de eventos al crear/actualizar/eliminar pedidos
- Datos en memoria (se pierden al reiniciar)

### Datos de Prueba Iniciales

El mock server inicia con 2 pedidos:

1. **Pedido 1**
   - ID: `pedido-001`
   - Referencia: `REF-2024-001`
   - Estado: `CONFIRMADO`
   - Importe: $250.50

2. **Pedido 2**
   - ID: `pedido-002`
   - Referencia: `REF-2024-002`
   - Estado: `PENDIENTE`
   - Importe: $150.00

## Esquemas de Datos

### Pedido
```typescript
interface Pedido {
  id: string;                    // Identificador único
  referencia: string;            // Referencia única
  descripcion?: string;          // Descripción del pedido
  estado: EstadoPedido;         // Estado actual
  fechaCreacion: string;        // ISO 8601 datetime
  importe?: number;             // Importe en euros
}
```

### EstadoPedido (Enum)
- `PENDIENTE` - Estado inicial
- `CONFIRMADO` - Pedido confirmado
- `ENVIADO` - Pedido enviado
- `ENTREGADO` - Pedido entregado
- `CANCELADO` - Pedido cancelado

### Eventos SSE
```typescript
interface PedidoEvento {
  event: 'pedido-creado' | 'pedido-actualizado' | 'pedido-eliminado';
  data: Pedido;
  timestamp?: string;
}
```

## Testing

### Ejemplos con cURL

**Listar pedidos:**
```bash
curl -X GET http://localhost:24000/api/pedidos
```

**Crear pedido:**
```bash
curl -X POST http://localhost:24000/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "referencia": "REF-2024-003",
    "descripcion": "Nuevo pedido",
    "importe": 150.00
  }'
```

**Obtener pedido:**
```bash
curl -X GET http://localhost:24000/api/pedidos/pedido-001
```

**Actualizar pedido:**
```bash
curl -X PUT http://localhost:24000/api/pedidos/pedido-001 \
  -H "Content-Type: application/json" \
  -d '{
    "referencia": "REF-2024-003-UPD",
    "importe": 175.00
  }'
```

**Cambiar estado:**
```bash
curl -X PATCH http://localhost:24000/api/pedidos/pedido-001/estado \
  -H "Content-Type: application/json" \
  -d '{"estado": "CONFIRMADO"}'
```

**Escuchar eventos SSE:**
```bash
curl -N http://localhost:24000/api/pedidos/eventos
```

**Eliminar pedido:**
```bash
curl -X DELETE http://localhost:24000/api/pedidos/pedido-001
```

## Troubleshooting

### Error 404 Not Found
- Verificar que el API Gateway está corriendo en puerto 24000
- Verificar que el endpoint existe en el backend
- Validar que el ID del pedido existe

### Error 400 Bad Request
- Validar que todos los campos requeridos están presentes (`referencia`)
- Verificar el formato de los datos JSON
- Verificar que los estados son válidos

### Eventos SSE no recibidos
- Asegurar que la conexión está mantenida abierta (el servidor puede tardar en enviar eventos)
- Verificar CORS está habilitado (mock server incluye CORS)
- Validar que el cliente está conectado antes de que ocurran cambios

## Referencias

- Especificación OpenAPI: `swagger/gdpd-pedidos-api.yaml`
- Cliente Java: `api-generated/java-client/`
- Cliente TypeScript: `gdpd-pedidos-front/src/app/core/api/`
- Configuración Gateway: `gateway/application.yml`
- Mock Server: `mock-server/index.js`
- Rama feature: `feature/gdpd-integration`
