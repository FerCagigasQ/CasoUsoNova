# Release Plan v1.0.0 — GDPD

**Release Manager**: Paperclip Release Team  
**Date**: 2026-06-09  
**Target Version**: 1.0.0  

## Objetivo

Preparar release v1.0.0 del producto GDPD con validación NOVA CLI, criterios de calidad, y flujo de despliegue.

## Arquitectura GDPD

- **gdpd-pedidos-api**: API REST (Java 11, Spring Boot 2.7.18, Puerto 8080)
- **gdpd-event-processor**: Daemon event-driven (JMS/ActiveMQ)
- **gdpd-batch-reportes**: Spring Batch para reportes
- **gdpd-scheduler-reportes**: Scheduler con cron
- **gdpd-pedidos-front**: Angular 12 + Thin3 (Puerto 4200)

## Quality Gates

- Cobertura: >= 80%
- Vulnerabilidades críticas: 0
- SonarQube: PASSED
- Build: Exitoso sin warnings

## Validación NOVA CLI

```bash
cd gdpd-backend/gdpd-pedidos-api && nova validate api
cd gdpd-backend/gdpd-event-processor && nova validate demon
cd gdpd-backend/gdpd-batch-reportes && nova validate batch
cd gdpd-backend/gdpd-scheduler-reportes && nova validate scheduler
cd gdpd-pedidos-front && nova validate frontal
```

## Flujo Promoción

Feature /* → Main → INT → PRE → PRO

## Timeline

- Feature → Main: 2026-06-10 (2h)
- Main → INT: 2026-06-11 (30min)
- INT → PRE: 2026-06-12 (1h)
- PRE → PRO: 2026-06-14 10:00 UTC (1h)

## Entregables

- Dockerfiles multi-stage para 5 servicios
- docker-compose.yml
- release-plan.md
- deploy-checklist.md
- Database migration scripts v1.0.0

---
**Version**: 1.0.0  
**Last Updated**: 2026-06-09
