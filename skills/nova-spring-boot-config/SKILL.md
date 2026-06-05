---
name: Spring Boot Configuration for NOVA
slug: nova-spring-boot-config
description: Plantillas y patrones de configuración Spring Boot 2.7.x para servicios NOVA — pom.xml, application.yml, bootstrap.yml, profiles, Config Server, y starters.
---

# Spring Boot Configuration — NOVA

## pom.xml template base

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.18</version>
    </parent>

    <groupId>com.bbva.nova</groupId>
    <artifactId>${servicio.nombre}</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <java.version>11</java.version>
        <spring-cloud.version>2021.0.8</spring-cloud.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- ===== CORE ===== -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- ===== CLOUD ===== -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <!-- ===== DATA ===== -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- ===== TEST ===== -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## Starters por tipo de servicio

| Tipo NOVA | Starters adicionales |
|-----------|---------------------|
| api | `spring-boot-starter-web`, `springdoc-openapi-ui` |
| demon | `spring-boot-starter-activemq` |
| batch | `spring-boot-starter-batch` |
| scheduler | `spring-boot-starter-batch`, `spring-boot-starter-quartz` |

## application.yml por tipo

### API REST

```yaml
spring:
  application:
    name: ${servicio.nombre}
  datasource:
    url: jdbc:postgresql://localhost:5432/${db.nombre}
    username: nova
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false
    properties:
      hibernate.dialect: org.hibernate.dialect.PostgreSQLDialect
      hibernate.jdbc.batch_size: 25

server:
  port: 8080
  servlet:
    context-path: /

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${random.value}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### Demonio (JMS Consumer)

```yaml
spring:
  application:
    name: ${servicio.nombre}
  activemq:
    broker-url: tcp://localhost:61616
    user: admin
    password: admin
  jms:
    listener:
      concurrency: 3
      max-concurrency: 10

# Sin server.port (no expone HTTP por defecto)
# O puerto para actuator solamente:
server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info
```

### Batch

```yaml
spring:
  application:
    name: ${servicio.nombre}
  datasource:
    url: jdbc:postgresql://localhost:5432/${db.nombre}
    username: nova
    password: ${DB_PASSWORD}
  batch:
    jdbc:
      initialize-schema: always
    job:
      enabled: false  # No ejecutar al arrancar; se lanza por scheduler o endpoint
```

## bootstrap.yml (Config Server client)

```yaml
spring:
  cloud:
    config:
      uri: http://localhost:8888
      name: ${spring.application.name}
      profile: ${spring.profiles.active:dev}
      fail-fast: true
      retry:
        max-attempts: 6
        initial-interval: 1000
        max-interval: 10000
        multiplier: 1.5
```

## Profiles y entornos NOVA

| Profile | Entorno NOVA | Activación |
|---------|-------------|------------|
| `dev` | Local (NOVA Click) | Por defecto |
| `int` | Integrado | `spring.profiles.active=int` |
| `pre` | Preproducción | `spring.profiles.active=pre` |
| `pro` | Producción | `spring.profiles.active=pro` |

Propiedades específicas por perfil se alojan en Config Server:
```
config-server/
├── application.yml          # Propiedades comunes
├── mi-servicio-dev.yml      # Solo para dev
├── mi-servicio-int.yml      # Solo para integrado
├── mi-servicio-pre.yml      # Solo para pre
└── mi-servicio-pro.yml      # Solo para producción
```

## Comandos Maven comunes

```bash
mvn clean install                  # Build + tests
mvn spring-boot:run               # Arrancar en dev
mvn package -DskipTests           # Build rápido sin tests
mvn dependency:tree                # Árbol de dependencias
mvn versions:display-dependency-updates  # Actualizaciones disponibles
```
