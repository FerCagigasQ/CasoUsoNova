---
name: Deploy Pipeline & Release
slug: nova-deploy-pipeline
description: Ciclo de release NOVA, Docker multi-stage builds, Spring Boot Actuator, Jenkins pipeline, promoción entre entornos, calidad (SonarQube) y seguridad (OWASP).
---

# Deploy Pipeline — NOVA

## Ciclo de vida del release

```
Local (dev) → Integrado → Preproducción → Producción
     │             │             │              │
  nova runtime   auto-deploy   aprobación    doble aprobación
  nova validate  tests CI      tests UAT     smoke tests
```

## Prerequisitos del toolchain

Antes de crear el pipeline de deploy:
1. **Runtime local**: Verificar que `nova runtime start core` levanta correctamente los servicios core:
   - `postgresql` (:5555), `nova-local-gateway` (:24000), `config-server` (:8888), `nova-webseal-mock` (:23000)
2. **Validación**: `nova validate-swagger <path>` para APIs, `nova validate-batch-scheduler` para batch
3. **Maven**: El `settings.xml` debe estar configurado con repos NOVA. Ruta real: `$NOVA_HOME/tools/maven/conf/`
4. **Java 11**: Si el proyecto usa Java 11, sustituir `$NOVA_HOME/tools/java/` por Zulu JDK 11
5. **Frontales Angular**: Ejecutar `node prepare-apis-generated.js` antes de `ng build` para compilar e instalar las librerías API generadas

## Dockerfile multi-stage

```dockerfile
# BUILD (usa la misma versión de JDK que Zulu 11 del toolchain)
FROM maven:3.8-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

# RUNTIME
FROM openjdk:11-jre-slim
WORKDIR /app
RUN addgroup --system app && adduser --system --ingroup app app
USER app
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## Dockerfile frontend (Angular)

```dockerfile
# BUILD
FROM node:16-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build -- --configuration=production

# SERVE
FROM nginx:alpine
COPY --from=build /app/dist/mi-frontal /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
```

## Spring Boot Actuator

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  health:
    db:
      enabled: true
    jms:
      enabled: true
```

Endpoints:
- `/actuator/health` → Liveness probe
- `/actuator/health/readiness` → Readiness probe
- `/actuator/info` → Build info
- `/actuator/prometheus` → Métricas Prometheus

## Jenkins Pipeline

```groovy
pipeline {
    agent any
    environment {
        REGISTRY = 'registry.nova.bbva.com'
        SERVICE = 'mi-servicio'
    }
    stages {
        stage('Build') { steps { sh 'mvn clean package -B' } }
        stage('Test') {
            steps { sh 'mvn verify -B' }
            post { always { junit '**/surefire-reports/*.xml' } }
        }
        stage('Quality') {
            steps {
                withSonarQubeEnv('SonarQube') { sh 'mvn sonar:sonar' }
                waitForQualityGate abortPipeline: true
            }
        }
        stage('Security') {
            steps { dependencyCheck additionalArguments: '--scan ./' }
        }
        stage('Docker') {
            steps {
                script {
                    def img = docker.build("${REGISTRY}/${SERVICE}:${BUILD_NUMBER}")
                    docker.withRegistry("https://${REGISTRY}") { img.push() }
                }
            }
        }
        stage('Deploy INT') {
            when { branch 'develop' }
            steps { sh "nova deploy --env integrado --version ${BUILD_NUMBER}" }
        }
    }
}
```

## Checklist pre-release

1. `nova validate <tipo>` sin errores
2. Tests unitarios >80% coverage
3. Tests integración passing
4. SonarQube quality gate OK
5. OWASP scan sin vulnerabilidades críticas
6. Swagger/OpenAPI actualizado
7. `nova.yml` correcto por entorno
8. Docker image publicada
9. `/actuator/health` → 200
10. Release notes documentadas

## Promoción entre entornos

| Paso | Entorno | Requisito | Automático |
|------|---------|-----------|------------|
| 1 | Integrado | Push a Git | Sí |
| 2 | Preproducción | Aprobación 1 persona | No |
| 3 | Producción | Aprobación 2 personas | No |

## Rollback

```bash
# Si algo falla en producción:
# 1. Portal NOVA → Releases → Seleccionar versión anterior
# 2. Promover versión anterior a producción
# 3. Verificar /actuator/health
# 4. Crear post-mortem
```
