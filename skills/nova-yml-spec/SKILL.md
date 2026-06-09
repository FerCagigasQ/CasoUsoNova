---
name: NOVA YAML Specification
slug: nova-yml-spec
description: Especificación completa del fichero nova.yml que define la configuración de cada servicio NOVA (subsistema, tipo, dependencias, propiedades, recursos).
---

# nova.yml — Especificación

El fichero `nova.yml` es el descriptor principal de cada servicio NOVA. Define su identidad, tipo, dependencias, propiedades por entorno, y recursos asignados.

## Estructura completa

```yaml
# ===== IDENTIFICACIÓN =====
subsistema: nombre-del-subsistema    # Repo Git donde vive el código
servicio:
  nombre: nombre-del-servicio        # Identificador único del servicio
  tipo: api                          # api | demon | batch | scheduler | frontal
  tecnologia: java11                 # java8 | java11 | angular12 | python37

# ===== DEPENDENCIAS =====
dependencias:
  # APIs que este servicio consume
  apis:
    - nombre: servicio-externo
      swagger: ./swagger/servicio-externo.yaml    # Ruta relativa al spec
      version: "1.0.0"
    - nombre: otro-servicio
      swagger: ./swagger/otro-servicio.yaml

  # Brokers/colas que usa
  brokers:
    - nombre: cola.eventos.clientes
      tipo: queue                    # queue | topic
      direccion: productor           # productor | consumidor | ambos
    - nombre: topic.alertas.sistema
      tipo: topic
      direccion: consumidor

  # Librerías internas que importa
  librerias:
    - nombre: lib-comun-nova
      version: "2.1.0"
      groupId: com.bbva.nova

# ===== PROPIEDADES POR ENTORNO =====
propiedades:
  - nombre: spring.datasource.url
    entorno:
      dev: jdbc:postgresql://localhost:5432/midb
      int: jdbc:postgresql://db-int.nova:5432/midb
      pre: jdbc:postgresql://db-pre.nova:5432/midb
      pro: jdbc:postgresql://db-pro.nova:5432/midb

  - nombre: spring.datasource.username
    entorno:
      dev: nova
      int: ${DB_USER_INT}
      pre: ${DB_USER_PRE}
      pro: ${DB_USER_PRO}

  - nombre: eureka.client.service-url.defaultZone
    entorno:
      dev: http://localhost:8761/eureka/
      int: http://eureka-int:8761/eureka/
      pre: http://eureka-pre:8761/eureka/
      pro: http://eureka-pro:8761/eureka/

  - nombre: spring.activemq.broker-url
    entorno:
      dev: tcp://localhost:61616
      int: tcp://activemq-int:61616
      pre: amqp://rabbitmq-pre:5672
      pro: amqp://rabbitmq-pro:5672

# ===== RECURSOS =====
recursos:
  # Compute
  cpu: 512m                         # Millicores
  memoria: 1Gi                      # Memory limit
  replicas:
    min: 1
    max: 4
    autoscaling:
      metrica: cpu
      umbral: 70

  # Filesystems
  filesystems:
    - nombre: fs-datos-entrada
      tipo: lectura                  # lectura | escritura | lectura-escritura
      path: /datos/entrada
      capacidad: 10Gi

  # Adaptadores (conexiones a sistemas externos)
  adaptadores:
    - nombre: adaptador-mainframe
      tipo: soap                     # soap | rest | jdbc | file
      wsdl: ./wsdl/mainframe.wsdl

  # Transferencias de ficheros
  transferencias:
    - nombre: fichero-diario
      tipo: ConnectDirect            # ConnectDirect | Xcom
      direccion: entrada             # entrada | salida
      planificacion: "0 6 * * MON-FRI"
      origen: /datos/externos/fichero.csv
      destino: /app/data/input/
```

## Ejemplos por tipo de servicio

### API REST

```yaml
subsistema: gestion-clientes
servicio:
  nombre: api-clientes
  tipo: api
  tecnologia: java11
dependencias:
  apis:
    - nombre: servicio-cuentas
      swagger: ./swagger/servicio-cuentas.yaml
propiedades:
  - nombre: spring.datasource.url
    entorno:
      dev: jdbc:postgresql://localhost:5432/clientesdb
      int: jdbc:postgresql://db-int:5432/clientesdb
      pre: jdbc:postgresql://db-pre:5432/clientesdb
      pro: jdbc:postgresql://db-pro:5432/clientesdb
recursos:
  cpu: 512m
  memoria: 1Gi
  replicas:
    min: 2
    max: 8
```

### Demonio (consumidor de broker)

```yaml
subsistema: notificaciones
servicio:
  nombre: demon-notificaciones
  tipo: demon
  tecnologia: java11
dependencias:
  brokers:
    - nombre: cola.eventos.clientes
      tipo: queue
      direccion: consumidor
propiedades:
  - nombre: spring.activemq.broker-url
    entorno:
      dev: tcp://localhost:61616
      int: tcp://activemq-int:61616
      pre: amqp://rabbitmq-pre:5672
      pro: amqp://rabbitmq-pro:5672
recursos:
  cpu: 256m
  memoria: 512Mi
  replicas:
    min: 2
    max: 8
```

### Batch

```yaml
subsistema: procesamiento-masivo
servicio:
  nombre: batch-procesar-ficheros
  tipo: batch
  tecnologia: java11
dependencias:
  librerias:
    - nombre: lib-parseo-csv
      version: "1.2.0"
propiedades:
  - nombre: spring.datasource.url
    entorno:
      dev: jdbc:postgresql://localhost:5432/batchdb
      int: jdbc:postgresql://db-int:5432/batchdb
      pre: jdbc:postgresql://db-pre:5432/batchdb
      pro: jdbc:postgresql://db-pro:5432/batchdb
recursos:
  cpu: 1000m
  memoria: 2Gi
  filesystems:
    - nombre: fs-input
      tipo: lectura
      path: /datos/input
  transferencias:
    - nombre: fichero-diario-clientes
      tipo: ConnectDirect
      direccion: entrada
      planificacion: "0 6 * * MON-FRI"
```

### Frontal (Angular/Thin3)

```yaml
subsistema: portal-clientes
servicio:
  nombre: frontal-clientes
  tipo: frontal
  tecnologia: angular12
dependencias:
  apis:
    - nombre: api-clientes
      swagger: ./swagger/api-clientes.yaml
    - nombre: api-notificaciones
      swagger: ./swagger/api-notificaciones.yaml
propiedades:
  - nombre: API_BASE_URL
    entorno:
      dev: http://localhost:8080
      int: https://int.nova.bbva.com/api
      pre: https://pre.nova.bbva.com/api
      pro: https://nova.bbva.com/api
recursos:
  cdn:
    tipo: estatico
    cache: 24h
```

## Reglas

1. El `nombre` del servicio debe ser único dentro del subsistema
2. El `tipo` determina qué templates genera `nova create`
3. Las `propiedades` se cargan via Spring Cloud Config Server (backend) o environment files (frontend)
4. Los `recursos` determinan la asignación en el contenedor Docker
5. Las `dependencias.apis` se usan con `nova generate-api-code` para generar clientes
6. Las `transferencias` se configuran automáticamente en Control-M al desplegar
