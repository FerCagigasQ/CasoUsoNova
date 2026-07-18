# Demo 3: Búsqueda global y paleta de comandos (Ctrl/Cmd + K) con resultados en vivo

**Sprint de desarrollo**: Navegación rápida y búsqueda transversal full-stack
**Duración estimada**: 45-60 minutos (algo menos que la demo del dashboard)
**Complejidad**: Avanzado
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan** (delegación máxima, **toda la org NOVA**): nova-repo-provisioner, nova-service-gen, nova-frontend-gen, nova-api-integr, nova-async-comm, nova-ops-monitor, nova-release-mgr

> Demo de desarrollo sobre la plataforma ya entregada (dashboard v2 con SSE y observabilidad **ya en
> `main`**). El resultado es un **efecto muy visible en la UI**: overlay `Ctrl/Cmd + K` estilo
> "command palette" que busca registros y acciones, navega, y **refresca los resultados en vivo** si los
> datos cambian mientras está abierta.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo y lo descompone con delegación máxima**: crea **una sub-incidencia
> por cada agente de la organización** (con sus dependencias/blockers), de modo que **todos los agentes
> NOVA trabajan** — no queda ninguno en standby. El arquitecto revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

---

## 1. Objetivo

Añadir una **paleta de comandos / buscador global** (`Ctrl/Cmd + K` + icono de lupa) que busca y salta a
registros y acciones desde cualquier pantalla, con endpoint de búsqueda en backend, **resultados que se
refrescan en vivo** vía SSE si los avales cambian con la paleta abierta, y **métricas** del endpoint.

## 2. Contexto

La paleta de comandos es un patrón reconocible y de gran impacto en demo. Añadir el refresco en vivo y
la instrumentación la convierte en un escaparate de **delegación máxima**: overlay accesible en frontend,
búsqueda con relevancia en backend, contrato OpenAPI, evento SSE y métricas.

### Estado actual (ya en `main`)

- Navegación solo por menús/tabla; sin búsqueda global.
- Angular CDK ya instalado; canal SSE de eventos y Actuator con `prometheus`.

## 3. Alcance (por área)

### Toolchain (`nova-repo-provisioner`)

- [ ] Verificar que `@angular/cdk` (Overlay/Dialog) está fijado en `package.json`/lockfile y que el
      build resuelve los módulos; no se añaden librerías nuevas.

### Backend (`nova-service-gen`)

- [ ] Endpoint `GET /api/v1/search?q=&limit=10` que devuelve resultados ligeros
      (`{ id, label, type, route }`) buscando por texto en los campos indexables (referencia,
      beneficiario, ordenante), con orden por relevancia simple. Tests de integración.

### Frontend (`nova-frontend-gen`)

- [ ] `CommandPaletteComponent` como overlay (CDK `Overlay`/`Dialog`) con atajo global `Ctrl/Cmd + K`
      y botón de lupa en la barra.
- [ ] Campo con **debounce** (~250 ms); grupos **Registros** (endpoint) y **Acciones** ("Crear nuevo",
      "Ir al dashboard", "Ir al tablero"); navegación con flechas/`Enter`, cierre con `Esc`.
- [ ] Accesibilidad: `role="dialog"`, trampa de foco, ARIA, estado vacío ("sin resultados").

### Integración / contrato (`nova-api-integr`)

- [ ] Documentar `GET /api/v1/search` en OpenAPI con ejemplos; revalidar CORS.

### Tiempo real (`nova-async-comm`)

- [ ] Con la paleta abierta, escuchar los eventos de aval del canal SSE existente y **re-ejecutar la
      búsqueda activa**: si otro usuario crea/edita un aval que coincide con el término, el resultado
      **aparece solo** (con un realce sutil).

### Observabilidad (`nova-ops-monitor`)

- [ ] Métricas Micrometer del endpoint de búsqueda: contador `search.requests` y timer de latencia,
      visibles en `/actuator/prometheus`.

### Release (`nova-release-mgr`)

- [ ] Verificar build + arranque Docker de ambos servicios y ejecutar el gate `nova-post-gen-validation`.

## 4. Aceptación

- `Ctrl/Cmd + K` abre el overlay desde cualquier ruta; `Esc` lo cierra; accesible por teclado.
- Escribir filtra con debounce; grupos "Registros" y "Acciones"; flechas y `Enter` navegan al destino.
- Con la paleta abierta en una pestaña, crear un aval coincidente en otra hace **aparecer el resultado
  solo** vía SSE.
- `/actuator/prometheus` expone `search_requests_total` y la latencia del endpoint.
- OpenAPI refleja el endpoint; sin errores en consola; TypeScript estricto sin `any`.
- Tests: endpoint de búsqueda (integración) y componente (atajo, navegación, vacío, refresco con mock).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| No hay búsqueda global; la navegación es solo por menús/tabla. | `Ctrl/Cmd + K` abre una paleta que busca registros y acciones, navega y se refresca en vivo cuando cambian los datos. |

**Guion**: pulsar `Ctrl/Cmd + K` → escribir un término y ver resultados agrupados → seleccionar con
teclado y navegar → reabrir la paleta y, desde otra pestaña, crear un aval coincidente → verlo
**aparecer solo** → abrir `/actuator/prometheus` y enseñar las métricas de búsqueda.

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone con delegación
> máxima** y crea **una sub-incidencia por cada agente** de la org **NOVA** (`QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador, y **ningún agente queda en standby**.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Verificar toolchain CDK Overlay/Dialog (versiones fijadas + lockfile) y que el build resuelve. | **nova-repo-provisioner** | Claude Code | — |
| 2 | Endpoint `GET /api/v1/search` (texto, límite, relevancia simple). Tests de integración. | **nova-service-gen** | Codex | — |
| 3 | Overlay, atajo global, debounce, grupos de resultados, navegación por teclado y accesibilidad. Tests front. | **nova-frontend-gen** | Antigravity | #1, #2 |
| 4 | Documentar el endpoint en OpenAPI y revalidar CORS. | **nova-api-integr** | Antigravity | #2 |
| 5 | Refresco en vivo de la búsqueda activa consumiendo los eventos SSE de aval. | **nova-async-comm** | Codex | #2, #3 |
| 6 | Métricas Micrometer del endpoint de búsqueda (contador + latencia) en Prometheus. | **nova-ops-monitor** | Codex | #2 |
| 7 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Codex | #3, #4, #5, #6 |

> **Delegación máxima**: los 7 agentes ejecutores trabajan; `nova-architect` coordina, fija los contratos
> y aprueba. Ningún agente queda en standby.

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone con delegación máxima y **delega** las 7 sub-incidencias, fijando el contrato de búsqueda.
3. **nova-repo-provisioner** verifica el toolchain y **nova-service-gen** implementa el endpoint (ambos sin bloqueo previo).
4. Con el contrato listo, **nova-frontend-gen**, **nova-api-integr** y **nova-ops-monitor** trabajan en paralelo; **nova-async-comm** cierra el refresco en vivo.
5. **nova-release-mgr** valida y aplica el gate cuando convergen; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
