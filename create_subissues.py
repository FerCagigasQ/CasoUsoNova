import json, os, urllib.request, urllib.error

API_URL = os.environ.get('PAPERCLIP_API_URL', 'http://127.0.0.1:3100')
API_KEY = os.environ.get('PAPERCLIP_API_KEY', '')
RUN_ID = os.environ.get('PAPERCLIP_RUN_ID', '')
COMPANY_ID = os.environ.get('PAPERCLIP_COMPANY_ID', '212e808d-67aa-4a4c-a432-018a1e4564f8')
PARENT_ID = 'e46d9ef9-c897-44e5-b04d-cd9d5de3be1e'
PROJECT_ID = '5347f974-9dd4-4067-b9eb-7cc49445e051'

issues = [
    {
        'title': 'NOV-43A: Crear servicios backend GDPD con NOVA CLI',
        'assigneeAgentId': '5cbd6f1b-7c18-4f93-b23c-75700687cd50',
        'description': (
            '## Contexto\n'
            'Eres el Backend Service Generator del equipo NOVA. Debes crear los servicios backend del producto GDPD '
            'en https://github.com/FerCagigasQ/CasoUsoNova\n\n'
            '## Rama de trabajo\n'
            'Crea o usa la rama: `feature/gdpd-backend`\n\n'
            '## Estructura ya existente\n'
            'En `gdpd-backend/` existen subdirectorios base: gdpd-pedidos-api, gdpd-event-processor, '
            'gdpd-batch-reportes, gdpd-scheduler-reportes. Construye sobre esa base.\n\n'
            '## Tareas\n'
            '1. Clona https://github.com/FerCagigasQ/CasoUsoNova y crea rama `feature/gdpd-backend`\n'
            '2. `gdpd-backend/gdpd-pedidos-api/`:\n'
            '   - `PedidoController.java`: endpoints CRUD GET/POST/PUT/DELETE /api/pedidos\n'
            '   - `PedidoService.java`, `PedidoRepository.java` (Spring Data JPA), `Pedido.java` entidad JPA\n'
            '   - `application.yml`: Eureka client + datasource PostgreSQL + Actuator\n'
            '   - `pom.xml`: Spring Boot 2.7.x, Java 11, spring-data-jpa, activemq, eureka-client\n'
            '3. `gdpd-backend/gdpd-event-processor/`:\n'
            '   - Consumidor ActiveMQ queue `pedidos.eventos`\n'
            '   - `EventProcessorService.java`\n'
            '   - `pom.xml` con spring-boot-starter-activemq\n'
            '4. `gdpd-backend/gdpd-batch-reportes/`:\n'
            '   - Spring Batch Job con ItemReader/Processor/Writer\n'
            '5. `gdpd-backend/gdpd-scheduler-reportes/`:\n'
            '   - @Scheduled con cron que lanza el batch\n'
            '6. Commitea: `git add gdpd-backend/ && git commit -m "feat(backend): implement GDPD backend services"`\n'
            '7. Push: `git push origin feature/gdpd-backend`\n'
            '8. **OBLIGATORIO**: Crea Pull Request a `main` en https://github.com/FerCagigasQ/CasoUsoNova\n'
            '9. Comenta en esta issue con el link del PR\n\n'
            '## Co-author\n'
            'Incluye siempre: `Co-Authored-By: Paperclip <noreply@paperclip.ing>`'
        ),
    },
    {
        'title': 'NOV-43B: Crear frontal Thin3/Angular GDPD con NOVA CLI',
        'assigneeAgentId': 'a82cd4cc-fde4-484f-84d8-88af3390cf23',
        'description': (
            '## Contexto\n'
            'Eres el Frontend Generator del equipo NOVA. Debes completar el frontal Angular del producto GDPD.\n\n'
            '## Rama de trabajo\n'
            'Usa la rama: `feature/gdpd-frontend` (ya existe en remoto con estructura base)\n\n'
            '## Tareas\n'
            '1. Clona https://github.com/FerCagigasQ/CasoUsoNova y cambia a `feature/gdpd-frontend`\n'
            '2. En `gdpd-pedidos-front/src/app/features/pedidos/`:\n'
            '   - `pedidos-list.component.ts/html/css`: tabla bbva-web-table con paginacion\n'
            '   - `pedido-detail.component.ts/html`: vista detalle con bbva-web-card\n'
            '   - `pedido-create.component.ts/html`: formulario con bbva-web-form-field\n'
            '3. `gdpd-pedidos-front/src/app/core/pedidos-api.service.ts`: servicio HTTP para CRUD de pedidos\n'
            '4. `proxy.conf.json`: proxy `/api/**` -> `http://localhost:24000`\n'
            '5. `app-routing.module.ts`: lazy loading por feature module\n'
            '6. Verifica que `angular.json` tiene `proxyConfig: proxy.conf.json`\n'
            '7. Commitea: `git add gdpd-pedidos-front/ && git commit -m "feat(frontend): implement GDPD Angular frontend"`\n'
            '8. Push: `git push origin feature/gdpd-frontend`\n'
            '9. **OBLIGATORIO**: Crea Pull Request a `main` en https://github.com/FerCagigasQ/CasoUsoNova\n'
            '10. Comenta en esta issue con el link del PR\n\n'
            '## Stack\n'
            'Angular 12+, Thin3 BBVA, bbva-web-table, bbva-web-form-field, bbva-web-card\n\n'
            '## Co-author\n'
            'Incluye siempre: `Co-Authored-By: Paperclip <noreply@paperclip.ing>`'
        ),
    },
    {
        'title': 'NOV-43C: Generar codigo cliente y configurar integracion GDPD',
        'assigneeAgentId': '759b4bda-4d7c-47fa-8755-0f48fd657807',
        'description': (
            '## Contexto\n'
            'Eres el API Integration Expert del equipo NOVA. Configura la capa de integracion del producto GDPD.\n\n'
            '## Rama de trabajo\n'
            'Crea la rama: `feature/gdpd-integration`\n\n'
            '## Tareas\n'
            '1. Clona https://github.com/FerCagigasQ/CasoUsoNova y crea `feature/gdpd-integration` desde main\n'
            '2. Genera cliente Java desde Swagger:\n'
            '   - Swagger: `swagger/gdpd-pedidos-api.yaml` (crealo si no existe con todos los endpoints CRUD)\n'
            '   - Usa OpenAPI Generator, resultado en `api-generated/java-client/`\n'
            '3. Genera cliente TypeScript:\n'
            '   - Tipos TS en `gdpd-pedidos-front/src/app/core/api/`\n'
            '4. Configura API Gateway local en `gateway/application.yml`:\n'
            '   - Ruta: `/api/pedidos/**` -> `lb://gdpd-pedidos-api`, puerto 24000\n'
            '5. Configura mock server en `mock-server/` con datos de prueba\n'
            '6. Documenta en `docs/integracion.md`: flujo, endpoints, gateway config\n'
            '7. Commitea, push a `feature/gdpd-integration`\n'
            '8. **OBLIGATORIO**: Crea Pull Request a `main` y comenta el link en esta issue\n\n'
            '## Co-author\n'
            'Incluye siempre: `Co-Authored-By: Paperclip <noreply@paperclip.ing>`'
        ),
    },
    {
        'title': 'NOV-43D: Configurar comunicacion asincrona GDPD',
        'assigneeAgentId': '3cf04d18-50f3-4fb8-a961-da5839f91da4',
        'description': (
            '## Contexto\n'
            'Eres el Async Communication Expert del equipo NOVA. Configura la comunicacion asincrona del producto GDPD.\n\n'
            '## Rama de trabajo\n'
            'Crea la rama: `feature/gdpd-async`\n\n'
            '## Tareas\n'
            '1. Clona https://github.com/FerCagigasQ/CasoUsoNova y crea `feature/gdpd-async` desde main\n'
            '2. Back-to-back (ActiveMQ/Spring Cloud Stream):\n'
            '   - `gdpd-pedidos-api`: productor con Spring Cloud Stream, canal `pedidos-out`\n'
            '   - `gdpd-event-processor`: consumidor canal `pedidos-in` con binding a cola `pedidos.eventos`\n'
            '   - DLQ configurada: `pedidos.eventos.dlq` con max-redeliveries=3\n'
            '   - `application.yml` de ambos servicios con spring.cloud.stream config\n'
            '3. Back-to-front (SSE):\n'
            '   - `PedidoSseController.java` en gdpd-pedidos-api con SseEmitter en `/api/pedidos/events`\n'
            '   - En gdpd-pedidos-front: EventSource suscrito a `/api/pedidos/events` en pedidos-list.component\n'
            '4. Documenta en `docs/async-communication.md`:\n'
            '   - AsyncAPI YAML spec del canal `pedidos.eventos`\n'
            '   - Diagrama de flujo de mensajes\n'
            '   - Config DLQ y politica de reintentos\n'
            '5. Commitea, push a `feature/gdpd-async`\n'
            '6. **OBLIGATORIO**: Crea Pull Request a `main` y comenta el link en esta issue\n\n'
            '## Stack\n'
            'Spring Cloud Stream, ActiveMQ 5.x (dev), RabbitMQ 3.x (prod), SseEmitter, EventSource\n\n'
            '## Co-author\n'
            'Incluye siempre: `Co-Authored-By: Paperclip <noreply@paperclip.ing>`'
        ),
    },
    {
        'title': 'NOV-43E: Preparar release v1.0.0 y plan de despliegue GDPD',
        'assigneeAgentId': 'd7c851f3-98ce-4ccc-a680-eb4d5b9d0172',
        'description': (
            '## Contexto\n'
            'Eres el Release Manager del equipo NOVA. Prepara el plan de release del producto GDPD v1.0.0.\n\n'
            '## Estado actual\n'
            'NOTA: La rama `feature/gdpd-release` fue mergeada a main via PR #8 '
            '(https://github.com/FerCagigasQ/CasoUsoNova/pull/8) con documentacion de release. '
            'Revisa ese contenido en main.\n\n'
            '## Tareas\n'
            '1. Revisa en main: `docs/release-plan.md` y `docs/deploy-checklist.md`\n'
            '2. Si el contenido es completo (quality gates, flujo INT->PRE->PRO, criterios de aprobacion):\n'
            '   - Comenta en esta issue confirmando que PR #8 ya cubre el trabajo y adjunta resumen\n'
            '   - Esta issue puede cerrarse como done\n'
            '3. Si falta contenido critico:\n'
            '   - Crea rama `feature/gdpd-release-v2` y añade lo que falta\n'
            '   - Quality Gate: cobertura minima 80%, SonarQube, OWASP dependency-check\n'
            '   - Flujo promocion INT -> PRE -> PRO con criterios de aprobacion\n'
            '   - Revisa `docker-compose.nova.yml` para incluir todos los servicios GDPD\n'
            '   - Push + PR a main + comenta el link del PR en esta issue\n\n'
            '## Criterios de completitud para el release\n'
            '- release-plan.md: version semver, quality gates, criterios de promocion por entorno\n'
            '- deploy-checklist.md: checklist por entorno INT/PRE/PRO\n'
            '- docker-compose.nova.yml: todos los servicios GDPD con variables de entorno\n\n'
            '## Co-author\n'
            'Incluye siempre: `Co-Authored-By: Paperclip <noreply@paperclip.ing>`'
        ),
    },
    {
        'title': 'NOV-43F: Configurar monitorizacion y operaciones GDPD',
        'assigneeAgentId': '864c41d9-48f7-4ab6-9f7b-2aa76528a547',
        'description': (
            '## Contexto\n'
            'Eres el Operations Monitor del equipo NOVA. Configura la monitorizacion y operaciones del producto GDPD.\n\n'
            '## Rama de trabajo\n'
            'Crea la rama: `feature/gdpd-ops`\n\n'
            '## Tareas\n'
            '1. Clona https://github.com/FerCagigasQ/CasoUsoNova y crea `feature/gdpd-ops` desde main\n'
            '2. Configura Spring Boot Actuator para todos los servicios backend:\n'
            '   - `docs/actuator-config.yml` con config: management.endpoints.web.exposure.include=health,metrics,info\n'
            '   - Añade config a cada servicio en `gdpd-backend/*/src/main/resources/application.yml`\n'
            '3. Configura Micrometer + Prometheus:\n'
            '   - Dependencia `micrometer-registry-prometheus` en pom.xml de cada servicio\n'
            '   - Expone `/actuator/prometheus`\n'
            '4. Define alertas en `docs/gdpd-alerts.yml`:\n'
            '   - Prometheus AlertManager rules: alta latencia (>1s), errores 5xx (>5%), memoria >80%, cola ActiveMQ >1000 msgs\n'
            '5. Configura Spring Cloud Sleuth para distributed tracing en `docs/sleuth-config.yml`\n'
            '6. Documenta en `docs/operations.md`:\n'
            '   - Runbook: arrancar/parar servicios\n'
            '   - Health check URLs por servicio\n'
            '   - Procedimiento de escalado\n'
            '   - Eventos de log criticos a monitorizar\n'
            '7. Commitea, push a `feature/gdpd-ops`\n'
            '8. **OBLIGATORIO**: Crea Pull Request a `main` y comenta el link en esta issue\n\n'
            '## Stack\n'
            'Spring Boot Actuator, Micrometer, Prometheus, Spring Cloud Sleuth\n\n'
            '## Co-author\n'
            'Incluye siempre: `Co-Authored-By: Paperclip <noreply@paperclip.ing>`'
        ),
    },
]

url = f'{API_URL}/api/companies/{COMPANY_ID}/issues'
headers = {
    'Authorization': f'Bearer {API_KEY}',
    'X-Paperclip-Run-Id': RUN_ID,
    'Content-Type': 'application/json',
}

created = []
for issue in issues:
    payload = json.dumps({
        'title': issue['title'],
        'description': issue['description'],
        'parentId': PARENT_ID,
        'projectId': PROJECT_ID,
        'assigneeAgentId': issue['assigneeAgentId'],
        'status': 'todo',
        'priority': 'medium',
    }).encode('utf-8')

    req = urllib.request.Request(url, data=payload, headers=headers, method='POST')
    try:
        with urllib.request.urlopen(req) as resp:
            d = json.loads(resp.read().decode('utf-8'))
            created.append({'id': d.get('id','?'), 'identifier': d.get('identifier','?'), 'title': d.get('title','?')})
            print(f'OK: {d.get("identifier","?")} id={d.get("id","?")} — {d.get("title","?")}')
    except urllib.error.HTTPError as e:
        body = e.read().decode()
        print(f'ERROR {e.code} for "{issue["title"]}": {body[:200]}')

print(f'\nTotal creadas: {len(created)}')
