package com.example.guarantees.service;

import com.example.guarantees.domain.Guarantee;
import com.example.guarantees.domain.GuaranteeStatus;
import com.example.guarantees.domain.GuaranteeType;
import com.example.guarantees.repository.GuaranteeRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class ExportService {

    private final GuaranteeRepository guaranteeRepository;
    private final GuaranteeEventService guaranteeEventService;
    private final ExportJobStore jobStore;
    private final MeterRegistry meterRegistry;
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    public ExportService(GuaranteeRepository guaranteeRepository,
                        GuaranteeEventService guaranteeEventService,
                        ExportJobStore jobStore,
                        MeterRegistry meterRegistry) {
        this.guaranteeRepository = guaranteeRepository;
        this.guaranteeEventService = guaranteeEventService;
        this.jobStore = jobStore;
        this.meterRegistry = meterRegistry;
    }

    @Async
    public void exportToExcel(String jobId, String format, GuaranteeStatus status,
                             GuaranteeType type, String sortBy, String sortDirection) {
        long start = System.nanoTime();
        try {
            jobStore.updateProgress(jobId, 10, "Fetching guarantees...");

            // Fetch guarantees based on filters
            List<Guarantee> guarantees = fetchGuarantees(status, type);

            // Apply sorting
            if (sortBy != null && !sortBy.isBlank()) {
                guarantees = applySorting(guarantees, sortBy, sortDirection);
            }

            jobStore.updateProgress(jobId, 50, "Generating " + format.toUpperCase() + " file...");

            // Generate file based on format
            File resultFile;
            if ("csv".equalsIgnoreCase(format)) {
                resultFile = generateCSV(jobId, guarantees);
            } else {
                resultFile = generateExcel(jobId, guarantees);
            }

            jobStore.updateProgress(jobId, 90, "Finalizing export...");

            // Mark job as completed
            jobStore.markCompleted(jobId, resultFile);

            // Record success metrics
            Counter.builder("exports.jobs")
                    .tags("format", format, "result", "ok")
                    .register(meterRegistry)
                    .increment();
            Timer.builder("exports.jobs.duration")
                    .tags("format", format)
                    .register(meterRegistry)
                    .record(System.nanoTime() - start, TimeUnit.NANOSECONDS);

            // Emit event for completion (for #5)
            guaranteeEventService.publishEvent("guarantee-events", "export-ready",
                new java.util.HashMap<String, String>() {{
                    put("jobId", jobId);
                    put("format", format);
                    put("count", String.valueOf(guarantees.size()));
                }});

        } catch (Exception e) {
            jobStore.markFailed(jobId, e);

            // Record failure metrics
            Counter.builder("exports.jobs")
                    .tags("format", format, "result", "error")
                    .register(meterRegistry)
                    .increment();
            Timer.builder("exports.jobs.duration")
                    .tags("format", format)
                    .register(meterRegistry)
                    .record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    private List<Guarantee> fetchGuarantees(GuaranteeStatus status, GuaranteeType type) {
        if (status != null && type != null) {
            return guaranteeRepository.findByStatusAndType(status, type);
        } else if (status != null) {
            return guaranteeRepository.findByStatus(status);
        } else if (type != null) {
            return guaranteeRepository.findByType(type);
        } else {
            return guaranteeRepository.findAll();
        }
    }

    private List<Guarantee> applySorting(List<Guarantee> guarantees, String sortBy, String sortDirection) {
        List<Guarantee> sorted = new ArrayList<>(guarantees);
        boolean ascending = sortDirection == null || !"desc".equalsIgnoreCase(sortDirection);

        sorted.sort((g1, g2) -> {
            int comparison = 0;
            switch (sortBy.toLowerCase()) {
                case "reference":
                    comparison = g1.getReference().compareTo(g2.getReference());
                    break;
                case "issuedate":
                    comparison = g1.getIssueDate().compareTo(g2.getIssueDate());
                    break;
                case "expirydate":
                    comparison = g1.getExpiryDate().compareTo(g2.getExpiryDate());
                    break;
                case "amount":
                    comparison = g1.getAmount().compareTo(g2.getAmount());
                    break;
                case "status":
                    comparison = g1.getStatus().compareTo(g2.getStatus());
                    break;
                case "type":
                    comparison = g1.getType().compareTo(g2.getType());
                    break;
                default:
                    return 0;
            }
            return ascending ? comparison : -comparison;
        });

        return sorted;
    }

    private File generateExcel(String jobId, List<Guarantee> guarantees) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Guarantees");
        CreationHelper createHelper = workbook.getCreationHelper();

        // Define header style
        CellStyle headerStyle = workbook.createCellStyle();
        XSSFFont headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Define date style
        CellStyle dateStyle = workbook.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

        // Create header row
        XSSFRow headerRow = sheet.createRow(0);
        String[] headers = {"ID", "Reference", "Type", "Status", "Amount", "Currency",
                           "Issue Date", "Expiry Date", "Applicant", "Beneficiary", "Issuing Bank"};
        for (int i = 0; i < headers.length; i++) {
            var cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Fill data rows
        int rowNum = 1;
        for (Guarantee guarantee : guarantees) {
            XSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(guarantee.getId());
            row.createCell(1).setCellValue(guarantee.getReference());
            row.createCell(2).setCellValue(guarantee.getType().name());
            row.createCell(3).setCellValue(guarantee.getStatus().name());
            row.createCell(4).setCellValue(guarantee.getAmount().doubleValue());
            row.createCell(5).setCellValue(guarantee.getCurrency());

            var dateCell6 = row.createCell(6);
            dateCell6.setCellValue(java.sql.Date.valueOf(guarantee.getIssueDate()));
            dateCell6.setCellStyle(dateStyle);

            var dateCell7 = row.createCell(7);
            dateCell7.setCellValue(java.sql.Date.valueOf(guarantee.getExpiryDate()));
            dateCell7.setCellStyle(dateStyle);

            row.createCell(8).setCellValue(
                guarantee.getApplicant().getFirstName() + " " +
                guarantee.getApplicant().getLastName()
            );
            row.createCell(9).setCellValue(
                guarantee.getBeneficiary().getFirstName() + " " +
                guarantee.getBeneficiary().getLastName()
            );
            row.createCell(10).setCellValue(guarantee.getIssuingBank().getName());
        }

        // Write file to temp directory
        File tempFile = new File(TEMP_DIR, "guarantees-" + jobId + ".xlsx");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            workbook.write(fos);
        } finally {
            workbook.close();
        }

        return tempFile;
    }

    private File generateCSV(String jobId, List<Guarantee> guarantees) throws IOException {
        File tempFile = new File(TEMP_DIR, "guarantees-" + jobId + ".csv");

        try (PrintWriter writer = new PrintWriter(new java.io.FileWriter(tempFile))) {
            // Write header
            writer.println("ID,Reference,Type,Status,Amount,Currency,Issue Date,Expiry Date,Applicant,Beneficiary,Issuing Bank");

            // Write data rows
            for (Guarantee guarantee : guarantees) {
                writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",%f,\"%s\",\"%s\",\"%s\",\"%s %s\",\"%s %s\",\"%s\"",
                    guarantee.getId(),
                    escapeCSV(guarantee.getReference()),
                    guarantee.getType().name(),
                    guarantee.getStatus().name(),
                    guarantee.getAmount().doubleValue(),
                    guarantee.getCurrency(),
                    guarantee.getIssueDate(),
                    guarantee.getExpiryDate(),
                    escapeCSV(guarantee.getApplicant().getFirstName()),
                    escapeCSV(guarantee.getApplicant().getLastName()),
                    escapeCSV(guarantee.getBeneficiary().getFirstName()),
                    escapeCSV(guarantee.getBeneficiary().getLastName()),
                    escapeCSV(guarantee.getIssuingBank().getName())
                ));
            }
        }

        return tempFile;
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    public String generateJobId() {
        return "exp-" + UUID.randomUUID().toString().substring(0, 12);
    }
}
