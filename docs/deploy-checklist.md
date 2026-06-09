# Deploy Checklist v1.0.0 — GDPD

**Deployment Order**: Config -> Eureka -> Gateway -> API -> Daemon -> Scheduler -> Batch -> Frontend

---

## Pre-Deployment Validation

- [ ] Release Tag: v1.0.0 creado en git
- [ ] Docker Images: Todas pusheadas a registry
- [ ] Database Backups: Completado
- [ ] Rollback Plan: Documentado y testeado
- [ ] Stakeholders Notificados: BBVA, On-call, DBA
- [ ] Maintenance Window: Comunicado a usuarios

---

## 1. Configuration Server

**Servicio**: config-server  
**Puerto**: 8888  

### Pre-Deploy

```bash
git log --oneline config-repo/application-{int,pre,pro}.yml | head -3
yamllint config-repo/
docker build -t config-server:1.0.0 ./config-server
docker push registry/config-server:1.0.0
```

### Deploy

```bash
kubectl apply -f k8s/config-server/deployment.yaml --namespace=gdpd-prod
kubectl rollout status deployment/config-server -n gdpd-prod --timeout=5m
```

### Health Check

```bash
curl -s http://config-server.gdpd-prod.svc:8888/health | jq .
curl -s http://config-server:8888/gdpd-pedidos-api/production | jq '.propertySources[0].source'
```

**Sign-Off**: _________________ Date: _______

---

## 2. Eureka (Service Discovery)

**Servicio**: eureka-server  
**Puerto**: 8761  

### Pre-Deploy

```bash
docker build -t eureka-server:1.0.0 ./eureka-server
docker push registry/eureka-server:1.0.0
```

### Deploy

```bash
kubectl apply -f k8s/eureka-server/deployment.yaml --namespace=gdpd-prod
kubectl rollout status deployment/eureka-server -n gdpd-prod --timeout=5m
```

### Health Check

```bash
curl -s http://eureka-server.gdpd-prod.svc:8761/eureka/apps | grep '<application>' | wc -l
curl -s http://eureka-server:8761/eureka/status | grep '<status>'
```

**Sign-Off**: _________________ Date: _______

---

## 3. API Gateway

**Servicio**: api-gateway  
**Puerto**: 24000  

### Pre-Deploy

```bash
cat k8s/api-gateway/application-prod.yml | grep -A 2 'routes:'
docker build -t api-gateway:1.0.0 ./api-gateway
docker push registry/api-gateway:1.0.0
```

### Deploy

```bash
kubectl apply -f k8s/api-gateway/deployment.yaml --namespace=gdpd-prod
kubectl rollout status deployment/api-gateway -n gdpd-prod --timeout=5m
kubectl get svc api-gateway -n gdpd-prod
```

### Health Check

```bash
curl -s http://api-gateway:24000/actuator/health | jq .
curl -s -H "Authorization: Bearer DUMMY" http://api-gateway:24000/api/health
```

**Sign-Off**: _________________ Date: _______

---

## 4. gdpd-pedidos-api (Core API)

**Servicio**: gdpd-pedidos-api  
**Puerto**: 8080  

### Pre-Deploy

```bash
docker build -t gdpd-pedidos-api:1.0.0 ./gdpd-backend/gdpd-pedidos-api
docker push registry/gdpd-pedidos-api:1.0.0
```

### Deploy

```bash
kubectl apply -f k8s/gdpd-pedidos-api/deployment.yaml --namespace=gdpd-prod
kubectl rollout status deployment/gdpd-pedidos-api -n gdpd-prod --timeout=10m
kubectl get pods -l app=gdpd-pedidos-api -n gdpd-prod
```

### Health Check

```bash
curl -s http://gdpd-pedidos-api:8080/actuator/health | jq .status
curl -s http://gdpd-pedidos-api:8080/actuator/health/db | jq .status
curl -s -H "Authorization: Bearer $(get-oauth-token)" \
  http://api-gateway:24000/api/pedidos?limit=10 | jq '.items | length'
```

**Sign-Off**: _________________ Date: _______

---

## 5. gdpd-event-processor (Daemon)

**Servicio**: gdpd-event-processor  

### Pre-Deploy

```bash
docker build -t gdpd-event-processor:1.0.0 ./gdpd-backend/gdpd-event-processor
docker push registry/gdpd-event-processor:1.0.0
```

### Deploy

```bash
kubectl apply -f k8s/gdpd-event-processor/deployment.yaml --namespace=gdpd-prod
kubectl rollout status deployment/gdpd-event-processor -n gdpd-prod --timeout=10m
```

### Health Check

```bash
kubectl logs -f deployment/gdpd-event-processor -n gdpd-prod | grep 'Listening'
curl -s http://localhost:8080/actuator/health/custom | jq '.broker'
```

**Sign-Off**: _________________ Date: _______

---

## 6. gdpd-scheduler-reportes (Scheduler)

**Servicio**: gdpd-scheduler-reportes  

### Pre-Deploy

```bash
docker build -t gdpd-scheduler-reportes:1.0.0 ./gdpd-backend/gdpd-scheduler-reportes
docker push registry/gdpd-scheduler-reportes:1.0.0
```

### Deploy

```bash
kubectl apply -f k8s/gdpd-scheduler-reportes/deployment.yaml --namespace=gdpd-prod
kubectl rollout status deployment/gdpd-scheduler-reportes -n gdpd-prod --timeout=10m
```

### Health Check

```bash
kubectl logs -f deployment/gdpd-scheduler-reportes -n gdpd-prod | grep 'Scheduler initialized'
```

**Sign-Off**: _________________ Date: _______

---

## 7. gdpd-batch-reportes (Batch Job)

**Servicio**: gdpd-batch-reportes  
**Tipo**: CronJob  

### Pre-Deploy

```bash
docker build -t gdpd-batch-reportes:1.0.0 ./gdpd-backend/gdpd-batch-reportes
docker push registry/gdpd-batch-reportes:1.0.0
```

### Deploy

```bash
kubectl apply -f k8s/gdpd-batch-reportes/cronjob.yaml --namespace=gdpd-prod
kubectl get cronjobs -n gdpd-prod | grep batch-reportes
```

### Health Check

```bash
kubectl get jobs -l app=gdpd-batch-reportes -n gdpd-prod --sort-by=.metadata.creationTimestamp
```

**Sign-Off**: _________________ Date: _______

---

## 8. gdpd-pedidos-front (Frontend)

**Servicio**: gdpd-pedidos-front  
**Puerto**: 4200 / CDN  

### Pre-Deploy

```bash
npm run build --prefix gdpd-pedidos-front
docker build -t gdpd-pedidos-front:1.0.0 ./gdpd-pedidos-front
docker push registry/gdpd-pedidos-front:1.0.0
```

### Deploy

```bash
kubectl apply -f k8s/gdpd-pedidos-front/deployment.yaml --namespace=gdpd-prod
kubectl rollout status deployment/gdpd-pedidos-front -n gdpd-prod --timeout=5m
kubectl apply -f k8s/gdpd-pedidos-front/ingress.yaml --namespace=gdpd-prod
```

### Health Check

```bash
curl -s http://gdpd-pedidos-front:4200/health | jq .
curl -s http://gdpd-pedidos-front:4200/index.html | grep '<title>'
```

**Sign-Off**: _________________ Date: _______

---

## Post-Deployment Validation

- [ ] All Services UP: kubectl get pods -n gdpd-prod
- [ ] No CrashLoopBackOff
- [ ] Metrics Ingesting
- [ ] Logs Flowing to ELK
- [ ] Alerts OK
- [ ] External Integrations OK

---

## Rollback Procedure

```bash
kubectl rollout undo deployment/gdpd-pedidos-api -n gdpd-prod
kubectl rollout status deployment/gdpd-pedidos-api -n gdpd-prod --timeout=5m
kubectl exec -it db-pod -- psql -U postgres -d gdpd < rollback-v1.0.0.sql
```

---

**Deployment Version**: 1.0.0  
**Status**: PENDING
