# Demo 6: Tablero Kanban de avales con drag & drop y actualización en vivo

**Sprint de desarrollo**: Nueva vista de gestión visual del ciclo de vida
**Duración estimada**: 45 minutos (algo menos que la demo del dashboard)
**Complejidad**: Avanzado
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan** (delegación máxima, **toda la org NOVA**): Repo Provisioner (nova-repo-provisioner), Backend Service Generator (nova-service-gen), Frontend Generator (nova-frontend-gen), API Integration Expert (nova-api-integr), Async Communication Expert (nova-async-comm), Operations Monitor (nova-ops-monitor), Release Manager (nova-release-mgr)

> Demo de desarrollo sobre la plataforma ya entregada (tabla de avales, dashboard v2 con SSE y
> observabilidad **ya en `main`**). Añade una vista **`/board`** tipo Kanban donde cada aval es una
> tarjeta y cada columna un estado del ciclo de vida (URDG 758). El resultado es un **efecto muy
> visible en la UI**: de una tabla plana a un tablero interactivo con drag & drop y refresco en vivo.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo y lo descompone con delegación máxima**: crea **una sub-incidencia
> por cada agente de la organización** (con sus dependencias/blockers), de modo que **todos los agentes
> NOVA trabajan** — no queda ninguno en standby. El arquitecto revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

---

## 1. Objetivo

Añadir una vista **`/board`** con un **tablero Kanban** del ciclo de vida de los avales: columnas por
estado (`DRAFT`, `ISSUED`, `AMENDED`, `CLAIMED`, `EXPIRED`, `CANCELLED`), tarjetas arrastrables entre
columnas válidas (**drag & drop** con Angular CDK), **transiciones de estado validadas en el backend**,
y **refresco en vivo** reutilizando el canal SSE existente para que el tablero de otro usuario se
actualice solo al mover una tarjeta.

## 2. Contexto

La plataforma ya cuenta con la tabla de avales, el dashboard v2 (charts, filtros, SSE, Actuator) y el
gate de release. Esta demo es el siguiente escaparate de **delegación máxima**: una feature nueva,
acotada a ~45 minutos, donde cada agente aporta una pieza concreta y el efecto visual es inmediato
(tabla → tablero interactivo en tiempo real).

### Estado actual (ya en `main`)

- CRUD de avales con máquina de estados URDG 758 en el backend.
- Canal SSE de eventos de aval (`guarantee-events`) consumido por el dashboard.
- Actuator con `health`, `metrics` y `prometheus` expuestos.
- Angular Material 17 y `@angular/cdk` ya instalados (falta habilitar `DragDropModule` donde se use).

## 3. Alcance (por área)

### Toolchain (`nova-repo-provisioner`)

- [ ] Verificar que `@angular/cdk` está fijado en `package.json`/lockfile y que el build resuelve
      `@angular/cdk/drag-drop`; no se añaden librerías nuevas.

### Backend (`nova-service-gen`)

- [ ] Endpoint `PATCH /api/v1/guarantees/{id}/status` con body `{ "status": "..." }` que **valida la
      transición** contra la máquina de estados (403/422 si es inválida) y persiste el cambio.
- [ ] Exponer `GET /api/v1/guarantees/board` que devuelve los avales agrupados por estado
      (`{ "DRAFT": [...], "ISSUED": [...], ... }`). Tests de transiciones válidas e inválidas.

### Frontend (`nova-frontend-gen`)

- [ ] Vista `/board` con columnas por estado y tarjetas (referencia, beneficiario, importe+moneda,
      chip de tipo) usando **CDK DragDropModule** (`cdkDropListGroup`).
- [ ] Al soltar: llamada al `PATCH`; si el backend rechaza la transición, la tarjeta **vuelve a su
      columna** con un snackbar de error. Estados de carga/vacío y responsive.
- [ ] Enlace "Tablero" en la barra de navegación junto a "Dashboard".

### Integración / contrato (`nova-api-integr`)

- [ ] Documentar en OpenAPI el `PATCH` de estado (con la matriz de transiciones válidas como ejemplos)
      y el endpoint `board`; revalidar CORS.

### Tiempo real (`nova-async-comm`)

- [ ] Publicar el evento de **cambio de estado** en el canal SSE existente y hacer que la vista `/board`
      lo consuma: mover una tarjeta en una pestaña **mueve la tarjeta sola** en otra pestaña abierta.

### Observabilidad (`nova-ops-monitor`)

- [ ] Contador Micrometer `guarantees.status.transitions` (tags `from`, `to`, `result=ok|rejected`)
      visible en `/actuator/prometheus`.

### Release (`nova-release-mgr`)

- [ ] Verificar build + arranque Docker de ambos servicios y ejecutar el gate `nova-post-gen-validation`.

## 4. Aceptación

- `/board` muestra los avales agrupados por estado y permite arrastrar tarjetas entre columnas.
- Las transiciones inválidas se rechazan en backend y la UI revierte la tarjeta con feedback claro.
- Con dos pestañas abiertas, mover una tarjeta en una **actualiza la otra en vivo** vía SSE.
- `/actuator/prometheus` expone `guarantees_status_transitions_total` con sus tags.
- OpenAPI refleja los nuevos endpoints; sin errores en consola; TypeScript estricto sin `any`.
- Tests: transiciones de estado (backend, válidas e inválidas) y componente del tablero (render + drop con mock).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| Los estados solo se ven como chips en la tabla; cambiar un estado requiere entrar al detalle. | Tablero Kanban `/board` con drag & drop, validación de transiciones y sincronización en vivo entre pestañas. |

**Guion**: abrir `/board` → arrastrar un aval `DRAFT` a `ISSUED` y ver la tarjeta cambiar de columna →
intentar una transición inválida (p. ej. `EXPIRED` → `DRAFT`) y ver la tarjeta revertir con el aviso →
abrir una segunda pestaña, mover una tarjeta y verla **moverse sola** en la otra → abrir
`/actuator/prometheus` y enseñar el contador de transiciones.

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone con delegación
> máxima** y crea **una sub-incidencia por cada agente** de la org **NOVA** (`QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador, y **ningún agente queda en standby**.
> La org dispone de agentes reales para cada especialidad — Repo Provisioner, Backend Service Generator,
> Frontend Generator, API Integration Expert, Async Communication Expert, Operations Monitor y Release
> Manager — así que **anímate a delegar**: cada pieza va al especialista que le corresponde.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Verificar toolchain CDK drag-drop (versión fijada + lockfile) y que el build resuelve el módulo. | **nova-repo-provisioner** | Claude Code (local) | — |
| 2 | `PATCH /status` con validación de transiciones y `GET /board` agrupado por estado. Tests. | **nova-service-gen** | Codex (local) | — |
| 3 | Vista `/board` con columnas, tarjetas, drag & drop, revert en error y enlace de navegación. Tests front. | **nova-frontend-gen** | Claude Code (local) | #1, #2 |
| 4 | Documentar en OpenAPI el `PATCH` de estado y el endpoint `board`; revalidar CORS. | **nova-api-integr** | Claude Code (local) | #2 |
| 5 | Emitir el evento de cambio de estado por SSE y consumirlo en `/board` (sincronización entre pestañas). | **nova-async-comm** | Claude Code (local) | #2, #3 |
| 6 | Contador Micrometer de transiciones (`from`, `to`, `result`) expuesto en Prometheus. | **nova-ops-monitor** | Claude Code (local) | #2 |
| 7 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Claude Code (local) | #3, #4, #5, #6 |

> **Delegación máxima**: los 7 agentes ejecutores trabajan; `nova-architect` coordina, fija los contratos
> y aprueba. Ningún agente queda en standby.

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador hace `pull`, crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone con delegación máxima y **delega** las 7 sub-incidencias, fijando el contrato del `PATCH`, del `board` y del evento SSE.
3. **nova-repo-provisioner** verifica el toolchain y **nova-service-gen** implementa los endpoints (ambos sin bloqueo previo).
4. Con el contrato listo, **nova-frontend-gen**, **nova-api-integr** y **nova-ops-monitor** trabajan en paralelo; **nova-async-comm** cierra la sincronización en vivo sobre la vista.
5. **nova-release-mgr** valida y aplica el gate cuando convergen; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
