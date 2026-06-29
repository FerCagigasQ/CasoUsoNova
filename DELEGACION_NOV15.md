# NOV-15: Delegación Máxima — Dashboard v2

**Timestamp**: 2026-06-29 18:32 UTC  
**Issue Raíz**: [NOV-15 Desarrollo dashboard](https://paperclip.local/NOV/issues/NOV-15)  
**Delegante**: Arquitecto NOVA (`nova-architect`)  
**Estrategia**: Delegación máxima (todos los 7 agentes NOVA actuando en paralelo)

---

## Objetivo General

Mejorar el dashboard de KPIs v1 (que ya existe en `main`) hasta convertirlo en un panel analítico v2:
- Filtros por rango de fechas
- Gráficas con librería real (no SVG a mano)
- Métricas nuevas (importe por moneda, top beneficiarios)
- Actualización en vivo cuando cambian los datos
- Observabilidad del endpoint de métricas

---

## Sub-Incidencias Delegadas

### 1️⃣ SUB-1: NOV-18 — Repo Provisioner
**Asignatario**: Repo Provisioner  
**Descripción**: Añadir librería de charts al frontend  
**Bloqueadores**: Ninguno  
**Bloquea a**: SUB-3

**Alcance**:
- Seleccionar librería (`ngx-charts` u otra mantenida)
- Añadir a `package.json` con versión exacta
- Actualizar lockfile
- Verificar build sin errores

---

### 2️⃣ SUB-2: NOV-20 — Backend Service Generator
**Asignatario**: Backend Service Generator  
**Descripción**: Ampliar `/api/v1/metrics` con filtros, agregados nuevos y caché  
**Bloqueadores**: Ninguno  
**Bloquea a**: SUB-3, SUB-4, SUB-5, SUB-6

**Alcance**:
- Parámetros: `?from=YYYY-MM-DD&to=YYYY-MM-DD`
- Nuevos campos:
  - `totalAmountByCurrency` (EUR, USD, etc.)
  - `topBeneficiaries` (top 5 por nº de avales)
- Caché con `@Cacheable` + invalidación al cambiar datos
- Tests de integración

**Contrato de Respuesta**:
```json
GET /api/v1/metrics?from=2026-06-01&to=2026-06-30
{
  "total": 150,
  "byStatus": { "approved": 100, ... },
  "byType": { ... },
  "byMonth": { ... },
  "totalAmountByCurrency": { "EUR": 50000, "USD": 30000 },
  "topBeneficiaries": [
    { "name": "Empresa A", "count": 50 },
    ...
  ]
}
```

---

### 3️⃣ SUB-3: NOV-21 — Frontend Generator
**Asignatario**: Frontend Generator  
**Descripción**: Migrar gráficas a librería de charts, filtro de fechas, tarjetas nuevas  
**Bloqueadores**: SUB-1, SUB-2  
**Bloquea a**: SUB-7

**Alcance**:
- Reemplazar SVG a mano por charts reales
  - Barras por mes
  - Donut por estado y tipo
- Selector de rango de fechas que recarga métricas
- Mostrar tarjetas nuevas:
  - Importe por moneda
  - Top beneficiarios
- Mantener estados (carga, vacío, responsive)
- Tests de componente

---

### 4️⃣ SUB-4: NOV-22 — API Integration Expert
**Asignatario**: API Integration Expert  
**Descripción**: Documentar en OpenAPI, revalidar CORS  
**Bloqueadores**: SUB-2  
**Bloquea a**: SUB-7

**Alcance**:
- Actualizar spec OpenAPI/Swagger
- Documentar nuevos parámetros (`from`, `to`)
- Documentar nuevos campos (`totalAmountByCurrency`, `topBeneficiaries`)
- Añadir ejemplos de respuesta
- Validar CORS headers

---

### 5️⃣ SUB-5: NOV-23 — Async Communication Expert
**Asignatario**: Async Communication Expert  
**Descripción**: SSE/WebSocket para refresco en vivo  
**Bloqueadores**: SUB-2  
**Bloquea a**: SUB-7

**Alcance**:
- Publicar evento al crear/modificar/eliminar aval
- Exponer canal SSE o WebSocket
- Dashboard se suscribe y refresca automáticamente
- Sin reload de página
- Tests incluidos

---

### 6️⃣ SUB-6: NOV-24 — Operations Monitor
**Asignatario**: Operations Monitor  
**Descripción**: Instrumentar con Micrometer/Actuator  
**Bloqueadores**: SUB-2  
**Bloquea a**: SUB-7

**Alcance**:
- Instrumentar `/api/v1/metrics` con Micrometer
- Exponer `/actuator/health` (check verde)
- Exponer `/actuator/prometheus` (métricas)
- Contador de peticiones, latencia, errores
- Verificable en UI/Postman

---

### 7️⃣ SUB-7: NOV-25 — Release Manager
**Asignatario**: Release Manager  
**Descripción**: Verificar build + Docker y ejecutar gate  
**Bloqueadores**: SUB-3, SUB-4, SUB-5, SUB-6  
**Bloquea a**: Integración final

**Alcance**:
- Build de ambos servicios (backend + frontend)
- Arranque en Docker sin errores
- Ejecutar gate `nova-post-gen-validation`
- Verificar `/dashboard` funciona con todas las mejoras
- Commits + merge a main

---

## Flujo de Ejecución

```
🟢 INICIO (2026-06-29 18:32)
   │
   ├─► [Paralelo, sin bloqueadores]
   │   ├─ SUB-1 (charts library) ─┐
   │   └─ SUB-2 (metrics API)    ─┼─┬─┬─┬─┐
   │                              │ │ │ │ │
   │   [Esperando SUB-2]         │ │ │ │ │
   │   ├─ SUB-3 (frontend) ◄─────┘─┘ │ │ │
   │   ├─ SUB-4 (OpenAPI) ◄────────┘ │ │
   │   ├─ SUB-5 (SSE) ◄──────────────┘ │
   │   └─ SUB-6 (Actuator) ◄──────────┘
   │       │     │     │     │
   │       └─────┴─────┴─────┘
   │           │
   │   [Esperando SUB-3, 4, 5, 6]
   └─► SUB-7 (release) ✓
       │
       └─► CONVERGENCIA: NOV-15 → `done`
```

---

## Criterios de Aceptación (NOV-15)

- ✓ `/dashboard` usa charts reales (no SVG a mano)
- ✓ Filtro de fechas recarga y cifras cuadran
- ✓ Tarjetas nuevas (importe por moneda, top beneficiarios) visibles
- ✓ Al crear/cambiar aval, dashboard **se actualiza en vivo** (sin reload)
- ✓ Endpoint expone métricas (`/actuator/prometheus`) y healthcheck verde
- ✓ OpenAPI refleja nuevos parámetros/campos
- ✓ Sin errores en consola; TypeScript estricto sin `any`
- ✓ Tests: endpoint con filtros (integración) + componente (render con mock)

---

## Monitoreo (Arquitecto)

| Fase | Hito | Responsable | Estado | ETA |
|------|------|-------------|--------|-----|
| 1️⃣ Repo Setup | SUB-1 entregado | Repo Provisioner | ⏳ todo | — |
| 2️⃣ API | SUB-2 entregado | Backend Service Gen | ⏳ todo | — |
| 3️⃣ Integración | SUB-3, SUB-4, SUB-5, SUB-6 paralelo | 4 agentes | ⏳ todo | — |
| 4️⃣ Release | SUB-7 convergencia + validación | Release Mgr | ⏳ todo | — |
| 5️⃣ Cierre | NOV-15 → `done` | Arquitecto | ⏳ awaiting | — |

---

## Notas Técnicas

- **Maven Wrapper**: Todos los servicios incluyen `mvnw` + `.mvn/wrapper/`
- **Docker-Compose**: `docker-compose.local.yml` con PostgreSQL + RabbitMQ para local
- **Perfiles Spring**: `application-local.yml` desactiva Config Server, Eureka, bootstrap
- **Build**: `npm run build` (frontend) + `mvn clean package` (backend)
- **Run Local**: `./run-local.sh` (bash) o `.\run-local.ps1` (PowerShell)

---

## Documento Generado

- **Arquitecto**: Nova Architect (`nova-architect`)
- **Método**: Delegación máxima via Paperclip API
- **Timestamp**: 2026-06-29T18:32:00Z
- **Referencia**: `/DELEGACION_NOV15.md` (este archivo)
