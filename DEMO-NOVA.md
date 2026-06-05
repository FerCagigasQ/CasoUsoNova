# Demo NOVA — 7 Agentes IA crean un Producto NOVA desde cero

> 7 agentes autónomos (Claude) colaboran **entre sí** en Paperclip para
> crear, desarrollar, desplegar y operar un producto NOVA completo.
>
> **Repositorio de trabajo**: https://github.com/FerCagigasQ/CasoUsoNova
> (vacío — los agentes lo construyen desde cero)

---

## Cómo funciona la colaboración en Paperclip

Antes de empezar: Paperclip no es solo un gestor de tareas. Es un sistema de
coordinación donde los agentes se comunican, delegan trabajo y reaccionan a
los resultados de otros agentes.

### Mecanismos de comunicación entre agentes

```
┌─────────────────────────────────────────────────────────────┐
│                    PAPERCLIP BOARD                           │
│                                                             │
│  1. ISSUES (tareas)                                         │
│     • Cada agente ve sus issues asignadas                   │
│     • Un agente puede crear sub-issues (parentId)           │
│       y asignarlas a otro agente (assigneeAgentId)          │
│     • requestDepth: 0 (CEO) → 1 (delegado) → 2 (sub-del.)  │
│                                                             │
│  2. COMENTARIOS (@mentions)                                 │
│     • Los agentes se comunican vía comentarios en issues    │
│     • @nombre-agente despierta al agente mencionado         │
│     • Markdown soportado, historial auditable               │
│                                                             │
│  3. HEARTBEATS (ciclos de actividad)                        │
│     • Cada agente se despierta periódicamente o por evento  │
│     • Al despertar: revisa issues → checkout → trabaja →    │
│       comenta resultado → cierra o delega                   │
│     • Se puede forzar wakeup manual desde la UI             │
│                                                             │
│  4. STATUS FLOW                                             │
│     backlog → todo → in_progress → in_review → done         │
│                   ↘ blocked (si hay dependencia)            │
│                                                             │
│  5. WORKSPACE (repo compartido)                             │
│     • Proyecto configurado con cwd apuntando al repo        │
│     • Cada issue → worktree aislado → rama → PR             │
│     • Todos los agentes pushean al mismo repo               │
└─────────────────────────────────────────────────────────────┘
```

### Flujo de delegación del Arquitecto

```
Arquitecto recibe issue raíz (requestDepth: 0)
    │
    ├── Crea sub-issue + asigna a Service-Gen (requestDepth: 1)
    ├── Crea sub-issue + asigna a Frontend-Gen (requestDepth: 1)
    ├── Crea sub-issue + asigna a API-Integr (requestDepth: 1)
    ├── Crea sub-issue + asigna a Async-Comm (requestDepth: 1)
    ├── Crea sub-issue + asigna a Release-Mgr (requestDepth: 1)
    └── Crea sub-issue + asigna a Ops-Monitor (requestDepth: 1)
          │
          └── @mention en comentario → despierta al agente
              → agente hace checkout → trabaja → comenta resultado
              → Arquitecto recibe notificación → revisa
```

---

## Contexto: ¿Qué es NOVA?

NOVA es la plataforma de desarrollo de BBVA que gestiona el ciclo de vida
completo de las aplicaciones: desde la creación en local con **NOVA Click**
(CLI) hasta el despliegue en producción a través del **portal NOVA**.

**Conceptos clave:**
- **UUAA**: Código de 4 letras que identifica una aplicación (ej: `GDPD`)
- **Producto**: Agrupa subsistemas bajo una UUAA. Tiene su landing en el portal.
- **Subsistema**: Repositorio Git que contiene uno o más servicios.
- **Servicio**: Unidad desplegable — API REST, Demonio, Batch, Scheduler, Frontal CDN.
- **Entornos**: Desarrollo (local) → Integrado (INT) → Preproducción (PRE) → Producción (PRO).
- **Release**: Versión del subsistema lista para desplegar (vX.Y.Z).
- **NOVA Click (CLI)**: Herramienta local — genera servicios, levanta runtime, valida, genera código cliente.

---

## Escenario de la demo

> **Producto**: "Gestión de Pedidos" — UUAA: `GDPD`
>
> Un equipo de 7 agentes IA va a desarrollar este producto **desde cero**
> en https://github.com/FerCagigasQ/CasoUsoNova. El Arquitecto recibe
> una única tarea inicial y orquesta todo el trabajo delegando a los
> demás agentes a través de Paperclip.

### Mapa de agentes (org chart)

```
nova-architect (Arquitecto — CEO)
│   Define arquitectura, delega trabajo, revisa resultados.
│   Reporta a: nadie (es el CEO)
│
├── nova-service-gen (Generador Backend)
│   │   Reporta a: Arquitecto
│   │   Crea servicios Java/Spring Boot con nova create api|demon|batch|scheduler
│   │
│   ├── nova-api-integr (Integración de APIs)
│   │   Reporta a: Service-Gen o Arquitecto
│   │   Genera código cliente, configura API Gateway y mocks
│   │
│   └── nova-async-comm (Comunicación Asíncrona)
│       Reporta a: Service-Gen o Arquitecto
│       Configura brokers, AsyncAPI back-to-back y back-to-front (SSE)
│
├── nova-frontend-gen (Generador Frontend)
│   Reporta a: Arquitecto
│   Crea frontales Angular/Thin3, integra componentes corporativos
│
├── nova-release-mgr (Release Manager)
│   Reporta a: Arquitecto
│   Crea releases, valida calidad/seguridad, gestiona despliegue INT→PRE→PRO
│
└── nova-ops-monitor (Operaciones)
    Reporta a: Arquitecto
    Monitoriza servicios, configura alertas, transferencias, eventos de logs
```

---

## Prerequisitos

- **Node.js** 18+ y **pnpm** instalados
- **PostgreSQL 17** en `localhost:5432` (usuario/BD: `paperclip/paperclip`)
- **Claude CLI** autenticado: `npm install -g @anthropic-ai/claude-code@latest` + `claude login`
- **Repo CosmosPaperClip** clonado con toolchain NOVA en `containers/nova-org/toolchain/nova-le/`

---

## PASO 0 — Levantar Paperclip y preparar el workspace

### 0.1 — Arrancar el servidor

#### Opción A — Local (pnpm dev)

```bash
# Terminal 1: Servidor Paperclip
cd CosmosPaperClip
DATABASE_URL="postgres://paperclip:paperclip@127.0.0.1:5432/paperclip" pnpm dev

# Terminal 2: Importar la company NOVA + configurar CLI para agentes
node containers/nova-org/scripts/import-nova-company.mjs
node containers/nova-org/scripts/configure-nova-env.mjs
```

#### Opción B — Docker Compose

```bash
cd CosmosPaperClip
BETTER_AUTH_SECRET=paperclip-dev-secret \
  docker compose -f containers/nova-org/docker-compose.nova.yml up --build

# Login Claude en el contenedor (solo primera vez):
docker exec -it -u node nova-org-paperclip-1 claude /login
```

#### Verificar

```bash
curl http://localhost:3100/api/health
# → {"status":"ok"}

curl -s http://localhost:3100/api/companies | jq '.[0].name'
# → "NOVA Development Team"

# Verificar los 7 agentes están activos:
curl -s http://localhost:3100/api/companies/{companyId}/agents | jq '.[].name'
```

### 0.2 — Configurar el workspace del proyecto con el repo CasoUsoNova

Antes de crear issues, hay que conectar el proyecto Paperclip con el repo
donde los agentes van a trabajar.

**Vía UI**: Ir a Proyecto **NOVA Bootstrap** → Settings → Workspaces → Add Workspace:
- **Name**: `CasoUsoNova`
- **cwd**: `/ruta/absoluta/a/CasoUsoNova` (donde clonaste el repo)
- **isPrimary**: `true`

**Vía API** (alternativa):
```bash
# Obtener el projectId
PROJECT_ID=$(curl -s http://localhost:3100/api/companies/{companyId}/projects | jq -r '.[0].id')

# Crear workspace apuntando al repo
curl -X POST http://localhost:3100/api/projects/$PROJECT_ID/workspaces \
  -H "Content-Type: application/json" \
  -d '{
    "name": "CasoUsoNova",
    "cwd": "/ruta/absoluta/a/CasoUsoNova",
    "isPrimary": true
  }'
```

> **Efecto**: Ahora cuando un agente hace checkout de un issue, Paperclip
> crea un worktree aislado del repo, el agente trabaja en su rama, y puede
> hacer push + PR a https://github.com/FerCagigasQ/CasoUsoNova.

### 0.3 — Habilitar heartbeats de los agentes

Los agentes solo trabajan si sus heartbeats están activos:

**Vía UI**: Para cada agente → Toggle **Heartbeat** ON.

**Vía API**:
```bash
# Para cada agente, habilitar su heartbeat con intervalo (ej: 300s = 5 min)
curl -X PATCH http://localhost:3100/api/agents/{agentId} \
  -H "Content-Type: application/json" \
  -d '{"heartbeatEnabled": true, "intervalSec": 300}'
```

> **Tip para la demo**: Puedes dejar los heartbeats deshabilitados y hacer
> **wakeup manual** de cada agente cuando quieras mostrarlo paso a paso.

---

## PASO 1 — Crear la tarea raíz y asignarla al Arquitecto

> **Objetivo**: Crear una **única issue raíz** que el Arquitecto va a
> descomponer y delegar a los demás agentes. Esto demuestra la cadena
> de delegación real de Paperclip.

### En la UI de Paperclip

1. Ir a **NOVA Bootstrap** → **Issues** → **New Issue**
2. Rellenar:

| Campo | Valor |
|-------|-------|
| **Title** | `Crear producto NOVA "Gestión de Pedidos" (GDPD) desde cero` |
| **Priority** | `critical` |
| **Assignee** | `Arquitecto NOVA` |
| **Status** | `todo` |

3. **Description** (copiar y pegar):

```
Eres el Arquitecto Principal del equipo NOVA. Tu misión es crear el producto
"Gestión de Pedidos" con UUAA GDPD desde cero en el repositorio:
https://github.com/FerCagigasQ/CasoUsoNova

Este repo está vacío. Debes diseñar la arquitectura completa y DELEGAR el
trabajo a tu equipo creando sub-issues en Paperclip.

== REGLA OBLIGATORIA PARA TODO EL EQUIPO ==
Cada agente DEBE trabajar en una rama propia (feature/gdpd-*) y al terminar
crear un Pull Request a main en https://github.com/FerCagigasQ/CasoUsoNova.
Ningún agente hace push directo a main (excepto tú para la estructura base).
Flujo: rama → commit → push → PR a main → review del Arquitecto.

== FASE 1: DISEÑO (tú directamente) ==

1. Clona https://github.com/FerCagigasQ/CasoUsoNova
2. Ejecuta `nova --version` para verificar el entorno (espera: NOVA 26.03, CLI 7.8.0)
3. Crea la estructura base del repo:
   - README.md describiendo el producto GDPD
   - docs/arquitectura.md con el diseño de subsistemas
   - nova.yml raíz del producto
4. Define los subsistemas:
   - gdpd-backend/ (API REST + Demonio + Batch + Scheduler)
   - gdpd-frontend/ (Frontal Thin3/Angular)
5. Push la estructura inicial a main

== FASE 2: DELEGACIÓN (crea sub-issues para tu equipo) ==

Para cada tarea, crea una sub-issue en Paperclip con:
- parentId: esta issue
- assigneeAgentId: el agente correspondiente
- status: "todo"
- Descripción detallada con lo que debe hacer

Tareas a delegar:

A) @nova-service-gen — "Crear servicios backend con NOVA CLI"
   - Ejecutar `nova create api` para gdpd-pedidos-api (Java 11, Spring Boot)
   - Ejecutar `nova create demon` para gdpd-event-processor
   - Ejecutar `nova create batch` + `nova create scheduler` para reportes
   - Implementar endpoints CRUD, consumidor de eventos, job de reportes
   - Crear rama feature/gdpd-backend, commitear todo, push
   - OBLIGATORIO: Crear Pull Request a main en 
     https://github.com/FerCagigasQ/CasoUsoNova
   - Pegar el link del PR en un comentario de la sub-issue

B) @nova-frontend-gen — "Crear frontal Thin3 con NOVA CLI"
   - Ejecutar `nova create frontal` para gdpd-pedidos-front
   - Diseñar vistas: lista pedidos, detalle, crear pedido
   - Componentes bbva-web-*: tabla, formulario, cards
   - Routing con lazy loading, proxy config para gateway local
   - Crear rama feature/gdpd-frontend, commitear todo, push
   - OBLIGATORIO: Crear Pull Request a main en 
     https://github.com/FerCagigasQ/CasoUsoNova
   - Pegar el link del PR en un comentario de la sub-issue

C) @nova-api-integr — "Generar código cliente y configurar integración"
   - Ejecutar `nova generate-api-code` desde el Swagger del API
   - Ejecutar `prepare-apis-generated.js` para el frontal
   - Configurar API Gateway local (:24000) y mock server
   - Documentar en docs/integracion.md
   - Crear rama feature/gdpd-integration, commitear todo, push
   - OBLIGATORIO: Crear Pull Request a main en 
     https://github.com/FerCagigasQ/CasoUsoNova
   - Pegar el link del PR en un comentario de la sub-issue

D) @nova-async-comm — "Configurar comunicación asíncrona"
   - Back-to-back: ActiveMQ/RabbitMQ, Spring Cloud Stream, canales
   - Back-to-front: SSE con SseEmitter + EventSource
   - AsyncAPI spec, DLQ para reintentos
   - Documentar en docs/async-communication.md
   - Crear rama feature/gdpd-async, commitear todo, push
   - OBLIGATORIO: Crear Pull Request a main en 
     https://github.com/FerCagigasQ/CasoUsoNova
   - Pegar el link del PR en un comentario de la sub-issue

E) @nova-release-mgr — "Preparar release v1.0.0 y plan de despliegue"
   - Ejecutar `nova validate` para todos los servicios
   - Crear plan de release con Quality Gate (SonarQube, cobertura, seguridad)
   - Documentar flujo INT → PRE → PRO con criterios de promoción
   - Documentar en docs/release-plan.md + docs/deploy-checklist.md
   - Crear rama feature/gdpd-release, commitear todo, push
   - OBLIGATORIO: Crear Pull Request a main en 
     https://github.com/FerCagigasQ/CasoUsoNova
   - Pegar el link del PR en un comentario de la sub-issue

F) @nova-ops-monitor — "Configurar monitorización y operaciones"
   - Verificar runtime: `nova runtime start all`, `nova runtime status`
   - Configurar Actuator: /health, /metrics, /info
   - Definir alertas, transferencias de ficheros, eventos de logs
   - Documentar en docs/operations.md
   - Crear rama feature/gdpd-ops, commitear todo, push
   - OBLIGATORIO: Crear Pull Request a main en 
     https://github.com/FerCagigasQ/CasoUsoNova
   - Pegar el link del PR en un comentario de la sub-issue

== FASE 3: REVISIÓN Y MERGE ==

Cuando cada agente termine y comente en su sub-issue con el link del PR:
1. Revisa el PR en https://github.com/FerCagigasQ/CasoUsoNova/pulls
2. Si el PR está bien → comenta "Aprobado. Mergea el PR." → cierra sub-issue
3. Si necesita cambios → comenta lo que falta → el agente se despierta y corrige
4. Cuando todas las 6 sub-issues estén en "done" y todos los PRs mergeados:
   → Cierra esta issue raíz
   → El repo main tiene el producto NOVA completo
```

4. Click **Create**

### Qué va a pasar

```
 ┌─────────────────────────────────────────────────────────────┐
 │  El Arquitecto se despierta (heartbeat o wakeup manual)     │
 │                                                             │
 │  1. Ve la issue NOV-1 asignada a él → checkout              │
 │  2. Clona el repo, crea estructura, push a main             │
 │  3. Crea 6 sub-issues via API:                              │
 │     POST /api/companies/{id}/issues                         │
 │     {                                                       │
 │       "title": "Crear servicios backend...",                │
 │       "parentId": "NOV-1",          ← sub-tarea             │
 │       "assigneeAgentId": "{id-service-gen}",                │
 │       "status": "todo",                                     │
 │       "priority": "high"                                    │
 │     }                                                       │
 │  4. Comenta en NOV-1: "He creado la estructura base y       │
 │     delegado 6 sub-issues al equipo. @nova-service-gen      │
 │     @nova-frontend-gen @nova-api-integr @nova-async-comm    │
 │     @nova-release-mgr @nova-ops-monitor — ya tenéis         │
 │     trabajo asignado."                                      │
 │  5. Los @mentions despiertan a los 6 agentes                │
 │                                                             │
 │  Resultado: 6 agentes reciben sus issues y se activan       │
 └─────────────────────────────────────────────────────────────┘
```

### Forzar el wakeup del Arquitecto (para la demo en vivo)

Si los heartbeats están deshabilitados, forzar manualmente:

**Vía UI**: Ir a Agents → Arquitecto NOVA → Click **Run Heartbeat**

**Vía API**:
```bash
curl -X POST http://localhost:3100/api/agents/{architectAgentId}/wakeup \
  -H "Content-Type: application/json" \
  -d '{"source": "on_demand", "reason": "demo_start"}'
```

> **En pantalla verás**: El Arquitecto haciendo checkout, ejecutando comandos,
> creando ficheros, haciendo push, y luego creando 6 issues en el board.

---

## PASO 2 — Los agentes reciben sus tareas y trabajan

> Una vez que el Arquitecto crea las sub-issues y hace @mention, los agentes
> se despiertan automáticamente (o manualmente si es demo controlada).
>
> **Cada agente**:
> 1. Ve su issue asignada → hace checkout (status: `in_progress`)
> 2. Trabaja: ejecuta NOVA CLI, escribe código, crea ficheros
> 3. Push a rama + crea PR en https://github.com/FerCagigasQ/CasoUsoNova
> 4. Comenta en su issue: "He completado la tarea. PR: {url}"
> 5. Pone la issue en `in_review`

### 2a — Service Generator (Backend)

**Issue creada por el Arquitecto**: `Crear servicios backend con NOVA CLI`

**Lo que hace el agente cuando se despierta**:
```
→ checkout de su issue
→ clona repo, crea rama feature/gdpd-backend
→ nova create api (gdpd-pedidos-api, UUAA GDPD, Java 11)
→ nova create demon (gdpd-event-processor)
→ nova create batch (gdpd-report-batch)
→ nova create scheduler (gdpd-report-scheduler)
→ implementa endpoints CRUD, consumidor JMS, job Spring Batch
→ git add, commit, push
→ crea PR a main en https://github.com/FerCagigasQ/CasoUsoNova
→ comenta en su issue: "Backend completo. PR: {url}. 
   @nova-architect review y merge.
   @nova-api-integr el Swagger está en gdpd-backend/swagger/ 
   para que generes el código cliente."
→ status → in_review
```

> **Comunicación inter-agente**: El Service-Gen avisa al API-Integr que
> el Swagger está listo, y al Arquitecto que puede revisar.

**Wakeup manual** (para demo controlada):
```bash
curl -X POST http://localhost:3100/api/agents/{serviceGenId}/wakeup \
  -H "Content-Type: application/json" \
  -d '{"source": "on_demand", "reason": "backend_services"}'
```

### 2b — Frontend Generator (Thin3)

**Issue creada por el Arquitecto**: `Crear frontal Thin3 con NOVA CLI`

**Lo que hace el agente**:
```
→ checkout, rama feature/gdpd-frontend
→ nova create frontal (gdpd-pedidos-front, Thin3, Angular 13)
→ vistas: lista (bbva-web-table), detalle (card), crear (form)
→ configuración menú en utils/configuration/
→ routing lazy load: /pedidos, /pedidos/:id, /pedidos/nuevo
→ proxy config para gateway :24000 y webseal :23000
→ push + crea PR a main en CasoUsoNova
→ comenta: "Frontal Thin3 completo. PR: {url}. 
   @nova-architect review y merge. 
   @nova-api-integr necesito el código cliente generado 
   desde el Swagger para conectar con el API."
→ status → in_review
```

> **Comunicación**: El Frontend-Gen pide al API-Integr que genere el
> código cliente TypeScript que necesita.

### 2c — API Integration Expert

**Issue**: `Generar código cliente y configurar integración`

**Lo que hace el agente** (depende del Swagger del Service-Gen):
```
→ checkout, rama feature/gdpd-integration
→ nova generate-api-code (desde swagger de gdpd-pedidos-api)
   → flavour spring.nova para backend
   → TypeScript para frontal en api-generated/
→ node prepare-apis-generated.js (npm install → ng build → install dist)
→ nova api-gateway add (registra ruta /SHIVA/GDPD/pedidos-api/v1/)
→ nova mock start -n <swagger-servidor> (mock REST para el frontal)
→ docs/integracion.md
→ push + crea PR a main en CasoUsoNova
→ comenta: "Integración lista. PR: {url}. Código cliente generado.
   @nova-frontend-gen ya puedes importar las libs de api-generated/.
   @nova-architect review y merge."
→ status → in_review
```

> **Comunicación**: Cuando el API-Integr termina, avisa al Frontend-Gen
> que el código cliente está listo para importar.

### 2d — Async Communication Expert

**Issue**: `Configurar comunicación asíncrona back-to-back y back-to-front`

```
→ checkout, rama feature/gdpd-async
→ Back-to-Back: config Spring Cloud Stream, AsyncAPI spec,
  canal gdpd.pedidos.eventos, DLQ con 3 reintentos
→ Back-to-Front: SseEmitter endpoint, EventSource en Angular
→ nova generate-api-code --async
→ docs/async-communication.md
→ push + crea PR a main en CasoUsoNova
→ comenta: "@nova-service-gen he añadido los publishers al API 
   y los listeners al demonio. Revisa que el binder es correcto.
   @nova-frontend-gen he generado el servicio SSE para Angular.
   @nova-architect review y merge."
→ status → in_review
```

> **Comunicación**: El Async-Comm se coordina con Service-Gen (backend)
> y Frontend-Gen (SSE client) porque toca código de ambos.

### 2e — Release Manager

**Issue**: `Preparar release v1.0.0 y plan de despliegue`

```
→ checkout, rama feature/gdpd-release
→ nova validate api, nova validate batch-scheduler
→ docs/release-plan.md: versión 1.0.0, quality gate, SonarQube
→ docs/deploy-checklist.md: pasos INT→PRE→PRO con criterios
→ push + crea PR a main en CasoUsoNova
→ comenta: "Plan de release listo. PR: {url}. 
   Todas las validaciones pasaron.
   @nova-architect review y merge.
   @nova-ops-monitor ya puedes configurar la monitorización 
   para el despliegue."
→ status → in_review
```

> **Comunicación**: Avisa al Ops-Monitor que está listo para monitorizar.

### 2f — Operations Monitor

**Issue**: `Configurar monitorización y operaciones`

```
→ checkout, rama feature/gdpd-ops
→ nova runtime start all, nova runtime status
→ Actuator config: /health, /metrics, /info
→ Alertas, transferencias ficheros (Xcom/ConnectDirect), eventos logs
→ docs/operations.md
→ push + crea PR a main en CasoUsoNova
→ comenta: "Monitorización configurada. PR: {url}.
   @nova-architect review y merge. Todos los servicios health UP."
→ status → in_review
```

---

## PASO 3 — El Arquitecto revisa y cierra

> Cuando los agentes terminan y comentan con @nova-architect, el
> Arquitecto se despierta automáticamente (wakeup por @mention).

### Lo que hace el Arquitecto al despertar

```
1. Lee los comentarios de los 6 agentes (cada uno con link a su PR)
2. Para cada sub-issue:
   a. Revisa el PR en https://github.com/FerCagigasQ/CasoUsoNova/pulls
   b. Si OK → mergea el PR a main → comenta "PR mergeado. Buen trabajo @{agente}."
      → cambia status de la sub-issue a "done"
   c. Si necesita cambios → comenta en la sub-issue lo que falta
      → el agente se despierta y corrige → actualiza el PR
3. Cuando todas las 6 sub-issues están en "done" y todos los PRs en main:
   → Comenta en la issue raíz: "Producto GDPD completo. 
      Todos los PRs mergeados a main. 6 servicios + docs + config.
      Repo: https://github.com/FerCagigasQ/CasoUsoNova"
   → Cierra la issue raíz como "done"
```

> **Resultado final en GitHub**: La rama `main` de CasoUsoNova contiene
> todo el código generado por los 6 agentes, mergeado vía PRs revisados
> por el Arquitecto.

**Forzar el wakeup para la review** (si es demo controlada):
```bash
curl -X POST http://localhost:3100/api/agents/{architectAgentId}/wakeup \
  -H "Content-Type: application/json" \
  -d '{"source": "on_demand", "reason": "review_results"}'
```

---

## Flujo visual completo

```
   HUMANO                                      PAPERCLIP
   ──────                                      ─────────
     │
     │  Crea issue raíz NOV-1
     │  Asigna al Arquitecto
     │  ─────────────────────────────────────►  NOV-1 (todo)
     │                                          asignado: Arquitecto
     │                                               │
     │  [Wakeup manual o heartbeat]                  │
     │                                               ▼
     │                                    ┌─────────────────────┐
     │                                    │  ARQUITECTO         │
     │                                    │  • checkout NOV-1   │
     │                                    │  • clona repo       │
     │                                    │  • crea estructura  │
     │                                    │  • push a main      │
     │                                    │  • crea 6 sub-issues│
     │                                    │  • @mention a todos │
     │                                    └─────────┬───────────┘
     │                                              │
     │            ┌──────────┬──────────┬──────────┬┴─────────┬──────────┐
     │            ▼          ▼          ▼          ▼          ▼          ▼
     │     ┌───────────┐┌────────┐┌────────┐┌────────┐┌────────┐┌────────┐
     │     │SERVICE-GEN││FRONT-  ││API-    ││ASYNC-  ││RELEASE-││OPS-    │
     │     │           ││END-GEN ││INTEGR  ││COMM    ││MGR     ││MONITOR │
     │     │• checkout ││        ││        ││        ││        ││        │
     │     │• nova     ││• nova  ││• genera││• broker││• nova  ││• nova  │
     │     │  create   ││  create││  code  ││• SSE   ││  valid ││  runtime│
     │     │  api/     ││  front ││  client││• async ││• release││  status│
     │     │  demon/   ││• thin3 ││• gate- ││  API   ││  plan  ││• actuat│
     │     │  batch    ││• vistas││  way   ││        ││        ││• alerts│
     │     │• push+PR  ││• push  ││• mock  ││• push  ││• push  ││• push  │
     │     │• @mention ││  +PR   ││• push  ││  +PR   ││  +PR   ││  +PR   │
     │     │           ││• @ment ││  +PR   ││• @ment ││• @ment ││• @ment │
     │     └─────┬─────┘└───┬────┘└───┬────┘└───┬────┘└───┬────┘└───┬────┘
     │           │          │         │         │         │         │
     │           └──────────┴────┬────┴─────────┴────┬────┘         │
     │                           │                   │              │
     │                           ▼                   ▼              │
     │                  ┌─────────────────┐ ┌──────────────┐        │
     │                  │ COMUNICACIÓN    │ │ DEPENDENCIAS │        │
     │                  │ Service→API-Int │ │ Release→Ops  │        │
     │                  │ "Swagger listo" │ │ "Valida OK"  │        │
     │                  │ API-Int→Front   │ └──────────────┘        │
     │                  │ "Código listo"  │                         │
     │                  │ Async→Service   │                         │
     │                  │ "Publishers OK" │                         │
     │                  │ Async→Front     │                         │
     │                  │ "SSE service OK"│                         │
     │                  └─────────────────┘                         │
     │                                                              │
     │           ◄───── @nova-architect (en cada sub-issue) ◄───────┘
     │                           │
     │                           ▼
     │                  ┌─────────────────┐
     │                  │  ARQUITECTO     │
     │                  │  se despierta   │
     │                  │  • revisa PRs   │
     │                  │  • aprueba/pide │
     │                  │    cambios      │
     │                  │  • cierra issues│
     │                  └─────────────────┘
     │                           │
     │  ◄────────────────────────┘
     │  Issue raíz NOV-1: done
     │  Repo CasoUsoNova: completo con 6+ PRs
```

---

## Modos de ejecución de la demo

### Modo 1: Automático (heartbeats activos)

Habilitas los heartbeats de los 7 agentes y solo creas la issue raíz.
El Arquitecto se despierta solo, delega, los agentes trabajan en paralelo,
y se coordinan entre sí.

**Pros**: Demuestra la autonomía total.
**Contras**: Menos control, puede tomar tiempo.

```bash
# Habilitar heartbeat de todos los agentes (intervalo 120s para demo)
for AGENT_ID in {lista de 7 agentIds}; do
  curl -X PATCH http://localhost:3100/api/agents/$AGENT_ID \
    -H "Content-Type: application/json" \
    -d '{"heartbeatEnabled": true, "intervalSec": 120}'
done
```

### Modo 2: Controlado (wakeup manual paso a paso)

Heartbeats deshabilitados. Tú decides cuándo despertar a cada agente.
Ideal para presentaciones en vivo.

```
1. Creas la issue raíz → Wakeup Arquitecto → esperas a que termine
2. Ves las 6 sub-issues creadas en el board
3. Wakeup Service-Gen → esperas → ves el PR
4. Wakeup Frontend-Gen → esperas → ves el PR
5. Wakeup API-Integr → esperas → ves el PR
6. Wakeup Async-Comm → esperas → ves el PR
7. Wakeup Release-Mgr → esperas → ves el PR
8. Wakeup Ops-Monitor → esperas → ves el PR
9. Wakeup Arquitecto de nuevo → revisa y cierra todo
```

### Modo 3: Híbrido

Activas el heartbeat del Arquitecto y haces wakeup manual de los demás
cuando quieras mostrarlos. El Arquitecto va revisando conforme llegan.

---

## Detalles técnicos NOVA para los prompts

### Servicios y comandos CLI

| Servicio | Comando CLI | Descripción NOVA |
|----------|-------------|------------------|
| API REST | `nova create api` | Online, siempre levantado, Swagger, Eureka, Config Server |
| Demonio | `nova create demon` | Event-driven, sin endpoints HTTP, consume del broker |
| Batch | `nova create batch` | Spring Batch: Job → Step → Chunk (Reader/Processor/Writer) |
| Scheduler | `nova create scheduler` | Orquestador, scheduler.yml con cron, lanza batches |
| Frontal | `nova create frontal` | CDN, Angular/Thin3, componentes bbva-web-* |

### Runtime local (NOVA Click)

| Servicio | Puerto | Comando |
|----------|--------|---------|
| PostgreSQL | :5555 | `nova runtime start core` |
| API Gateway | :24000 | `nova runtime start core` |
| Config Server | :8888 | `nova runtime start core` |
| WebSeal Mock | :23000 | `nova runtime start core` |
| ActiveMQ | :8161 | `nova runtime start all` |
| CES Mock | :36000 | `nova runtime start all` |

### Datos técnicos

| Dato | Valor |
|------|-------|
| NOVA CLI | v7.8.0 (NOVA: 26.03) |
| Java | Zulu JDK 11.0.11 |
| Spring Boot | 2.7.18 |
| Angular | 12+ (generator-thin3 v7.5.0) |
| Node.js | 16+ (embebido en nova-le/nodejs/) |
| Maven | 3.8 |
| Nº agentes | 7 |
| Adapter | claude_local |

---

## Troubleshooting

| Problema | Solución |
|----------|----------|
| `nova: command not found` | Ejecutar `configure-nova-env.mjs` — parchea agentes con NOVA_HOME/PATH |
| Agente no se despierta | Verificar heartbeat activo o hacer wakeup manual |
| Agente no ve su issue | Verificar `assigneeAgentId` correcto al crear la sub-issue |
| Issue queda en `todo` | El agente necesita despertar para hacer checkout |
| Dos agentes en la misma issue | Imposible — checkout atómico lo previene |
| `claude login` expirado | Re-ejecutar `claude login` en terminal del servidor |
| PostgreSQL embedded crash | Usar PostgreSQL externo con `DATABASE_URL=...` |
| Docker `exec format error` | Necesitas Linux containers (WSL2/Hyper-V) |
| `local agent jwt secret missing` | Normal en modo `local_trusted`, no afecta |
| Agente no encuentra repo | Verificar workspace del proyecto tiene `cwd` correcto |
| @mention no despierta agente | Verificar que el nombre del agente es exacto |
