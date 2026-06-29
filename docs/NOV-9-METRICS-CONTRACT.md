# NOV-9: Contrato de API `/api/v1/metrics`

**Fecha de creación**: 2026-06-29  
**Scope**: Dashboard de KPIs con gráficas  
**Status**: Definición del contrato (Fase 1 - Arquitecto NOVA)

---

## 1. Endpoint `/api/v1/metrics`

### Descripción
Devuelve agregados de la plataforma para alimentar el dashboard de KPIs:
- Total de garantías
- Desglose por estado
- Desglose por tipo
- Evolución mensual

### Request
```http
GET /api/v1/metrics
Content-Type: application/json
```

### Response (200 OK)
```json
{
  "total": 10,
  "byStatus": {
    "DRAFT": 2,
    "ISSUED": 5,
    "EXPIRED": 2,
    "CLAIMED": 1
  },
  "byType": {
    "BANK_GUARANTEE": 6,
    "STAND_BY_LETTER": 3,
    "FINANCIAL_GUARANTEE": 1
  },
  "byMonth": [
    {
      "month": "2026-01",
      "count": 2
    },
    {
      "month": "2026-02",
      "count": 3
    },
    {
      "month": "2026-03",
      "count": 5
    }
  ]
}
```

### Anotaciones
- **CORS**: Habilitado para `http://localhost:4200` y `http://localhost`
- **Agregación**: Reutilizar el repositorio existente con queries eficientes (GROUP BY)
- **Cálculo de meses**: Usar fecha de creación (createdAt) de las garantías

---

## 2. Contrato del componente Dashboard

### Ruta
`/dashboard`

### Elementos UI
1. **Tarjetas KPI** (Grid 4 columnas)
   - Total de garantías
   - Total por cada estado (ISSUED, DRAFT, EXPIRED)
   - Total por cada tipo (si espacio permite)

2. **Gráfica de barras** (por mes)
   - Eje X: Mes (formato YYYY-MM)
   - Eje Y: Conteo de garantías

3. **Gráfica de tarta/donut** (dual)
   - Panel izquierdo: Desglose por estado
   - Panel derecho: Desglose por tipo

### Estados
- **Loading**: Skeleton cards o spinner durante fetch
- **Empty**: Mensaje "Sin datos disponibles" si `total === 0`
- **Responsive**: Mobile (<768px), Tablet (768-1024px), Desktop (>1024px)

---

## 3. Estructura del DTO (Backend)

```java
public class MetricsDTO {
    private Long total;
    private Map<String, Long> byStatus;
    private Map<String, Long> byType;
    private List<MonthlyCountDTO> byMonth;
}

public class MonthlyCountDTO {
    private String month;  // YYYY-MM
    private Long count;
}
```

---

## 4. Plan de Implementación (≤5 Sub-tareas)

| # | Sub-tarea | Agente | Dependencia |
|---|-----------|--------|------------|
| 1 | **Backend: Endpoint /api/v1/metrics** | nova-service-gen | Ninguna (inicio) |
| 2 | **Frontend: Dashboard UI + Mock** | nova-frontend-gen | 1 (contrato) |
| 3 | **Integración: OpenAPI + CORS** | nova-api-integr | 1 (completado) |
| 4 | **Release: Docker + Gate** | nova-release-mgr | 1, 2, 3 |
| *(Revisión)* | Revisión y QA | nova-architect | 1, 2, 3, 4 |

---

## 5. Criterios de Aceptación

✅ Endpoint `/api/v1/metrics` devuelve agregados correctamente  
✅ Dashboard carga datos reales del endpoint y pinta gráficas  
✅ Totales de gráficas cuadran con los datos de la tabla  
✅ Estados de carga y vacío funcionan  
✅ Responsive en móvil/escritorio  
✅ Sin errores en consola (modo estricto TypeScript)  
✅ Tests: integración (endpoint) + render (dashboard)  
✅ Documentación OpenAPI con ejemplos  
✅ Build Docker limpio  

---

## 6. Notas Técnicas

- **Librería de charts**: ngx-charts (compatible con Angular 17) o Chart.js
- **Base de datos**: H2 local (seed data con 10-15 garantías)
- **JPA Query**: `SELECT new com.example.guarantees.dto.MonthlyCountDTO(...) FROM Guarantee g GROUP BY YEAR(g.createdAt), MONTH(g.createdAt)`
- **Caching**: No requerido para demo
- **Paginación**: No requerida
- **Autenticación**: Heredada del servicio existente (CORS cubierto)

