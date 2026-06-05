---
name: Spring Batch Configuration
slug: nova-batch-config
description: Patrones y configuración de Spring Batch 4.3.x para servicios NOVA — Jobs, Steps, Chunk processing, ItemReader/Processor/Writer, scheduling, y monitorización.
---

# Spring Batch Configuration — NOVA

## Arquitectura Spring Batch

```
JobLauncher → Job → Step1 → Step2 → ... → StepN
                      │
                      ├── Tasklet (simple)
                      └── Chunk (reader → processor → writer)
                            │         │            │
                            ▼         ▼            ▼
                        ItemReader  ItemProcessor  ItemWriter
                        (CSV/DB)    (transform)   (DB/file)
```

## Dependencias Maven

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Job Configuration (Chunk-oriented)

```java
@Configuration
@EnableBatchProcessing
public class ProcesamientoJobConfig {

    @Bean
    public Job procesarFicheroJob(JobRepository jobRepository,
                                   Step leerStep,
                                   Step transformarStep,
                                   Step escribirStep) {
        return new JobBuilder("procesarFicheroJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .validator(jobParametersValidator())
            .start(leerStep)
            .next(transformarStep)
            .next(escribirStep)
            .listener(jobCompletionListener())
            .build();
    }

    @Bean
    public JobParametersValidator jobParametersValidator() {
        DefaultJobParametersValidator validator = new DefaultJobParametersValidator();
        validator.setRequiredKeys(new String[]{"input.file"});
        validator.setOptionalKeys(new String[]{"timestamp"});
        return validator;
    }
}
```

## Step con Chunk Processing

```java
@Bean
public Step leerYTransformarStep(JobRepository jobRepository,
                                  PlatformTransactionManager txManager) {
    return new StepBuilder("leerYTransformar", jobRepository)
        .<InputRecord, OutputRecord>chunk(100, txManager)
        .reader(flatFileItemReader(null))
        .processor(transformProcessor())
        .writer(jpaItemWriter())
        .faultTolerant()
        .skipLimit(10)
        .skip(FlatFileParseException.class)
        .retryLimit(3)
        .retry(DeadlockLoserDataAccessException.class)
        .listener(stepExecutionListener())
        .build();
}
```

## ItemReader — Fichero CSV

```java
@Bean
@StepScope
public FlatFileItemReader<InputRecord> flatFileItemReader(
        @Value("#{jobParameters['input.file']}") String inputFile) {
    return new FlatFileItemReaderBuilder<InputRecord>()
        .name("inputReader")
        .resource(new FileSystemResource(inputFile))
        .linesToSkip(1)  // Header
        .delimited()
        .delimiter(";")
        .names("campo1", "campo2", "campo3", "campo4")
        .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
            setTargetType(InputRecord.class);
        }})
        .build();
}
```

## ItemReader — Base de datos (paginado)

```java
@Bean
@StepScope
public JdbcPagingItemReader<InputRecord> jdbcPagingReader(DataSource dataSource) {
    Map<String, Order> sortKeys = new HashMap<>();
    sortKeys.put("id", Order.ASCENDING);

    return new JdbcPagingItemReaderBuilder<InputRecord>()
        .name("dbReader")
        .dataSource(dataSource)
        .selectClause("SELECT id, campo1, campo2, estado")
        .fromClause("FROM registros")
        .whereClause("WHERE estado = 'PENDIENTE'")
        .sortKeys(sortKeys)
        .pageSize(100)
        .rowMapper(new BeanPropertyRowMapper<>(InputRecord.class))
        .build();
}
```

## ItemProcessor

```java
@Bean
public ItemProcessor<InputRecord, OutputRecord> transformProcessor() {
    return input -> {
        // Validación
        if (input.getCampo1() == null || input.getCampo1().isBlank()) {
            return null;  // null = skip this record
        }

        // Transformación
        OutputRecord output = new OutputRecord();
        output.setIdentificador(input.getCampo1().trim().toUpperCase());
        output.setValor(calcularValor(input));
        output.setFechaProceso(LocalDateTime.now());
        output.setEstado("PROCESADO");
        return output;
    };
}
```

## ItemWriter — JPA

```java
@Bean
public JpaItemWriter<OutputRecord> jpaItemWriter(EntityManagerFactory emf) {
    JpaItemWriter<OutputRecord> writer = new JpaItemWriter<>();
    writer.setEntityManagerFactory(emf);
    return writer;
}
```

## ItemWriter — Fichero CSV

```java
@Bean
public FlatFileItemWriter<OutputRecord> csvWriter() {
    return new FlatFileItemWriterBuilder<OutputRecord>()
        .name("csvWriter")
        .resource(new FileSystemResource("/app/data/output/resultado.csv"))
        .delimited()
        .delimiter(";")
        .names("identificador", "valor", "fechaProceso", "estado")
        .headerCallback(writer -> writer.write("ID;VALOR;FECHA;ESTADO"))
        .build();
}
```

## Tasklet (operaciones simples)

```java
@Bean
public Step limpiarDirectorioStep(JobRepository jobRepository,
                                   PlatformTransactionManager txManager) {
    return new StepBuilder("limpiarDirectorio", jobRepository)
        .tasklet((contribution, chunkContext) -> {
            File dir = new File("/app/data/temp");
            FileUtils.cleanDirectory(dir);
            return RepeatStatus.FINISHED;
        }, txManager)
        .build();
}
```

## Scheduling (lanzamiento programado)

```java
@Component
@EnableScheduling
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job procesarFicheroJob;

    @Scheduled(cron = "0 0 2 * * MON-FRI")
    public void ejecutarProcesamientoDiario() {
        JobParameters params = new JobParametersBuilder()
            .addString("input.file", "/app/data/input/fichero_" +
                LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv")
            .addLong("timestamp", System.currentTimeMillis())
            .toJobParameters();

        try {
            JobExecution execution = jobLauncher.run(procesarFicheroJob, params);
            log.info("Job ejecutado: status={}", execution.getStatus());
        } catch (Exception e) {
            log.error("Error lanzando job batch", e);
        }
    }
}
```

## application.yml para Batch

```yaml
spring:
  batch:
    jdbc:
      initialize-schema: always  # Crear tablas BATCH_* en DB
    job:
      enabled: false  # No ejecutar jobs al arrancar

  datasource:
    url: jdbc:postgresql://localhost:5432/batchdb
    username: nova
    password: ${DB_PASSWORD}
```

## Monitorización de Jobs

```java
@Component
public class JobCompletionListener extends JobExecutionListenerSupport {

    @Override
    public void afterJob(JobExecution execution) {
        if (execution.getStatus() == BatchStatus.COMPLETED) {
            log.info("JOB COMPLETADO: {} - registros procesados: {}",
                execution.getJobInstance().getJobName(),
                execution.getStepExecutions().stream()
                    .mapToLong(StepExecution::getWriteCount).sum());
        } else if (execution.getStatus() == BatchStatus.FAILED) {
            log.error("JOB FALLIDO: {} - errores: {}",
                execution.getJobInstance().getJobName(),
                execution.getAllFailureExceptions());
        }
    }
}
```
