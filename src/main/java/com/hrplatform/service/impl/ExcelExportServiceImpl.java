package com.hrplatform.service.impl;

import com.hrplatform.entity.DocumentSubmission;
import com.hrplatform.entity.Staff;
import com.hrplatform.service.ExcelExportService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public byte[] generateExcelFile(List<Staff> staffList, List<DocumentSubmission> allSubmissions) {
        log.info("Generating Excel file for {} staff members", staffList.size());

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Staff Documents");

            // Group submissions by staff
            Map<UUID, List<DocumentSubmission>> submissionsByStaff = allSubmissions.stream()
                    .collect(Collectors.groupingBy(submission -> submission.getStaff().getId()));

            // Get all unique document names for header
            Set<String> allDocumentNames = allSubmissions.stream()
                    .map(submission -> submission.getDocumentRequirement().getDocumentName())
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);

            int colIndex = 0;
            headerRow.createCell(colIndex++).setCellValue("Staff ID");
            headerRow.createCell(colIndex++).setCellValue("Staff Name");
            headerRow.createCell(colIndex++).setCellValue("Email");
            headerRow.createCell(colIndex++).setCellValue("Department");
            headerRow.createCell(colIndex++).setCellValue("Total Documents Submitted");
            headerRow.createCell(colIndex++).setCellValue("Submission Date");

            // Add document name columns
            for (String documentName : allDocumentNames) {
                headerRow.createCell(colIndex++).setCellValue(documentName);
            }

            // Apply header style
            for (int i = 0; i < colIndex; i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Create data rows
            int rowIndex = 1;
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle hyperlinkStyle = createHyperlinkStyle(workbook);

            for (Staff staff : staffList) {
                Row row = sheet.createRow(rowIndex++);

                List<DocumentSubmission> staffSubmissions = submissionsByStaff.getOrDefault(
                        staff.getId(),
                        new ArrayList<>()
                );

                colIndex = 0;
                row.createCell(colIndex++).setCellValue(staff.getStaffIdNumber());
                row.createCell(colIndex++).setCellValue(staff.getFullName());
                row.createCell(colIndex++).setCellValue(staff.getEmail());
                row.createCell(colIndex++).setCellValue(staff.getDepartment().getName());
                row.createCell(colIndex++).setCellValue(staffSubmissions.size());

                // Get latest submission date
                if (!staffSubmissions.isEmpty()) {
                    String latestDate = staffSubmissions.stream()
                            .map(submission -> submission.getCreatedAt().format(DATE_FORMATTER))
                            .max(String::compareTo)
                            .orElse("N/A");

                    Cell dateCell = row.createCell(colIndex++);
                    dateCell.setCellValue(latestDate);
                    dateCell.setCellStyle(dateStyle);
                } else {
                    row.createCell(colIndex++).setCellValue("N/A");
                }

                // Add document URLs
                Map<String, DocumentSubmission> submissionMap = staffSubmissions.stream()
                        .collect(Collectors.toMap(
                                submission -> submission.getDocumentRequirement().getDocumentName(),
                                submission -> submission,
                                (existing, replacement) -> existing
                        ));

                for (String documentName : allDocumentNames) {
                    Cell cell = row.createCell(colIndex++);

                    if (submissionMap.containsKey(documentName)) {
                        DocumentSubmission submission = submissionMap.get(documentName);
                        Hyperlink hyperlink = workbook.getCreationHelper()
                                .createHyperlink(HyperlinkType.URL);
                        hyperlink.setAddress(submission.getCloudinaryUrl());

                        cell.setHyperlink(hyperlink);
                        cell.setCellValue("View Document");
                        cell.setCellStyle(hyperlinkStyle);
                    } else {
                        cell.setCellValue("Not Submitted");
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < colIndex; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("Excel file generated successfully");
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error generating Excel file", e);
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createHyperlinkStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);
        return style;
    }
}