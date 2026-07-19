# NOV-28 — Contratos Fijos para Delegación Máxima

## 1. Endpoint Backend — Calendario de Expiración

```
GET /api/v1/guarantees/expiry-calendar?month=YYYY-MM
```

**Respuesta** (200 OK):
```json
{
  "month": "2026-07",
  "days": [
    {
      "day": 1,
      "guarantees": [
        {
          "id": "...",
          "reference": "REF-001",
          "beneficiary": { "firstName": "...", "lastName": "..." },
          "amount": 100000,
          "currency": "EUR",
          "expiryDate": "2026-07-01",
          "daysUntilExpiry": 0,
          "riskLevel": "critical"
        }
      ],
      "totalByAmount": 100000,
      "totalByCurrency": { "EUR": 100000 },
      "aggregateRiskLevel": "critical"
    }
  ],
  "riskCatalog": {
    "none": "No vencimientos",
    "low": "≤7 días AND importe < 50k",
    "medium": "8-30 días OR importe 50k-200k",
    "high": "31-60 días OR importe > 200k",
    "critical": ">60 días OR importe muy alto"
  }
}
```

## 2. Scheduler — Expiración Automática

- **Configuración**: `@Scheduled(fixedRate = 30000)` en perfil demo (cada 30 segundos)
- **Lógica**: Busca avales en estado `ISSUED` o `AMENDED` con `expiryDate <= today`
- **Acción**: Transiciona a `EXPIRED`, registra motivo = "Auto-expired by scheduler", emite evento SSE
- **Logging**: `log.info("Scheduler: expired X guarantees")`

## 3. Evento SSE — Expiración en Vivo

**Canal**: `guarantee-events` (existente)

**Evento**:
```json
{
  "type": "expiration-auto",
  "guaranteeId": "...",
  "reference": "REF-001",
  "status": "EXPIRED",
  "expiryDate": "2026-07-01",
  "reason": "Auto-expired by scheduler",
  "expiredAt": "2026-07-19T12:00:00Z"
}
```

## 4. Métricas Prometheus

- **Counter**: `guarantees_expired_auto_total` — total de avales expirados automáticamente
- **Timer**: `guarantees_expiry_scheduler_duration_seconds` — tiempo de ejecución del scheduler
- **Gauge**: `guarantees_expiring_soon_count` — cantidad de avales que vencen en ≤7 días
- **Endpoint**: `/actuator/prometheus`

## 5. Frontend — /calendar

- **Vista**: CSS Grid mensual 7x6 (lunes-domingo)
- **Celda**: día + número de vencimientos + color de fondo por riesgo + tooltip con suma de importes
- **Panel lateral**: click en día rojo → lista de avales (referencia, beneficiario, importe, días restantes)
- **Badge en tabla**: chip de color + "vence en N días" — actualizado en vivo por SSE
- **Navegación**: mes anterior/siguiente
- **Estados**: loading, empty, error

## 6. Tests Requeridos

- **Backend**: integración del scheduler (transición a EXPIRED), cálculo de riesgo
- **Frontend**: /calendar mocks del endpoint, panel de detalle, badges

## 7. OpenAPI

- Documentar endpoint `/api/v1/guarantees/expiry-calendar` con parámetro month
- Documentar catálogo de riesgo (none|low|medium|high|critical) con reglas
- Documentar evento SSE `expiration-auto`
- Revalidar CORS

---

**Vigencia**: Fija hasta merge a main de NOV-28. No cambiar sin escalada a arquitecto.
