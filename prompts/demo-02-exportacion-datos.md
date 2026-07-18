# Demo 2: Exportación a CSV / Excel con generación asíncrona y notificación en vivo

**Sprint de desarrollo**: Exportación de datos full-stack con job asíncrono
**Duración estimada**: 45-60 minutos (algo menos que la demo del dashboard)
**Complejidad**: Avanzado
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan** (delegación máxima, **toda la org NOVA**): nova-repo-provisioner, nova-service-gen, nova-frontend-gen, nova-api-integr, nova-async-comm, nova-ops-monitor, nova-release-mgr

> Demo de desarrollo sobre la plataforma ya entregada (dashboard v2 con SSE y observabilidad **ya en
> `main`**). El resultado es un **efecto muy visible en la UI**: botón "Exportar" con menú CSV/Excel,
> generación **asíncrona** del Excel con **notificación en vivo** cuando está listo, y descarga.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo y lo descompone con delegación máxima**: crea **una sub-incidencia
> por cada agente de la organización** (con sus dependencias/blockers), de modo que **todos los agentes
> NOVA trabajan** — no queda ninguno en standby. El arquitecto revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

---

## 1. Objetivo

Permitir **exportar la tabla de avales** a **CSV** (inmediato, en cliente) y **Excel (.xlsx)**
(generado en backend como **job asíncrono**), respetando filtros y orden aplicados. Al terminar el job,
el usuario recibe una **notificación en vivo** (SSE) con el enlace de descarga, y el proceso queda
**instrumentado** en observabilidad.

## 2. Contexto

La exportación es una mejora clásica de plataforma; elevarla a un flujo asíncrono con notificación en
tiempo real la convierte en un escaparate de **delegación máxima**: generación de ficheros en front y
back, un mini-flujo de jobs, contrato OpenAPI, evento SSE y métricas.

### Estado actual (ya en `main`)

- Tabla de avales con filtros y orden; sin exportación.
- Canal SSE de eventos (`guarantee-events`) y Actuator con `health`, `metrics` y `prometheus`.

## 3. Alcance (por área)

### Toolchain (`nova-repo-provisioner`)

- [ ] Añadir **Apache POI** al `pom.xml` del backend con versión fijada y verificar que el build Maven
      la resuelve; sin librerías nuevas en frontend.

### Backend (`nova-service-gen`)

- [ ] `POST /api/v1/guarantees/export` (body: `format=xlsx` + filtros) que crea un **job asíncrono**
      (`@Async`) y devuelve `202` con `{ jobId }`.
- [ ] `GET /api/v1/guarantees/export/{jobId}` que devuelve el estado o el `.xlsx`
      (`Content-Disposition: attachment`) cuando está listo. Tests de integración.

### Frontend (`nova-frontend-gen`)

- [ ] Botón `Exportar` (menú `mat-menu`: "CSV" / "Excel") en la cabecera de la lista.
- [ ] **CSV** en cliente a partir de las filas visibles (UTF-8 con BOM, cabeceras legibles), descarga
      con `Blob`.
- [ ] **Excel**: lanzar el job, mostrar progreso (spinner/chip "generando…") y descargar al recibir la
      notificación. Estados de carga/error y toasts.

### Integración / contrato (`nova-api-integr`)

- [ ] Documentar en OpenAPI el flujo `POST` (202 + `jobId`) y el `GET` (estados y binario) con ejemplos;
      revalidar CORS para la descarga.

### Tiempo real (`nova-async-comm`)

- [ ] Publicar el evento **`export-ready`** (con `jobId`) en el canal SSE existente cuando el job
      termina; la UI lo consume para mostrar la notificación y habilitar la descarga sin sondear.

### Observabilidad (`nova-ops-monitor`)

- [ ] Métricas Micrometer `exports.jobs` (tags `format`, `result=ok|error`) y timer de duración del job,
      visibles en `/actuator/prometheus`.

### Release (`nova-release-mgr`)

- [ ] Verificar build + arranque Docker de ambos servicios y ejecutar el gate `nova-post-gen-validation`.

## 4. Aceptación

- El CSV descarga al instante lo que se ve (filtros/orden respetados) y abre bien en Excel/LibreOffice.
- El Excel se genera en un job asíncrono: la UI muestra "generando…" y, al llegar el evento SSE, ofrece
  la descarga **sin recargar ni sondear**; el `.xlsx` contiene las mismas filas que la tabla filtrada.
- `/actuator/prometheus` expone `exports_jobs_total` y el timer de duración.
- OpenAPI refleja el flujo completo; sin errores en consola; TypeScript estricto sin `any`.
- Tests: job y endpoint de Excel (integración back) y generación CSV + flujo de notificación (front con mock).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| La lista no ofrece exportación. | Botón "Exportar": CSV instantáneo; Excel con job asíncrono, aviso en vivo al estar listo y descarga con las filas filtradas. |

**Guion**: abrir la lista → aplicar un filtro → "Exportar → CSV" y abrir el fichero → "Exportar → Excel"
→ ver el chip "generando…" → llega la **notificación en vivo** → descargar y abrir el `.xlsx` → abrir
`/actuator/prometheus` y enseñar las métricas del job.

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone con delegación
> máxima** y crea **una sub-incidencia por cada agente** de la org **NOVA** (`QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador, y **ningún agente queda en standby**.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Añadir Apache POI al `pom.xml` (versión fijada) y verificar el build Maven. | **nova-repo-provisioner** | Claude Code | — |
| 2 | Job asíncrono de exportación (`POST` 202 + `GET` estado/binario) reutilizando filtros y DTOs. Tests. | **nova-service-gen** | Codex | #1 |
| 3 | Botón/menú de exportación, CSV en cliente, flujo de job con progreso y descarga. Tests front. | **nova-frontend-gen** | Antigravity | #2 |
| 4 | Documentar el flujo de exportación en OpenAPI y revalidar CORS. | **nova-api-integr** | Antigravity | #2 |
| 5 | Emitir `export-ready` por SSE al terminar el job y consumirlo en la UI (notificación + descarga). | **nova-async-comm** | Codex | #2, #3 |
| 6 | Métricas Micrometer de jobs de exportación (contador + timer) en Prometheus. | **nova-ops-monitor** | Codex | #2 |
| 7 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Codex | #3, #4, #5, #6 |

> **Delegación máxima**: los 7 agentes ejecutores trabajan; `nova-architect` coordina, fija los contratos
> y aprueba. Ningún agente queda en standby.

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone con delegación máxima y **delega** las 7 sub-incidencias, fijando el contrato del job y del evento `export-ready`.
3. **nova-repo-provisioner** añade POI; **nova-service-gen** implementa el job de exportación.
4. Con el contrato listo, **nova-frontend-gen**, **nova-api-integr** y **nova-ops-monitor** trabajan en paralelo; **nova-async-comm** cierra la notificación en vivo.
5. **nova-release-mgr** valida y aplica el gate cuando convergen; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
