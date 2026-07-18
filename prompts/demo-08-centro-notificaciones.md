# Demo 8: Centro de notificaciones en vivo con reglas configurables y badge animado

**Sprint de desarrollo**: Sistema de notificaciones full-stack dirigido por reglas
**Duración estimada**: 50-60 minutos
**Complejidad**: Avanzado+
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan** (delegación máxima, **toda la org NOVA**): Repo Provisioner (nova-repo-provisioner), Backend Service Generator (nova-service-gen), Frontend Generator (nova-frontend-gen), API Integration Expert (nova-api-integr), Async Communication Expert (nova-async-comm), Operations Monitor (nova-ops-monitor), Release Manager (nova-release-mgr)

> Demo de desarrollo sobre la plataforma ya entregada. El resultado es un **efecto visible al detalle
> en la UI**: una **campana en la barra con badge animado** de no-leídas, un **panel deslizante** de
> notificaciones agrupadas por severidad con navegación al aval, **toasts en vivo** para eventos
> importantes y una pantalla de **reglas configurables** (qué eventos generan notificación y con qué
> severidad) cuyo efecto se ve al instante.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo y lo descompone con delegación máxima**: crea **una sub-incidencia
> por cada agente de la organización** (con sus dependencias/blockers), de modo que **todos los agentes
> NOVA trabajan** — no queda ninguno en standby. El arquitecto revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

---

## 1. Objetivo

Construir un **centro de notificaciones**: el backend evalúa cada evento de aval contra **reglas
persistidas** (evento → severidad `info|warning|critical`, activada/desactivada), genera notificaciones
con estado leída/no-leída, y la UI las muestra en una campana con badge, panel deslizante, toasts en
vivo y una pantalla de administración de reglas.

## 2. Contexto

La plataforma ya emite eventos SSE, pero se pierden si nadie mira la pantalla adecuada. Un centro de
notificaciones persistente y dirigido por reglas es la evolución natural, y su output es visible **al
detalle**: badge que se incrementa animado, toast que entra, fila que pasa de negrita a leída, regla
que se apaga y deja de notificar — cada comportamiento es demostrable en segundos.

### Estado actual (ya en `main`)

- Canal SSE de eventos de aval, exportación asíncrona, Actuator con `prometheus`.
- Sin persistencia de notificaciones ni reglas; sin indicador en la barra.

## 3. Alcance (por área)

### Toolchain (Repo Provisioner / `nova-repo-provisioner`)

- [ ] Verificar que Material (`MatBadge`, `MatSidenav`/CDK Overlay, `MatSnackBar`) y las animaciones
      están fijados en el lockfile y que el build los resuelve; sin librerías nuevas.

### Backend (Backend Service Generator / `nova-service-gen`)

- [ ] Entidades `NotificationRule` (`eventType`, `severity`, `enabled`) y `Notification`
      (`title`, `body`, `severity`, `guaranteeId`, `read`, `createdAt`); al ocurrir un evento de aval,
      evaluar las reglas activas y persistir la notificación resultante.
- [ ] Endpoints: `GET /api/v1/notifications?unread=&severity=` (paginado), `PATCH .../{id}/read`,
      `POST .../read-all`, y CRUD de `GET/PUT /api/v1/notification-rules`. Seed con reglas por defecto.
      Tests de integración de la evaluación de reglas.

### Frontend (Frontend Generator / `nova-frontend-gen`)

- [ ] **Campana** en la barra con `matBadge` de no-leídas (animación de "pulso" al incrementarse).
- [ ] **Panel deslizante** (overlay): lista agrupada por severidad con icono/color, fecha relativa,
      negrita si no-leída, click → navega al aval y la marca leída; acciones "marcar todas" y filtros.
- [ ] **Toasts** (`MatSnackBar`) solo para severidad `warning`/`critical`, con acción "Ver".
- [ ] Pantalla **/settings/notifications** con la tabla de reglas (toggle de activación y selector de
      severidad) que guarda al cambiar.

### Integración / contrato (API Integration Expert / `nova-api-integr`)

- [ ] Documentar en OpenAPI notificaciones y reglas (catálogo de `eventType` y `severity`, ejemplos);
      revalidar CORS.

### Tiempo real (Async Communication Expert / `nova-async-comm`)

- [ ] Emitir la notificación persistida por el canal SSE: el badge se incrementa, la fila aparece en el
      panel abierto y el toast entra **sin recargar**, en todas las pestañas abiertas.

### Observabilidad (Operations Monitor / `nova-ops-monitor`)

- [ ] Métricas Micrometer `notifications.emitted` (tags `severity`, `eventType`) y gauge de no-leídas,
      visibles en `/actuator/prometheus`.

### Release (Release Manager / `nova-release-mgr`)

- [ ] Verificar build + arranque Docker de ambos servicios y ejecutar el gate `nova-post-gen-validation`.

## 4. Aceptación

- Crear/enmendar/reclamar un aval genera notificaciones según las reglas activas; desactivar una regla
  detiene sus notificaciones **al instante** (comprobable en la demo).
- Badge con contador correcto y pulso al incrementarse; panel con agrupación por severidad, negrita de
  no-leídas, navegación al aval y "marcar todas".
- Toast en vivo para `warning`/`critical` en todas las pestañas, sin recargar.
- `/actuator/prometheus` expone `notifications_emitted_total` y el gauge de no-leídas.
- OpenAPI completo; sin errores en consola; TypeScript estricto sin `any`.
- Tests: evaluación de reglas y endpoints (integración) y campana/panel/toast (front con mock).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| Los eventos ocurren en silencio; nada lo indica en la barra. | Campana con badge animado, panel con notificaciones por severidad, toasts en vivo y reglas configurables cuyo efecto se ve al momento. |

**Guion**: abrir la app → crear un aval en otra pestaña → ver el **badge pulsar** y el toast entrar →
abrir el panel, recorrer severidades, click en una notificación → navega al aval y queda leída →
abrir **/settings/notifications**, desactivar la regla de creación → crear otro aval → **no llega
notificación** → reactivarla y verla volver → enseñar `notifications_emitted_total` en Prometheus.

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone con delegación
> máxima** y crea **una sub-incidencia por cada agente** de la org **NOVA** (`QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador, y **ningún agente queda en standby**.
> La org dispone de agentes reales para cada especialidad — Repo Provisioner, Backend Service Generator,
> Frontend Generator, API Integration Expert, Async Communication Expert, Operations Monitor y Release
> Manager — así que **anímate a delegar**: cada pieza va al especialista que le corresponde.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Verificar toolchain Material/CDK/animaciones (lockfile) y que el build resuelve. | **nova-repo-provisioner** | Claude Code (local) | — |
| 2 | Entidades, motor de evaluación de reglas y endpoints de notificaciones + reglas. Seed y tests. | **nova-service-gen** | Codex (local) | — |
| 3 | Campana con badge, panel deslizante, toasts y pantalla de reglas. Tests front. | **nova-frontend-gen** | Claude Code (local) | #1, #2 |
| 4 | Documentar notificaciones y reglas en OpenAPI; revalidar CORS. | **nova-api-integr** | Claude Code (local) | #2 |
| 5 | Emitir notificaciones por SSE y actualizar badge/panel/toast en vivo en todas las pestañas. | **nova-async-comm** | Claude Code (local) | #2, #3 |
| 6 | Métricas `notifications.emitted` y gauge de no-leídas en Prometheus. | **nova-ops-monitor** | Claude Code (local) | #2 |
| 7 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Claude Code (local) | #3, #4, #5, #6 |

> **Delegación máxima**: los 7 agentes ejecutores trabajan; `nova-architect` coordina, fija los contratos
> y aprueba. Ningún agente queda en standby.

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone con delegación máxima y **delega** las 7 sub-incidencias, fijando el modelo de notificación/regla y el evento SSE.
3. **Repo Provisioner** verifica el toolchain y **Backend Service Generator** implementa el motor de reglas (sin bloqueo previo).
4. Con el contrato listo, **Frontend Generator**, **API Integration Expert** y **Operations Monitor** trabajan en paralelo; **Async Communication Expert** cierra el flujo en vivo.
5. **Release Manager** valida y aplica el gate cuando convergen; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
