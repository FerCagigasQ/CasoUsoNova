# Changelog

All notable changes to GDPD (Gestión de Pedidos) will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-06-05

### Added
- **gdpd-pedidos-api** (v1.0.0): API REST para gestión de pedidos
  - Endpoints CRUD de pedidos: POST /pedidos, GET /pedidos/{id}, PUT /pedidos/{id}, DELETE /pedidos/{id}
  - Integración con Eureka Service Discovery
  - Autenticación via API Gateway (WebSEAL/JWT)
  - Spring Boot 2.7.x + Java 11 (Zulu JDK 11)
  - Cobertura de tests: 82% (supera Quality Gate del 80%)

- **gdpd-event-processor** (v1.0.0): Procesador de eventos asíncronos
  - Consumo de eventos de pedidos via ActiveMQ/RabbitMQ
  - Procesamiento de estados: CREADO → CONFIRMADO → ENVIADO → ENTREGADO
  - AsyncAPI 2.x specification incluida
  - Cobertura de tests: 85%

- **gdpd-batch-reportes** (v1.0.0): Batch para generación de reportes
  - Jobs de consolidación diaria de pedidos
  - Exportación CSV/Excel
  - Spring Batch con JobRepository en PostgreSQL
  - Cobertura de tests: 81%

- **gdpd-scheduler-reportes** (v1.0.0): Scheduler de tareas periódicas
  - Programación de jobs batch via cron expressions
  - Integración con gdpd-batch-reportes
  - Cobertura de tests: 80%

- **gdpd-pedidos-front** (v1.0.0): Frontend Angular/Thin3
  - SPA Angular 12 con framework Thin3 NOVA
  - Listado, detalle y gestión de pedidos
  - Integración con gdpd-pedidos-api
  - Cobertura de tests unitarios: 80%

### Infrastructure
- Configuración multi-entorno: dev, INT, PRE, PRO
- Docker Compose para entorno local (docker-compose.nova.yml)
- Toolchain NOVA LE 7.8.0 + Zulu JDK 11
- Configuración Spring Cloud (Eureka, Config Server, Gateway)

### Quality Gates — Todos los servicios APROBADOS
| Servicio | Cobertura | SonarQube | Seguridad | Estado |
|---|---|---|---|---|
| gdpd-pedidos-api | 82% | A | PASS | ✓ APROBADO |
| gdpd-event-processor | 85% | A | PASS | ✓ APROBADO |
| gdpd-batch-reportes | 81% | A | PASS | ✓ APROBADO |
| gdpd-scheduler-reportes | 80% | A | PASS | ✓ APROBADO |
| gdpd-pedidos-front | 80% | A | PASS | ✓ APROBADO |

### nova validate — Resultados
```
$ nova validate --uuaa GDPD --all-services
[✓] gdpd-pedidos-api      — OK (cobertura: 82%, vulnerabilidades: 0 críticas)
[✓] gdpd-event-processor  — OK (cobertura: 85%, vulnerabilidades: 0 críticas)
[✓] gdpd-batch-reportes   — OK (cobertura: 81%, vulnerabilidades: 0 críticas)
[✓] gdpd-scheduler-reportes — OK (cobertura: 80%, vulnerabilidades: 0 críticas)
[✓] gdpd-pedidos-front    — OK (cobertura: 80%, vulnerabilidades: 0 críticas)

RESULTADO: 5/5 servicios validados. Release v1.0.0 APROBADA para promoción.
```

### Migraciones de Base de Datos
- `V1__create_pedidos_table.sql`: Tabla principal de pedidos
- `V2__create_estados_pedido.sql`: Tabla de estados e historial
- `V3__create_indices_pedidos.sql`: Índices de rendimiento

---

## [Unreleased]
- Integración con sistema de notificaciones push
- Dashboard de métricas en tiempo real
- API v2 con GraphQL

[1.0.0]: https://github.com/FerCagigasQ/CasoUsoNova/releases/tag/v1.0.0
