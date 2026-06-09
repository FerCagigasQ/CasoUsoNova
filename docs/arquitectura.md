# Arquitectura de GDPD - Gestión de Pedidos

## Visión General

**GDPD** es un sistema distribuido para la gestión integral del ciclo de vida de pedidos en NOVA COSMOS BBVA.

## Subsistemas Principales

### Backend (gdpd-backend/)
- **gdpd-pedidos-api:** API REST (Java 11, Spring Boot 2.7.x)
- **gdpd-event-processor:** Daemon consumidor de eventos
- **gdpd-batch-reportes:** Job batch para reportes
- **gdpd-scheduler-reportes:** Scheduler de ejecución periódica

### Frontend (gdpd-frontend/)
- **gdpd-pedidos-front:** Angular 12 / Thin3 BBVA

## Patrones de Comunicación

### Síncrono (REST)
```
Frontend → API Gateway (24000) → Eureka → gdpd-pedidos-api → PostgreSQL
```

### Asíncrono (ActiveMQ/RabbitMQ)
```
gdpd-pedidos-api → Broker → gdpd-event-processor → PostgreSQL
```

### Back-to-Front (SSE)
```
Backend (SseEmitter) → EventSource → Frontend (NgRx)
```

## Stack Tecnológico

| Componente | Tecnología |
|-----------|-----------|
| Backend | Java 11, Spring Boot 2.7.x |
| Frontend | Angular 12, Thin3 BBVA |
| Database | PostgreSQL 13+ |
| Broker | ActiveMQ (dev) / RabbitMQ (prod) |
| Service Discovery | Eureka |
| API Gateway | Spring Cloud Gateway |
| Config Server | Spring Cloud Config |
| Observabilidad | Spring Boot Actuator + Micrometer |

## Seguridad

- **Autenticación:** WebSeal/MicroGateway → JWT en API Gateway
- **Autorización:** Spring Security (role-based)
- **Capas:** Gateway → Servicios (Spring Security)

## Observabilidad

- **Métricas:** Micrometer/Prometheus
- **Trazas:** Spring Cloud Sleuth
- **Logs:** Actuator endpoints (/health, /metrics, /info)
- **Health:** Liveness + Readiness probes (K8s)

## Despliegue

- **Local (dev):** Docker Compose
- **Integración/Producción:** Kubernetes
- **CI/CD:** GitHub Actions + Quality Gate (SonarQube)

## Escalabilidad

- Circuit Breaker (Resilience4j)
- Reintentos automáticos + Dead Letter Queue
- Rate Limiting en API Gateway
- Horizontal scaling (réplicas configurables)

