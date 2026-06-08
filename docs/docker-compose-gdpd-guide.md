# GDPD v1.0.0 - Docker Compose Deployment Guide

**Producto**: GDPD - Gestión de Pedidos  
**Versión**: 1.0.0  
**Referencia**: [docs/release-plan.md](./release-plan.md) | [docs/deploy-checklist.md](./deploy-checklist.md)

---

## Overview

El fichero `docker-compose-gdpd.yml` levanta el stack completo de GDPD v1.0.0 incluyendo:

**Backend Services**:
- `gdpd-pedidos-api` — API REST (puerto 8080)
- `gdpd-event-processor` — Procesador de eventos (puerto 8081)
- `gdpd-batch-reportes` — Batch (puerto 8082)
- `gdpd-scheduler-reportes` — Scheduler (puerto 8083)

**Frontend**:
- `gdpd-pedidos-front` — Angular SPA (puerto 4200)

**Infraestructura**:
- PostgreSQL 14 (BD principal)
- ActiveMQ 5.17 (broker INT)
- RabbitMQ 3.11 (broker PRE/PRO)

---

## Quick Start

### Desarrollo/INT
```bash
docker compose -f docker-compose-gdpd.yml --profile int up --build
```

### PRE/PRO
```bash
GDPD_ENV=pre docker compose -f docker-compose-gdpd.yml --profile pre up --build
```

---

## Verification

```bash
# API Health
curl http://localhost:8080/actuator/health

# Frontend
curl http://localhost:4200/health

# Database
docker exec -it gdpd-postgres psql -U nova -d novadb -c "SELECT version();"
```

---

## Cleanup

```bash
# Stop services
docker compose -f docker-compose-gdpd.yml down

# Remove volumes
docker compose -f docker-compose-gdpd.yml down -v
```

---

## See Also

- [Release Plan](./release-plan.md)
- [Deploy Checklist](./deploy-checklist.md)
