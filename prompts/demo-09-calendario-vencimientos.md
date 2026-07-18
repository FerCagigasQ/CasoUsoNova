# Demo 9: Calendario de vencimientos con heatmap de riesgo y expiración automática en vivo

**Sprint de desarrollo**: Gestión visual de vencimientos con scheduler y semáforo de riesgo
**Duración estimada**: 50-60 minutos
**Complejidad**: Avanzado+
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan** (delegación máxima, **toda la org NOVA**): Repo Provisioner (nova-repo-provisioner), Backend Service Generator (nova-service-gen), Frontend Generator (nova-frontend-gen), API Integration Expert (nova-api-integr), Async Communication Expert (nova-async-comm), Operations Monitor (nova-ops-monitor), Release Manager (nova-release-mgr)

> Demo de desarrollo sobre la plataforma ya entregada. El resultado es un **efecto visible al detalle
> en la UI**: una vista **/calendar** con un **calendario mensual** donde cada día muestra sus avales
> que vencen y un **heatmap de riesgo** (verde → ámbar → rojo según proximidad e importe), un panel
> lateral con el detalle del día, **badges de riesgo** en la tabla, y avales que **expiran solos** ante
> tus ojos cuando el scheduler los procesa.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo y lo descompone con delegación máxima**: crea **una sub-incidencia
> por cada agente de la organización** (con sus dependencias/blockers), de modo que **todos los agentes
> NOVA trabajan** — no queda ninguno en standby. El arquitecto revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

---

## 1. Objetivo

Añadir la gestión visual de vencimientos: **scheduler** en backend que expira automáticamente los
avales vencidos (URDG 758), endpoint de agregación por día con **nivel de riesgo calculado**
(proximidad del vencimiento × importe), vista **/calendar** con heatmap mensual y panel de detalle por
día, **badges de riesgo** en la tabla, y **expiración en vivo** visible sin recargar.

## 2. Contexto

El vencimiento es el evento más crítico de un aval y hoy nada lo destaca. Esta demo combina lógica
temporal (scheduler), agregación con reglas de negocio (riesgo) y una visualización rica; el resultado
se inspecciona **al detalle**: el color exacto de cada celda, el tooltip con la suma de importes, el
badge de cada fila y el momento exacto en que un aval pasa a `EXPIRED` delante del espectador.

### Estado actual (ya en `main`)

- Avales con `expiryDate` y máquina de estados, pero sin expiración automática ni vista temporal.
- Canal SSE de eventos, dashboard con charts, Actuator con `prometheus`.

## 3. Alcance (por área)

### Toolchain (Repo Provisioner / `nova-repo-provisioner`)

- [ ] Habilitar `@EnableScheduling` en la configuración del backend y verificar el build; en frontend
      no se añaden librerías (el calendario se construye con CSS Grid + Material).

### Backend (Backend Service Generator / `nova-service-gen`)

- [ ] **Scheduler** (`@Scheduled`, cada 30 s en perfil demo) que transiciona a `EXPIRED` los avales
      `ISSUED`/`AMENDED` con `expiryDate` vencida, registrando el motivo.
- [ ] `GET /api/v1/guarantees/expiry-calendar?month=YYYY-MM`: por día, avales que vencen, suma de
      importes por moneda y **nivel de riesgo** (`none|low|medium|high|critical`) calculado con reglas
      documentadas (días restantes × importe). Tests de integración del scheduler y del cálculo.

### Frontend (Frontend Generator / `nova-frontend-gen`)

- [ ] Vista **/calendar**: cuadrícula mensual (CSS Grid) con navegación mes anterior/siguiente; cada
      celda muestra el nº de vencimientos y color de fondo según riesgo (leyenda visible); tooltip con
      la suma de importes.
- [ ] **Panel lateral** al hacer click en un día: lista de avales que vencen (referencia, beneficiario,
      importe, días restantes) con navegación al detalle.
- [ ] **Badge de riesgo** en la tabla principal (chip de color con días restantes: "vence en 3 días");
      enlace "Calendario" en la barra. Estados de carga/vacío y responsive.

### Integración / contrato (API Integration Expert / `nova-api-integr`)

- [ ] Documentar en OpenAPI el endpoint del calendario (estructura por día, catálogo de riesgo con las
      reglas de cálculo como descripción) y la transición automática a `EXPIRED`; revalidar CORS.

### Tiempo real (Async Communication Expert / `nova-async-comm`)

- [ ] Emitir por SSE el evento de **expiración automática**: la fila de la tabla cambia a `EXPIRED`,
      la celda del calendario se actualiza y aparece un aviso — todo **sin recargar**.

### Observabilidad (Operations Monitor / `nova-ops-monitor`)

- [ ] Métricas Micrometer: contador `guarantees.expired.auto`, timer de la ejecución del scheduler y
      gauge de avales que vencen en ≤7 días, visibles en `/actuator/prometheus`.

### Release (Release Manager / `nova-release-mgr`)

- [ ] Verificar build + arranque Docker de ambos servicios y ejecutar el gate `nova-post-gen-validation`.

## 4. Aceptación

- El scheduler expira automáticamente los avales vencidos y queda registrado el motivo.
- `/calendar` muestra el heatmap correcto (colores según las reglas de riesgo, leyenda incluida),
  tooltips con importes y panel de detalle por día con navegación.
- La tabla muestra el badge de riesgo con días restantes coherente con el calendario.
- Con la app abierta, al vencer un aval (seed con vencimiento inminente) se ve **expirar en vivo**:
  fila, calendario y aviso se actualizan sin recargar.
- `/actuator/prometheus` expone `guarantees_expired_auto_total`, el timer del scheduler y el gauge ≤7 días.
- OpenAPI completo; sin errores en consola; TypeScript estricto sin `any`.
- Tests: scheduler + cálculo de riesgo (integración) y calendario/panel/badge (front con mock).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| Los vencimientos solo se ven como una fecha más en la tabla; nada expira automáticamente. | Calendario heatmap con riesgo por día, panel de detalle, badges "vence en N días" y avales que expiran solos en pantalla. |

**Guion**: abrir **/calendar** → recorrer el heatmap y los tooltips → click en un día rojo y ver el
panel → volver a la tabla y enseñar los badges de riesgo → con un aval seed a punto de vencer, esperar
la pasada del scheduler y verlo **expirar en vivo** (fila + calendario + aviso) → enseñar las métricas
del scheduler en `/actuator/prometheus`.

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone con delegación
> máxima** y crea **una sub-incidencia por cada agente** de la org **NOVA** (`QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador, y **ningún agente queda en standby**.
> La org dispone de agentes reales para cada especialidad — Repo Provisioner, Backend Service Generator,
> Frontend Generator, API Integration Expert, Async Communication Expert, Operations Monitor y Release
> Manager — así que **anímate a delegar**: cada pieza va al especialista que le corresponde.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Habilitar scheduling en el backend y verificar builds de ambos servicios. | **nova-repo-provisioner** | Claude Code (local) | — |
| 2 | Scheduler de expiración + endpoint `expiry-calendar` con cálculo de riesgo. Seed y tests. | **nova-service-gen** | Codex (local) | #1 |
| 3 | Vista /calendar (heatmap + panel), badges de riesgo en tabla y navegación. Tests front. | **nova-frontend-gen** | Claude Code (local) | #2 |
| 4 | Documentar calendario, reglas de riesgo y expiración automática en OpenAPI; revalidar CORS. | **nova-api-integr** | Claude Code (local) | #2 |
| 5 | Emitir la expiración automática por SSE y actualizar tabla/calendario/aviso en vivo. | **nova-async-comm** | Claude Code (local) | #2, #3 |
| 6 | Métricas del scheduler (contador, timer, gauge ≤7 días) en Prometheus. | **nova-ops-monitor** | Claude Code (local) | #2 |
| 7 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Claude Code (local) | #3, #4, #5, #6 |

> **Delegación máxima**: los 7 agentes ejecutores trabajan; `nova-architect` coordina, fija los contratos
> y aprueba. Ningún agente queda en standby.

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone con delegación máxima y **delega** las 7 sub-incidencias, fijando las reglas de riesgo y el contrato del calendario.
3. **Repo Provisioner** habilita el scheduling; **Backend Service Generator** implementa scheduler + endpoint.
4. Con el contrato listo, **Frontend Generator**, **API Integration Expert** y **Operations Monitor** trabajan en paralelo; **Async Communication Expert** cierra la expiración en vivo.
5. **Release Manager** valida y aplica el gate cuando convergen; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
