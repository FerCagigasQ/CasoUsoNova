# FASE 2: Sub-Issues para Paperclip

**Parent Issue:** NOV-9 — Crear producto NOVA "Gestión de Pedidos" (GDPD) desde cero

**Status:** Listo para crear en Paperclip — Especificación completa para 6 sub-issues

---

## NOV-9A: Crear servicios backend con NOVA CLI

**Assignee Agent:** `nova-service-gen`  
**Status:** `todo`  
**Priority:** Critical

### Description:

Crear los 4 servicios backend del sistema GDPD utilizando NOVA CLI

### Tasks:

- [ ] Ejecutar `nova create api` para gdpd-pedidos-api (Java 11, Spring Boot)
- [ ] Ejecutar `nova create demon` para gdpd-event-processor
- [ ] Ejecutar `nova create batch` para gdpd-batch-reportes
- [ ] Ejecutar `nova create scheduler` para gdpd-scheduler-reportes
- [ ] Implementar endpoints CRUD (GET/POST/PUT/DELETE /api/pedidos)
- [ ] Implementar consumidor de eventos en event-processor
- [ ] Implementar job batch para reportes
- [ ] Crear rama `feature/gdpd-backend`
- [ ] Push y crear Pull Request a main
- [ ] Comentar link del PR en esta sub-issue

---

## NOV-9B: Crear frontal Thin3 con NOVA CLI

**Assignee Agent:** `nova-frontend-gen`  
**Status:** `todo`  
**Priority:** Critical

### Description:

Crear la interfaz de usuario frontal para GDPD usando Angular 12 + Thin3

### Tasks:

- [ ] Ejecutar `nova create frontal` para gdpd-pedidos-front
- [ ] Implementar vistas: lista, detalle, crear, editar pedidos
- [ ] Componentes BBVA: tabla, formulario, cards
- [ ] Routing con lazy loading
- [ ] Proxy config para API Gateway (:24000)
- [ ] Stores NgRx para gestión de estado
- [ ] Crear rama `feature/gdpd-frontend`
- [ ] Push y crear Pull Request a main
- [ ] Comentar link del PR en esta sub-issue

---

## NOV-9C: Generar código cliente y configurar integración

**Assignee Agent:** `nova-api-integr`  
**Status:** `todo`  
**Priority:** High

### Description:

Generar código cliente y configurar API Gateway

### Tasks:

- [ ] Ejecutar `nova generate-api-code` desde Swagger
- [ ] Ejecutar `prepare-apis-generated.js`
- [ ] Configurar API Gateway local (:24000 → :8080)
- [ ] Rate limiting y CORS
- [ ] Mock server para desarrollo
- [ ] Documentar en `docs/integracion.md`
- [ ] Crear rama `feature/gdpd-integration`
- [ ] Push y crear Pull Request a main
- [ ] Comentar link del PR en esta sub-issue

---

## NOV-9D: Configurar comunicación asíncrona

**Assignee Agent:** `nova-async-comm`  
**Status:** `todo`  
**Priority:** High

### Description:

Implementar ActiveMQ/RabbitMQ y SSE para comunicación asíncrona

### Tasks:

- [ ] Spring Cloud Stream + canales (pedidos.created, .updated, .deleted)
- [ ] Publisher en gdpd-pedidos-api
- [ ] Subscriber en gdpd-event-processor
- [ ] Dead Letter Queue para reintentos
- [ ] SSE endpoint (/api/pedidos/eventos)
- [ ] EventSource en Angular
- [ ] AsyncAPI spec en `docs/async-communication.md`
- [ ] Crear rama `feature/gdpd-async`
- [ ] Push y crear Pull Request a main
- [ ] Comentar link del PR en esta sub-issue

---

## NOV-9E: Preparar release v1.0.0 y plan de despliegue

**Assignee Agent:** `nova-release-mgr`  
**Status:** `todo`  
**Priority:** High

### Description:

Validar release con Quality Gates y documentar flujo de despliegue

### Tasks:

- [ ] Ejecutar `nova validate` para todos los servicios
- [ ] Quality Gates: cobertura 80%, SonarQube, análisis de seguridad
- [ ] CHANGELOG.md para v1.0.0
- [ ] `docs/release-plan.md`: versiones, dependencias, migración DB
- [ ] `docs/deploy-checklist.md`: pre/during/post, rollback
- [ ] Crear rama `feature/gdpd-release`
- [ ] Push y crear Pull Request a main
- [ ] Comentar link del PR en esta sub-issue

---

## NOV-9F: Configurar monitorización y operaciones

**Assignee Agent:** `nova-ops-monitor`  
**Status:** `todo`  
**Priority:** High

### Description:

Configurar observabilidad, métricas y alertas del sistema

### Tasks:

- [ ] `nova runtime start all`, `nova runtime status`
- [ ] Actuator endpoints: /health, /metrics, /info
- [ ] Health checks: Database, Broker, Eureka
- [ ] Micrometer + custom metrics
- [ ] Spring Cloud Sleuth para distributed tracing
- [ ] Alertas: CPU, memoria, errores HTTP, broker, DB pool
- [ ] `docs/operations.md`: arranque, troubleshooting, escalado
- [ ] `docs/monitoring-guide.md`: dashboards, métricas
- [ ] Crear rama `feature/gdpd-ops`
- [ ] Push y crear Pull Request a main
- [ ] Comentar link del PR en esta sub-issue

---

## Regla Obligatoria

✅ Cada agente:
- Crea rama `feature/gdpd-*` dedicada
- NO toca `main` directamente
- Hace commits descriptivos
- Push a su rama
- Crea PR a main
- Comenta link del PR en su sub-issue

---

**Generado por:** Arquitecto NOVA | **Fecha:** 2026-06-05 | **Estado:** Listo para FASE 2
