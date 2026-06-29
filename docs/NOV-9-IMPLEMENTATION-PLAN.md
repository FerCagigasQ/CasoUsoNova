# NOV-9: Plan de Implementación del Dashboard de KPIs

**Issue**: NOV-9 — Desarrollo del dashboard de kpis  
**Arquitecto**: nova-architect (Claude Code)  
**Fecha de creación**: 2026-06-29  
**Duración estimada**: 4-5 horas  
**Estado**: 4 sub-tareas creadas, listas para delegación

---

## 📋 Resumen Ejecutivo

Implementaremos un **dashboard analítico** (`/dashboard`) con **tarjetas KPI** y **gráficas** (barras + donut) que se alimentan de un **nuevo endpoint de métricas** (`GET /api/v1/metrics`) en el backend.

### Entregables Finales
1. Endpoint `/api/v1/metrics` devolviendo agregados de garantías
2. Ruta `/dashboard` con tarjetas y gráficas funcionales
3. Documentación OpenAPI con CORS validado
4. Build Docker limpio y gate de validación

---

## 🎯 Sub-tareas (4 total)

### Tarea #1: Backend — Endpoint `/api/v1/metrics`
**Agente**: nova-service-gen  
**Dependencias**: Ninguna (inicio)  
**Tiempo estimado**: 60-90 min

**Qué hacer:**
1. Crear clase `MetricsDTO` con campos:
   - `total: Long`
   - `byStatus: Map<String, Long>` (DRAFT, ISSUED, EXPIRED, CLAIMED)
   - `byType: Map<String, Long>` (BANK_GUARANTEE, STAND_BY_LETTER, FINANCIAL_GUARANTEE)
   - `byMonth: List<MonthlyCountDTO>` (mes + count)

2. Crear clase `MonthlyCountDTO` con campos:
   - `month: String` (formato YYYY-MM)
   - `count: Long`

3. Implementar `MetricsController`:
   ```java
   @GetMapping("/api/v1/metrics")
   public ResponseEntity<MetricsDTO> getMetrics() { ... }
   ```

4. Usar `GuaranteeRepository` con queries JPA:
   - COUNT(*) total
   - COUNT(*) GROUP BY status
   - COUNT(*) GROUP BY type
   - COUNT(*) GROUP BY YEAR, MONTH del createdAt

5. Incluir tests de integración validando los agregados

**Salida esperada:**
```bash
curl http://localhost:8080/api/v1/metrics
{
  "total": 10,
  "byStatus": { "DRAFT": 2, "ISSUED": 5, "EXPIRED": 2, "CLAIMED": 1 },
  "byType": { "BANK_GUARANTEE": 6, "STAND_BY_LETTER": 3, "FINANCIAL_GUARANTEE": 1 },
  "byMonth": [ { "month": "2026-01", "count": 2 }, ... ]
}
```

**Referencia**: `docs/NOV-9-METRICS-CONTRACT.md` § 1-3

---

### Tarea #2: Frontend — Componente `/dashboard`
**Agente**: nova-frontend-gen  
**Dependencias**: Tarea #1 (contrato definido; puede usar mock hasta que esté disponible)  
**Tiempo estimado**: 90-120 min

**Qué hacer:**
1. Crear ruta `/dashboard` en `app.routes.ts`:
   ```typescript
   { path: 'dashboard', component: DashboardComponent }
   ```

2. Crear `DashboardComponent` que:
   - Injecte `GuaranteeService` (existente)
   - Llame a un nuevo método `getMetrics()` que haga GET a `/api/v1/metrics`

3. Crear `MetricsService` (si no existe) que devuelva Observable<MetricsDTO>

4. Implementar UI con **Angular Material**:
   - **Tarjetas KPI** (Grid 2x2 o similar):
     - Card 1: Total (número grande)
     - Card 2: ISSUED (conteo)
     - Card 3: DRAFT (conteo)
     - Card 4: EXPIRED (conteo)
   - **Gráfica de barras** (por mes):
     - Usar ngx-charts o Chart.js
     - Eje X: Mes (YYYY-MM)
     - Eje Y: Conteo
   - **Gráfica de tarta/donut** (dual):
     - Panel izq: Estado (ISSUED, DRAFT, EXPIRED, CLAIMED)
     - Panel dcho: Tipo (BANK_GUARANTEE, STAND_BY_LETTER, FINANCIAL_GUARANTEE)

5. Implementar **estados**:
   - **Loading**: Skeleton cards + spinner mientras fetch
   - **Empty**: Mensaje "Sin datos disponibles" si total === 0
   - **Success**: Datos completos

6. **Responsive design**:
   - Mobile (<768px): Stack vertical
   - Tablet (768-1024px): 2 columnas
   - Desktop (>1024px): 3-4 columnas + 2 gráficas lado a lado

7. Añadir enlace en navegación (app.component.html):
   ```html
   <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
   ```

8. Tests:
   - Render con datos mock
   - Validar gráficas se renderizan correctamente
   - Validar responsive (simular breakpoints)

**Mock data** (usar inicialmente):
```json
{
  "total": 10,
  "byStatus": { "DRAFT": 2, "ISSUED": 5, "EXPIRED": 2, "CLAIMED": 1 },
  "byType": { "BANK_GUARANTEE": 6, "STAND_BY_LETTER": 3, "FINANCIAL_GUARANTEE": 1 },
  "byMonth": [
    { "month": "2026-01", "count": 2 },
    { "month": "2026-02", "count": 3 },
    { "month": "2026-03", "count": 5 }
  ]
}
```

**Salida esperada:**
- Ruta `/dashboard` carga sin errores
- Tarjetas visibles con totales
- Gráficas se renderizan con datos mock
- En consola: sin errores de TypeScript (modo estricto)

**Referencia**: `docs/NOV-9-METRICS-CONTRACT.md` § 2

---

### Tarea #3: Integración — OpenAPI + CORS
**Agente**: nova-api-integr  
**Dependencias**: Tarea #1 (completada)  
**Tiempo estimado**: 30-45 min

**Qué hacer:**
1. Documentar `GET /api/v1/metrics` en OpenAPI:
   - Añadir anotaciones `@Operation` y `@ApiResponse` en `MetricsController`
   - Incluir ejemplo de respuesta (200 OK)
   - Describir qué representa cada campo

2. Verificar CORS:
   - El endpoint debe ser accesible desde http://localhost:4200
   - Revisar que `@CrossOrigin` en el controller cubre ambos orígenes
   - (Nota: GuaranteeController ya tiene CORS habilitado)

3. Validar en Swagger:
   - Acceder a http://localhost:8080/swagger-ui.html
   - Verificar que `/api/v1/metrics` aparece
   - Probar "Try it out" desde el navegador

**Salida esperada:**
```
✅ GET /api/v1/metrics visible en Swagger
✅ Ejemplo de respuesta documentado
✅ Llamada desde curl/navegador no genera error CORS
```

**Referencia**: `docs/NOV-9-METRICS-CONTRACT.md` § 1

---

### Tarea #4: Release — Build Docker + Gate
**Agente**: nova-release-mgr  
**Dependencias**: Tareas #1, #2, #3 (completadas)  
**Tiempo estimado**: 45-60 min

**Qué hacer:**
1. Verificar build del backend:
   ```bash
   cd guarantees-service
   ./mvnw clean package
   # Debe completarse sin errores
   ```

2. Verificar build del frontend:
   ```bash
   cd guarantees-ui
   npm install
   npm run build
   # Debe crear dist/ sin errores
   ```

3. Ejecutar full stack con Docker:
   ```bash
   docker compose up --build
   # Esperar a que ambos servicios estén listos (logs verdes)
   ```

4. Validaciones:
   - [ ] Backend responde en http://localhost:8080 (200)
   - [ ] Frontend carga en http://localhost (200)
   - [ ] `/api/v1/metrics` devuelve JSON (200)
   - [ ] `/dashboard` carga sin errores de red
   - [ ] Gráficas se renderizan con datos reales del backend
   - [ ] Consola del navegador sin errores
   - [ ] Docker logs sin excepciones

5. Ejecutar gate (si disponible):
   ```bash
   nova runtime start all  # Si toolchain está disponible
   # O validar manualmente que todo funciona
   ```

6. Tests:
   - [ ] Tests del backend pasan: `./mvnw test`
   - [ ] Tests del frontend pasan: `npm test` (si aplica)

**Salida esperada:**
```
✅ docker compose up completa sin errores
✅ /dashboard carga y muestra datos reales
✅ Totales de gráficas coinciden con tabla de garantías
✅ No hay errores en consola (navegador + Docker logs)
```

---

## 🔄 Flujo de Ejecución

```
Tarea #1 (Backend)  ──┐
                      ├─→ Tarea #3 (OpenAPI + CORS)
Tarea #2 (Frontend)  ──┤
                      └─→ Tarea #4 (Docker + Gate)
```

**Ejecución recomendada:**
1. Empezar Tareas #1 y #2 en **paralelo** (se comunican via mock)
2. Una vez #1 completada, continuar con #3
3. Una vez #1, #2, #3 completadas, ejecutar #4

---

## ✅ Criterios de Aceptación Global

El dashboard está **completo y funcional** cuando:

- ✅ `/api/v1/metrics` devuelve agregados correctamente
- ✅ `/dashboard` carga las métricas reales del backend
- ✅ Tarjetas KPI muestran totales correctos
- ✅ Gráfica de barras y donut se renderizan correctamente
- ✅ Totales de gráficas cuadran con los datos de la tabla `/guarantees`
- ✅ Responsive en móvil/escritorio/tablet
- ✅ Estados de carga (loading) y vacío funcionan
- ✅ No hay errores en consola del navegador
- ✅ No hay `any` en TypeScript (modo estricto)
- ✅ Tests de integración (backend) + render (frontend) pasan
- ✅ Documentación OpenAPI está actualizada
- ✅ Docker compose up funciona de extremo a extremo
- ✅ Crear/editar una garantía se refleja en el dashboard (opcional, pero deseable)

---

## 📚 Documentos de Referencia

- **Contrato de API**: `docs/NOV-9-METRICS-CONTRACT.md`
- **Setup Local**: README.md § Quick Start
- **H2 Database**: `docs/H2_DATABASE_GUIDE.md`
- **Código existente**: 
  - Backend: `guarantees-service/src/main/java/com/example/guarantees/`
  - Frontend: `guarantees-ui/src/app/`

---

## 🚀 Comandos Útiles

### Backend
```bash
# Desarrollo local
cd guarantees-service
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"

# Tests
./mvnw test

# Build Docker
docker build -f Dockerfile -t guarantees-service:latest .
```

### Frontend
```bash
# Desarrollo local
cd guarantees-ui
npm install
npm start   # http://localhost:4200

# Build producción
npm run build

# Tests (si están configurados)
npm test
```

### Full Stack
```bash
# Todo junto con Docker
docker compose up --build

# Solo backend (más rápido para dev)
docker compose -f docker-compose.local.yml up --build
```

### Validación
```bash
# Swagger
curl -s http://localhost:8080/swagger-ui.html

# Endpoint de métricas
curl -s http://localhost:8080/api/v1/metrics | jq

# Frontend
curl -s http://localhost/ | head -20
```

---

## 📝 Notas Técnicas

- **Base de datos**: H2 in-memory con seed data de 10-15 garantías
- **Librería de charts**: ngx-charts (recomendado) o Chart.js
- **Responsive**: Usar CSS Grid o flexbox con breakpoints de Material
- **Caché**: No requerido para demo
- **Seguridad**: Heredada del servicio existente
- **TypeScript**: Modo estricto (no permitir `any`)
- **Tests**: Integración en backend, render en frontend

---

## ❓ Q&A Rápido

**P: ¿Qué pasa si el backend no está disponible?**  
R: El frontend usa mock data. El dashboard sigue siendo visible y funcional.

**P: ¿Puedo cambiar los colores de las gráficas?**  
R: Sí, pero mantén coherencia visual (Material Design).

**P: ¿Se necesita autenticación?**  
R: No para esta demo (CORS ya está habilitado en el backend).

**P: ¿Qué pasa si hay 0 garantías?**  
R: Mostrar estado vacío ("Sin datos disponibles") y tarjetas con 0.

---

**Status**: ✅ Plan completo. Listo para delegación a agentes.

