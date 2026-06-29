# H2 Database Configuration Guide

## 1. Overview: ¿Cómo funciona H2 en NOVA?

### What is H2?
H2 es una base de datos SQL escrita en Java que puede ejecutarse:
- **In-Memory (`mem`)**: La DB vive en RAM, se destruye al terminar la JVM. Ideal para tests y demo local.
- **File-based (`file`)**: La DB persiste en disco. Útil para desarrollo con datos que no pierden.
- **Server mode**: H2 corre como servidor independiente.

### Architecture en CasoUsoNova

```
┌─────────────────────────────────────────────────┐
│          Docker Container (guarantees-backend)  │
│  ┌─────────────────────────────────────────┐   │
│  │      Spring Boot Application            │   │
│  │   (SPRING_PROFILES_ACTIVE=docker)       │   │
│  └────────────┬────────────────────────────┘   │
│               │                                 │
│               ▼                                 │
│  ┌─────────────────────────────────────────┐   │
│  │   application-docker.yml                │   │
│  │   ├─ JDBC URL: jdbc:h2:mem:testdb      │   │
│  │   ├─ H2 Console enabled: /h2-console   │   │
│  │   └─ Hibernate DDL-AUTO: update        │   │
│  └────────────┬────────────────────────────┘   │
│               │                                 │
│               ▼                                 │
│  ┌─────────────────────────────────────────┐   │
│  │   H2 In-Memory Database (RAM)           │   │
│  │   ├─ Schema: public (auto-created)      │   │
│  │   ├─ Tables: auto-generated por JPA     │   │
│  │   └─ Data: solo durante ejecución       │   │
│  └─────────────────────────────────────────┘   │
└─────────────────────────────────────────────────┘
         ▲
         │ HTTP requests
         │
    localhost:8080
    /h2-console (consola web)
    /actuator/health (health check)
    /api/* (application endpoints)
```

### Flujo de inicialización

1. **Docker start** → Spring Boot inicia con `SPRING_PROFILES_ACTIVE=docker`
2. **Profile load** → Spring carga `application.yml` + `application-docker.yml` (override)
3. **H2 connection** → Se crea una conexión a `jdbc:h2:mem:testdb`
4. **Schema creation** → H2 crea el schema `public` automáticamente
5. **Entity scanning** → Hibernate detecta `@Entity` classes
6. **DDL-AUTO** → `hibernate.ddl-auto=update` genera/actualiza tablas
7. **Ready** → Aplicación lista en puerto 8080

## 2. Configuration Files y su propósito

### `application.yml` (Default / Base)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb  # Base: H2 en memoria
    driverClassName: org.h2.Driver
    username: sa
    password: null
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update      # Auto-crea/actualiza tablas
  h2:
    console:
      enabled: true
      path: /h2-console
```

**Cuándo se usa**: Nunca actualmente, se sobrescribe por profil específico.

### `application-docker.yml` (Docker Profile)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS public
```

**Parámetros clave**:
- `DB_CLOSE_DELAY=-1`: No cierra la DB inmediatamente (mantiene el schema activo)
- `DB_CLOSE_ON_EXIT=FALSE`: No cierra en shutdown de la JVM (permite reconexión)
- `INIT=CREATE SCHEMA IF NOT EXISTS public`: Crea el schema automáticamente

**Cuándo se usa**: Al ejecutar `docker-compose up` (perfil `docker` activado en docker-compose.yml)

### `application-local.yml` (Local Development)
Idéntico a docker, pero sin `web-allow-others: true` (consola H2 solo accesible localhost).

**Cuándo se usa**: 
```bash
java -jar app.jar --spring.profiles.active=local
```

## 3. Database Schema & Entities

### Generated Tables (auto por Hibernate)

Cuando Hibernate detecta `@Entity` classes, crea tablas automáticamente. Ejemplo estructura esperada:

```sql
-- Tabla de Garantías (esperada en CasoUsoNova)
CREATE TABLE guarantee (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  guarantee_number VARCHAR(50) NOT NULL UNIQUE,
  status VARCHAR(20) NOT NULL,  -- ACTIVE, INACTIVE, EXPIRED
  amount DECIMAL(19,2) NOT NULL,
  currency VARCHAR(3),
  start_date TIMESTAMP,
  end_date TIMESTAMP,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Movimientos/Transacciones (esperada)
CREATE TABLE guarantee_movement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  guarantee_id BIGINT NOT NULL,
  movement_type VARCHAR(20) NOT NULL,  -- CREATION, UPDATE, CANCELLATION
  amount DECIMAL(19,2),
  reason VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (guarantee_id) REFERENCES guarantee(id)
);
```

### Ver el schema actual

1. **Desde H2 Console Web**:
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:mem:testdb`
   - User: `sa`
   - Password: (vacío)
   - Click "Connect"
   - Panel izq: ver todos los objetos del schema

2. **Desde logs Spring Boot**:
   ```
   docker logs guarantees-backend-local | grep -i "create table"
   ```

3. **Desde aplicación (SQL query)**:
   ```java
   @Repository
   public interface TableRepository extends JpaRepository<Object, Long> {
       @Query(value = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'", nativeQuery = true)
       List<String> getAllTableNames();
   }
   ```

## 4. Troubleshooting

### Error: "[Database "/root/test" not found...]"

**Causa**: H2 intenta crear/acceder a un fichero en `/root/test` pero no tiene permisos.

**Solución**:
1. Asegurar `jdbc:h2:mem:testdb` (in-memory, NO file-based)
2. Usar `DB_CLOSE_DELAY=-1` para mantener schema vivo
3. Usar perfil correcto: `SPRING_PROFILES_ACTIVE=docker` en docker-compose.yml

**Verificación**:
```bash
docker logs guarantees-backend-local | grep -i "jdbc url"
# Debe mostrar: jdbc:h2:mem:testdb
```

### Error: "Connection refused" desde H2 Console

**Causa**: Contenedor no está en la misma red o health check falla.

**Solución**:
```bash
# Verificar health
docker ps | grep guarantees-backend

# Verificar logs
docker logs guarantees-backend-local

# Verificar conectividad a la red
docker network inspect guarantees-network
```

### Error: "Table not found" en queries

**Causa**: Hibernate no ha ejecutado `ddl-auto: update` aún.

**Solución**:
1. Aumentar startup timeout en docker-compose (esperar más tiempo)
2. Ver logs de Hibernate DDL:
   ```yaml
   logging:
     level:
       org.hibernate.tool.hbm2ddl: DEBUG
   ```

## 5. Running & Testing

### Start en Docker (Producción simulada)
```bash
docker-compose up --build
# o con archivo local
docker-compose -f docker-compose.local.yml up --build
```

Acceder:
- API: `http://localhost:8080/actuator/health`
- H2 Console: `http://localhost:8080/h2-console`
- Swagger: `http://localhost:8080/swagger-ui.html`

### Start en Local (Dev)
```bash
# Terminal 1: Backend
cd guarantees-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# Terminal 2: Frontend
cd guarantees-ui
npm start
```

### Cleanup
```bash
# Detener containers
docker-compose down

# Eliminar volúmenes (NO aplica a H2 in-memory, pero por si acaso)
docker-compose down -v
```

## 6. Performance & Limitations

### In-Memory DB (Actual config)
| Aspecto | Valor |
|---------|-------|
| **Persistencia** | ❌ Se pierden datos al restart |
| **Velocidad** | ✅ Muy rápida (RAM) |
| **Multi-access** | ⚠️ Solo mismo JVM |
| **Ideal para** | Tests, demo, desarrollo rápido |

### Para Producción (no aplicable a demo)
Se usaría PostgreSQL en lugar de H2:
- Persistencia ✅
- Multi-access ✅
- Replicación ✅
- Backups ✅

## 7. Changing to File-Based H2

Si en el futuro necesitas persistencia en desarrollo:

**application-dev-file.yml**:
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/testdb;MODE=PostgreSQL;AUTO_SERVER=TRUE
    driverClassName: org.h2.Driver
```

Luego ejecutar:
```bash
java -jar app.jar --spring.profiles.active=dev-file
```

La DB se guardará en `./data/testdb.mv.db`.

---

**Last updated**: 2026-06-29  
**Status**: Verified working with Spring Boot 2.7.x, H2 2.x, Java 11+
