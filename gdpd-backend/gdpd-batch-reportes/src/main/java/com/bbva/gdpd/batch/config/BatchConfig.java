package com.bbva.gdpd.batch.config;

import com.bbva.gdpd.batch.model.PedidoResumen;
import com.bbva.gdpd.batch.model.ReportePedido;
import com.bbva.gdpd.batch.processor.ReporteItemProcessor;
import com.bbva.gdpd.batch.reader.PedidoItemReader;
import com.bbva.gdpd.batch.writer.ReporteItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job generarReportesJob(Step procesarPedidosStep) {
        return jobBuilderFactory.get("generarReportesJob")
                .incrementer(new RunIdIncrementer())
                .start(procesarPedidosStep)
                .build();
    }

    @Bean
    public Step procesarPedidosStep(PedidoItemReader reader,
                                     ReporteItemProcessor processor,
                                     ReporteItemWriter writer) {
        return stepBuilderFactory.get("procesarPedidosStep")
                .<PedidoResumen, ReportePedido>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(5)
                .skip(Exception.class)
                .build();
    }
}
