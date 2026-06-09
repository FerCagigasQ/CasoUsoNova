package com.bbva.gdpd.gdpdreportbatch.writer;

import com.bbva.gdpd.gdpdreportbatch.domain.ReporteRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReporteItemWriter implements ItemWriter<ReporteRow> {
    private static final Logger LOG = LoggerFactory.getLogger(ReporteItemWriter.class);

    @Value("${batch.output.path:/tmp/gdpd-reports}")
    private String outputPath;

    @Override
    public void write(List<? extends ReporteRow> items) throws IOException {
        Files.createDirectories(Paths.get(outputPath));
        String file = outputPath + "/pedidos-reporte-"
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        try (PrintWriter w = new PrintWriter(new FileWriter(file, true))) {
            for (ReporteRow row : items) w.println(row.toCsvLine());
        }
        LOG.info("Escritas {} lineas en CSV: {}", items.size(), file);
    }
}
