# Demo 5: Internacionalización (ES / EN) full-stack con sincronización en vivo

**Sprint de desarrollo**: i18n de plataforma con contrato de errores traducible
**Duración estimada**: 45-60 minutos (algo menos que la demo del dashboard)
**Complejidad**: Avanzado
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan** (delegación máxima, **toda la org NOVA**): nova-repo-provisioner, nova-service-gen, nova-frontend-gen, nova-api-integr, nova-async-comm, nova-ops-monitor, nova-release-mgr

> Demo de desarrollo sobre la plataforma ya entregada (dashboard v2 con SSE y observabilidad **ya en
> `main`**). El resultado es un **efecto muy visible en la UI**: toda la interfaz (textos, fechas,
> números y errores del backend) cambia de idioma al vuelo, la preferencia se persiste y las demás
> pestañas **se traducen solas**.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo y lo descompone con delegación máxima**: crea **una sub-incidencia
> por cada agente de la organización** (con sus dependencias/blockers), de modo que **todos los agentes
> NOVA trabajan** — no queda ninguno en standby. El arquitecto revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

---

## 1. Objetivo

Internacionalizar la plataforma con soporte **Español / Inglés**: selector de idioma en la barra que
traduce la UI **en caliente**, **errores del backend como códigos traducibles**, preferencia persistida
en `/api/v1/preferences`, **sincronización en vivo entre pestañas** vía SSE y métricas de uso de idioma.

## 2. Contexto

La i18n es transversal y muy demostrable. Elevarla a full-stack (contrato de errores por código,
preferencia en backend, evento en tiempo real y métricas) la convierte en un escaparate de **delegación
máxima** donde cada agente aporta una pieza concreta.

### Estado actual (ya en `main`)

- UI en un único idioma con textos fijos; errores del backend como mensajes literales.
- Canal SSE de eventos y Actuator con `health`, `metrics` y `prometheus`.

## 3. Alcance (por área)

### Toolchain (`nova-repo-provisioner`)

- [ ] Añadir `@ngx-translate/core` + `@ngx-translate/http-loader` con versiones fijadas en
      `package.json`/lockfile y verificar que el build las resuelve.

### Backend (`nova-service-gen`)

- [ ] Añadir al contrato de error un campo **`code`** estable (p. ej. `GUARANTEE_NOT_FOUND`,
      `INVALID_TRANSITION`) manteniendo `message` por compatibilidad.
- [ ] Persistir el idioma en `/api/v1/preferences` (`{ "lang": "es" | "en" }`, junto a otras
      preferencias). Tests de integración.

### Frontend (`nova-frontend-gen`)

- [ ] Integrar ngx-translate con catálogos `assets/i18n/es.json` / `en.json` y **extraer todas las
      cadenas visibles** (tabla, formularios, diálogos, botones, validaciones, badges, dashboard).
- [ ] Selector ES/EN en la barra: cambia en caliente, guarda vía `PUT /preferences` con fallback a
      `localStorage` (`nova.lang`) e idioma del navegador.
- [ ] Localizar **fechas y números** (`LOCALE_ID` `es`/`en`) y traducir los errores del backend a partir
      de su `code`.

### Integración / contrato (`nova-api-integr`)

- [ ] Documentar en OpenAPI el campo `code` del contrato de error (catálogo de códigos con ejemplos) y
      el campo `lang` de preferencias; revalidar CORS.

### Tiempo real (`nova-async-comm`)

- [ ] Publicar el evento de **cambio de idioma** en el canal SSE existente y consumirlo: cambiar el
      idioma en una pestaña **traduce solas** las demás pestañas abiertas.

### Observabilidad (`nova-ops-monitor`)

- [ ] Contador Micrometer `ui.lang.changes` (tag `lang=es|en`) incrementado en el `PUT`, visible en
      `/actuator/prometheus`.

### Release (`nova-release-mgr`)

- [ ] Verificar build + arranque Docker de ambos servicios y ejecutar el gate `nova-post-gen-validation`.

## 4. Aceptación

- El selector cambia el idioma de **toda** la UI al instante, sin recargar; fechas y números localizados.
- No quedan textos sin traducir en las pantallas principales; los errores del backend se muestran
  traducidos a partir de su `code`.
- La preferencia persiste en backend y, con dos pestañas abiertas, cambiar el idioma en una **traduce la
  otra en vivo** vía SSE.
- `/actuator/prometheus` expone `ui_lang_changes_total` con su tag.
- OpenAPI refleja `code` y `lang`; sin errores en consola; TypeScript estricto sin `any`.
- Tests: contrato de error con `code` y preferencia `lang` (integración) y render de un componente en
  ambos idiomas + servicio de traducción (front).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| UI en un único idioma con textos fijos y errores literales. | Selector ES/EN: toda la UI (textos, fechas, números y errores) se traduce al vuelo, persiste y las demás pestañas cambian solas. |

**Guion**: abrir la app en español → cambiar a inglés y ver toda la UI traducirse (incluidas fechas y
números) → provocar un error de validación y verlo traducido → abrir una segunda pestaña y cambiar el
idioma: la primera **se traduce sola** → recargar y comprobar que persiste → abrir `/actuator/prometheus`
y enseñar el contador de cambios de idioma.

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone con delegación
> máxima** y crea **una sub-incidencia por cada agente** de la org **NOVA** (`QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador, y **ningún agente queda en standby**.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Añadir ngx-translate (versiones fijadas + lockfile) y verificar el build. | **nova-repo-provisioner** | Claude Code | — |
| 2 | Campo `code` en el contrato de error y preferencia `lang` en `/preferences`. Tests. | **nova-service-gen** | Codex | — |
| 3 | Catálogos ES/EN, extracción de cadenas, selector, localización de fechas/números y errores por `code`. Tests front. | **nova-frontend-gen** | Antigravity | #1, #2 |
| 4 | Documentar `code` (catálogo de errores) y `lang` en OpenAPI; revalidar CORS. | **nova-api-integr** | Antigravity | #2 |
| 5 | Emitir el evento de cambio de idioma por SSE y consumirlo para traducir las demás pestañas en vivo. | **nova-async-comm** | Codex | #2, #3 |
| 6 | Contador Micrometer `ui.lang.changes` expuesto en Prometheus. | **nova-ops-monitor** | Codex | #2 |
| 7 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Codex | #3, #4, #5, #6 |

> **Delegación máxima**: los 7 agentes ejecutores trabajan; `nova-architect` coordina, fija los contratos
> y aprueba. Ningún agente queda en standby.

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone con delegación máxima y **delega** las 7 sub-incidencias, fijando la estrategia de i18n y el catálogo de códigos de error.
3. **nova-repo-provisioner** añade ngx-translate y **nova-service-gen** implementa `code` + `lang` (ambos sin bloqueo previo).
4. Con el contrato listo, **nova-frontend-gen**, **nova-api-integr** y **nova-ops-monitor** trabajan en paralelo; **nova-async-comm** cierra la sincronización en vivo.
5. **nova-release-mgr** valida y aplica el gate cuando convergen; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
