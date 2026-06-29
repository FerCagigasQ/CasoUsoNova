# Demo 2: Exportación de la lista a CSV / Excel

**Sprint de desarrollo**: Exportación de datos desde la tabla
**Duración estimada**: 2-3 horas
**Complejidad**: Principiante-Intermedio
**Punto de entrada**: `nova-architect` (recibe el objetivo y delega)
**Agentes que ejecutan**: nova-frontend-gen, nova-service-gen, nova-api-integr, nova-release-mgr

> Demo de desarrollo. Los agentes NOVA escriben código y el resultado es un **efecto visible en la UI**
> (botón nuevo + descarga generada). No modifica la lógica de negocio existente.

> **Cómo se entrega este prompt.** El operador crea **un único objetivo (Goal) y una incidencia raíz
> asignada a `nova-architect`** con el contenido de este PRD. **No se asignan sub-tareas a mano.**
> `nova-architect` **recibe el objetivo, lo descompone en ≤5 sub-tareas y las delega** creando una
> sub-incidencia por agente con sus dependencias (blockers), revisa el código entregado y aprueba la
> release. El apartado 6 describe **la delegación que ejecuta el arquitecto**, no un reparto previo del operador.

---

## 1. Objetivo

Permitir **exportar la tabla actual** (`guarantees-ui`) a **CSV** y **Excel (.xlsx)** desde un botón
"Exportar" en la barra de la lista, **respetando los filtros y el orden** aplicados en pantalla.

## 2. Contexto

Exportar a CSV/Excel es una mejora de plataforma habitual y autocontenida: ejercita la generación de
ficheros en frontend (CSV) y/o backend (Excel), el respeto del estado de la tabla (filtros, orden) y la
descarga en el navegador. Es ideal para mostrar a los agentes de frontend y backend cooperando.

## 3. Alcance

### Frontend (`nova-frontend-gen`)

- [ ] Añadir un botón `Exportar` (`mat-button` con menú `mat-menu`: "CSV" / "Excel") en la cabecera de
      la lista.
- [ ] **CSV** en cliente: generar el CSV a partir de las filas visibles (tras filtros/orden), con
      cabeceras legibles y separador configurable; disparar la descarga con un `Blob` + enlace temporal.
- [ ] **Excel**: invocar el endpoint del backend (ver abajo) y descargar el `.xlsx` recibido.
- [ ] Estado de carga en el botón mientras se genera; toast de éxito/error.

### Backend (`nova-service-gen`)

- [ ] Endpoint `GET /api/v1/guarantees/export?format=xlsx&status=&type=` que reutilice los filtros
      existentes y devuelva un `.xlsx` (cabecera `Content-Disposition: attachment`).
- [ ] Generar el Excel con Apache POI (o librería equivalente ya disponible) reutilizando el mapeo a DTO.

### Integración / contrato (`nova-api-integr`)

- [ ] Documentar el endpoint en OpenAPI (parámetros de filtro, `produces` del binario, ejemplo).
- [ ] Verificar que CORS permite la descarga desde `http://localhost` (frontend).

## 4. Aceptación

- El botón exporta **lo que se ve**: si hay filtros/orden activos, el fichero los respeta.
- El CSV abre correctamente en Excel/LibreOffice (codificación UTF-8 con BOM, separador correcto).
- El `.xlsx` contiene las mismas columnas y filas que la tabla filtrada.
- Mensajes claros de carga y error; sin errores en consola.
- Tests: generación de CSV (unitario front) y del endpoint de Excel (integración back).

## 5. Demostración (estado inicial → final)

| Antes | Después |
|-------|---------|
| La lista no ofrece exportación. | Botón "Exportar" con menú CSV/Excel; al elegir, el navegador descarga el fichero con las filas visibles. |

**Guion**: abrir la lista → aplicar un filtro → pulsar "Exportar" → "Excel" → se descarga el `.xlsx`
con exactamente esas filas → abrirlo para mostrar el contenido.

## 6. Delegación que ejecuta `nova-architect`

> El operador entrega **solo el objetivo** a `nova-architect`. El arquitecto **descompone y delega**
> creando estas sub-incidencias (una por agente de la org **NOVA**, `QPaperClip/containers/nova-org`),
> con dependencias entre ellas. No es un reparto hecho a mano por el operador.

| # | Sub-incidencia que crea el arquitecto | Agente delegado | Adapter | Depende de |
|---|----------------------------------------|-----------------|---------|------------|
| 1 | Endpoint `GET /export`, generación del `.xlsx` con POI reutilizando filtros y DTOs. Tests de integración. | **nova-service-gen** | Codex | — |
| 2 | Botón/menú de exportación, generación de CSV en cliente, descarga del `.xlsx`, estados de carga/error. Tests front. | **nova-frontend-gen** | Antigravity | — |
| 3 | Documentar el endpoint en OpenAPI y validar CORS para la descarga. | **nova-api-integr** | Antigravity | #1 |
| 4 | Verificar build + arranque Docker y ejecutar el gate `nova-post-gen-validation`. | **nova-release-mgr** | Codex | #1, #2 |

`nova-async-comm` y `nova-ops-monitor` quedan en **standby**.

### Flujo de ejecución (orquestado por el arquitecto)

1. El operador crea el Goal y asigna la **incidencia raíz a `nova-architect`**.
2. **nova-architect** descompone el objetivo (≤5 sub-tareas, sin cascadas) y **delega** las sub-incidencias anteriores, fijando el contrato del endpoint.
3. **nova-service-gen** y **nova-frontend-gen** implementan en paralelo (backend Excel / frontend CSV+descarga).
4. **nova-api-integr** documenta y verifica CORS.
5. **nova-release-mgr** valida y aplica el gate; **nova-architect** revisa y aprueba la entrega (PR en rama separada).
