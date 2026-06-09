# Gestión de Pedidos — Producto NOVA (UUAA: GDPD)

> Producto empresarial para la gestión completa del ciclo de vida de pedidos:
> creación, procesamiento, notificaciones en tiempo real y generación de reportes.

---

## Visión General

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        PRODUCTO GDPD                                        │
│                   "Gestión de Pedidos"                                       │
│                                                                             │
│   ┌────────────────────────────────────────────────────────────────────┐    │
│   │                     SUBSISTEMA BACKEND                             │    │
│   │                                                                    │    │
│   │  ┌──────────────┐  ┌───────────────┐  ┌────────┐  ┌───────────┐  │    │
│   │  │  API REST    │  │   Demonio     │  │ Batch  │  │ Scheduler │  │    │
│   │  │              │  │               │  │        │  │           │  │    │
│   │  │ CRUD pedidos │  │ Procesador de │  │ Report │  │ Cron jobs │  │    │
│   │  │ Swagger/OAS  │  │ eventos       │  │ genera │  │ orquesta  │  │    │
│   │  │ Spring Boot  │  │ Spring Cloud  │  │ Spring │  │ lanza     │  │    │
│   │  │              │  │ Stream        │  │ Batch  │  │ batches   │  │    │
│   │  └──────┬───────┘  └───────┬───────┘  └────┬───┘  └─────┬─────┘  │    │
│   │         │                  │               │            │         │    │
│   └─────────┼──────────────────┼───────────────┼────────────┼─────────┘    │
│             │                  │               │            │              │
│   ┌─────────┼──────────────────┼───────────────┼────────────┼─────────┐    │
│   │         ▼                  ▼               ▼            ▼         │    │
│   │              INFRAESTRUCTURA DE COMUNICACIÓN                       │    │
│   │                                                                    │    │
│   │  ┌──────────────────┐  ┌──────────────┐  ┌────────────────────┐  │    │
│   │  │   API Gateway    │  │   Broker     │  │  Config Server     │  │    │
│   │  │   Enrutamiento   │  │  (ActiveMQ)  │  │  Configuración     │  │    │
│   │  │   + Seguridad    │  │  Eventos     │  │  centralizada      │  │    │
│   │  └────────┬─────────┘  └──────────────┘  └────────────────────┘  │    │
│   │           │                                                       │    │
│   └───────────┼───────────────────────────────────────────────────────┘    │
│               │                                                             │
│   ┌───────────┼───────────────────────────────────────────────────────┐    │
│   │           ▼          SUBSISTEMA FRONTEND                          │    │
│   │                                                                    │    │
│   │  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐    │    │
│   │  │ Lista        │  │ Detalle      │  │ Crear/Editar         │    │    │
│   │  │ Pedidos      │  │ Pedido       │  │ Pedido               │    │    │
│   │  │ (tabla)      │  │ (card)       │  │ (formulario)         │    │    │
│   │  └──────────────┘  └──────────────┘  └──────────────────────┘    │    │
│   │                                                                    │    │
│   │  Routing: /pedidos  ·  /pedidos/:id  ·  /pedidos/nuevo            │    │
│   │  Componentes corporativos  ·  Notificaciones SSE en tiempo real   │    │
│   │                                                                    │    │
│   └────────────────────────────────────────────────────────────────────┘    │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Arquitectura de Servicios

### Backend (`gdpd-backend/`)

| Servicio | Tipo | Responsabilidad |
|----------|------|-----------------|
| `gdpd-pedidos-api` | API REST | Endpoints CRUD de pedidos. Publica Swagger/OpenAPI. Registrado en Eureka. |
| `gdpd-event-processor` | Demonio | Consume eventos del broker. Procesa cambios de estado de pedidos. Sin endpoints HTTP. |
| `gdpd-report-batch` | Batch | Genera reportes periódicos. Lógica Reader → Processor → Writer. |
| `gdpd-report-scheduler` | Scheduler | Orquesta la ejecución de batches mediante cron. |

**Stack**: Java 11 · Spring Boot 2.7 · Maven 3.8

> Los servicios se generan mediante **NOVA CLI**. El API requiere definir su contrato OpenAPI.

### Frontend (`gdpd-frontend/`)

| Vista | Descripción |
|-------|-------------|
| Lista de pedidos | Tabla con filtros, paginación y acciones |
| Detalle de pedido | Card con información completa + historial de estados |
| Crear/Editar pedido | Formulario con validaciones |

**Stack**: Angular 12+ · Componentes corporativos (tabla, form, card) · Lazy loading

> El frontal se genera con **NOVA CLI** y utiliza componentes del catálogo corporativo.
> Requiere código cliente generado a partir del Swagger del API para comunicarse con el backend.

---

## Comunicación entre Servicios

```
┌──────────────┐         ┌─────────────────┐         ┌──────────────────┐
│              │  HTTP    │                 │  HTTP    │                  │
│   Frontend   │────────►│   API Gateway   │────────►│   API REST       │
│              │         │                 │         │   (pedidos)      │
└──────┬───────┘         └─────────────────┘         └────────┬─────────┘
       │                                                       │
       │  SSE (Server-Sent Events)                            │  Publica evento
       │◄─────────────────────────────────────────────────────┤
       │  Notificaciones en tiempo real                       │
       │                                                       ▼
       │                                              ┌──────────────────┐
       │                                              │     Broker       │
       │                                              │    (ActiveMQ)    │
       │                                              │                  │
       │                                              └────────┬─────────┘
       │                                                       │
       │                                                       │  Consume evento
       │                                                       ▼
       │                                              ┌──────────────────┐
       │                                              │    Demonio       │
       │                                              │  (event-proc.)  │
       │                                              └──────────────────┘
```

### Patrones de comunicación

| Patrón | Origen → Destino | Mecanismo |
|--------|-----------------|-----------|
| Síncrono | Frontend → API | HTTP REST vía API Gateway |
| Back-to-Back | API → Demonio | Broker de mensajería (canal de eventos) |
| Back-to-Front | API → Frontend | SSE (Server-Sent Events) para notificaciones en tiempo real |

> La comunicación asíncrona requiere configuración de **NOVA CLI** para el broker,
> definición de canales, y especificación AsyncAPI.
> También se necesita generar código cliente tanto para el backend (Java) como para el frontend (TypeScript).

---

## Integración y API Gateway

```
                    ┌─────────────────────────────────────────┐
                    │           API GATEWAY                    │
                    │                                         │
  Frontend ──────►  │   /SHIVA/GDPD/pedidos-api/v1/*         │  ──────► API REST
                    │                                         │
  (WebSeal) ─────►  │   Autenticación + Routing              │
                    │                                         │
                    └─────────────────────────────────────────┘
```

- **Código cliente generado**: A partir del Swagger del API se generan clientes Java (para servicios backend) y TypeScript (para el frontal)
- **Mock Server**: Para desarrollo independiente del frontal sin depender del backend real
- **Registro de rutas**: El API se registra en el gateway con su ruta corporativa

> La generación de código cliente y el registro en gateway se realizan con **NOVA CLI**.

---

## Runtime Local

Para desarrollo y testing, el entorno local levanta todos los servicios de infraestructura:

| Servicio | Puerto | Función |
|----------|--------|---------|
| PostgreSQL | :5555 | Base de datos |
| API Gateway | :24000 | Enrutamiento |
| Config Server | :8888 | Configuración centralizada |
| WebSeal Mock | :23000 | Simulación de autenticación |
| ActiveMQ | :8161 | Broker de mensajería |
| CES Mock | :36000 | Simulación de servicios externos |

> El runtime local completo se gestiona con **NOVA CLI**.

---

## Calidad y Despliegue

### Quality Gates

Antes de cada release se validan:
- Análisis estático (SonarQube)
- Cobertura de tests mínima
- Escaneo de seguridad
- Validación de servicios NOVA

### Flujo de entornos

```
┌─────────────┐      ┌─────────────────┐      ┌─────────────────┐
│   LOCAL     │      │   INTEGRADO     │      │  PREPRODUCCIÓN  │      ┌────────────┐
│  (Desarro-  │─────►│     (INT)       │─────►│     (PRE)       │─────►│ PRODUCCIÓN │
│   llo)      │      │                 │      │                 │      │   (PRO)    │
└─────────────┘      └─────────────────┘      └─────────────────┘      └────────────┘
                          │                        │                         │
                     Tests de                 Tests E2E              Aprobación
                     integración              + Carga                manual
                     + Quality Gate           + Quality Gate         + Quality Gate
```

> La validación de servicios antes de release se ejecuta con **NOVA CLI**.

---

## Monitorización y Operaciones

| Aspecto | Detalle |
|---------|---------|
| Health checks | Endpoints Actuator `/health`, `/metrics`, `/info` por servicio |
| Alertas | Reglas por servicio (latencia, errores, disponibilidad) |
| Logs | Eventos centralizados con trazabilidad distribuida |
| Transferencias | Ficheros entre sistemas (Xcom/ConnectDirect) |

> La verificación del estado de los servicios en runtime se gestiona con **NOVA CLI**.

---

## Estructura objetivo del repositorio

```
CasoUsoNova/
├── README.md
├── nova.yml                          ← Configuración raíz del producto
├── docs/
│   ├── arquitectura.md
│   ├── integracion.md
│   ├── async-communication.md
│   ├── release-plan.md
│   ├── deploy-checklist.md
│   └── operations.md
├── gdpd-backend/
│   ├── gdpd-pedidos-api/             ← API REST (CRUD pedidos)
│   ├── gdpd-event-processor/         ← Demonio (consumidor eventos)
│   ├── gdpd-report-batch/            ← Batch (generación reportes)
│   └── gdpd-report-scheduler/        ← Scheduler (cron)
├── gdpd-frontend/
│   └── gdpd-pedidos-front/           ← App Angular/Thin3
├── api-generated/                    ← Código cliente (Java + TypeScript)
├── gateway/                          ← Config API Gateway
└── mock-server/                      ← Mocks para desarrollo
```

---

## Datos del producto

| Dato | Valor |
|------|-------|
| UUAA | GDPD |
| Nombre | Gestión de Pedidos |
| NOVA | 26.03 |
| CLI | v7.8.0 |
| Java | Azul Zulu JDK 11.0.11 |
| Spring Boot | 2.7.18 |
| Angular | 12+ |
| Node.js | 16 |
| Maven | 3.8 |
