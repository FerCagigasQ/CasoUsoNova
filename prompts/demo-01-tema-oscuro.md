# Demo 1: Tema oscuro / claro con conmutador persistente

**Sprint de desarrollo**: Theming de la UI (Angular Material)
**Duración estimada**: 2-3 horas
**Complejidad**: Principiante
**Agentes NOVA**: nova-architect, nova-frontend-gen, nova-api-integr (contrato de preferencia), nova-release-mgr

> Demo de desarrollo. El objetivo es **ver a los agentes NOVA escribir código** sobre la plataforma
> y observar un **efecto visible en la UI** (estado inicial → final). No introduce lógica de negocio.

---

## 1. Objetivo

Añadir un **conmutador de tema claro/oscuro** a la barra superior de `guarantees-ui` (Angular 17 +
Material) que aplique el tema en toda la aplicación y **persista** la elección del usuario entre
recargas y sesiones.

## 2. Contexto

La aplicación arranca hoy con un único tema claro (`indigo-pink`). Un conmutador de tema es una
mejora de plataforma transversal: afecta a tipografía, superficies, tablas, diálogos y badges, por lo
que es un buen escaparate de cómo el agente de frontend toca **tokens de diseño** sin romper el
dominio existente.

## 3. Alcance

### Frontend (`nova-frontend-gen`)

- [ ] Crear `ThemeService` (Angular, `providedIn: 'root'`) con estado reactivo (`signal`/`BehaviorSubject`)
      `theme: 'light' | 'dark'` y métodos `toggle()`, `set(theme)`, `current()`.
- [ ] Persistir la preferencia en `localStorage` (clave `nova.theme`) y leerla al iniciar; si no existe,
      respetar `prefers-color-scheme` del sistema.
- [ ] Definir dos temas Material en `styles.scss` con `@use '@angular/material' as mat;`
      (`mat.define-light-theme` / `mat.define-dark-theme`) y aplicarlos bajo las clases `.theme-light`
      / `.theme-dark` en `<body>`.
- [ ] Añadir un botón con icono (`mat-icon-button` con `light_mode`/`dark_mode`) en `app.component.html`
      que llame a `ThemeService.toggle()`.
- [ ] Asegurar contraste correcto en tabla, badges de estado/tipo, diálogos y formularios en ambos temas.

### Integración / contrato (`nova-api-integr`) — opcional, ampliación

- [ ] Si se quiere persistencia por usuario en servidor (en vez de solo `localStorage`), definir el
      contrato `GET/PUT /api/v1/preferences` con `{ "theme": "light" | "dark" }` y documentarlo en OpenAPI.

## 4. Aceptación

- Al pulsar el conmutador, **toda** la UI cambia de tema al instante (sin recargar).
- La preferencia **se mantiene** tras recargar la página y al volver a abrir la app.
- Sin preferencia previa, se respeta el tema del sistema operativo.
- Contraste AA en textos y badges en ambos temas; sin errores en consola.
- Tests unitarios del `ThemeService` (toggle, set, lectura de `localStorage`).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| App en tema claro, sin control de tema en la barra. | Botón de tema en la barra; un clic conmuta toda la UI a oscuro y la elección persiste tras recargar. |

**Guion**: abrir `http://localhost` → mostrar tema claro → pulsar el botón → la app pasa a oscuro
(tabla, badges, diálogos) → recargar → sigue en oscuro.

## 6. Equipo y reparto de trabajo

> Agentes de la organización **NOVA** (`QPaperClip/containers/nova-org`).

| Agente | Adapter | Responsabilidad en este PRD |
|--------|---------|------------------------------|
| **nova-architect** (CTO) | Claude Code | Descompone el PRD en ≤5 sub-tareas, define el contrato de `ThemeService` y, si aplica, el de preferencias. Revisa el código. |
| **nova-frontend-gen** | Antigravity | Implementa `ThemeService`, los dos temas Material, el botón de la barra y la persistencia. Tests con Karma/Jasmine. |
| **nova-api-integr** | Antigravity | (Ampliación) Define y documenta `GET/PUT /api/v1/preferences` en OpenAPI si se persiste en servidor. |
| **nova-release-mgr** | Codex | Verifica build de producción (`ng build`) y arranque Docker; aplica la skill `nova-post-gen-validation` como gate. |

`nova-async-comm` y `nova-ops-monitor` quedan en **standby** (no se necesitan eventos ni observabilidad nueva).

### Flujo de ejecución

1. **nova-architect** descompone y asigna sub-tareas (modo demo, ≤5, sin cascadas).
2. **nova-frontend-gen** implementa el conmutador y los temas.
3. **nova-release-mgr** valida build + arranque y ejecuta el gate.
4. **nova-architect** aprueba la entrega (PR en rama separada, sin merge directo a `main`).
