# Demo 1: Tema oscuro / claro con conmutador persistente

**Sprint de desarrollo**: Theming de la UI (Angular Material)
**Duración estimada**: 2-3 horas
**Complejidad**: Principiante
**Punto de entrada**: `nova-architect` (recibe el objetivo y **solo delega**, no programa)
**Agentes que ejecutan** (2): nova-frontend-gen, nova-release-mgr

> Demo de desarrollo. El objetivo es **ver a los agentes NOVA escribir código** sobre la plataforma
> y observar un **efecto visible en la UI** (estado inicial → final). No introduce lógica de negocio.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo y lo descompone delegándolo a 2 agentes** (una sub-incidencia
> por agente con sus dependencias/blockers). El arquitecto **no escribe código**: su trabajo es delegar,
> revisar lo entregado y aprobar la release. El apartado 6 describe **la delegación que ejecuta el
> arquitecto**, no un reparto previo del operador.

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

### Release (`nova-release-mgr`)

- [ ] Verificar el build de producción (`ng build`) y el arranque Docker, y aplicar la skill
      `nova-post-gen-validation` como gate antes de aprobar.

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

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone y delega**
> creando estas sub-incidencias (una por agente de la org **NOVA**, `QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador.

El arquitecto **solo delega** (no programa) y crea **2 sub-incidencias**, una por agente:

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Implementar `ThemeService`, los dos temas Material, el botón de la barra y la persistencia (tests Karma/Jasmine). | **nova-frontend-gen** | Antigravity | — |
| 2 | Verificar build de producción (`ng build`) + arranque Docker; aplicar la skill `nova-post-gen-validation` como gate. | **nova-release-mgr** | Codex | #1 |

`nova-api-integr`, `nova-async-comm` y `nova-ops-monitor` quedan en **standby** (no se necesita contrato de servidor, eventos ni observabilidad nueva).

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** **solo delega**: descompone el objetivo y crea las 2 sub-incidencias anteriores (no escribe código).
3. **nova-frontend-gen** implementa el conmutador y los temas.
4. **nova-release-mgr** valida build + arranque y ejecuta el gate (bloqueado hasta que #1 termine).
5. **nova-architect** revisa y aprueba la entrega (PR en rama separada, sin merge directo a `main`).
