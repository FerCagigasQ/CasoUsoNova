# Demo 7: Timeline de auditoría del aval con diffs visuales y reproducción en vivo

**Sprint de desarrollo**: Auditoría full-stack con visualización detallada del historial
**Duración estimada**: 50-60 minutos
**Complejidad**: Avanzado+
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan** (delegación máxima, **toda la org NOVA**): Repo Provisioner (nova-repo-provisioner), Backend Service Generator (nova-service-gen), Frontend Generator (nova-frontend-gen), API Integration Expert (nova-api-integr), Async Communication Expert (nova-async-comm), Operations Monitor (nova-ops-monitor), Release Manager (nova-release-mgr)

> Demo de desarrollo sobre la plataforma ya entregada. El resultado es un **efecto visible al detalle
> en la UI**: en el detalle de cada aval aparece una **línea de tiempo vertical** con cada evento de su
> vida (creación, emisión, enmiendas, reclamaciones, cambios de estado), incluyendo **diffs campo a
> campo** de cada enmienda (valor anterior tachado → valor nuevo resaltado) y eventos nuevos que
> **entran solos con animación** mientras la pantalla está abierta.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo y lo descompone con delegación máxima**: crea **una sub-incidencia
> por cada agente de la organización** (con sus dependencias/blockers), de modo que **todos los agentes
> NOVA trabajan** — no queda ninguno en standby. El arquitecto revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

---

## 1. Objetivo

Añadir a la vista de detalle del aval una pestaña **"Historial"** con una **timeline de auditoría**:
cada mutación del aval queda registrada como un **evento de auditoría inmutable** (quién, cuándo, qué
cambió, snapshot antes/después) y se visualiza como línea de tiempo con **diffs campo a campo**,
filtros por tipo de evento, y **actualización en vivo** con animación cuando ocurre un cambio.

## 2. Contexto

La plataforma ya persiste avales, enmiendas y reclamaciones, pero no conserva un rastro auditable de
**qué cambió exactamente** en cada operación. Un audit trail con diffs es un requisito típico de trade
finance (URDG 758) y su visualización es el escaparate perfecto para enseñar el output de los agentes
**al detalle**: cada píxel de la timeline proviene de una pieza construida por un agente distinto.

### Estado actual (ya en `main`)

- CRUD de avales con enmiendas y reclamaciones; sin registro de auditoría por campo.
- Canal SSE de eventos (`guarantee-events`), exportación asíncrona y Actuator con `prometheus`.

## 3. Alcance (por área)

### Toolchain (Repo Provisioner / `nova-repo-provisioner`)

- [ ] Verificar que `@angular/animations` está habilitado (BrowserAnimations) y fijado en el lockfile
      para las transiciones de entrada de eventos; sin librerías nuevas de backend (diff propio).

### Backend (Backend Service Generator / `nova-service-gen`)

- [ ] Entidad `AuditEvent` inmutable: `guaranteeId`, `type` (`CREATED`, `ISSUED`, `AMENDED`, `CLAIMED`,
      `STATUS_CHANGED`, …), `actor`, `timestamp`, y `changes: [{ field, oldValue, newValue }]` calculado
      comparando el estado antes/después de cada mutación (interceptado en la capa de servicio).
- [ ] `GET /api/v1/guarantees/{id}/audit?type=&from=&to=` paginado y ordenado descendente.
      Tests de integración: cada operación CRUD genera su evento con el diff correcto.

### Frontend (Frontend Generator / `nova-frontend-gen`)

- [ ] Pestaña **"Historial"** en el detalle del aval con **timeline vertical** (Material): nodo con
      icono y color por tipo de evento, actor y fecha relativa ("hace 2 min") con tooltip de fecha exacta.
- [ ] Cada nodo expandible muestra el **diff campo a campo**: etiqueta del campo, valor anterior
      (tachado, rojo suave) → valor nuevo (resaltado, verde suave); importes y fechas formateados.
- [ ] Filtros por tipo de evento (chips) y por rango de fechas; paginación "cargar más"; estados de
      carga/vacío; animación de entrada (slide + highlight que se desvanece) para eventos nuevos.

### Integración / contrato (API Integration Expert / `nova-api-integr`)

- [ ] Documentar en OpenAPI el modelo `AuditEvent` (catálogo de `type` y estructura de `changes`) con
      ejemplos reales de diff; revalidar CORS.

### Tiempo real (Async Communication Expert / `nova-async-comm`)

- [ ] Publicar cada `AuditEvent` en el canal SSE existente; con la pestaña "Historial" abierta, el
      evento nuevo **se inserta solo en la parte superior** de la timeline con la animación de entrada.

### Observabilidad (Operations Monitor / `nova-ops-monitor`)

- [ ] Contador Micrometer `audit.events` (tag `type`) y gauge del tamaño del log de auditoría,
      visibles en `/actuator/prometheus`.

### Release (Release Manager / `nova-release-mgr`)

- [ ] Verificar build + arranque Docker de ambos servicios y ejecutar el gate `nova-post-gen-validation`.

## 4. Aceptación

- Toda mutación (crear, emitir, enmendar, reclamar, cambiar estado) genera un `AuditEvent` con su diff
  exacto campo a campo; los eventos son inmutables (no hay update/delete de auditoría).
- La pestaña "Historial" muestra la timeline con iconos/colores por tipo, diffs expandibles legibles,
  filtros por tipo y fechas, y paginación.
- Con la pestaña abierta, enmendar el aval desde otra pestaña hace **aparecer el evento solo, animado**.
- `/actuator/prometheus` expone `audit_events_total` por tipo.
- OpenAPI documenta el modelo con ejemplos; sin errores en consola; TypeScript estricto sin `any`.
- Tests: generación de diffs en cada operación (integración) y render de timeline + diff (front con mock).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| El detalle del aval muestra solo su estado actual; el pasado es invisible. | Pestaña "Historial": timeline completa con quién/cuándo/qué cambió, diffs antes→después campo a campo y eventos que entran solos animados. |

**Guion**: abrir el detalle de un aval → pestaña "Historial" y recorrer la timeline → expandir una
enmienda y enseñar el diff campo a campo (importe tachado → importe nuevo) → filtrar por tipo → desde
otra pestaña, enmendar el aval → volver y ver el **evento entrar solo con animación** → abrir
`/actuator/prometheus` y enseñar `audit_events_total`.

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone con delegación
> máxima** y crea **una sub-incidencia por cada agente** de la org **NOVA** (`QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador, y **ningún agente queda en standby**.
> La org dispone de agentes reales para cada especialidad — Repo Provisioner, Backend Service Generator,
> Frontend Generator, API Integration Expert, Async Communication Expert, Operations Monitor y Release
> Manager — así que **anímate a delegar**: cada pieza va al especialista que le corresponde.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Verificar toolchain de animaciones Angular (lockfile) y que el build resuelve. | **nova-repo-provisioner** | Claude Code (local) | — |
| 2 | Entidad `AuditEvent`, cálculo de diffs en cada mutación y endpoint paginado con filtros. Tests. | **nova-service-gen** | Codex (local) | — |
| 3 | Pestaña "Historial": timeline, diffs expandibles, filtros, paginación y animaciones. Tests front. | **nova-frontend-gen** | Claude Code (local) | #1, #2 |
| 4 | Documentar `AuditEvent` (tipos, `changes`) en OpenAPI con ejemplos; revalidar CORS. | **nova-api-integr** | Claude Code (local) | #2 |
| 5 | Emitir cada `AuditEvent` por SSE e insertarlo animado en la timeline abierta. | **nova-async-comm** | Claude Code (local) | #2, #3 |
| 6 | Métricas `audit.events` (contador por tipo) y gauge del log en Prometheus. | **nova-ops-monitor** | Claude Code (local) | #2 |
| 7 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Claude Code (local) | #3, #4, #5, #6 |

> **Delegación máxima**: los 7 agentes ejecutores trabajan; `nova-architect` coordina, fija los contratos
> y aprueba. Ningún agente queda en standby.

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone con delegación máxima y **delega** las 7 sub-incidencias, fijando el modelo `AuditEvent` y el formato de `changes`.
3. **Repo Provisioner** verifica el toolchain y **Backend Service Generator** implementa auditoría + endpoint (sin bloqueo previo).
4. Con el contrato listo, **Frontend Generator**, **API Integration Expert** y **Operations Monitor** trabajan en paralelo; **Async Communication Expert** cierra la inserción en vivo.
5. **Release Manager** valida y aplica el gate cuando convergen; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
