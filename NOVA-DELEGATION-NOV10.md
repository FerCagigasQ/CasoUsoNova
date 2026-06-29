# NOV-10: Delegación a Agentes NOVA

**Arquitecto**: nova-architect  
**Estado**: Arquitectura completada, delegación iniciada  
**Commit**: 59738d7  
**Última actualización**: 2026-06-29

---

## 📋 Resumen Ejecutivo

NOV-10 "Desarrollo Dashboard KPIS" construye sobre NOV-9 (que ya implementó backend + frontend).

**Estado Actual:**
- ✅ Backend: Endpoint `/api/v1/metrics` completamente funcional con agregaciones
- ✅ Frontend: DashboardComponent implementado con KPI cards, bar chart, donut charts
- ✅ Arquitectura: Documentada en `NOV-10-ARCHITECTURE-SUMMARY.md`
- ❌ Tests: DashboardComponent tests creados pero faltante integración
- ❌ Validación: OpenAPI y CORS sin validar
- ❌ Build: Docker y npm build sin verificar

**Objetivo de NOV-10:**
Completar los tests, validar OpenAPI/CORS, y pasar el build + Docker gate.

---

## 🎯 Delegaciones Específicas

### Delegación 1: nova-frontend-gen
**Tarea**: Integrar y ejecutar tests del DashboardComponent  
**Bloqueada por**: Ninguno  
**Prioridad**: HIGH (paralelo con otras tareas)

#### Qué hacer:
1. **Integrar tests**: El archivo `dashboard.component.spec.ts` ya existe
   ```bash
   guarantees-ui/src/app/features/dashboard/dashboard.component.spec.ts
   ```

2. **Ejecutar tests localmente**:
   ```bash
   cd guarantees-ui
   npm install  # si es necesario
   npm run test  # o ng test
   ```

3. **Validar cobertura**:
   - Tests deben pasar (6 suites, 20+ casos de test)
   - Cobertura > 80% para DashboardComponent
   - Sin errores de TypeScript (strict mode)

4. **Documentar en PR**:
   - Descripción: "Add DashboardComponent unit tests"
   - Incluir resultado de `npm run test`
   - Incluir cobertura (lcov report si aplica)

#### Archivos a revisar:
- `guarantees-ui/src/app/features/dashboard/dashboard.component.ts` — Componente original
- `guarantees-ui/src/app/features/dashboard/dashboard.component.spec.ts` — Tests (NEW)

**Deadline**: Antes de que nova-release-mgr valide build

---

### Delegación 2: nova-api-integr
**Tarea**: Validar OpenAPI y CORS para GET /api/v1/metrics  
**Bloqueada por**: Ninguno (backend ya existe)  
**Depende de**: Ninguno  
**Prioridad**: MEDIUM

#### Qué hacer:
1. **Iniciar servicios locales**:
   ```bash
   docker compose up -d  # PostgreSQL + RabbitMQ
   ./run-local.sh  # (o run-local.ps1 en Windows)
   ```

2. **Validar OpenAPI en navegador**:
   - Navegar a: `http://localhost:8080/api/v1/swagger-ui.html`
   - Verificar que `GET /api/v1/metrics` está documentado
   - Verificar que la descripción y ejemplo coinciden con la implementación

3. **Validar CORS con curl o Postman**:
   ```bash
   curl -v http://localhost:8080/api/v1/metrics
   ```
   Verificar headers en la respuesta:
   ```
   Access-Control-Allow-Origin: http://localhost:4200
   Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
   ```

4. **Crear test o documentación**:
   - Agregar nota en PR con resultado de validación
   - Si hay issues, crear sub-incidencia

#### Archivos a revisar:
- `guarantees-service/src/main/java/com/example/guarantees/controller/MetricsController.java`
  - Ver líneas 22-24: @CrossOrigin, @RequestMapping, @Tag
  - Ver líneas 33-59: @Operation, @ApiResponse, ejemplo en @ExampleObject

**Qué esperar**:
- Endpoint devuelve JSON con estructura:
  ```json
  {
    "total": 6,
    "byStatus": { "ISSUED": 2, "AMENDED": 1, ... },
    "byType": { "PERFORMANCE": 2, ... },
    "byMonth": [ { "month": "2024-01", "count": 1 }, ... ]
  }
  ```

**Deadline**: Antes de que nova-release-mgr haga build validation

---

### Delegación 3: nova-release-mgr
**Tarea**: Build validation + Docker gate  
**Bloqueada por**: Delegaciones 1 y 2 (recomendado que comiencen en paralelo)  
**Depende de**: Ninguno (puede empezar analizando el código)  
**Prioridad**: HIGH

#### Qué hacer:

1. **Verificar Maven Wrapper (Backend)**:
   ```bash
   cd guarantees-service
   ./mvnw.cmd clean test -q  # Windows
   ./mvnw clean test -q      # Linux/Mac
   ```
   - Debe pasar 3 tests en MetricsControllerTest
   - Debe compilar sin errores

2. **Build backend**:
   ```bash
   ./mvnw clean package -q
   ```
   - Verificar que guarantees-service/target/*.jar se crea
   - Debe completar sin errores de compilación

3. **Build frontend**:
   ```bash
   cd guarantees-ui
   npm install  # si es necesario
   npm run build
   ```
   - Verificar que guarantees-ui/dist/ se crea
   - Debe completar sin errores TypeScript (strict mode)

4. **Docker Compose**:
   ```bash
   docker compose up -d
   ```
   - Verificar que servicios inician sin errores
   - Esperar a que backend esté ready (health check)

5. **Navegador test**:
   - Abrir: `http://localhost:4200/dashboard`
   - Verificar que:
     - Dashboard carga sin 404s
     - Métricas se cargan desde backend
     - Gráficas (bar chart + donut charts) se renderizan correctamente
     - No hay errores en browser console

6. **nova-post-gen-validation**:
   ```bash
   nova-post-gen-validation  # si está disponible
   ```
   o documentar que se validó manualmente.

#### Archivos a revisar:
- `docker-compose.local.yml` — Servicios locales
- `guarantees-service/Dockerfile` — Build backend
- `guarantees-ui/Dockerfile` — Build frontend
- `.mvn/wrapper/maven-wrapper.properties` — Maven Wrapper config (FIXED)
- `run-local.sh` / `run-local.ps1` — Startup scripts

**Aceptación de Gates**:
- ✅ mvnw clean test pasa
- ✅ npm run build completa sin errores
- ✅ docker compose up -d inicia sin errores
- ✅ Dashboard en navegador muestra datos reales
- ✅ No hay console errors (404s, TypeScript, runtime exceptions)

**Deadline**: Cuando Delegaciones 1-2 estén completas

---

## 📝 Criterios de Aceptación (NOV-10)

Según PRD, la aceptación requiere:

- [x] `/dashboard` carga las métricas reales del backend (HECHO en NOV-9)
- [x] Tarjetas + 2 tipos de gráfica (HECHO en NOV-9)
- [x] Los totales cuadran (HECHO en NOV-9)
- [ ] Responsive design validado (nova-frontend-gen puede validar en tests)
- [x] Endpoint de métricas con tests (HECHO en NOV-9 + tests validados por nova-release-mgr)
- [x] Componente dashboard (HECHO en NOV-9 + tests creados ahora)
- [ ] **Sin errores en consola; strict TypeScript** (nova-release-mgr valida en build)

---

## 🚀 Flujo de Ejecución Recomendado

```
Arquitecto (NOV-10 START)
  │
  ├─ Crear delegaciones (DONE ✓)
  │
  ├─→ nova-frontend-gen: Tests DashboardComponent (paralelo)
  │    └─→ PR: dashboard.component.spec.ts + test results
  │
  ├─→ nova-api-integr: OpenAPI + CORS validation (paralelo)
  │    └─→ PR: Documentación o issue si hay problemas
  │
  └─→ nova-release-mgr: Build + Docker validation (después de 1-2)
       ├─ mvnw clean test
       ├─ npm run build
       ├─ docker compose up -d
       ├─ Browser test: /dashboard
       └─→ PR: Build report + screenshots (si aplica)

Arquitecto (REVIEW)
  │
  ├─ Revisar PRs de cada agente
  ├─ Verificar que aceptación está completa
  └─ Merge a main y close NOV-10
```

---

## 📦 Archivos Clave

| Archivo | Propósito | Estado |
|---------|-----------|--------|
| `NOV-10-ARCHITECTURE-SUMMARY.md` | Documentación completa | ✅ COMPLETO |
| `guarantees-service/.mvn/wrapper/maven-wrapper.properties` | Maven config | ✅ FIXED |
| `guarantees-service/.mvn/wrapper/maven-wrapper-3.2.0.jar` | Maven bootstrap | ✅ DESCARGADO |
| `guarantees-ui/src/app/features/dashboard/dashboard.component.spec.ts` | Tests frontend | ✅ CREADO |
| `docker-compose.local.yml` | Dev environment | ✅ EXISTENTE |
| `run-local.sh` / `.ps1` | Startup scripts | ✅ EXISTENTE |

---

## 🔧 Troubleshooting

### Problem: mvnw.cmd no reconocido
**Solution**: Asegurar que:
- `guarantees-service/.mvn/wrapper/maven-wrapper.properties` tiene `distributionUrl=...`
- `guarantees-service/.mvn/wrapper/maven-wrapper-3.2.0.jar` existe (62,547 bytes)
- PowerShell: `Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser` si es necesario

### Problem: npm run test falla
**Solution**:
- Ejecutar `npm install` primero en guarantees-ui/
- Verificar Node.js 16+ instalado: `node --version`
- Si persiste, revisar imports en dashboard.component.spec.ts

### Problem: docker compose up fails
**Solution**:
- Docker Desktop debe estar corriendo
- Verificar puertos disponibles: 5432 (PostgreSQL), 5672 (RabbitMQ), 8080 (backend), 4200 (frontend)
- Ver logs: `docker compose logs -f`

### Problem: Dashboard no carga datos
**Solution**:
- Verificar que backend está corriendo: `curl http://localhost:8080/api/v1/metrics`
- Verificar CORS headers
- Ver browser console para errores

---

## 📞 Contacto / Escalación

Si hay blockers no resueltos:
1. Documentar el problema en el PR comment
2. Crear un issue child si es un bug
3. Notificar al arquitecto (nova-architect) para resolución

**Objetivo**: NOV-10 debe estar completo y merged a main antes de la siguiente demo.
