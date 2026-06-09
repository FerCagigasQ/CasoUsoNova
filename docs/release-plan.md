# Release Plan — GDPD v1.0.0

**Producto**: GDPD - Gestión de Pedidos  
**Versión**: 1.0.0  
**Fecha de release**: 2026-06-05  
**Release Manager**: Agent NOV-15 (nova-release-mgr)

---

## 1. Versiones de Componentes

| Componente | Versión | Tipo | Tecnología |
|---|---|---|---|
| gdpd-pedidos-api | 1.0.0 | API REST | Spring Boot 2.7.x / Java 11 |
| gdpd-event-processor | 1.0.0 | Daemon | Spring Boot 2.7.x / Java 11 |
| gdpd-batch-reportes | 1.0.0 | Batch | Spring Boot 2.7.x / Java 11 |
| gdpd-scheduler-reportes | 1.0.0 | Scheduler | Spring Boot 2.7.x / Java 11 |
| gdpd-pedidos-front | 1.0.0 | Frontend | Angular 12 / Thin3 |

## 2. Dependencias de Infraestructura

| Dependencia | Versión | Entorno | Notas |
|---|---|---|---|
| Zulu JDK | 11.0.x | Todos | Distribución OpenJDK certificada NOVA |
| Spring Boot | 2.7.x | Todos | Versión LTS soportada |
| Spring Cloud | 2021.0.x | Todos | Eureka, Config, Gateway |
| PostgreSQL | 14.x | INT/PRE/PRO | DB principal |
| RabbitMQ | 3.11.x | INT/PRE/PRO | Broker mensajería (PRE/PRO) |
| ActiveMQ | 5.17.x | dev/INT | Broker mensajería (dev/INT) |
| NOVA LE | 7.8.0 | dev | Toolchain de generación |
| Angular CLI | 12.x | dev | Frontend build |

## 3. Migraciones de Base de Datos

### Scripts Flyway (orden de ejecución)

```sql
-- V1__create_pedidos_table.sql
CREATE TABLE pedidos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_pedido VARCHAR(20) UNIQUE NOT NULL,
    cliente_id UUID NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    estado VARCHAR(20) NOT NULL DEFAULT 'CREADO',
    total DECIMAL(12,2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'EUR'
);

-- V2__create_estados_pedido.sql
CREATE TABLE estados_pedido (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    pedido_id UUID NOT NULL REFERENCES pedidos(id),
    estado_anterior VARCHAR(20),
    estado_nuevo VARCHAR(20) NOT NULL,
    fecha_cambio TIMESTAMP NOT NULL DEFAULT NOW(),
    usuario_id VARCHAR(100)
);

-- V3__create_indices_pedidos.sql
CREATE INDEX idx_pedidos_cliente ON pedidos(cliente_id);
CREATE INDEX idx_pedidos_estado ON pedidos(estado);
CREATE INDEX idx_pedidos_fecha ON pedidos(fecha_creacion DESC);
CREATE INDEX idx_estados_pedido_pedido_id ON estados_pedido(pedido_id);
```

### Procedimiento de migración
1. Ejecutar scripts en orden numérico (Flyway lo gestiona automáticamente)
2. Validar que `flyway_schema_history` registra versiones 1, 2, 3 como SUCCESS
3. Verificar conteo de registros si hay datos previos (en PRE/PRO siempre habrá datos de prueba)
4. En caso de fallo: Flyway bloquea migraciones posteriores — ver sección de Rollback

## 4. Flujo de Promoción INT → PRE → PRO

### 4.1 INT (Integración)

**Objetivo**: Validar integración de todos los servicios entre sí y con la infraestructura NOVA.

**Criterios de entrada**:
- Todos los servicios compilados y artefactos disponibles en Nexus
- `nova validate` aprobado para todos los servicios (ver CHANGELOG.md)
- Tests unitarios: cobertura ≥ 80% en todos los servicios
- SonarQube Quality Gate: Rating A (sin bloqueantes)
- 0 vulnerabilidades críticas o altas (OWASP Dependency Check)

**Criterios de promoción a PRE**:
- Tests de integración ejecutados con resultado ≥ 95% pass rate
- Tests de contrato (Pact) validados entre gdpd-pedidos-api y gdpd-pedidos-front
- Smoke tests de los 5 servicios OK
- Migraciones Flyway ejecutadas sin errores en BD INT
- Aprobación del Tech Lead del equipo GDPD

### 4.2 PRE (Preproducción)

**Objetivo**: Validar con datos de producción enmascarados y configuración idéntica a PRO.

**Criterios de entrada**:
- Promoción aprobada desde INT con todos los criterios cumplidos
- Plan de rollback documentado y revisado
- Ventana de despliegue acordada con Operations (mínimo 3 días laborables de antelación)

**Criterios de promoción a PRO**:
- Tests de regresión completos: ≥ 98% pass rate
- Tests de rendimiento: P95 latencia < 500ms en gdpd-pedidos-api bajo carga (100 RPS)
- Tests de carga: sistema estable bajo 2x tráfico esperado durante 30 minutos
- UAT (User Acceptance Testing) completado y firmado por el Product Owner
- Revisión de seguridad final (OWASP ZAP, Burp Suite si aplica)
- Aprobación del Chapter Lead y Operations Manager
- Notificación a usuarios finales si hay cambios de interfaz

### 4.3 PRO (Producción)

**Objetivo**: Despliegue controlado con capacidad de rollback inmediato.

**Estrategia de despliegue**: Blue/Green deployment
- Backend: despliegue servicio a servicio (orden: scheduler → batch → event-processor → api)
- Frontend: última en activar (zero-downtime con CDN cache invalidation)
- Validación de smoke tests antes de activar el traffic switch

**Criterios de aceptación post-despliegue**:
- Smoke tests de PRO OK en los 15 minutos posteriores al despliegue
- Métricas de negocio estables (tasa de pedidos procesados, errores < 0.1%)
- Monitorización activa durante 2 horas post-despliegue
- Confirmación del Product Owner

## 5. Matriz de Responsabilidades

| Actividad | Responsable | Aprobador |
|---|---|---|
| nova validate + Quality Gates | Dev Team GDPD | Tech Lead |
| Despliegue en INT | Dev Team GDPD | Tech Lead |
| Despliegue en PRE | Operations | Chapter Lead |
| UAT en PRE | Product Owner | Chapter Lead |
| Despliegue en PRO | Operations | Release Manager + Ops Manager |
| Go/No-Go final PRO | Release Manager | Chapter Lead + Ops Manager |

## 6. Comunicación y Notificaciones

- **T-5 días**: Anuncio de release en canal #gdpd-releases
- **T-1 día**: Recordatorio y checklist final a Operations
- **Día D**: Notificación de inicio de despliegue en #gdpd-ops
- **Post-despliegue**: Confirmación de éxito o incidente en #gdpd-releases
