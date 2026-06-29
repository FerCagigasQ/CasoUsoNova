# Demo 1: Guarantee Lifecycle (Create → Issue → Amend)

> **Cómo leer esta demo**: este documento NO es una lista de tareas para que un humano las teclee.
> Es el **guion de una demo de orquestación**: muestra cómo un único **objetivo de negocio**
> entra por el tablero de **Paperclip** y la organización **NOVA** (8 agentes autónomos) lo
> ejecuta de principio a fin — con descomposición, heartbeats, checkout atómico, presupuestos,
> aprobaciones humanas, memoria compartida y auditoría — sin que nadie reparta trabajo a mano.

**Escenario**: Ciclo de vida de un aval bancario (CRUD + transiciones de estado)
**Duración de la presentación**: 30–40 min
**Lo que demuestra**: orquestación multi-agente de Paperclip sobre el stack NOVA (Spring Boot + Angular/Thin3)
**Org responsable**: NOVA Development Team (8 agentes) — gobernada desde un solo tablero

---

## 1. La idea en una frase

> "Quiero un servicio NOVA que gestione el ciclo de vida de un aval: crearlo en DRAFT,
> emitirlo (ISSUED), enmendarlo (AMENDED), con su frontal para operarlo, validando las
> transiciones de estado y con trazabilidad ICC URDG 758."

El operador **no** abre 9 tickets técnicos. Crea **un objetivo** y **una incidencia raíz**.
A partir de ahí, **Paperclip y la organización NOVA hacen el resto**. Esta demo enseña *cómo*.

---

## 2. La organización que ejecuta la demo (Org Chart de Paperclip)

Paperclip modela una **empresa**, no un pila de scripts: cada agente tiene jefe, cargo, permisos
y presupuesto. Estos son los 8 agentes que intervienen (todos visibles en el org chart del tablero):

| Agente | Cargo / Rol Paperclip | Reporta a | Adapter | Presupuesto/mes | Papel en la Demo 1 |
|--------|----------------------|-----------|---------|-----------------|--------------------|
| **nova-architect** | Arquitecto Principal / `ceo` | — (raíz) | Claude Code | 120 € | Recibe el objetivo, decide arquitectura, **descompone en ≤5 sub-tareas** y aprueba |
| **nova-repo-provisioner** | DevOps / Configurador de Repos | architect | Claude Code | 50 € | Bootstrap del repo: scripts, `.gitignore`, verificación del toolchain |
| **nova-service-gen** | Backend Expert / `engineer` | architect | Codex | 100 € | Genera el servicio API del aval: entidad, repos, endpoints, máquina de estados, seed |
| **nova-frontend-gen** | Frontend Expert / Thin3 | architect | Antigravity | 80 € | Genera el frontal Angular/Thin3: lista, detalle, formularios, diálogo de enmienda |
| **nova-api-integr** | Integration / API Gateway | service-gen | Antigravity | 70 € | Contrato front-back, Swagger/OpenAPI, CORS, manejo de errores |
| **nova-async-comm** | Messaging / Async | service-gen | Codex | 70 € | *(Standby)* eventos de ciclo de vida vía ActiveMQ/SSE — opcional, fuera de scope demo |
| **nova-release-mgr** | Release & Deploy | architect | Codex | 60 € | Dockerfile + `docker-compose` + gate de validación post-generación |
| **nova-ops-monitor** | Operations / Monitoring | release-mgr | Codex | 50 € | *(Standby)* salud local (`run-local.sh`, healthchecks) — opcional en modo demo |

> **Modo demo (directiva de la empresa)**: el arquitecto delega **como máximo 5 sub-tareas** por
> incidencia raíz y **no crea cascadas**. Por eso `nova-async-comm` y `nova-ops-monitor` quedan
> en *standby*: aparecen en el org chart y están disponibles, pero solo entran si el flujo lo
> exige. La demo enseña justo esto: **la gobernanza limita el scope automáticamente.**

---

## 3. Capacidades de Paperclip que esta demo pone en escena

Cada fila es algo que se **ve en pantalla** durante la presentación, no teoría:

| Capacidad Paperclip | Dónde se ve en la Demo 1 |
|---------------------|--------------------------|
| 🎯 **Goal alignment** | La incidencia raíz cuelga del objetivo "Ciclo de vida del aval"; cada sub-tarea hereda el *por qué* (goal ancestry visible en cada ticket) |
| 🧩 **Descomposición jerárquica** | El architect crea ≤5 sub-tareas con `parentId` → árbol de trabajo navegable |
| 💓 **Heartbeats** | Los agentes despiertan por schedule y por evento (asignación, @mención); no hay que lanzarlos a mano |
| 🔒 **Checkout atómico / single-assignee** | Dos agentes nunca cogen la misma tarea; lock de ejecución visible en el estado del ticket |
| 🔗 **Blocker dependencies** | "Frontal" se marca *blocked by* "API"; "Integración" *blocked by* "API". El tablero respeta el orden |
| 🛡️ **Governance & approvals** | La emisión del aval (DRAFT→ISSUED) y el merge a `main` pasan por **approval gate** del operador |
| 💰 **Budget & cost control** | Coste por agente/tarea/modelo en vivo; si un agente agota su presupuesto, **se auto-pausa** |
| 🧠 **Memoria de agentes** | Cada agente busca memorias previas (p. ej. "contrato DTO front-back") y escribe `lesson`/`pattern`/`decision` al terminar |
| 📚 **Runtime skill injection** | Los agentes cargan skills NOVA (`nova-cli-commands`, `nova-post-gen-validation`…) en tiempo de ejecución, sin reentrenar |
| 🗂️ **Workspaces aislados** | Cada run trabaja en su worktree/branch; el frontal expone **preview URL** del runtime |
| 📦 **Work products / artefactos** | El Swagger generado, capturas de la UI y el `docker-compose` se adjuntan como work products inspeccionables |
| 🕓 **Routines & schedules** | *(Opcional)* una rutina cron puede re-validar el contrato Swagger cada noche y abrir incidencia si rompe |
| 🧾 **Activity & audit log** | Toda mutación (checkout, comentario, aprobación, coste, cambio de estado) queda en log inmutable |
| 🏢 **Multi-company isolation** | NOVA es una empresa entre varias en el mismo despliegue; datos y auditoría aislados |
| 🔌 **Bring your own agent** | Conviven adapters distintos: Claude Code, Codex y Antigravity bajo un mismo org chart |

---

## 4. Flujo de la demo, paso a paso

Cada paso indica **qué hace el operador**, **qué hace la organización** y **qué señalar en la UI de Paperclip**.

### Paso 0 — Preparación (antes de la demo)
- La empresa **NOVA Development Team** está importada en Paperclip (`containers/nova-org`), con los 8 agentes, presupuestos y skills cargados.
- El secreto `COSMOS_CLI_TOKEN` y `NOVA_HOME` están provisionados (inputs declarados por agente).
- **Señalar**: org chart con los 8 agentes; presupuesto mensual de la empresa (600 €) y por agente.

### Paso 1 — El operador define el objetivo y la incidencia raíz
- Crear **Goal**: *"Ciclo de vida del aval bancario (Create → Issue → Amend)"*.
- Crear **Issue raíz** colgando del goal con el requisito en lenguaje de negocio (la frase de §1).
- Asignar la raíz a **nova-architect**.
- **Señalar**: cómo el ticket muestra la *goal ancestry* (el "por qué" viaja con la tarea).

### Paso 2 — El architect despierta (heartbeat) y descompone
- En su heartbeat, `nova-architect` hace checkout atómico de la raíz, **revisa el board para no duplicar**, decide el mínimo viable (1 API + 1 frontal) y crea **≤5 sub-tareas**:
  1. **Bootstrap repo** → `nova-repo-provisioner`
  2. **API del aval** (entidad, estados, endpoints, seed) → `nova-service-gen`
  3. **Frontal del aval** (lista, detalle, formularios, enmienda) → `nova-frontend-gen` · *blocked by* #2
  4. **Contrato + Swagger + CORS** → `nova-api-integr` · *blocked by* #2
  5. **Dockerización + gate de validación** → `nova-release-mgr` · *blocked by* #2,#3,#4
- **Señalar**: el árbol padre→hijos, las **dependencias de bloqueo**, y que **no** se generó una cascada de 9 tickets (gobernanza de modo demo en acción).

### Paso 3 — Los agentes ejecutan por heartbeat, en paralelo donde se puede
- `nova-repo-provisioner` prepara el repo (skill `nova-repo-bootstrap`) → desbloquea al resto.
- `nova-service-gen` genera la API con NOVA CLI (`nova create-service` tipo API, `nova generate-api-code`), implementa la **máquina de estados** y datos seed; aplica el gate `nova-post-gen-validation`.
- En cuanto la API existe, `nova-frontend-gen` y `nova-api-integr` arrancan **en paralelo** (sus bloqueos se levantan).
- **Señalar**: checkout atómico (nadie pisa a nadie), coste creciendo por agente/modelo, agentes consultando **memoria** ("¿cómo casamos los DTO con el front?").

### Paso 4 — Approval gate: emitir el aval y mergear
- La transición **DRAFT → ISSUED** y el **merge a `main`** están bajo política de aprobación.
- El operador revisa el work product (Swagger + capturas de UI) y **aprueba** (o rechaza, con rollback).
- **Señalar**: la cola de aprobaciones; nada llega a `main` sin firma; decisión registrada en el audit log.

### Paso 5 — Verificación y cierre
- `nova-release-mgr` aporta `docker-compose.local.yml`; el operador hace `docker compose up` y abre el **preview URL** del frontal.
- Recorrido funcional (ver §6) y cada agente escribe **memorias** (`lesson`, `pattern`, `milestone`).
- **Señalar**: coste total de la demo agregado por agente/goal; audit log completo de extremo a extremo.

---

## 5. Mapa de agentes → trabajo (resumen)

```
Goal: Ciclo de vida del aval
└─ Issue raíz (nova-architect)               [governance: approval para ISSUED y merge]
   ├─ #1 Bootstrap repo        → nova-repo-provisioner
   ├─ #2 API del aval          → nova-service-gen        (skill: nova-post-gen-validation)
   ├─ #3 Frontal del aval      → nova-frontend-gen        [blocked by #2]
   ├─ #4 Contrato + Swagger    → nova-api-integr          [blocked by #2]
   └─ #5 Docker + gate final   → nova-release-mgr         [blocked by #2,#3,#4]

Standby (disponibles, no activados en modo demo):
   · nova-async-comm   → eventos de ciclo de vida (ActiveMQ/SSE) si se quisiera notificación reactiva
   · nova-ops-monitor  → run-local.sh + healthchecks si la demo necesitara observabilidad
```

---

## 6. Entregables técnicos (qué produce la organización)

El contenido de negocio sigue siendo el ciclo de vida del aval. Estos son los entregables que
los agentes deben dejar funcionando; el detalle de campos/endpoints es el contrato que el
**gate `nova-post-gen-validation`** verifica antes de cualquier push.

### Backend — `nova-service-gen`
- Entidad `Guarantee` (`reference` UNIQUE, `type`, `amount`, `currency`, `issueDate`, `expiryDate`, `status`, FKs a `Applicant`/`Beneficiary`/`IssuingBank`) + entidades `Amendment` y `Claim`.
- Enums `GuaranteeStatus` (DRAFT, ISSUED, AMENDED, CLAIMED, EXPIRED, CANCELLED) y `GuaranteeType` (PERFORMANCE, ADVANCE_PAYMENT, BID_BOND, WARRANTY).
- Endpoints REST `/api/v1/guarantees`: `GET` (lista + filtros `?status=&type=`), `GET /{id}`, `POST` (auto-DRAFT), `PUT /{id}`, `DELETE /{id}` (solo DRAFT), `POST /{id}/issue` (DRAFT→ISSUED), `POST /{id}/amendments` (→AMENDED).
- **Máquina de estados**: solo ISSUED puede enmendarse/reclamarse; máx. 3 enmiendas; validar `amount>0`, `expiryDate>issueDate`, partes no nulas; 400/404 según corresponda.
- `DataSeeder` con 6 avales en estados variados + 3 applicants/beneficiaries/issuingBanks.
- DTOs con nombres EXACTOS del contrato (`reference`, `issueDate`, `expiryDate`; objetos anidados, no IDs).

### Frontend — `nova-frontend-gen`
- **Lista**: tabla (reference, type, amount, currency, status, fechas), badges por estado/tipo, orden, paginación, filtros multiselección, click→detalle.
- **Detalle**: pestañas General / Amendments / Claims; botones *Issue* (solo DRAFT), *Edit* (solo DRAFT), *Delete* (solo DRAFT).
- **Formulario** crear/editar con validación (`amount>0`, `expiryDate>issueDate`) → `POST`/`PUT`.
- **Diálogo de enmienda** (description, newAmount, newExpiryDate) → `POST /{id}/amendments` + refresco.
- Angular Material (tema indigo-pink), responsive, `GuaranteeService` con `HttpClient`.

### Integración — `nova-api-integr`
- Swagger/OpenAPI 3.0 (`/swagger-ui.html`, `/v3/api-docs`) con parámetros, bodies, respuestas y ejemplos (`BG-2026-001`, `EUR`, `50000`).
- Validación de contrato campo a campo (nombres exactos, objetos anidados, enums como string).
- CORS (4200→8080), errores consistentes (`{error, status}`: 400/404/409).

### Release — `nova-release-mgr`
- `Dockerfile` + `docker-compose.local.yml`; aplica el **gate de validación post-generación** como verificación final antes de habilitar el merge.

---

## 7. Guion de presentación (puntos de conversación)

1. **"Esto parece un gestor de tareas"** → y por debajo hay org chart, presupuestos y gobernanza.
2. **Un objetivo, no nueve tickets** → enseñar la descomposición automática del architect.
3. **Heartbeats vs. babysitting** → nadie lanza agentes a mano; despiertan solos y se coordinan.
4. **Checkout atómico** → por qué nunca hay doble trabajo ni contexto perdido entre reinicios.
5. **Approval gate** → la emisión del aval y el merge requieren firma humana; rollback disponible.
6. **Coste en vivo** → presupuesto por agente; auto-pausa al agotarse (sin sustos de tokens).
7. **Memoria compartida** → el equipo "aprende" entre demos (lecciones de contrato DTO, Jakarta/javax…).
8. **Bring your own agent** → Claude Code + Codex + Antigravity bajo un mismo org chart.
9. **Auditoría** → recorrer el activity log de extremo a extremo: todo está explicado y trazado.

---

## 8. Checklist de verificación

**Orquestación (Paperclip)**
- [ ] El objetivo y la incidencia raíz existen; las sub-tareas heredan goal ancestry.
- [ ] El architect creó **≤5 sub-tareas** con dependencias de bloqueo correctas (sin cascada).
- [ ] Cada tarea tuvo **un único** asignado (checkout atómico) y log de actividad.
- [ ] La transición ISSUED y el merge pasaron por **approval gate**.
- [ ] Coste por agente/goal visible; ningún agente superó su presupuesto sin auto-pausa.
- [ ] Hay **work products** adjuntos (Swagger, capturas UI, docker-compose).
- [ ] Cada agente escribió al menos una **memoria** (`lesson`/`pattern`/`decision`).

**Funcional (la demo arranca y se opera)**
- [ ] `docker compose up` levanta backend + frontend sin errores.
- [ ] La lista carga 6 avales seed; crear uno nuevo → aparece en la lista.
- [ ] *Issue* cambia DRAFT→ISSUED; enmienda cambia ISSUED→AMENDED.
- [ ] Filtro `status=AMENDED` devuelve solo el enmendado; borrar un AMENDED → error controlado.
- [ ] Swagger UI muestra todos los endpoints; el frontal consume sin errores de contrato.

---

## 9. Definition of Done (Demo 1)

La demo está lista cuando, **desde un único objetivo en el tablero**, la organización NOVA
entrega el ciclo de vida del aval funcionando (`docker compose up`), con la emisión y el merge
**aprobados por el operador**, coste dentro de presupuesto, work products adjuntos, memorias
escritas y el **audit log completo** que explica quién hizo qué, cuándo y por qué.

---

## 10. Referencias

- **Org / agentes NOVA**: `QPaperClip/containers/nova-org/company/` (`COMPANY.md`, `.paperclip.yaml`, `agents/*/AGENTS.md`)
- **Skills NOVA**: `skills/nova-cli-commands`, `nova-post-gen-validation`, `nova-repo-bootstrap`
- **Capacidades Paperclip**: org chart, heartbeats, checkout atómico, approvals, budgets, memoria, routines, work products, audit (ver `QPaperClip/README.md` y `doc/GOAL.md`)
- **Stack NOVA**: Spring Boot 2.7.x + Angular 12+/Thin3 + Docker; NOVA CLI 7.8.0
- **Normativa de negocio**: ICC URDG 758 (avales a primer requerimiento)
```