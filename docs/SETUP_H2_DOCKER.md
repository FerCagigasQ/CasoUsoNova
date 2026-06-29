# H2 Docker Setup - Quick Fix

## El Problema: "[Database "/root/test" not found]"

### Causa más frecuente: JDBC URL incorrecta en el formulario H2 Console

> **IMPORTANTE**: Este error ocurre casi siempre porque el formulario de login de H2 Console
> tiene por defecto `jdbc:h2:~/test` — en Docker eso se resuelve como `/root/test`.
>
> **Solución inmediata**: En `http://localhost:8080/h2-console`, cambia el campo
> **JDBC URL** de `~/test` a `jdbc:h2:mem:testdb` antes de hacer clic en Connect.

---

### Causa secundaria: configuración incorrecta de Spring Boot

El error también puede aparecer al arrancar si no existe `application-docker.yml`.

## Solución (3 pasos)

### Paso 1: Verificar que exista `application-docker.yml`

```bash
# Desde la raíz del repo
ls -la guarantees-service/src/main/resources/

# Debe mostrar:
# -rw-r--r--  application.yml
# -rw-r--r--  application-docker.yml  ← DEBE EXISTIR
# -rw-r--r--  application-local.yml   ← NUEVO
```

Si **NO existe**, está incluido en este fix.

### Paso 2: Limpiar la caché de Docker

```bash
# Eliminar containers antiguos
docker compose down

# Eliminar images (fuerza rebuild)
docker image rm guarantees-backend guarantees-frontend

# O simplemente
docker system prune -a
```

### Paso 3: Ejecutar con el archivo correcto

```bash
# Opción A: Producción (con frontend)
docker compose -f docker-compose.yml up --build

# Opción B: Dev-only backend (más rápido)
docker compose -f docker-compose.local.yml up --build
```

**Esperar a que el backend esté ready** (healthcheck pasa):
```
guarantees-backend-local  | ... Tomcat started on port(s): 8080 ...
guarantees-backend-local  | ... Started GuaranteesServiceApplication in X.XXX seconds
```

---

## Acceso

| URL | Propósito |
|-----|-----------|
| `http://localhost:8080/h2-console` | Consola H2 (user: `sa`, pass: vacío) |
| `http://localhost:8080/actuator/health` | Health check |
| `http://localhost:8080/swagger-ui.html` | API Swagger |
| `http://localhost:80` | Frontend (si usa docker-compose.yml) |

---

## ¿Por qué antes daba error?

**Sin `application-docker.yml`**:
- Spring Boot cargaba solo `application.yml`
- El perfil `docker` se activaba pero no había override
- H2 fallaba al intentar crear schema

**Con `application-docker.yml`**:
- Spring Boot carga `application.yml` + `application-docker.yml` (override)
- JDBC URL = `jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;...`
- H2 crea schema en RAM correctamente

---

## Configuración en detalle

### application-docker.yml (nuevo)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;INIT=CREATE SCHEMA IF NOT EXISTS public
    driverClassName: org.h2.Driver
```

**Parámetros**:
- `mem:testdb` → Base de datos en memoria, nombre "testdb"
- `DB_CLOSE_DELAY=-1` → No cierra inmediatamente (mantiene schema vivo)
- `DB_CLOSE_ON_EXIT=FALSE` → Permite reconexión después de shutdown
- `INIT=CREATE SCHEMA...` → Auto-crea schema "public"

### docker-compose.yml / docker-compose.local.yml
```yaml
services:
  backend:
    environment:
      SPRING_PROFILES_ACTIVE: docker  ← Activa el perfil
```

---

## Verificación rápida

```bash
# Ver logs de inicialización
docker logs guarantees-backend-local

# Buscar la línea correcta
docker logs guarantees-backend-local | grep -i "jdbc:h2"

# Debe mostrar algo como:
# Spring config: dataSource jdbc URL = jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;...
```

Si ves `/root/test` en los logs, el fix no se aplicó correctamente.

---

## Desarrollo iterativo

Si cambias configuración mientras el contenedor corre:

```bash
# Detener
docker compose down

# Limpiar volumenes (opcional, para fresh DB)
docker compose down -v

# Rebuild fuerza recompilación
docker compose up --build
```

---

## Documentación adicional

Para entender H2 en profundidad:
- Leer: `docs/H2_DATABASE_GUIDE.md`
- Leer: `docs/CODE_STRUCTURE.md`
- Leer: `README.md` (H2 Configuration section)

---

**Resultado esperado**: Error desaparece, H2 Console funciona, puedes crear/leer datos.

**Última actualización**: 2026-06-29
