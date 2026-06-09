# Arquitectura GDPD — Gestión de Pedidos

> Documento de diseño arquitectónico del producto NOVA "Gestión de Pedidos" (UUAA: GDPD).
> Arquitecto Principal: Arquitecto NOVA | Versión: 1.0.0

---

## 1. Visión del producto

El producto GDPD gestiona el ciclo de vida completo de pedidos en la plataforma COSMOS/BBVA:

- **Creación y consulta** vía API REST
- **Procesamiento asíncrono** de eventos de cambio de estado
- **Reportes periódicos** mediante batch programado
- **Notificaciones en tiempo real** al frontal vía SSE

---

## 2. Subsistemas

### 2.1 Backend (`gdpd-backend/`)

Cuatro servicios NOVA generados con `nova create`:

| Servicio | Tipo NOVA | Puerto | Tecnología |
|----------|-----------|--------|------------|
| `gdpd-pedidos-api` | `api` | 8080 | Java 11 · Spring Boot 2.7 · Maven |
| `gdpd-event-processor` | `demon` | — | Java 11 · Spring Cloud Stream |
| `gdpd-report-batch` | `batch` | — | Java 11 · Spring Batch |
| `gdpd-report-scheduler` | `scheduler` | — | Java 11 · Cron |

### 2.2 Frontend (`gdpd-frontend/`)

| Servicio | Tipo NOVA | Tecnología |
|----------|-----------|------------|
| `gdpd-pedidos-front` | `frontal` | Angular 12+ · Thin3 · Node 16 |

---

## 3. Comunicación entre servicios

```
Frontend ──HTTP──► API Gateway (:24000) ──► gdpd-pedidos-api (:8080)
    │                                               │
    │◄── SSE (notificaciones en tiempo real) ───────┤
    │                                               │ publica evento
    │                                               ▼
    │                                        ActiveMQ (:8161)
    │                                               │ consume evento
    │                                               ▼
    │                                    gdpd-event-processor
    │
    └── REST ──► gdpd-report-scheduler ──► gdpd-report-batch
```

### Patrones

| Patrón | Origen → Destino | Mecanismo |
|--------|-----------------|-----------|
| Síncrono | Frontend → API | HTTP REST vía API Gateway · Spring MVC |
| Back-to-Back | API → Demonio | ActiveMQ · Spring Cloud Stream · queue `pedidos-events` |
| Back-to-Front | API → Frontend | SSE con `SseEmitter` + `EventSource` JS |
| Batch scheduling | Scheduler → Batch | REST interno o direct bean invocation |

---

## 4. Integración API Gateway

- Ruta corporativa: `/SHIVA/GDPD/pedidos-api/v1/*`
- Autenticación: WebSeal (capa exterior) → MicroGateway → servicio
- Código cliente generado con `nova generate-api-code` desde el Swagger del API
- Mock Server para desarrollo independiente del frontal

---

## 5. Dependencias de infraestructura

| Componente | Puerto local | Función |
|-----------|-------------|---------|
| PostgreSQL | :5555 | Persistencia de pedidos |
| ActiveMQ | :8161 | Broker de mensajería |
| API Gateway local | :24000 | Enrutamiento |
| Config Server | :8888 | Propiedades centralizadas |
| WebSeal Mock | :23000 | Autenticación simulada |
| CES Mock | :36000 | Servicios externos simulados |

Todos gestionados con `nova runtime start all`.

---

## 6. Decisiones de diseño

### ADR-001: Spring Cloud Stream sobre ActiveMQ directo
**Decisión**: Usar Spring Cloud Stream con binder ActiveMQ (local) / RabbitMQ (producción).
**Motivo**: Desacopla el código del broker concreto; permite migrar a RabbitMQ en PRO sin cambios de código.
**Trade-off**: Abstracción adicional; configuración de binder por entorno.

### ADR-002: SSE para notificaciones back-to-front
**Decisión**: `SseEmitter` en el API REST; `EventSource` en el frontal.
**Motivo**: Unidireccional (servidor → cliente), sin necesidad de WebSocket. Compatible con proxy corporativo.
**Trade-off**: Solo soporta push del servidor; no bidireccional.

### ADR-003: Scheduler separado del Batch
**Decisión**: Dos servicios NOVA distintos (`scheduler` + `batch`), no un único servicio con `@Scheduled`.
**Motivo**: Cumple el modelo NOVA (tipos de servicio explícitos). Permite escalar el batch independientemente.
**Trade-off**: Coordinación entre dos procesos; el scheduler necesita conocer el endpoint o canal del batch.

### ADR-004: Nova CLI para generación de servicios
**Decisión**: Todos los servicios se generan con `nova create <tipo>`, sin scaffolding manual.
**Motivo**: Garantiza cumplimiento de estándares COSMOS (Actuator, Eureka, Config Server, Dockerfile).
**Trade-off**: Requiere entorno NOVA configurado (`nova --version` → NOVA: 26.03, CLI: 7.8.0).

---

## 7. Estructura de ramas y flujo de entrega

```
main (arquitectura base, structure commits)
  ├── feature/gdpd-backend      → PR → merge → main
  ├── feature/gdpd-frontend     → PR → merge → main
  ├── feature/gdpd-async        → PR → merge → main
  ├── feature/gdpd-integration  → PR → merge → main
  ├── feature/gdpd-release      → PR → merge → main
  └── feature/gdpd-ops          → PR → merge → main
```

Ningún agente hace push directo a main. Todos los cambios entran por Pull Request revisado por el Arquitecto.

---

## 8. Equipo y responsabilidades

| Agente | Responsabilidad | Rama |
|--------|-----------------|------|
| **Arquitecto NOVA** | Diseño, base structure, revisión de PRs | main (base) |
| **Backend Service Generator** | `nova create api/demon/batch/scheduler`, implementación | feature/gdpd-backend |
| **Frontend Generator** | `nova create frontal`, vistas Angular, componentes | feature/gdpd-frontend |
| **API Integration Expert** | `nova generate-api-code`, gateway config, mock server | feature/gdpd-integration |
| **Async Communication Expert** | Spring Cloud Stream, SSE, AsyncAPI spec, DLQ | feature/gdpd-async |
| **Release Manager** | `nova validate`, Quality Gate, pipeline INT→PRE→PRO | feature/gdpd-release |
| **Operations Monitor** | Actuator config, alertas, logs, runtime verification | feature/gdpd-ops |
