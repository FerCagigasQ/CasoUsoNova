# CasoUsoNova — Producto NOVA "Gestión de Pedidos" (GDPD)

> Producto NOVA desarrollado por 7 agentes autónomos (Claude) colaborando
> a través de [Paperclip](https://github.com/PaperclipAI/paperclip).

## NOVA Toolchain

Este repo incluye el toolchain NOVA completo para operar:

```bash
# Configurar entorno NOVA
source setup-nova.sh

# Verificar CLI
nova --version
# → NOVA 26.03, CLI 7.8.0
```

### Comandos NOVA CLI principales

| Comando | Descripción |
|---------|-------------|
| `nova create api` | Servicio API REST (Java 11, Spring Boot 2.7.18) |
| `nova create demon` | Demonio event-driven (JMS/ActiveMQ) |
| `nova create batch` | Job Spring Batch (Reader/Processor/Writer) |
| `nova create scheduler` | Scheduler con cron (scheduler.yml) |
| `nova create frontal` | Frontal Angular/Thin3 (CDN) |
| `nova generate-api-code` | Código cliente desde Swagger |
| `nova validate` | Validar servicios antes de release |
| `nova runtime start all` | Levantar runtime local completo |
| `nova runtime status` | Estado de los servicios |

### Runtime local (puertos)

| Servicio | Puerto |
|----------|--------|
| PostgreSQL | :5555 |
| API Gateway | :24000 |
| Config Server | :8888 |
| WebSeal Mock | :23000 |
| ActiveMQ | :8161 |
| CES Mock | :36000 |

## Estructura del repo

```
CasoUsoNova/
├── README.md                     ← Este fichero
├── DEMO-NOVA.md                  ← Guía paso a paso de la demo
├── nova.yml                      ← Config del producto GDPD
├── .paperclip.yaml               ← Config Paperclip: 7 agentes, roles, budgets
├── .gitattributes                ← Git LFS para binarios del toolchain
├── setup-nova.sh                 ← source setup-nova.sh para configurar env
├── docker-compose.nova.yml       ← Paperclip + PostgreSQL para Docker
│
├── toolchain/                    ← NOVA CLI + runtime (Git LFS)
│   ├── nova-le/                  ← NOVA Click 7.8.0
│   │   ├── nova-cli/bin/nova.js  ← Entry point del CLI
│   │   ├── generators/           ← Yeoman generators (api, demon, batch...)
│   │   ├── tools/                ← Java, Maven, PostgreSQL, runtime JARs
│   │   ├── nodejs/               ← Node.js 16 embebido
│   │   └── configuration/        ← Templates (gateway, webseal, etc.)
│   ├── zulu-jdk11/               ← Azul Zulu JDK 11.0.11
│   └── prepare-apis-generated.js ← Script generación código cliente API
│
├── aprendizajes/                 ← 10 docs extraídos del análisis del CLI
├── skills/                       ← 9 skills NOVA para agentes Paperclip
├── agents/                       ← AGENTS.md de cada agente
├── scripts/                      ← configure-nova-env.mjs, import-nova-company.mjs
└── docs/                         ← Documentación del producto (arquitectura, etc.)
```

## Los 7 agentes

| Agente | Rol | Descripción |
|--------|-----|-------------|
| `nova-architect` | CEO | Define arquitectura, delega, revisa PRs |
| `nova-service-gen` | Engineer | Crea servicios backend con NOVA CLI |
| `nova-frontend-gen` | Engineer | Crea frontales Angular/Thin3 |
| `nova-api-integr` | Engineer | Genera código cliente, configura Gateway |
| `nova-async-comm` | Engineer | Configura messaging asíncrono |
| `nova-release-mgr` | Engineer | Gestiona releases y despliegues |
| `nova-ops-monitor` | Engineer | Monitorización y operaciones |

## Stack técnico

| Dato | Valor |
|------|-------|
| NOVA CLI | v7.8.0 (NOVA: 26.03) |
| Java | Azul Zulu JDK 11.0.11 |
| Spring Boot | 2.7.18 |
| Maven | 3.8 |
| Angular | 12+ (generator-thin3 v7.5.0) |
| Node.js | 16 (embebido en toolchain) |

## Demo

Consulta [DEMO-NOVA.md](DEMO-NOVA.md) para la guía completa paso a paso.
