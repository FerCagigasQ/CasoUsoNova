# Demo 1: Tema oscuro / claro con preferencias persistidas y sincronización en vivo

**Sprint de desarrollo**: Theming de plataforma con preferencias de usuario full-stack
**Duración estimada**: 45-60 minutos (algo menos que la demo del dashboard)
**Complejidad**: Avanzado
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan** (delegación máxima, **toda la org NOVA**): nova-repo-provisioner, nova-service-gen, nova-frontend-gen, nova-api-integr, nova-async-comm, nova-ops-monitor, nova-release-mgr

> Demo de desarrollo sobre la plataforma ya entregada (dashboard v2 con SSE y observabilidad **ya en
> `main`**). El resultado es un **efecto muy visible en la UI**: toda la aplicación conmuta entre tema
> claro y oscuro, la preferencia se **persiste en el backend** y se **sincroniza en vivo** entre pestañas.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo y lo descompone con delegación máxima**: crea **una sub-incidencia
> por cada agente de la organización** (con sus dependencias/blockers), de modo que **todos los agentes
> NOVA trabajan** — no queda ninguno en standby. El arquitecto revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

---

## 1. Objetivo

Añadir un **conmutador de tema claro/oscuro** en la barra superior que aplique el tema en toda la app
(Angular 17 + Material), **persista la preferencia en el backend** (`/api/v1/preferences`), la
**sincronice en vivo entre pestañas** reutilizando el canal SSE existente y quede **instrumentada**
en observabilidad.

## 2. Contexto

La plataforma arranca hoy con un único tema claro (`indigo-pink`). Elevar el theming a una preferencia
de usuario full-stack lo convierte en el escaparate ideal de **delegación máxima**: tokens de diseño en
frontend, un mini-dominio de preferencias en backend, contrato OpenAPI, evento en tiempo real y métricas.

### Estado actual (ya en `main`)

- UI con tema claro fijo, sin control de tema.
- Canal SSE de eventos (`guarantee-events`) y Actuator con `health`, `metrics` y `prometheus`.

## 3. Alcance (por área)

### Toolchain (`nova-repo-provisioner`)

- [ ] Verificar que el toolchain Sass/Material soporta `mat.define-light-theme`/`mat.define-dark-theme`
      (versiones fijadas en `package.json`/lockfile) y que el build resuelve los dos temas.

### Backend (`nova-service-gen`)

- [ ] Endpoints `GET /api/v1/preferences` y `PUT /api/v1/preferences` con body `{ "theme": "light" | "dark" }`,
      persistidos en H2 (entidad `UserPreference`, validación 422 para valores inválidos). Tests de integración.

### Frontend (`nova-frontend-gen`)

- [ ] `ThemeService` reactivo (`signal`) con `toggle()`/`set()`; carga inicial desde `GET /preferences`
      con fallback a `localStorage` (`nova.theme`) y a `prefers-color-scheme`.
- [ ] Dos temas Material (`.theme-light`/`.theme-dark` en `<body>`) con contraste AA en tabla, badges,
      diálogos, formularios y dashboard.
- [ ] Botón `mat-icon-button` (`light_mode`/`dark_mode`) en la barra que conmuta y guarda vía `PUT`.

### Integración / contrato (`nova-api-integr`)

- [ ] Documentar `GET`/`PUT /preferences` en OpenAPI con ejemplos y errores; revalidar CORS.

### Tiempo real (`nova-async-comm`)

- [ ] Publicar un evento de **cambio de preferencia** en el canal SSE existente y consumirlo en la app:
      cambiar el tema en una pestaña **conmuta el tema solo** en las demás pestañas abiertas.

### Observabilidad (`nova-ops-monitor`)

- [ ] Contador Micrometer `ui.theme.toggles` (tag `theme=light|dark`) incrementado en el `PUT`,
      visible en `/actuator/prometheus`.

### Release (`nova-release-mgr`)

- [ ] Verificar build + arranque Docker de ambos servicios y ejecutar el gate `nova-post-gen-validation`.

## 4. Aceptación

- Al pulsar el conmutador, **toda** la UI (incluido el dashboard con charts) cambia de tema al instante.
- La preferencia persiste en backend: tras recargar o abrir otro navegador con la misma sesión, se respeta.
- Con dos pestañas abiertas, cambiar el tema en una **lo cambia en vivo** en la otra vía SSE.
- `/actuator/prometheus` expone `ui_theme_toggles_total` con su tag.
- OpenAPI refleja los endpoints; contraste AA en ambos temas; sin errores en consola; TypeScript estricto sin `any`.
- Tests: endpoints de preferencias (integración) y `ThemeService` + render en ambos temas (front).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| App en tema claro fijo, sin control de tema. | Conmutador en la barra: un clic oscurece toda la UI, la preferencia se guarda en backend y las demás pestañas cambian solas. |

**Guion**: abrir `http://localhost` → pulsar el conmutador y ver toda la UI (tabla, dashboard, diálogos)
pasar a oscuro → abrir una segunda pestaña y conmutar: la primera **cambia sola** → recargar y comprobar
que persiste → abrir `/actuator/prometheus` y enseñar el contador de toggles.

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone con delegación
> máxima** y crea **una sub-incidencia por cada agente** de la org **NOVA** (`QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador, y **ningún agente queda en standby**.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Verificar toolchain Sass/Material para dos temas (versiones fijadas + lockfile) y que el build resuelve. | **nova-repo-provisioner** | Claude Code | — |
| 2 | Endpoints `GET`/`PUT /api/v1/preferences` con entidad `UserPreference` y validación. Tests. | **nova-service-gen** | Codex | — |
| 3 | `ThemeService`, dos temas Material, botón en la barra, carga/guardado vía API y fallbacks. Tests front. | **nova-frontend-gen** | Antigravity | #1, #2 |
| 4 | Documentar `GET`/`PUT /preferences` en OpenAPI y revalidar CORS. | **nova-api-integr** | Antigravity | #2 |
| 5 | Emitir el evento de cambio de preferencia por SSE y consumirlo para sincronizar el tema entre pestañas. | **nova-async-comm** | Codex | #2, #3 |
| 6 | Contador Micrometer `ui.theme.toggles` expuesto en Prometheus. | **nova-ops-monitor** | Codex | #2 |
| 7 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Codex | #3, #4, #5, #6 |

> **Delegación máxima**: los 7 agentes ejecutores trabajan; `nova-architect` coordina, fija los contratos
> y aprueba. Ningún agente queda en standby.

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone con delegación máxima y **delega** las 7 sub-incidencias, fijando el contrato de `/preferences` y del evento SSE.
3. **nova-repo-provisioner** verifica el toolchain y **nova-service-gen** implementa las preferencias (ambos sin bloqueo previo).
4. Con el contrato listo, **nova-frontend-gen**, **nova-api-integr** y **nova-ops-monitor** trabajan en paralelo; **nova-async-comm** cierra la sincronización en vivo.
5. **nova-release-mgr** valida y aplica el gate cuando convergen; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
