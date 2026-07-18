# Demo 4: Dashboard de KPIs — v2 (mejora con delegación máxima)

**Sprint de desarrollo**: Evolución del dashboard analítico ya existente
**Duración estimada**: 5-6 horas
**Complejidad**: Avanzado
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan** (delegación máxima, **toda la org NOVA**): Repo Provisioner (nova-repo-provisioner), Backend Service Generator (nova-service-gen), Frontend Generator (nova-frontend-gen), API Integration Expert (nova-api-integr), Async Communication Expert (nova-async-comm), Operations Monitor (nova-ops-monitor), Release Manager (nova-release-mgr)

> Demo de desarrollo **sobre código ya entregado**. La v1 del dashboard (`/dashboard` con tarjetas KPI y
> gráficas SVG hechas a mano, alimentadas por `GET /api/v1/metrics`) **ya está en `main`**. Esta v2 lo
> **mejora**; el resultado sigue siendo un **efecto visible en la UI**. No cambia la lógica de negocio de avales.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo y lo descompone con delegación máxima**: crea **una sub-incidencia
> por cada agente de la organización** (con sus dependencias/blockers), de modo que **todos los agentes
> NOVA trabajan** — no queda ninguno en standby. El arquitecto revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

---

## 1. Objetivo

Partiendo del dashboard ya implementado, **mejorarlo** hasta convertirlo en un panel analítico de nivel
producto: **filtros por rango de fechas**, **gráficas con librería real** (no SVG a mano), **métricas
nuevas** (importe agregado por moneda y top beneficiarios), **actualización en vivo** cuando cambian los
datos, y **observabilidad** del endpoint de métricas.

## 2. Contexto

La v1 demostró la cadena full-stack básica. La v2 es el escaparate de **delegación máxima**: cada agente
de la organización NOVA aporta una mejora concreta sobre el mismo objetivo, coordinados por el arquitecto.
Es el caso ideal para ver a **todos** los agentes trabajando en paralelo sobre una base existente, con
dependencias reales entre sus entregas.

### Estado actual (v1, ya en `main`)

- `GET /api/v1/metrics` devuelve `{ total, byStatus, byType, byMonth }` (agregados con `group by`).
- Vista `/dashboard` con tarjetas KPI y donut/barras dibujados con **SVG calculado a mano** en el componente.
- Sin filtros, sin auto-refresco, sin librería de charts, sin instrumentación del endpoint.

## 3. Alcance (mejoras por área)

### Toolchain (`nova-repo-provisioner`)

- [ ] Añadir al frontend una **librería de charts** mantenida (`ngx-charts` o `chart.js` + `ng2-charts`),
      fijando versión en `package.json` y actualizando el lockfile; verificar que el build la resuelve.

### Backend (`nova-service-gen`)

- [ ] Ampliar `GET /api/v1/metrics` con **parámetros de filtro** `?from=YYYY-MM-DD&to=YYYY-MM-DD`.
- [ ] Añadir agregados nuevos: `totalAmountByCurrency` y `topBeneficiaries` (top 5 por nº de avales).
- [ ] Cachear el resultado (`@Cacheable`) e invalidar al crear/modificar un aval. Tests de integración.

### Frontend (`nova-frontend-gen`)

- [ ] Sustituir las gráficas SVG a mano por la **librería de charts** (barras por mes, donut por estado y tipo).
- [ ] Añadir un **selector de rango de fechas** que recarga las métricas filtradas.
- [ ] Mostrar las tarjetas nuevas (importe por moneda, top beneficiarios) y mantener estados carga/vacío/responsive.

### Integración / contrato (`nova-api-integr`)

- [ ] Documentar en OpenAPI los nuevos parámetros y campos de respuesta, con ejemplos; revalidar CORS.

### Tiempo real (`nova-async-comm`)

- [ ] Publicar un **evento** al crear/modificar/eliminar un aval y exponer un canal **SSE/WebSocket** que el
      dashboard consuma para **refrescar las métricas en vivo** (sin recargar la página).

### Observabilidad (`nova-ops-monitor`)

- [ ] Instrumentar el endpoint de métricas con **Micrometer/Actuator** (contador de peticiones, latencia) y
      exponer `/actuator/health` y `/actuator/prometheus`; añadir un panel/healthcheck verificable.

### Release (`nova-release-mgr`)

- [ ] Verificar build + arranque Docker de ambos servicios y ejecutar el gate `nova-post-gen-validation`.

## 4. Aceptación

- `/dashboard` usa una librería de charts real; el filtro de fechas recarga y las cifras cuadran con la tabla.
- Aparecen las métricas nuevas (importe por moneda, top beneficiarios).
- Al crear/cambiar un aval, el dashboard **se actualiza en vivo** vía el canal de eventos.
- El endpoint expone métricas de observabilidad (`/actuator/prometheus`) y healthcheck verde.
- OpenAPI refleja los nuevos parámetros/campos; sin errores en consola; TypeScript estricto sin `any`.
- Tests: endpoint con filtros y agregados nuevos (integración) y componente de dashboard (render con mock).

## 5. Demostración (estado inicial → final)

| Antes (v1) | Después (v2) |
|------------|--------------|
| Gráficas SVG a mano, sin filtros, sin tiempo real ni observabilidad. | Charts de librería, filtro por fechas, métricas nuevas, refresco en vivo al cambiar datos y endpoint instrumentado. |

**Guion**: abrir `/dashboard` → aplicar un rango de fechas y ver recalcular → mostrar las tarjetas nuevas →
en otra pestaña crear un aval → ver el dashboard **actualizarse solo** → abrir `/actuator/prometheus` para
enseñar la métrica del endpoint.

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone con delegación
> máxima** y crea **una sub-incidencia por cada agente** de la org **NOVA** (`QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador, y **ningún agente queda en standby**.
> La org dispone de agentes reales para cada especialidad — Repo Provisioner, Backend Service Generator,
> Frontend Generator, API Integration Expert, Async Communication Expert, Operations Monitor y Release
> Manager — así que **anímate a delegar**: cada pieza va al especialista que le corresponde.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Añadir la librería de charts al frontend (versión fijada + lockfile) y verificar el build. | **nova-repo-provisioner** | Claude Code (local) | — |
| 2 | Ampliar `GET /api/v1/metrics` con filtros `from/to`, `totalAmountByCurrency`, `topBeneficiaries` y caché. Tests. | **nova-service-gen** | Codex (local) | — |
| 3 | Migrar las gráficas a la librería, añadir selector de rango de fechas y las tarjetas nuevas. Tests front. | **nova-frontend-gen** | Claude Code (local) | #1, #2 |
| 4 | Documentar en OpenAPI los nuevos parámetros/campos y revalidar CORS. | **nova-api-integr** | Claude Code (local) | #2 |
| 5 | Publicar eventos de cambio de aval y exponer canal SSE/WebSocket para refresco en vivo del dashboard. | **nova-async-comm** | Claude Code (local) | #2 |
| 6 | Instrumentar el endpoint con Micrometer/Actuator y exponer `health` + `prometheus`. | **nova-ops-monitor** | Claude Code (local) | #2 |
| 7 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Claude Code (local) | #3, #4, #5, #6 |

> **Delegación máxima**: los 7 agentes ejecutores trabajan; `nova-architect` coordina, fija los contratos
> y aprueba. Ningún agente queda en standby.

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador hace `pull` (la v1 ya está en `main`), crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone con delegación máxima y **delega** las 7 sub-incidencias, fijando el contrato del endpoint y del canal de eventos.
3. **nova-repo-provisioner** prepara el toolchain de charts; **nova-service-gen** amplía el endpoint (ambos sin bloqueo previo).
4. Con el contrato listo, **nova-frontend-gen**, **nova-api-integr**, **nova-async-comm** y **nova-ops-monitor** trabajan en paralelo sus mejoras.
5. **nova-release-mgr** valida y aplica el gate cuando convergen; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
