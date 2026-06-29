# Demo 4: Dashboard de KPIs con gráficas

**Sprint de desarrollo**: Vista analítica con métricas y gráficos
**Duración estimada**: 4-5 horas
**Complejidad**: Intermedio-Avanzado
**Agentes NOVA**: nova-architect, nova-service-gen, nova-api-integr, nova-frontend-gen, nova-release-mgr

> Demo de desarrollo. Los agentes NOVA escriben código en backend y frontend y el resultado es un
> **efecto visible en la UI** (una vista nueva con tarjetas y gráficos). No cambia la lógica de negocio.

---

## 1. Objetivo

Añadir una vista **`/dashboard`** con **tarjetas de KPI** y **gráficas** (barras + tarta/donut) que
resuman la información de la plataforma, alimentadas por un **endpoint de métricas** del backend.

## 2. Contexto

Un dashboard analítico es una mejora de plataforma de alto impacto visual y ejercita la cadena completa:
el agente de backend agrega datos en un endpoint de métricas, el de integración lo documenta y el de
frontend lo pinta con una librería de charts. Es el escaparate ideal de "agentes trabajando" full-stack.

## 3. Alcance

### Backend (`nova-service-gen`)

- [ ] Endpoint `GET /api/v1/metrics` que devuelva agregados, p. ej.:
      `{ "total": N, "byStatus": { ... }, "byType": { ... }, "byMonth": [ { "month": "2026-01", "count": N } ] }`.
- [ ] Calcular los agregados con consultas eficientes (group by) reutilizando el repositorio existente.

### Frontend (`nova-frontend-gen`)

- [ ] Ruta `/dashboard` y enlace en la navegación.
- [ ] Tarjetas de KPI (totales y desgloses clave) con Angular Material.
- [ ] Gráfica de **barras** (por mes) y **tarta/donut** (por estado y por tipo) con una librería de charts
      (ngx-charts o Chart.js) ya instalable en el proyecto.
- [ ] Estados de **carga** (skeletons) y **vacío** ("sin datos") y diseño **responsive**.

### Integración / contrato (`nova-api-integr`)

- [ ] Documentar `GET /api/v1/metrics` en OpenAPI con un ejemplo de respuesta y verificar CORS.

## 4. Aceptación

- `/dashboard` carga las métricas reales del backend y pinta tarjetas + 2 tipos de gráfica.
- Los totales de las gráficas cuadran con los datos de la tabla.
- Hay estados de carga y vacío; la vista es responsive (móvil/escritorio).
- Sin errores en consola; modo estricto de TypeScript sin `any`.
- Tests: endpoint de métricas (integración) y componente de dashboard (render con datos mock).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| La app solo ofrece la tabla/listado. | Nueva vista `/dashboard` con tarjetas de KPI y gráficas (barras + donut) alimentadas por el backend. |

**Guion**: abrir `/dashboard` → ver tarjetas con totales → mostrar la gráfica por mes y la de
desglose → crear/cambiar un registro y refrescar para ver los números actualizarse.

## 6. Equipo y reparto de trabajo

> Agentes de la organización **NOVA** (`QPaperClip/containers/nova-org`).

| Agente | Adapter | Responsabilidad en este PRD |
|--------|---------|------------------------------|
| **nova-architect** (CTO) | Claude Code | Descompone el PRD, define el contrato de `GET /api/v1/metrics` (forma del agregado) y revisa el código. |
| **nova-service-gen** | Codex | Implementa el endpoint de métricas con agregaciones eficientes. Tests de integración. |
| **nova-frontend-gen** | Antigravity | Crea la ruta `/dashboard`, tarjetas y gráficas, estados de carga/vacío y responsive. Tests front. |
| **nova-api-integr** | Antigravity | Documenta `GET /api/v1/metrics` en OpenAPI y valida CORS. |
| **nova-release-mgr** | Codex | Verifica build + arranque Docker y ejecuta el gate `nova-post-gen-validation`. |

`nova-async-comm` y `nova-ops-monitor` quedan en **standby** (no se requieren eventos; la observabilidad
solo entraría si se quisiera instrumentar el endpoint).

### Flujo de ejecución

1. **nova-architect** descompone (≤5 sub-tareas) y fija el contrato de métricas.
2. **nova-service-gen** implementa el endpoint; **nova-frontend-gen** la vista en paralelo (con mock hasta tener el contrato).
3. **nova-api-integr** documenta y verifica CORS.
4. **nova-release-mgr** valida y aplica el gate.
5. **nova-architect** aprueba la entrega (PR en rama separada).
