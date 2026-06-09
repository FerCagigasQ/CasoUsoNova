# Deploy Checklist — GDPD v1.0.0

**Producto**: GDPD - Gestión de Pedidos  
**Versión**: 1.0.0  
**Referencia**: [docs/release-plan.md](./release-plan.md)

---

## PRE-DESPLIEGUE

### 1. Verificaciones de Release (48h antes)
- [ ] `nova validate --uuaa GDPD --all-services` ejecutado y APROBADO
- [ ] Todos los artefactos publicados en Nexus con versión 1.0.0
- [ ] CHANGELOG.md revisado y aprobado por Tech Lead
- [ ] Release notes comunicadas al Product Owner
- [ ] Ventana de mantenimiento comunicada a usuarios (si aplica)

### 2. Verificaciones de Infraestructura (24h antes)
- [ ] Espacio en disco suficiente en servidores destino (≥ 20% libre)
- [ ] Conectividad verificada: servicios → PostgreSQL, RabbitMQ, Eureka, Config Server
- [ ] Configuración de entorno revisada (application-{env}.yml en Config Server)
- [ ] Certificados SSL vigentes (caducidad > 30 días)
- [ ] Credenciales de despliegue activas y con permisos correctos
- [ ] Plan de rollback revisado y accesible al equipo de Operations

### 3. Backup (2h antes del despliegue)
- [ ] Backup de BD PostgreSQL completado y verificado (restore probado en entorno aislado)
- [ ] Backup de configuración del Config Server
- [ ] Snapshot de instancias (si aplica según entorno)
- [ ] Backup disponible en storage offline (no solo en el mismo servidor)

### 4. Equipo y Comunicación (1h antes)
- [ ] Release Manager disponible y confirmado
- [ ] Operations Engineer designado para el despliegue
- [ ] Tech Lead disponible para soporte técnico
- [ ] Canal de comunicación de incidentes activo (#gdpd-ops)
- [ ] Número de escalada de Operations conocido por todos

---

## DURANTE EL DESPLIEGUE

### 5. Orden de Despliegue Backend (ejecutar en este orden)

#### 5.1 gdpd-scheduler-reportes
- [ ] Detener jobs activos (esperar finalización o cancelar jobs no críticos)
- [ ] Desplegar nueva versión: `nova deploy --service gdpd-scheduler-reportes --version 1.0.0`
- [ ] Verificar arranque: `nova status gdpd-scheduler-reportes` → RUNNING
- [ ] Smoke test: verificar que el scheduler registra próxima ejecución correctamente

#### 5.2 gdpd-batch-reportes
- [ ] Verificar que no hay jobs batch en ejecución
- [ ] Desplegar: `nova deploy --service gdpd-batch-reportes --version 1.0.0`
- [ ] Verificar arranque + migración Flyway completada (V1, V2, V3 → SUCCESS)
- [ ] Smoke test: lanzar job de prueba y verificar completación

#### 5.3 gdpd-event-processor
- [ ] Verificar cola de mensajes (RabbitMQ): drena o pausa antes del swap
- [ ] Desplegar: `nova deploy --service gdpd-event-processor --version 1.0.0`
- [ ] Verificar arranque y reconexión al broker
- [ ] Smoke test: publicar evento de prueba y verificar procesamiento

#### 5.4 gdpd-pedidos-api
- [ ] Desplegar: `nova deploy --service gdpd-pedidos-api --version 1.0.0`
- [ ] Verificar arranque y registro en Eureka
- [ ] Health check: `GET /actuator/health` → `{"status":"UP"}`
- [ ] Smoke test: `POST /pedidos` con payload de prueba → 201 Created
- [ ] Smoke test: `GET /pedidos/{id}` → 200 OK

### 6. Despliegue Frontend

#### 6.1 gdpd-pedidos-front
- [ ] Desplegar build Angular: `nova deploy --service gdpd-pedidos-front --version 1.0.0`
- [ ] Invalidar caché CDN si aplica
- [ ] Smoke test: cargar SPA en navegador, verificar login y listado de pedidos
- [ ] Verificar consola del navegador: 0 errores críticos (JS exceptions)

### 7. Verificaciones Post-Despliegue Inmediatas (primeros 15 min)
- [ ] Todos los servicios en estado RUNNING: `nova status --uuaa GDPD`
- [ ] Métricas de error rate < 0.1% en API Gateway
- [ ] Latencia P95 < 500ms en gdpd-pedidos-api
- [ ] No hay alertas activas en sistema de monitorización (Dynatrace/Prometheus)
- [ ] Eureka: los 4 servicios backend registrados correctamente
- [ ] Logs sin errores FATAL o ERROR no esperados

---

## POST-DESPLIEGUE

### 8. Validación de Negocio (primeras 2 horas)
- [ ] Flujo completo de pedido verificado (crear → confirmar → enviar)
- [ ] Reportes batch ejecutan correctamente en horario programado
- [ ] Eventos procesados sin pérdidas (verificar colas RabbitMQ: 0 mensajes no consumidos)
- [ ] Product Owner confirma funcionalidad OK
- [ ] Monitorización activa durante 2 horas post-despliegue

### 9. Cierre de Release
- [ ] Incidencias durante despliegue documentadas (si las hubo)
- [ ] Etiqueta Git creada: `git tag v1.0.0`
- [ ] Release publicada en GitHub: `gh release create v1.0.0`
- [ ] Comunicación de éxito en #gdpd-releases
- [ ] JIRA/Paperclip: cerrar tickets de la release
- [ ] Retrospectiva de release programada (si hubo incidencias)

---

## ROLLBACK

### Cuándo ejecutar rollback
Ejecutar rollback si se cumple cualquiera de las siguientes condiciones:
- Error rate > 1% durante más de 5 minutos tras el despliegue
- Algún servicio no arranca tras 3 intentos
- Migración Flyway falla (estado FAILED en flyway_schema_history)
- Funcionalidad crítica de negocio no operativa
- Decisión del Release Manager o Tech Lead

### Procedimiento de Rollback

#### Rollback Backend (orden inverso al despliegue)
```bash
# 1. Revertir API primero (detiene nuevas peticiones a la nueva versión)
nova deploy --service gdpd-pedidos-api --version 0.9.x

# 2. Revertir Event Processor
nova deploy --service gdpd-event-processor --version 0.9.x

# 3. Revertir Batch
nova deploy --service gdpd-batch-reportes --version 0.9.x

# 4. Revertir Scheduler
nova deploy --service gdpd-scheduler-reportes --version 0.9.x
```

#### Rollback Base de Datos
```bash
# Solo si las migraciones Flyway causaron el problema
# PRECAUCIÓN: requiere restaurar backup previo

# Opción A: Restaurar backup (DESTRUCTIVA — requiere ventana de mantenimiento)
pg_restore -d gdpd_db /backups/gdpd_pre_v1.0.0.dump

# Opción B: Si V3 falló (solo índices — más segura)
DROP INDEX IF EXISTS idx_pedidos_cliente;
DROP INDEX IF EXISTS idx_pedidos_estado;
DROP INDEX IF EXISTS idx_pedidos_fecha;
DROP INDEX IF EXISTS idx_estados_pedido_pedido_id;
DELETE FROM flyway_schema_history WHERE version = '3';

# Opción C: Si V2 falló (tabla estados — requiere análisis)
-- Consultar con DBA antes de ejecutar
```

#### Rollback Frontend
```bash
nova deploy --service gdpd-pedidos-front --version 0.9.x
# Reinvalidar caché CDN si aplica
```

#### Verificación post-rollback
- [ ] Todos los servicios en versión anterior RUNNING
- [ ] Flujo básico de pedidos operativo
- [ ] Errores en log acotados (no nuevos errores)
- [ ] Comunicar rollback a Product Owner y Management
- [ ] Abrir incidencia con RCA (Root Cause Analysis)
- [ ] Programar post-mortem en las 24h siguientes

---

## Contactos de Emergencia

| Rol | Responsabilidad | Canal |
|---|---|---|
| Release Manager | Go/No-Go, coordinación | #gdpd-ops |
| Tech Lead GDPD | Soporte técnico, decisiones de rollback | #gdpd-ops |
| Operations Engineer | Ejecución del despliegue | #gdpd-ops |
| DBA | Rollback de BD, migraciones | #dba-oncall |
| Product Owner | Validación de negocio, Go/No-Go final | #gdpd-releases |
