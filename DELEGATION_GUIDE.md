# Guía de delegación NOVA

**Estado**: Demos de desarrollo listas. Marco de delegación alineado con `prompts/`.
**Última actualización**: 2026-06-29
**Autor**: Arquitecto NOVA

---

## Qué contiene `prompts/`

5 PRDs de **desarrollo de código**, cada uno con un **efecto visible en la UI** (estado inicial → final).
No introducen dominio de trade finance; son mejoras de plataforma transversales sobre la UI Angular 17
(+ backend Spring Boot 3.2 donde aplica). Cada PRD trae su propio `Alcance`, `Aceptación`,
`Equipo y reparto de trabajo` y `Flujo de ejecución`.

| # | Demo | Efecto en UI (inicial → final) | Agentes principales |
|---|------|--------------------------------|---------------------|
| 1 | [Tema oscuro / claro](./prompts/demo-01-tema-oscuro.md) | Sin control de tema → conmutador que cambia toda la UI y persiste | nova-frontend-gen |
| 2 | [Exportación CSV / Excel](./prompts/demo-02-exportacion-datos.md) | Sin exportación → botón que descarga las filas visibles | nova-frontend-gen, nova-service-gen |
| 3 | [Búsqueda global + paleta de comandos](./prompts/demo-03-busqueda-global.md) | Sin buscador → overlay `Ctrl/Cmd+K` que busca y navega | nova-frontend-gen, nova-service-gen |
| 4 | [Dashboard de KPIs](./prompts/demo-04-dashboard-kpis.md) | Solo tabla → vista `/dashboard` con tarjetas y charts | nova-service-gen, nova-frontend-gen |
| 5 | [Internacionalización ES/EN](./prompts/demo-05-internacionalizacion.md) | UI en un idioma → selector que traduce al vuelo y persiste | nova-frontend-gen |

---

## Cómo delegar a los equipos de agentes

### Paso 1: elegir demo y agente

Revisar `prompts/README.md` → seleccionar la combinación demo + agente.

**Ejemplo**: Demo 5 (Internacionalización ES/EN) + `nova-frontend-gen`.

### Paso 2: crear una incidencia por sub-tarea

Para cada elemento del `Alcance` del PRD, crear una incidencia:

**Plantilla de título**:
```
[NOV-XX] (agente): título de la sub-tarea
```

**Ejemplo**:
```
[NOV-5-A] nova-frontend-gen: Selector de idioma ES/EN con cambio en caliente (ngx-translate)
```

**Plantilla de cuerpo**:
```markdown
## Contexto
Demo: [Demo 5: Internacionalización ES/EN](./prompts/demo-05-internacionalizacion.md)

## Tarea
[Copiar el punto del Alcance del PRD]

Ejemplo de demo-05-internacionalizacion.md:
- Selector de idioma ES/EN en la barra con persistencia en localStorage

## Criterios de aceptación
- [ ] Implementación completa según el PRD
- [ ] Tests (unitarios + integración cuando aplique)
- [ ] Documentación / OpenAPI actualizada si hay endpoint
- [ ] La demo corre de inicio a fin con el efecto en UI

## Enlaces
- Guía de demo: prompts/demo-05-internacionalizacion.md
- Reparto y flujo: sección "Equipo y reparto de trabajo" del PRD
```

**Etiquetas**: `agent:nova-frontend-gen`, `demo:5`, `phase:2-implementation`

---

## Matriz de delegación: sub-tareas por agente

> El `nova-architect` descompone cada PRD en **≤5 sub-tareas** (modo demo, sin cascadas) y aprueba la
> entrega. `nova-release-mgr` cierra cada demo con build + arranque Docker y el gate
> `nova-post-gen-validation`. `nova-async-comm` y `nova-ops-monitor` quedan en **standby**.

### nova-frontend-gen (Angular 17 + Material)

- **Demo 1 — Tema oscuro**: `ThemeService` reactivo, temas Material claro/oscuro, botón en la barra, persistencia en `localStorage` + `prefers-color-scheme`.
- **Demo 2 — Exportación**: botón/menú "Exportar", generación de CSV en cliente, descarga del `.xlsx`, estados de carga/error.
- **Demo 3 — Búsqueda global**: overlay `CommandPalette` (CDK), atajo `Ctrl/Cmd+K`, debounce, navegación por teclado, accesibilidad y estado vacío.
- **Demo 4 — Dashboard**: ruta `/dashboard`, tarjetas de KPI, gráficas (barras + donut), estados de carga/vacío, responsive.
- **Demo 5 — i18n**: integrar ngx-translate, catálogos `es/en`, extracción de cadenas, selector de idioma, localización de fechas/números.

### nova-service-gen (Spring Boot 3.2 / Java 17)

- **Demo 2 — Exportación**: endpoint `GET /api/v1/guarantees/export?format=xlsx` reutilizando filtros y DTOs (Apache POI).
- **Demo 3 — Búsqueda global**: endpoint `GET /api/v1/search?q=` con resultados ligeros (id, etiqueta, tipo, ruta), límite y relevancia simple.
- **Demo 4 — Dashboard**: endpoint `GET /api/v1/metrics` con agregados (`total`, `byStatus`, `byType`, `byMonth`).

### nova-api-integr (Integración / OpenAPI / CORS)

- **Demo 1 — Tema oscuro** (opcional): contrato `GET/PUT /api/v1/preferences` si se persiste el tema en servidor.
- **Demo 2 — Exportación**: documentar `GET /export` en OpenAPI (binario, filtros) y validar CORS de la descarga.
- **Demo 3 — Búsqueda global**: documentar `GET /search` en OpenAPI y validar CORS.
- **Demo 4 — Dashboard**: documentar `GET /metrics` en OpenAPI con ejemplo de respuesta y validar CORS.
- **Demo 5 — i18n** (opcional): acordar que los mensajes de error del backend viajen como `code` traducible.

### nova-release-mgr (Docker & gate de validación)

- **Todas las demos**: verificar build de producción (`ng build` / `mvn package`) y arranque con `docker compose`; aplicar la skill `nova-post-gen-validation` como verificación final antes de la aprobación del `nova-architect`.

### nova-architect (CTO)

- **Todas las demos**: descomposición ≤5 sub-tareas, definición de contratos de API, code review y aprobación de la entrega (PR en rama separada, sin merge directo a `main`).

---

## Cómo seguir el progreso

**Etiquetas GitHub** recomendadas:
- `phase:2-implementation`
- `agent:nova-frontend-gen`, `agent:nova-service-gen`, `agent:nova-api-integr`, `agent:nova-release-mgr`, `agent:nova-architect`
- `demo:1` … `demo:5`
- `priority:p0`, `priority:p1`

**Integración Paperclip** (si se usa):
- Crear sub-incidencias enlazadas a la raíz de cada demo.
- Enlazar cada incidencia a su PRD en `prompts/`.

---

## Ubicación de los PRDs

Todo el material de referencia en la carpeta **`prompts/`**:
- `prompts/README.md` — índice y referencia rápida
- `prompts/demo-01-tema-oscuro.md`
- `prompts/demo-02-exportacion-datos.md`
- `prompts/demo-03-busqueda-global.md`
- `prompts/demo-04-dashboard-kpis.md`
- `prompts/demo-05-internacionalizacion.md`

Cada PRD incluye: objetivo, alcance front/back, aceptación, demostración (inicial → final), equipo y
reparto de trabajo, y flujo de ejecución.

---

**Owner**: Arquitecto NOVA
**Estado**: Listo para delegación
