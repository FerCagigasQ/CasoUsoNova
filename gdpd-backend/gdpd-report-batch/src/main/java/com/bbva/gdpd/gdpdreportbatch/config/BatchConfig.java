package com.bbva.gdpd.gdpdreportbatch.config;

import com.bbva.gdpd.gdpdreportbatch.domain.PedidoRow;
import com.bbva.gdpd.gdpdreportbatch.domain.ReporteRow;
import com.bbva.gdpd.gdpdreportbatch.processor.PedidoItemProcessor;
import com.bbva.gdpd.gdpdreportbatch.writer.ReporteItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.persistence.EntityManagerFactory;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
    private static final int CHUNK_SIZE = 100;

    @Autowired private JobBuilderFactory jobs;
    @Autowired private StepBuilderFactory steps;
    @Autowired private EntityManagerFactory emf;
    @Autowired private PedidoItemProcessor processor;
    @Autowired private ReporteItemWriter writer;

    @Bean
    public JpaPagingItemReader<PedidoRow> pedidoItemReader() {
        return new JpaPagingItemReaderBuilder<PedidoRow>()
                .name("pedidoItemReader")
                .entityManagerFactory(emf)
                .queryString("SELECT p FROM PedidoRow p ORDER BY p.id ASC")
                .pageSize(CHUNK_SIZE)
                .build();
    }

    @Bean
    public Step generarReporteStep() {
        return steps.get("generarReporteStep")
                .<PedidoRow, ReporteRow>chunk(CHUNK_SIZE)
                .reader(pedidoItemReader())
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }

    @Bean
    public Job generarReporteJob() {
        return jobs.get("gdpdreportbatch")
                .incrementer(new RunIdIncrementer())
                .start(generarReporteStep())
                .build();
    }
}
