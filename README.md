# Trade Finance — Bank Guarantees Demo

Aplicación de demo de **Trade Finance** centrada en garantías bancarias internacionales
(Bank Guarantees / Standby Letters of Credit) bajo terminología **ICC URDG 758**.

Construida por un equipo de agentes IA autónomos coordinados vía Paperclip.

---

## Qué se va a construir

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        TRADE FINANCE DEMO                               │
│                     Bank Guarantees (URDG 758)                          │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                         │
│   ┌─────────────────────┐         ┌──────────────────────────────┐     │
│   │   guarantees-ui      │  HTTP   │   guarantees-service          │     │
│   │   Angular 17         │────────▶│   Spring Boot 3.2.x           │     │
│   │   Material Design    │  /api   │   Java 17                     │     │
│   │   Puerto :4200       │◀────────│   Puerto :8080                │     │
│   └─────────────────────┘  JSON   │                                │     │
│                                    │   ┌────────────────────────┐  │     │
│                                    │   │  H2 Database            │  │     │
│                                    │   │  En memoria             │  │     │
│                                    │   │  (datos semilla)        │  │     │
│                                    │   └────────────────────────┘  │     │
│                                    │                                │     │
│                                    │   ┌────────────────────────┐  │     │
│                                    │   │  Swagger UI             │  │     │
│                                    │   │  /swagger-ui.html       │  │     │
│                                    │   └────────────────────────┘  │     │
│                                    └──────────────────────────────┘     │
│                                                                         │
│   ┌─────────────────────────────────────────────────────────────────┐   │
│   │  Docker Compose — todo arranca con: docker compose up --build    │   │
│   └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Modelo de dominio

```
┌──────────────┐       ┌──────────────────────────────────────────┐
│  IssuingBank  │       │              Guarantee                    │
│──────────────│       │──────────────────────────────────────────│
│ id            │       │ id                                        │
│ name          │◀──────│ reference (único)                         │
│ bic (SWIFT)   │  1:N  │ type: PERFORMANCE | ADVANCE_PAYMENT      │
│ country       │       │        BID_BOND | WARRANTY                │
└──────────────┘       │ amount (BigDecimal)                       │
                        │ currency (EUR, USD, GBP)                  │
┌──────────────┐       │ issueDate                                 │
│  Applicant    │       │ expiryDate                                │
│──────────────│       │ status: DRAFT | ISSUED | AMENDED          │
│ id            │◀──────│         CLAIMED | EXPIRED | CANCELLED     │
│ name          │  1:N  │                                           │
│ address       │       └───────────┬───────────────┬──────────────┘
│ country       │                   │               │
└──────────────┘                   │ 1:N           │ 1:N
                                    ▼               ▼
┌──────────────┐       ┌──────────────┐    ┌──────────────┐
│ Beneficiary   │       │  Amendment    │    │    Claim      │
│──────────────│       │──────────────│    │──────────────│
│ id            │       │ id            │    │ id            │
│ name          │       │ amendmentDate │    │ claimDate     │
│ address       │       │ description   │    │ claimedAmount │
│ country       │       │ newAmount     │    │ status:       │
└──────────────┘       │ newExpiryDate │    │  SUBMITTED    │
       ▲                └──────────────┘    │  UNDER_REVIEW │
       │  1:N                               │  PAID         │
       └───────── Guarantee                 │  REJECTED     │
                                            │ reason        │
                                            └──────────────┘
```

---

## Ciclo de vida de una garantía

```
                    ┌─────────┐
                    │  DRAFT   │
                    └────┬────┘
                         │ issue()
                         ▼
                    ┌─────────┐
               ┌────│ ISSUED   │────┐
               │    └────┬────┘    │
               │         │         │
         amend()│         │         │ expire()
               │         │         │
               ▼         │         ▼
          ┌─────────┐   │    ┌─────────┐
          │ AMENDED  │   │    │ EXPIRED  │
          └────┬────┘   │    └─────────┘
               │         │
     submitClaim()  submitClaim()
               │         │
               ▼         ▼
          ┌─────────────────┐
          │    CLAIMED       │
          └─────────────────┘

          En cualquier momento:
          cancel() → CANCELLED
```

---

## API REST

```
/api/v1
├── /guarantees
│   ├── GET          → Listado (filtros: ?status=ISSUED&type=PERFORMANCE)
│   ├── GET /:id     → Detalle de una garantía
│   ├── POST         → Crear nueva garantía (estado DRAFT)
│   ├── PUT /:id     → Actualizar garantía
│   └── DELETE /:id  → Eliminar garantía
│
├── /guarantees/:id/issue
│   └── POST         → Emitir garantía (DRAFT → ISSUED)
│
├── /guarantees/:id/amendments
│   └── POST         → Crear enmienda (nuevo importe/fecha)
│
└── /guarantees/:id/claims
    ├── GET          → Listar reclamaciones de una garantía
    └── POST         → Registrar reclamación
```

---

## Pantallas del frontend

```
┌─────────────────────────────────────────────────────────────────┐
│  LISTADO DE GARANTÍAS                           [+ Nueva]       │
│─────────────────────────────────────────────────────────────────│
│  Filtros: [Estado ▾] [Tipo ▾]                                   │
│                                                                  │
│  Referencia    Tipo          Importe      Beneficiario  Estado   │
│  ─────────────────────────────────────────────────────────────  │
│  BG-2024-001  Performance   EUR 500.000  Airbus SE     [ISSUED] │
│  BG-2024-002  Advance Pay   USD 1.2M     Boeing Co     [DRAFT]  │
│  BG-2024-003  Bid Bond      GBP 250.000  BAE Systems   [AMENDED]│
│  BG-2024-004  Warranty      EUR 800.000  Siemens AG    [CLAIMED]│
│  BG-2024-005  Performance   USD 3.5M     Bechtel Corp  [EXPIRED]│
│  BG-2024-006  Advance Pay   EUR 150.000  Thales Group  [DRAFT]  │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│  DETALLE: BG-2024-001                                           │
│─────────────────────────────────────────────────────────────────│
│  Referencia: BG-2024-001          Estado: [ISSUED]              │
│  Tipo: Performance Guarantee                                     │
│  Importe: EUR 500.000,00                                        │
│  Emisión: 2024-01-15   Vencimiento: 2025-01-15                 │
│                                                                  │
│  Solicitante: Airbus SE (Francia)                               │
│  Beneficiario: Boeing Commercial (EEUU)                         │
│  Banco emisor: BBVA (BBVAESMMXXX)                               │
│                                                                  │
│  [Crear Enmienda]  [Registrar Reclamación]                      │
│                                                                  │
│  ┌─ Enmiendas ─────┐  ┌─ Reclamaciones ──────┐                 │
│  │ (ninguna)        │  │ (ninguna)            │                 │
│  └──────────────────┘  └─────────────────────┘                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## Stack tecnológico

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Frontend | Angular (standalone) | 17 |
| UI Components | Angular Material | 17 |
| Lenguaje frontend | TypeScript | 5.x |
| Backend | Spring Boot | 3.2.x |
| Lenguaje backend | Java | 17 |
| Build backend | Maven (via Maven Wrapper) | 3.8+ |
| Base de datos | H2 (en memoria) | Embebida |
| Documentación API | springdoc-openapi / Swagger UI | 2.x |
| Validación | Bean Validation (Jakarta) | 3.x |
| Containerización | Docker + Docker Compose | — |
| Servidor frontend (prod) | nginx | alpine |

---

## Estructura objetivo del repositorio

```
CasoUsoNova/
├── README.md                          ← Este fichero
├── docker-compose.yml                 ← Arranca TODO con un comando
├── run-local.sh                       ← Arranque sin Docker (Linux/Mac)
├── run-local.ps1                      ← Arranque sin Docker (Windows)
│
├── guarantees-service/                ← Backend Spring Boot
│   ├── Dockerfile
│   ├── mvnw / mvnw.cmd / .mvn/       ← Maven Wrapper (NO requiere Maven instalado)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/.../
│       │   │   ├── config/            ← CORS, OpenAPI, etc.
│       │   │   ├── controller/        ← REST endpoints
│       │   │   ├── service/           ← Lógica de negocio
│       │   │   ├── repository/        ← Spring Data JPA
│       │   │   ├── domain/            ← Entidades JPA
│       │   │   ├── dto/               ← Data Transfer Objects
│       │   │   └── mapper/            ← Entity ↔ DTO
│       │   └── resources/
│       │       └── application.yml    ← H2, Swagger, CORS
│       └── test/
│
└── guarantees-ui/                     ← Frontend Angular
    ├── Dockerfile
    ├── nginx.conf                     ← Proxy /api → backend
    ├── proxy.conf.json                ← Dev proxy
    ├── package.json
    ├── angular.json
    └── src/
        ├── app/
        │   ├── core/                  ← Servicios, modelos, interceptors
        │   └── features/
        │       └── guarantees/        ← Listado, detalle, formularios
        └── environments/
```

---

## Cómo se arranca (objetivo final)

### Con Docker (recomendado — 1 comando)

```bash
git clone https://github.com/FerCagigasQ/CasoUsoNova.git
cd CasoUsoNova
docker compose up --build
```

### Sin Docker

```bash
# Terminal 1 — Backend
cd guarantees-service
./mvnw spring-boot:run

# Terminal 2 — Frontend
cd guarantees-ui
npm install && npm start
```

### URLs de acceso

| Servicio | URL |
|----------|-----|
| Frontend | http://localhost:4200 |
| API REST | http://localhost:8080/api/v1/guarantees |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 Console | http://localhost:8080/h2-console |

---

## Datos semilla precargados

La aplicación arranca con **6 garantías** en distintos estados para poder hacer la demo inmediatamente:

| Referencia | Tipo | Importe | Beneficiario | Estado |
|-----------|------|---------|-------------|--------|
| BG-2024-001 | Performance | EUR 500.000 | Airbus SE | ISSUED |
| BG-2024-002 | Advance Payment | USD 1.200.000 | Boeing Commercial | DRAFT |
| BG-2024-003 | Bid Bond | GBP 250.000 | BAE Systems | AMENDED |
| BG-2024-004 | Warranty | EUR 800.000 | Siemens Energy | CLAIMED |
| BG-2024-005 | Performance | USD 3.500.000 | Bechtel Corporation | EXPIRED |
| BG-2024-006 | Advance Payment | EUR 150.000 | Thales Group | DRAFT |

3 bancos emisores, 4 solicitantes, 4 beneficiarios — todos con datos realistas para banca.

---

## Guion de demo (5 minutos)

1. **Abrir listado** → http://localhost:4200 → Ver las 6 garantías precargadas con colores por estado
2. **Crear garantía** → Clic "Nueva" → Tipo: Performance, EUR 500.000, Solicitante: Airbus, Beneficiario: Boeing, Banco: BBVA → Guardar → Aparece en estado DRAFT
3. **Emitir** → Abrir detalle → Clic "Emitir" → Estado cambia a ISSUED
4. **Enmendar** → Clic "Crear enmienda" → Nuevo importe: EUR 750.000 → Estado cambia a AMENDED
5. **Reclamar** → Clic "Registrar reclamación" → EUR 200.000 por incumplimiento contractual → Estado cambia a CLAIMED
6. **API** → Abrir http://localhost:8080/swagger-ui.html → Mostrar documentación completa
