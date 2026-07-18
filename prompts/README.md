# Prompts de desarrollo NOVA

Esta carpeta contiene **PRDs de desarrollo de código** pensados para **ver a los agentes de la
organización NOVA trabajar** sobre la plataforma. Cada prompt describe una **feature pequeña e
independiente** con un **efecto visible en la UI** (estado inicial → final), ideal para mostrar al inicio
y al final de una demo.

> Demos no oficiales de desarrollo. Cada PRD es una mejora de plataforma autocontenida; **no** introduce
> nuevo dominio de negocio de trade finance, sino capacidades transversales de la UI y la API.

## Propósito

- **Autocontenidos**: cada PRD se implementa por separado, de principio a fin.
- **Con efecto en la UI**: todos producen un cambio observable en pantalla (antes → después).
- **Full-stack cuando aplica**: frontend Angular 17 + Material y, si la feature lo requiere, backend
  Spring Boot 3.2 / Java 17.
- **Dirigidos por el arquitecto**: el operador entrega **solo el objetivo** a `nova-architect`
  (incidencia raíz); **no se asignan sub-tareas a mano**. El `nova-architect` **recibe el prompt,
  lo descompone con delegación máxima y delega una sub-incidencia por agente** (modo demo, sin
  cascadas), que escriben el código; luego revisa y aprueba.

## Demos

| # | PRD | Estado | Complejidad | Efecto en UI (inicial → final) | Agentes principales |
|---|-----|--------|-------------|--------------------------------|---------------------|
| 1 | [Tema oscuro / claro con preferencias y sync en vivo](./demo-01-tema-oscuro.md) | Pendiente (~45-60 min) | Avanzado | Tema claro fijo → conmutador que oscurece toda la UI, persiste en backend y sincroniza pestañas en vivo | toda la org NOVA |
| 2 | [Exportación CSV / Excel asíncrona con aviso en vivo](./demo-02-exportacion-datos.md) | Pendiente (~45-60 min) | Avanzado | Sin exportación → CSV instantáneo y Excel por job asíncrono con notificación SSE y descarga | toda la org NOVA |
| 3 | [Búsqueda global + paleta de comandos con resultados en vivo](./demo-03-busqueda-global.md) | Pendiente (~45-60 min) | Avanzado | Sin buscador → overlay `Ctrl/Cmd+K` que busca, navega y se refresca en vivo | toda la org NOVA |
| 4 | [Dashboard de KPIs con gráficas](./demo-04-dashboard-kpis.md) | ✔ Implementada (v1 y v2 en `main`) | Intermedio-Avanzado | Solo tabla → vista `/dashboard` con tarjetas y charts | toda la org NOVA |
| 5 | [Internacionalización ES/EN full-stack](./demo-05-internacionalizacion.md) | Pendiente (~45-60 min) | Avanzado | UI en un idioma → selector que traduce al vuelo (incl. errores del backend), persiste y sincroniza pestañas | toda la org NOVA |
| 6 | [Tablero Kanban con drag & drop en vivo](./demo-06-tablero-kanban.md) | Nueva (~45 min) | Avanzado | Tabla plana → tablero `/board` interactivo, sincronizado en vivo entre pestañas | toda la org NOVA |

## Organización de agentes NOVA

> Definidos en `QPaperClip/containers/nova-org/company/` (`COMPANY.md`, `.paperclip.yaml`, `agents/*/AGENTS.md`).

| Agente | Rol | Adapter |
|--------|-----|---------|
| **nova-architect** | CTO — descomposición técnica, contratos de API, code review, aprobación | Claude Code |
| **nova-repo-provisioner** | Bootstrap de repo y toolchain | Claude Code |
| **nova-service-gen** | Backend (Spring Boot / Java) | Codex |
| **nova-frontend-gen** | Frontend (Angular 17 + Material) | Antigravity |
| **nova-api-integr** | Integración, OpenAPI/Swagger, CORS, contrato de error | Antigravity |
| **nova-release-mgr** | Docker, release y gate de validación | Codex |
| **nova-async-comm** | Eventos / notificaciones reactivas | Codex |
| **nova-ops-monitor** | Observabilidad / healthchecks | Codex |

Todos los PRDs pendientes están diseñados con **delegación máxima**: los 7 agentes ejecutores trabajan
y **ningún agente queda en standby** (como en la demo del dashboard ya implementada).

Cada PRD incluye una sección **Delegación que ejecuta `nova-architect`** con las sub-incidencias que
el arquitecto crea y delega (una por agente, con sus dependencias) y el **flujo de ejecución**.

## Nivel de implementación

Cada PRD debe abordarse como un desarrollo **completo y proporcional** a su complejidad, no como una
maqueta. Reutilizar los patrones del repositorio, mantener contratos y compatibilidad, contemplar
validación, errores, estados de carga y vacío, accesibilidad y responsive, y añadir pruebas
proporcionales al riesgo. Toda entrega termina **validando los cambios, creando commit y haciendo
`push`** de la rama; se trabaja en **rama separada** y se entrega vía **PR** (sin merge directo a `main`).

## Cómo ejecutar una demo

```bash
git clone https://github.com/FerCagigasQ/CasoUsoNova.git
cd CasoUsoNova
docker compose up --build      # backend + frontend
# o, sin Docker:
./run-local.sh                 # Linux/Mac
.\run-local.ps1                # Windows
```

### Puntos de acceso

- **Frontend**: http://localhost (Docker) o http://localhost:4200 (dev)
- **Backend API**: http://localhost:8080/api/v1
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Consola H2**: http://localhost:8080/h2-console (usuario `sa`, sin contraseña)

---

**Última actualización**: 2026-07-18
