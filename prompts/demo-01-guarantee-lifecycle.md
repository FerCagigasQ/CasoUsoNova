# Demo 1: Guarantee Lifecycle (Create → Issue → Amend)

> **Qué es esta demo**: un **desarrollo técnico completo, de código real**, ejecutado por la
> organización **NOVA** (8 agentes autónomos) desde el tablero de **Paperclip**. El operador crea
> **un objetivo**; el `nova-architect` lo descompone en ≤5 sub-tareas y los agentes **escriben
> código** (entidad JPA, máquina de estados, REST, Swagger, frontal Angular, Docker) que se ve
> aparecer commit a commit. No es un guion de presentación: es la **especificación de implementación**
> que cada agente debe llevar a código funcionando.

**Escenario**: Ciclo de vida de un aval bancario (CRUD + transiciones de estado)
**Stack real del repo**: Spring Boot 3.2.x · Java 17 (`jakarta.*`) · Angular 17 (Material) · Docker
**Duración estimada de implementación**: 4–6 h (backend + frontend + integración + release)
**Org responsable**: NOVA Development Team (8 agentes) gobernada desde un único tablero

---

## 1. Objetivo de negocio (entrada de la demo)

> "Quiero un servicio NOVA que gestione el ciclo de vida de un aval: crearlo en `DRAFT`,
> emitirlo (`ISSUED`), enmendarlo (`AMENDED`), con su frontal para operarlo, validando las
> transiciones de estado y con trazabilidad ICC URDG 758."

El operador crea **un Goal** y **una incidencia raíz** asignada a `nova-architect`. A partir de ahí
Paperclip orquesta el desarrollo: heartbeats, checkout atómico, dependencias de bloqueo, presupuesto
por agente, approval gate para emitir/mergear, memoria compartida y audit log. **El valor de la demo
es ver a los agentes escribiendo este código**; las secciones §4–§7 son el contrato técnico que cada
uno implementa.

---

## 2. Reparto del desarrollo entre agentes NOVA (≤5 sub-tareas)

El `nova-architect` descompone la raíz en **un máximo de 5 sub-tareas** (modo demo, sin cascadas):

| # | Sub-tarea (código a producir) | Agente | Adapter | Depende de | Sección |
|---|-------------------------------|--------|---------|-----------|---------|
| 1 | Bootstrap del repo y toolchain | `nova-repo-provisioner` | Claude Code | — | — |
| 2 | **Backend**: entidad, enums, repo, DTO, máquina de estados, REST, seed | `nova-service-gen` | Codex | #1 | §4 |
| 3 | **Frontend**: modelos, service, lista, detalle, formularios, enmienda | `nova-frontend-gen` | Antigravity | #2 | §6 |
| 4 | **Integración**: Swagger/OpenAPI, CORS, contrato de error | `nova-api-integr` | Antigravity | #2 | §5 |
| 5 | **Release**: Dockerfile, `docker-compose`, gate de validación | `nova-release-mgr` | Codex | #2,#3,#4 | §7 |

`nova-async-comm` y `nova-ops-monitor` quedan en **standby** (§8): aparecen en el org chart pero solo
entran si el flujo lo exige. Así la gobernanza de Paperclip limita el scope automáticamente.

---

## 3. Modelo de dominio (referencia común)

```
GUARANTEE  ── ManyToOne ──>  APPLICANT
   │   │   │                 BENEFICIARY
   │   │   └─ ManyToOne ──>  ISSUING_BANK
   │   └─ OneToMany ──> AMENDMENT   (máx. 3 por aval)
   └────── OneToMany ──> CLAIM

GuaranteeStatus : DRAFT → ISSUED → AMENDED → CLAIMED | EXPIRED | CANCELLED
GuaranteeType   : PERFORMANCE | ADVANCE_PAYMENT | BID_BOND | WARRANTY
```

**Reglas de la máquina de estados** (las implementa `nova-service-gen` en §4):
- `POST` crea siempre en `DRAFT`.
- Solo `DRAFT` puede editarse (`PUT`) y borrarse (`DELETE`).
- `issue()` exige `DRAFT` → pasa a `ISSUED` (valida `amount > 0`, `expiryDate > issueDate`, partes no nulas).
- Solo `ISSUED`/`AMENDED` admiten enmienda; cada enmienda → `AMENDED`; **máximo 3 enmiendas**.
- Transición inválida → `400`; aval inexistente → `404`.

---

## 4. Backend — `nova-service-gen` (Spring Boot 3.2 / Java 17)

**Objetivo**: implementar el dominio, la persistencia, la máquina de estados y los 9 endpoints REST.

### 4.1 Enums

```java
package com.nova.guarantees.domain;

public enum GuaranteeStatus { DRAFT, ISSUED, AMENDED, CLAIMED, EXPIRED, CANCELLED }

public enum GuaranteeType { PERFORMANCE, ADVANCE_PAYMENT, BID_BOND, WARRANTY }
```

### 4.2 Entidad JPA `Guarantee`

```java
package com.nova.guarantees.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "GUARANTEE", indexes = {
        @Index(name = "idx_guarantee_status", columnList = "status"),
        @Index(name = "idx_guarantee_type", columnList = "type"),
        @Index(name = "idx_guarantee_status_type", columnList = "status,type")
})
public class Guarantee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;                 // p.ej. BG-2026-001

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuaranteeType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;                  // ISO-4217, p.ej. EUR

    @Column(nullable = false)
    private LocalDate issueDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GuaranteeStatus status = GuaranteeStatus.DRAFT;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "applicant_id")
    private Applicant applicant;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "beneficiary_id")
    private Beneficiary beneficiary;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "issuing_bank_id")
    private IssuingBank issuingBank;

    @OneToMany(mappedBy = "guarantee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Amendment> amendments = new ArrayList<>();

    @OneToMany(mappedBy = "guarantee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Claim> claims = new ArrayList<>();

    // getters / setters / constructors
}
```

> Crear igualmente `Applicant`, `Beneficiary`, `IssuingBank` (`firstName/lastName/email` y
> `name/bic/country` según corresponda), `Amendment` (`description`, `newAmount`, `newExpiryDate`,
> `createdAt`, `@ManyToOne Guarantee`) y `Claim` (`reason`, `amount`, `status`, `@ManyToOne Guarantee`).

### 4.3 Repositorio

```java
public interface GuaranteeRepository extends JpaRepository<Guarantee, Long> {
    List<Guarantee> findByStatus(GuaranteeStatus status);
    List<Guarantee> findByType(GuaranteeType type);
    List<Guarantee> findByStatusAndType(GuaranteeStatus status, GuaranteeType type);
    boolean existsByReference(String reference);
}
```

### 4.4 Servicio con máquina de estados

```java
@Service
@Transactional
public class GuaranteeService {

    private final GuaranteeRepository repository;
    public GuaranteeService(GuaranteeRepository repository) { this.repository = repository; }

    public Guarantee create(Guarantee g) {
        validateBusinessRules(g);
        g.setStatus(GuaranteeStatus.DRAFT);
        return repository.save(g);
    }

    public Guarantee issue(Long id) {
        Guarantee g = getOrThrow(id);
        if (g.getStatus() != GuaranteeStatus.DRAFT)
            throw new InvalidStateTransitionException("Solo un aval en DRAFT puede emitirse");
        validateBusinessRules(g);
        g.setStatus(GuaranteeStatus.ISSUED);
        return g;
    }

    public Amendment addAmendment(Long id, Amendment amendment) {
        Guarantee g = getOrThrow(id);
        if (g.getStatus() != GuaranteeStatus.ISSUED && g.getStatus() != GuaranteeStatus.AMENDED)
            throw new InvalidStateTransitionException("Solo ISSUED/AMENDED admite enmiendas");
        if (g.getAmendments().size() >= 3)
            throw new InvalidStateTransitionException("Máximo 3 enmiendas por aval");
        amendment.setGuarantee(g);
        g.getAmendments().add(amendment);
        g.setStatus(GuaranteeStatus.AMENDED);
        return amendment;
    }

    public void delete(Long id) {
        Guarantee g = getOrThrow(id);
        if (g.getStatus() != GuaranteeStatus.DRAFT)
            throw new InvalidStateTransitionException("Solo un aval en DRAFT puede borrarse");
        repository.delete(g);
    }

    private void validateBusinessRules(Guarantee g) {
        if (g.getAmount() == null || g.getAmount().signum() <= 0)
            throw new IllegalArgumentException("amount debe ser > 0");
        if (!g.getExpiryDate().isAfter(g.getIssueDate()))
            throw new IllegalArgumentException("expiryDate debe ser posterior a issueDate");
        if (g.getApplicant() == null || g.getBeneficiary() == null || g.getIssuingBank() == null)
            throw new IllegalArgumentException("applicant, beneficiary e issuingBank son obligatorios");
    }

    private Guarantee getOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new GuaranteeNotFoundException(id));
    }
}
```

### 4.5 Controlador REST (9 endpoints)

```java
@RestController
@RequestMapping("/api/v1/guarantees")
public class GuaranteeController {

    private final GuaranteeService service;
    private final GuaranteeRepository repository;
    private final GuaranteeMapper mapper;   // entidad <-> DTO

    @GetMapping
    public List<GuaranteeDTO> list(@RequestParam(required = false) GuaranteeStatus status,
                                   @RequestParam(required = false) GuaranteeType type) {
        List<Guarantee> result;
        if (status != null && type != null) result = repository.findByStatusAndType(status, type);
        else if (status != null)           result = repository.findByStatus(status);
        else if (type != null)             result = repository.findByType(type);
        else                               result = repository.findAll();
        return result.stream().map(mapper::toDto).toList();
    }

    @GetMapping("/{id}")
    public GuaranteeDTO get(@PathVariable Long id) { return mapper.toDto(service.getOrThrow(id)); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GuaranteeDTO create(@Valid @RequestBody GuaranteeRequest body) {
        return mapper.toDto(service.create(mapper.toEntity(body)));
    }

    @PutMapping("/{id}")
    public GuaranteeDTO update(@PathVariable Long id, @Valid @RequestBody GuaranteeRequest body) {
        return mapper.toDto(service.update(id, body));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) { service.delete(id); }

    @PostMapping("/{id}/issue")
    public GuaranteeDTO issue(@PathVariable Long id) { return mapper.toDto(service.issue(id)); }

    @PostMapping("/{id}/amendments")
    @ResponseStatus(HttpStatus.CREATED)
    public AmendmentDTO amend(@PathVariable Long id, @Valid @RequestBody AmendmentRequest body) {
        return mapper.toDto(service.addAmendment(id, mapper.toEntity(body)));
    }
}
```

### 4.6 DTOs y contrato de nombres

DTOs `GuaranteeDTO`, `ApplicantDTO`, `BeneficiaryDTO`, `IssuingBankDTO`, `AmendmentDTO`, `ClaimDTO`.
**Nombres exactos del contrato** (los consume el frontal en §6):

```jsonc
{
  "id": 1,
  "reference": "BG-2026-001",
  "type": "PERFORMANCE",
  "amount": 50000.00,
  "currency": "EUR",
  "issueDate": "2026-01-15",      // NO startDate
  "expiryDate": "2026-12-31",     // NO endDate
  "status": "ISSUED",             // enum como string, no número
  "applicant":   { "firstName": "...", "lastName": "...", "email": "..." },
  "beneficiary": { "firstName": "...", "lastName": "...", "email": "..." },
  "issuingBank": { "name": "...", "bic": "...", "country": "..." }
}
```

### 4.7 Seed de datos

`@Component DataSeeder implements ApplicationRunner`: inserta 3 `Applicant`, 3 `Beneficiary`,
3 `IssuingBank` y **6 avales** en estados variados (`DRAFT`, `ISSUED`, `AMENDED`, `CLAIMED`, `EXPIRED`)
solo si la tabla está vacía.

**Aceptación backend**: 9 endpoints con códigos correctos (201/200/204/400/404), máquina de estados
que rechaza transiciones inválidas, seed al arrancar, tests de repo/service/controller (>80%),
Swagger autogenerado.

---

## 5. Integración y contrato — `nova-api-integr`

### 5.1 OpenAPI/Swagger (springdoc)

```java
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI novaGuaranteesApi() {
        return new OpenAPI().info(new Info()
                .title("NOVA Guarantees API")
                .version("v1")
                .description("Ciclo de vida de avales bancarios (ICC URDG 758)"));
    }
}
```

Anotar endpoints con `@Operation` / `@ApiResponse` y verificar `/swagger-ui.html` y `/v3/api-docs`.
Incluir ejemplos (`BG-2026-001`, `EUR`, `50000`).

### 5.2 CORS (frontal 4200 → backend 8080)

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
```

### 5.3 Manejo de errores consistente

```java
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(GuaranteeNotFoundException.class)
    public ResponseEntity<ApiError> notFound(GuaranteeNotFoundException ex) {
        return ResponseEntity.status(404).body(new ApiError(ex.getMessage(), 404));
    }

    @ExceptionHandler({InvalidStateTransitionException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> badRequest(RuntimeException ex) {
        return ResponseEntity.badRequest().body(new ApiError(ex.getMessage(), 400));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> conflict(RuntimeException ex) {
        return ResponseEntity.status(409).body(new ApiError("Conflicto de concurrencia", 409));
    }
}

public record ApiError(String error, int status) {}
```

**Aceptación integración**: Swagger lista los 9 endpoints; el frontal llama sin errores de contrato
(nombres/anidados/enum-string); CORS permite 4200→8080; errores `{error, status}` con 400/404/409.

---

## 6. Frontend — `nova-frontend-gen` (Angular 17 + Material)

### 6.1 Modelos TypeScript (espejo del contrato §4.6)

```typescript
export type GuaranteeStatus = 'DRAFT' | 'ISSUED' | 'AMENDED' | 'CLAIMED' | 'EXPIRED' | 'CANCELLED';
export type GuaranteeType   = 'PERFORMANCE' | 'ADVANCE_PAYMENT' | 'BID_BOND' | 'WARRANTY';

export interface Party { firstName: string; lastName: string; email: string; }
export interface IssuingBank { name: string; bic: string; country: string; }

export interface Guarantee {
  id: number;
  reference: string;
  type: GuaranteeType;
  amount: number;
  currency: string;
  issueDate: string;
  expiryDate: string;
  status: GuaranteeStatus;
  applicant: Party;
  beneficiary: Party;
  issuingBank: IssuingBank;
}
```

### 6.2 `GuaranteeService` (HttpClient)

```typescript
@Injectable({ providedIn: 'root' })
export class GuaranteeService {
  private readonly base = `${environment.apiUrl}/api/v1/guarantees`;
  constructor(private http: HttpClient) {}

  list(filter?: { status?: GuaranteeStatus; type?: GuaranteeType }): Observable<Guarantee[]> {
    let params = new HttpParams();
    if (filter?.status) params = params.set('status', filter.status);
    if (filter?.type)   params = params.set('type', filter.type);
    return this.http.get<Guarantee[]>(this.base, { params });
  }
  getById(id: number): Observable<Guarantee> { return this.http.get<Guarantee>(`${this.base}/${id}`); }
  create(body: GuaranteeRequest): Observable<Guarantee> { return this.http.post<Guarantee>(this.base, body); }
  update(id: number, body: GuaranteeRequest): Observable<Guarantee> { return this.http.put<Guarantee>(`${this.base}/${id}`, body); }
  delete(id: number): Observable<void> { return this.http.delete<void>(`${this.base}/${id}`); }
  issue(id: number): Observable<Guarantee> { return this.http.post<Guarantee>(`${this.base}/${id}/issue`, {}); }
  addAmendment(id: number, body: AmendmentRequest): Observable<Amendment> {
    return this.http.post<Amendment>(`${this.base}/${id}/amendments`, body);
  }
}
```

### 6.3 Componentes a implementar

- **`GuaranteeListComponent`**: `MatTableDataSource` con columnas (reference, type, amount, currency,
  status, issueDate, expiryDate); badges color-codificados (`DRAFT`=azul, `ISSUED`=verde,
  `AMENDED`=naranja, `CLAIMED`=rojo, `EXPIRED`=gris); `MatSort`, `MatPaginator` (20/pág), filtros
  multiselección de status/type; click de fila → detalle.
- **`GuaranteeDetailComponent`**: pestañas `General` / `Amendments` / `Claims`; botones *Issue*
  (visible solo si `DRAFT`), *Edit* y *Delete* (habilitados solo si `DRAFT`).
- **`GuaranteeFormComponent`**: `ReactiveForms` con validación (`amount > 0`,
  `expiryDate > issueDate`, requeridos) → `create()` / `update()`; toast de éxito/error
  (`MatSnackBar`).
- **`AmendmentDialogComponent`**: `MatDialog` con `description`, `newAmount`, `newExpiryDate` →
  `addAmendment()` y refresco del detalle.

```typescript
// Validador cruzado de fechas (FormGroup)
export const expiryAfterIssue: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const issue = group.get('issueDate')?.value;
  const expiry = group.get('expiryDate')?.value;
  return issue && expiry && new Date(expiry) <= new Date(issue) ? { expiryBeforeIssue: true } : null;
};
```

**Aceptación frontend**: render sin errores de consola; formularios envían el shape exacto del
backend; crear/ver/emitir/enmendar desde la UI; responsive; TypeScript strict (sin `any`);
tests de service/componentes (>70%).

---

## 7. Release — `nova-release-mgr`

### 7.1 `Dockerfile` backend (multi-stage)

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 7.2 `docker-compose.local.yml`

```yaml
services:
  guarantees-service:
    build: ./guarantees-service
    ports: ["8080:8080"]
  guarantees-ui:
    build: ./guarantees-ui
    ports: ["4200:80"]
    depends_on: [guarantees-service]
```

### 7.3 Gate de validación post-generación

`nova-release-mgr` aplica la skill `nova-post-gen-validation` como **verificación final**: compila
backend y frontend, levanta `docker compose up`, comprueba `/swagger-ui.html` y la carga de la lista,
y solo entonces habilita el **approval gate** del merge a `main`.

---

## 8. Agentes en standby (modo demo)

```
· nova-async-comm  → eventos de ciclo de vida (ActiveMQ/SSE) si se quisiera notificación reactiva
· nova-ops-monitor → run-local.sh + healthchecks/observabilidad si la demo lo necesitara
```

Disponibles en el org chart; **no se activan** salvo que el flujo lo exija (gobernanza de scope).

---

## 9. Verificación end-to-end

**Funcional**
- [ ] `docker compose up --build` levanta backend + frontend sin errores.
- [ ] La lista carga 6 avales seed; crear uno nuevo (`POST`) → aparece en la lista.
- [ ] *Issue* cambia `DRAFT → ISSUED`; enmienda cambia a `AMENDED`.
- [ ] Filtro `?status=AMENDED&type=PERFORMANCE` devuelve solo lo esperado.
- [ ] Borrar un aval `AMENDED` → `400` controlado; 4ª enmienda → `400`.
- [ ] Swagger UI muestra los 9 endpoints; el frontal consume sin errores de contrato.

**Orquestación (Paperclip)**
- [ ] Objetivo + incidencia raíz; sub-tareas con goal ancestry y **≤5** sin cascada.
- [ ] Checkout atómico (un único asignado por tarea) y dependencias de bloqueo respetadas.
- [ ] `ISSUED` y merge a `main` pasaron por **approval gate**; decisión en el audit log.
- [ ] Coste por agente/goal visible dentro de presupuesto; work products adjuntos (Swagger, capturas,
      `docker-compose`); cada agente escribió ≥1 memoria (`lesson`/`pattern`/`decision`).

---

## 10. Capacidades de Paperclip que se ven durante el desarrollo

| Capacidad | Dónde se ve |
|-----------|-------------|
| 🎯 Goal alignment | Cada sub-tarea hereda el "por qué" del objetivo del aval |
| 🧩 Descomposición ≤5 | El architect crea el árbol de trabajo sin cascadas |
| 💓 Heartbeats | Los agentes despiertan y codifican solos, sin lanzarlos a mano |
| 🔒 Checkout atómico | Nunca dos agentes en la misma tarea |
| 🔗 Blocker dependencies | Frontend/Integración esperan al backend; Release a los tres |
| 🛡️ Approval gates | Emitir el aval y mergear requieren firma humana |
| 💰 Budgets | Coste por agente/modelo en vivo; auto-pausa al agotarse |
| 🧠 Memoria | Reutilizan lecciones (contrato DTO, `jakarta.*`) y escriben nuevas |
| 📚 Skill injection | Cargan `nova-cli-commands`, `nova-post-gen-validation` en runtime |
| 📦 Work products | Swagger, capturas de UI y `docker-compose` adjuntos al ticket |
| 🧾 Audit log | Toda mutación (commit, aprobación, coste, estado) queda trazada |
| 🔌 Bring your own agent | Claude Code + Codex + Antigravity bajo un mismo org chart |

---

## 11. Referencias

- **Org / agentes NOVA**: `QPaperClip/containers/nova-org/company/` (`COMPANY.md`, `.paperclip.yaml`, `agents/*/AGENTS.md`)
- **Skills NOVA**: `nova-cli-commands`, `nova-post-gen-validation`, `nova-repo-bootstrap`
- **Stack del repo CasoUsoNova**: Spring Boot 3.2.x (Java 17, `jakarta.*`) + Angular 17 (Material) + Docker
- **Normativa de negocio**: ICC URDG 758 (avales a primer requerimiento)
