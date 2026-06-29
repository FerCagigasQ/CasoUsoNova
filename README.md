# NOVA-1: Trade Finance Demo — Bank Guarantees (ICC URDG 758)

A complete demonstration application for **bank guarantees and standby letters of credit** (garantías bancarias / cartas de crédito stand-by) following ICC URDG 758 terminology.

Built with **Spring Boot 3.2.x + Angular 17 + Docker**, deployable with a single command.

---

## Quick Start

### Docker (Recommended - 1 Command)

Uses **H2 in-memory database** (no external DB needed).

```bash
# Production-like (with frontend)
docker compose up --build

# Dev-only backend (faster iteration)
docker compose -f docker-compose.local.yml up --build
```

Then access:
- **Frontend**: http://localhost (port 80)
- **Backend API**: http://localhost:8080/api/v1/guarantees
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console (user: sa, password: empty)
  - **JDBC URL**: `jdbc:h2:mem:testdb` (in-memory, data lost on restart)

### Local Development (No Docker)

**Terminal 1 — Backend**
```bash
cd guarantees-service

# Linux/Mac
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# Windows
mvnw.bat spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

**Terminal 2 — Frontend**
```bash
cd guarantees-ui
npm install
npm start
```

Frontend runs on http://localhost:4200 with proxy to backend.

---

## H2 Database Configuration

**Current Setup**: In-memory H2 database (perfect for demo/development)

| Aspect | Value |
|--------|-------|
| **Type** | In-memory (jdbc:h2:mem:testdb) |
| **Data Persistence** | ❌ Lost on restart |
| **Console** | ✅ Web UI at /h2-console |
| **Multi-access** | Same JVM only |
| **Ideal for** | Demo, dev, tests |

For detailed documentation on H2 configuration, schema, and troubleshooting, see:
- **[H2 Database Guide](docs/H2_DATABASE_GUIDE.md)** — How H2 works, configuration, profiles, troubleshooting
- **[Code Structure](docs/CODE_STRUCTURE.md)** — Entity mapping, data flow, lifecycle

### Troubleshooting H2 Errors

**Error**: `Database "/root/test" not found...`
- **Cause**: Incorrect JDBC URL or missing profile configuration
- **Fix**: Ensure `application-docker.yml` exists with `jdbc:h2:mem:testdb`
- **Verify**: `docker logs guarantees-backend-local | grep "jdbc:h2"`

See **[H2 Database Guide § Troubleshooting](docs/H2_DATABASE_GUIDE.md#5-troubleshooting)** for more issues.

---

## Architecture

### Backend: Spring Boot 3.2.x + Maven + Java 17

```
guarantees-service/
├── src/main/java/com/example/guarantees/
│   ├── controller/         # REST API endpoints (/api/v1/...)
│   ├── service/            # Business logic & state transitions
│   ├── repository/         # Spring Data JPA repositories
│   ├── domain/             # JPA entities (Guarantee, Amendment, Claim, etc.)
│   ├── dto/                # Data Transfer Objects for API
│   └── config/             # Spring configuration (DataInitializer, CORS, OpenAPI)
├── src/main/resources/
│   └── application.yml     # H2 database, port 8080, Swagger, Actuator
├── pom.xml                 # Dependencies (Spring Boot 3.2.5, H2, Actuator, OpenAPI)
└── Dockerfile              # Multi-stage build (maven:3.9 → eclipse-temurin:17-jre-alpine)
```

**Key Technologies**:
- Spring Boot Starter Web, Data JPA, Validation, Actuator
- H2 in-memory database (ZERO external dependencies)
- Spring doc OpenAPI (Swagger UI at /swagger-ui.html)
- Jakarta EE (NOT javax — Spring Boot 3.x requirement)
- Maven Wrapper (mvnw)

### Frontend: Angular 17 Standalone + Angular Material

```
guarantees-ui/
├── src/app/
│   ├── features/
│   │   ├── guarantee-list/      # Table with filters (status, type)
│   │   ├── guarantee-detail/    # Detail view with amendments & claims tabs
│   │   ├── guarantee-form/      # Create/edit form with dropdowns
│   │   └── dialogs/             # Amendment & claim dialogs
│   ├── services/
│   │   └── guarantee.service.ts # HTTP service with filters & CRUD
│   ├── models/
│   │   └── guarantee.model.ts   # TypeScript interfaces matching backend DTOs
│   ├── app.routes.ts            # Standalone routing
│   └── app.config.ts            # Angular Material theme (indigo-pink)
├── angular.json                 # Build config
├── proxy.conf.json              # Proxy /api → http://localhost:8080
├── package.json                 # Dependencies (Angular 17, Material, ...)
├── Dockerfile                   # Multi-stage (node build → nginx serving)
└── nginx.conf                   # Reverse proxy to backend
```

**Key Technologies**:
- Angular 17 standalone (no NgModules)
- Angular Material (indigo-pink theme)
- Reactive Forms with validation
- TypeScript 5.x strict mode

### Persistence: H2 In-Memory

- **No PostgreSQL, RabbitMQ, Eureka, Config Server**
- Database: jdbc:h2:mem:testdb
- H2 Console: http://localhost:8080/h2-console
- Seed data: 6 guarantees in various states (ISSUED, AMENDED, CLAIMED, EXPIRED, DRAFT)

---

## Domain Model (Data Model)

### Guarantee (Main Entity)
- `id`: auto-increment
- `reference`: unique identifier (e.g., "BG-2026-001")
- `type`: enum (PERFORMANCE, ADVANCE_PAYMENT, BID_BOND, WARRANTY)
- `amount`: BigDecimal
- `currency`: ISO 4217 (EUR, USD, GBP, etc.)
- `issueDate`: LocalDate
- `expiryDate`: LocalDate
- `status`: enum (DRAFT, ISSUED, AMENDED, CLAIMED, EXPIRED, CANCELLED)
- **Relations**:
  - `applicant`: ManyToOne → Applicant (EAGER)
  - `beneficiary`: ManyToOne → Beneficiary (EAGER)
  - `issuingBank`: ManyToOne → IssuingBank (EAGER)
  - `amendments`: OneToMany → Amendment[] (EAGER)
  - `claims`: OneToMany → Claim[] (EAGER)

### Applicant / Beneficiary (Parties)
- `id`, `firstName`, `lastName` (NOT just "name")
- `taxId`, `email`, `phone`
- `address`, `country`

### IssuingBank (Issuing Bank)
- `id`, `name`, `bic` (SWIFT BIC code)
- `country`

### Amendment (Document Modifications)
- `id`, `guarantee` (FK), `amendmentDate`
- `description`, `newAmount`, `newExpiryDate`

### Claim (Payment Claims)
- `id`, `guarantee` (FK), `claimDate`
- `claimedAmount`, `status` (enum: SUBMITTED, UNDER_REVIEW, PAID, REJECTED)
- `reason`

---

## REST API (`/api/v1`)

### Guarantees CRUD
- `GET /guarantees` — List all (supports `?status=ISSUED&type=PERFORMANCE` filters)
- `GET /guarantees/{id}` — Get one
- `POST /guarantees` — Create
- `PUT /guarantees/{id}` — Update
- `DELETE /guarantees/{id}` — Delete

### Actions (State Transitions)
- `POST /guarantees/{id}/issue` — DRAFT → ISSUED
- `POST /guarantees/{id}/amendments` — Add amendment (Guarantee → AMENDED)
- `POST /guarantees/{id}/claims` — Submit claim (Guarantee → CLAIMED)
- `GET /guarantees/{id}/claims` — List claims for a guarantee

### Reference Data (for form dropdowns)
- `GET /applicants` — List applicants
- `GET /beneficiaries` — List beneficiaries
- `GET /issuing-banks` — List issuing banks

### Health & Documentation
- `GET /actuator/health` — Service health
- `GET /swagger-ui.html` — Interactive API docs
- `GET /h2-console` — Database console

---

## Deployment Instructions

### Docker (Production-Ready)

```bash
docker compose up --build
```

Both services build and start automatically. Backend health check ensures frontend waits for API readiness.

**Ports**:
- Frontend: 80 (public HTTP)
- Backend: 8080 (internal to Docker network)

### Local Development

1. **Ensure Java 17 and Node.js 18+ are installed**
2. **Optional**: Run `run-local.sh` (Linux/Mac) or `run-local.ps1` (Windows) for automatic startup of both services
3. **Or manually**:
   ```bash
   # Terminal 1
   cd guarantees-service
   ./mvnw spring-boot:run
   
   # Terminal 2
   cd guarantees-ui
   npm install && npm start
   ```

---

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Build**: Maven 3.9
- **Database**: H2 in-memory
- **API Documentation**: springdoc-openapi 2.3.0
- **Health Monitoring**: Spring Boot Actuator
- **Container**: Docker with multi-stage build (Alpine JRE + wget)

### Frontend
- **Framework**: Angular 17 (standalone)
- **UI Library**: Angular Material (indigo-pink)
- **Language**: TypeScript 5.x
- **HTTP Client**: Angular HttpClient with proxy
- **Container**: Docker with multi-stage build (Node → Nginx)

### Infrastructure
- **Orchestration**: Docker Compose
- **Development**: npm, Maven Wrapper, Angular CLI
- **CI/CD**: Ready for Jenkins/GitHub Actions

---

## Known Errors to Avoid (E01-E22)

### Critical Errors

**E01 — Maven Wrapper JAR missing**
- ✓ Dockerfile uses `FROM maven:3.9-eclipse-temurin-17`, NOT `./mvnw`

**E02 — CRLF line endings in mvnw**
- ✓ run-local.sh fixes endings: `sed -i 's/\r$//' mvnw`

**E05 — Healthcheck without Actuator**
- ✓ pom.xml includes spring-boot-starter-actuator

**E06 — curl missing in Alpine**
- ✓ Dockerfile installs wget: `RUN apk add --no-cache wget`

**E20 — javax.persistence instead of jakarta**
- ✓ Spring Boot 3.x requires `jakarta.persistence.*`, NOT `javax.persistence.*`

### Frontend-Backend Contract (E11-E18)

**E13/E15 — Field name mismatches**
- ✓ Backend field: `reference` → Frontend: `guarantee.reference` (NOT `number`)
- ✓ Backend fields: `issueDate`, `expiryDate` → Frontend: same names (NOT `startDate`/`endDate`)
- ✓ Nested objects: `beneficiary: Beneficiary` → Frontend: `beneficiary: { firstName, lastName, ... }` (NOT string)

**E18 — CONTRACT-FIRST design**
- ✓ guarantee.model.ts mirrors backend DTOs exactly
- ✓ Form uses mat-select to send object IDs, not strings

### Filters (E19)

**E19 — Filters without backend support**
- ✓ Backend controller accepts `@RequestParam(required=false) String status, String type`
- ✓ Repository has filter methods: `findByStatus()`, `findByType()`, `findByStatusAndType()`
- ✓ Frontend sends params: `?status=ISSUED&type=PERFORMANCE`

---

## File Structure

```
CasoUsoNova/
├── guarantees-service/           # Backend Spring Boot
│   ├── pom.xml                   # Maven dependencies
│   ├── mvnw / mvnw.bat           # Maven Wrapper (development only)
│   ├── Dockerfile                # Multi-stage build
│   ├── src/main/java/...         # Java source
│   └── src/main/resources/       # application.yml
│
├── guarantees-ui/                # Frontend Angular
│   ├── package.json              # npm dependencies
│   ├── angular.json              # Angular config
│   ├── Dockerfile                # Multi-stage build
│   ├── nginx.conf                # Reverse proxy config
│   ├── proxy.conf.json           # Dev proxy (localhost:4200 → :8080)
│   └── src/                      # Angular source
│
├── docker-compose.yml            # Orchestration (backend + frontend + network)
├── run-local.sh                  # Linux/Mac startup script
├── run-local.ps1                 # Windows startup script
└── README.md                      # This file
```

---

## Next Steps (Phase 2: Delegation)

The architecture is now defined. Next phase involves:

1. **Backend team** (@nova-service-gen): Implement Spring Boot service, entities, API, Docker
2. **Frontend team** (@nova-frontend-gen): Implement Angular UI, forms, routing, Docker
3. **QA team** (@nova-release-mgr): Verify Docker deployment, E2E testing
4. **All teams**: Follow the 10 consolidated rules (E01-E22) to avoid known errors

---

## Support & References

- **Spring Boot Docs**: https://spring.io/projects/spring-boot
- **Angular Docs**: https://angular.io/docs
- **Docker Docs**: https://docs.docker.com
- **ICC URDG 758**: https://www.iccwbo.org/products-and-services/trade-finance-urdg-758

---

**Status**: Phase 1 Complete — Structure Base Ready  
**Next**: Phase 2 Delegation to specialist teams  
**Target**: Single-command Docker deployment (`docker compose up --build`)
