# Demo 4: Dashboard de KPIs con gráficas

**Sprint de desarrollo**: Vista analítica con métricas y gráficos
**Duración estimada**: 4-5 horas
**Complejidad**: Intermedio-Avanzado
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan**: nova-service-gen, nova-api-integr, nova-frontend-gen, nova-release-mgr

> Demo de desarrollo. Los agentes NOVA escriben código en backend y frontend y el resultado es un
> **efecto visible en la UI** (una vista nueva con tarjetas y gráficos). No cambia la lógica de negocio.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo, lo descompone en ≤5 sub-tareas y las delega** creando una
> sub-incidencia por agente con sus dependencias (blockers), revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

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

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone y delega**
> creando estas sub-incidencias (una por agente de la org **NOVA**, `QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Implementar el endpoint de métricas `GET /api/v1/metrics` con agregaciones eficientes. Tests de integración. | **nova-service-gen** | Codex | — |
| 2 | Crear la ruta `/dashboard`, tarjetas y gráficas, estados de carga/vacío y responsive. Tests front. | **nova-frontend-gen** | Antigravity | — (mock hasta tener #1) |
| 3 | Documentar `GET /api/v1/metrics` en OpenAPI y validar CORS. | **nova-api-integr** | Antigravity | #1 |
| 4 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Codex | #1, #2 |

`nova-async-comm` y `nova-ops-monitor` quedan en **standby** (no se requieren eventos; la observabilidad
solo entraría si se quisiera instrumentar el endpoint).

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone el objetivo (≤5 sub-tareas, sin cascadas) y **delega** las sub-incidencias anteriores, fijando el contrato de métricas.
3. **nova-service-gen** implementa el endpoint; **nova-frontend-gen** la vista en paralelo (con mock hasta tener el contrato).
4. **nova-api-integr** documenta y verifica CORS.
5. **nova-release-mgr** valida y aplica el gate; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
