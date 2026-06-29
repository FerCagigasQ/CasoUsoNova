# Demo 3: Búsqueda global y paleta de comandos (Ctrl/Cmd + K)

**Sprint de desarrollo**: Navegación rápida y búsqueda transversal
**Duración estimada**: 3-4 horas
**Complejidad**: Intermedio
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan**: nova-frontend-gen, nova-service-gen, nova-api-integr, nova-release-mgr

> Demo de desarrollo. Los agentes NOVA escriben código y el resultado es un **efecto visible en la UI**
> (un overlay de búsqueda que aparece y navega). No cambia la lógica de negocio.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo, lo descompone en ≤5 sub-tareas y las delega** creando una
> sub-incidencia por agente con sus dependencias (blockers), revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

---

## 1. Objetivo

Añadir una **paleta de comandos / buscador global** que se abre con `Ctrl/Cmd + K` (y un icono de lupa
en la barra), permite **buscar y saltar** a registros y acciones de la plataforma desde cualquier
pantalla.

## 2. Contexto

Una paleta de comandos es una mejora de UX transversal muy visual: muestra al agente de frontend
gestionando un overlay accesible (foco, teclado, debounce) y al de backend exponiendo un endpoint de
búsqueda ligero. Es un patrón reconocible (estilo "command palette") y de fuerte impacto en demo.

## 3. Alcance

### Frontend (`nova-frontend-gen`)

- [ ] Componente `CommandPaletteComponent` como overlay modal (Angular CDK `Overlay`/`Dialog`).
- [ ] Atajo global `Ctrl/Cmd + K` para abrir/cerrar (con `HostListener`), más botón de lupa en la barra.
- [ ] Campo de búsqueda con **debounce** (~250 ms); navegación con flechas y `Enter`; cierre con `Esc`.
- [ ] Dos grupos de resultados: **Registros** (desde el endpoint de búsqueda) y **Acciones** (p. ej.
      "Crear nuevo", "Ir al dashboard"), cada uno con navegación al seleccionar.
- [ ] Accesibilidad: `role="dialog"`, trampa de foco, etiquetas ARIA, estado vacío ("sin resultados").

### Backend (`nova-service-gen`)

- [ ] Endpoint `GET /api/v1/search?q=` que devuelva resultados ligeros (id, etiqueta, tipo, ruta) a
      partir de una búsqueda por texto sobre los campos indexables existentes.
- [ ] Límite de resultados (p. ej. 10) y orden por relevancia simple.

### Integración / contrato (`nova-api-integr`)

- [ ] Documentar `GET /api/v1/search` en OpenAPI con ejemplos y verificar CORS.

## 4. Aceptación

- `Ctrl/Cmd + K` abre el overlay desde cualquier ruta; `Esc` lo cierra.
- Escribir filtra resultados con debounce; las flechas y `Enter` navegan al destino.
- Se muestran grupos "Registros" y "Acciones"; hay estado vacío claro.
- Accesible por teclado y lector de pantalla; sin errores en consola.
- Tests: componente (apertura por atajo, navegación, vacío) y endpoint de búsqueda (integración).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| No hay búsqueda global; la navegación es solo por menús/tabla. | `Ctrl/Cmd + K` abre una paleta que busca registros y acciones y salta al destino elegido. |

**Guion**: en cualquier pantalla pulsar `Ctrl/Cmd + K` → escribir un término → ver resultados
agrupados → seleccionar con teclado → la app navega al registro.

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone y delega**
> creando estas sub-incidencias (una por agente de la org **NOVA**, `QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Implementar el endpoint de búsqueda `GET /api/v1/search` (texto, límite, relevancia simple). Tests de integración. | **nova-service-gen** | Codex | — |
| 2 | Implementar el overlay, el atajo global, el debounce, la navegación por teclado y la accesibilidad. Tests front. | **nova-frontend-gen** | Antigravity | — |
| 3 | Documentar el endpoint en OpenAPI y validar CORS. | **nova-api-integr** | Antigravity | #1 |
| 4 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Codex | #1, #2 |

`nova-async-comm` y `nova-ops-monitor` quedan en **standby**.

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone el objetivo (≤5 sub-tareas, sin cascadas) y **delega** las sub-incidencias anteriores, fijando el contrato de búsqueda.
3. **nova-service-gen** y **nova-frontend-gen** implementan en paralelo.
4. **nova-api-integr** documenta y verifica CORS.
5. **nova-release-mgr** valida y aplica el gate; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
