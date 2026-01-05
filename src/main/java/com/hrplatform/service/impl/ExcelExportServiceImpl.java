package com.hrplatform.service.impl;

import com.hrplatform.entity.Department;
import com.hrplatform.entity.DocumentSubmission;
import com.hrplatform.entity.Staff;
import com.hrplatform.repository.DepartmentRepository;
import com.hrplatform.service.ExcelExportService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class ExcelExportServiceImpl implements ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DepartmentRepository departmentRepository;

    @Override
    public byte[] generateExcelFile(List<Staff> staffList, List<DocumentSubmission> allSubmissions) {
        log.info("Generating Excel file for {} staff members with {} submissions",
                staffList.size(), allSubmissions.size());

        try (Workbook workbook = new XSSFWorkbook()) {

            // Check if this is a filtered export (specific departments only) or "All departments"
            boolean isAllDepartmentsExport = staffList.isEmpty() ||
                    staffList.stream()
                            .map(s -> s.getDepartment() != null ? s.getDepartment().getName() : "Not Assigned")
                            .distinct()
                            .count() > 1;

            // Group staff by department
            Map<String, List<Staff>> staffByDepartment = new LinkedHashMap<>();

            if (isAllDepartmentsExport && staffList.isEmpty()) {
                // TRUE "All departments" export - show all departments from DB
                List<Department> allDepartments = departmentRepository.findAll();
                log.info("All departments export requested - found {} total departments", allDepartments.size());

                // Initialize with all departments (even empty ones)
                for (Department dept : allDepartments) {
                    String deptName = dept.getName() != null ? dept.getName() : "Unknown Department";
                    staffByDepartment.put(deptName, new ArrayList<>());
                }
            }

            // Add staff to their respective departments
            for (Staff staff : staffList) {
                String deptName = staff.getDepartment() != null && staff.getDepartment().getName() != null
                        ? staff.getDepartment().getName()
                        : "Not Assigned";

                // Only add department if not already in map
                if (!staffByDepartment.containsKey(deptName)) {
                    staffByDepartment.put(deptName, new ArrayList<>());
                }

                staffByDepartment.get(deptName).add(staff);
            }

            // Group submissions by staff
            Map<UUID, List<DocumentSubmission>> submissionsByStaff = allSubmissions.stream()
                    .collect(Collectors.groupingBy(submission -> submission.getStaff().getId()));

            log.info("Creating {} department sheets (including empty departments)", staffByDepartment.size());

            // Create a sheet for each department (only departments with actual staff data)
            for (Map.Entry<String, List<Staff>> entry : staffByDepartment.entrySet()) {
                String departmentName = entry.getKey();
                List<Staff> deptStaff = entry.getValue();

                // Skip empty departments UNLESS it's an "All departments" export
                if (deptStaff.isEmpty() && !isAllDepartmentsExport) {
                    log.info("Skipping empty department: {}", departmentName);
                    continue;
                }

                // Sanitize sheet name (Excel has restrictions)
                String sheetName = sanitizeSheetName(departmentName);

                log.info("Creating sheet '{}' with {} staff", sheetName, deptStaff.size());

                if (deptStaff.isEmpty()) {
                    // Create empty sheet for departments with no staff (only for "All" export)
                    createEmptyDepartmentSheet(workbook, sheetName, departmentName);
                } else {
                    // Get submissions for this department's staff
                    List<UUID> deptStaffIds = deptStaff.stream()
                            .map(Staff::getId)
                            .collect(Collectors.toList());

                    List<DocumentSubmission> deptSubmissions = allSubmissions.stream()
                            .filter(sub -> deptStaffIds.contains(sub.getStaff().getId()))
                            .collect(Collectors.toList());

                    // Get unique document names for THIS department
                    Set<String> deptDocumentNames = deptSubmissions.stream()
                            .map(submission -> submission.getDocumentRequirement().getDocumentName())
                            .collect(Collectors.toCollection(TreeSet::new));

                    // Create the sheet
                    createDepartmentSheet(workbook, sheetName, deptStaff, deptSubmissions,
                            submissionsByStaff, deptDocumentNames);
                }
            }

            // Only create summary sheet if exporting multiple departments
            int sheetsCreated = (int) staffByDepartment.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty() || isAllDepartmentsExport)
                    .count();

            if (sheetsCreated > 1) {
                // Create summary sheet for multi-department exports
                int totalDepartmentsInSystem = isAllDepartmentsExport ?
                        departmentRepository.findAll().size() :
                        staffByDepartment.size();
                createSummarySheet(workbook, staffByDepartment, submissionsByStaff, totalDepartmentsInSystem);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            log.info("Excel file generated successfully with {} sheets",
                    sheetsCreated + (sheetsCreated > 1 ? 1 : 0)); // +1 for summary if applicable
            return outputStream.toByteArray();

        } catch (Exception e) {
            log.error("Error generating Excel file", e);
            throw new RuntimeException("Failed to generate Excel file: " + e.getMessage(), e);
        }
    }

    private void createEmptyDepartmentSheet(Workbook workbook, String sheetName, String departmentName) {
        Sheet sheet = workbook.createSheet(sheetName);

        // Create header row
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        createHeaderCell(headerRow, 0, "Department", headerStyle);
        createHeaderCell(headerRow, 1, "Status", headerStyle);

        // Create info row
        Row infoRow = sheet.createRow(2);
        Cell deptCell = infoRow.createCell(0);
        deptCell.setCellValue(departmentName);

        Cell statusCell = infoRow.createCell(1);
        statusCell.setCellValue("No staff assigned to this department yet");

        CellStyle italicStyle = workbook.createCellStyle();
        Font italicFont = workbook.createFont();
        italicFont.setItalic(true);
        italicFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        italicStyle.setFont(italicFont);
        statusCell.setCellStyle(italicStyle);

        // Auto-size columns
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.setColumnWidth(1, sheet.getColumnWidth(1) + 3000);
    }

    private void createSummarySheet(Workbook workbook,
                                    Map<String, List<Staff>> staffByDepartment,
                                    Map<UUID, List<DocumentSubmission>> submissionsByStaff,
                                    int totalDepartments) {

        Sheet summarySheet = workbook.createSheet("Summary");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle boldStyle = createBoldStyle(workbook);
        CellStyle centerStyle = createCenterAlignedStyle(workbook);

        int rowIndex = 0;

        // Title
        Row titleRow = summarySheet.createRow(rowIndex++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Staff Document Submission Summary - All Departments");
        titleCell.setCellStyle(boldStyle);
        rowIndex++; // Empty row

        // Department count info
        Row deptCountRow = summarySheet.createRow(rowIndex++);
        deptCountRow.createCell(0).setCellValue("Total Departments in System: " + totalDepartments);
        rowIndex++; // Empty row

        // Headers
        Row headerRow = summarySheet.createRow(rowIndex++);
        createHeaderCell(headerRow, 0, "Department", headerStyle);
        createHeaderCell(headerRow, 1, "Total Staff", headerStyle);
        createHeaderCell(headerRow, 2, "Staff with Submissions", headerStyle);
        createHeaderCell(headerRow, 3, "Staff without Submissions", headerStyle);
        createHeaderCell(headerRow, 4, "Total Submissions", headerStyle);
        createHeaderCell(headerRow, 5, "Submission Rate", headerStyle);

        // Data rows
        int totalStaff = 0;
        int totalWithSubmissions = 0;
        int totalSubmissions = 0;
        int departmentsWithStaff = 0;

        for (Map.Entry<String, List<Staff>> entry : staffByDepartment.entrySet()) {
            String dept = entry.getKey();
            List<Staff> staff = entry.getValue();

            int deptStaffCount = staff.size();

            if (deptStaffCount > 0) {
                departmentsWithStaff++;

                int deptWithSubmissions = (int) staff.stream()
                        .filter(s -> submissionsByStaff.containsKey(s.getId()) &&
                                !submissionsByStaff.get(s.getId()).isEmpty())
                        .count();
                int deptWithoutSubmissions = deptStaffCount - deptWithSubmissions;
                int deptSubmissions = staff.stream()
                        .mapToInt(s -> submissionsByStaff.getOrDefault(s.getId(), new ArrayList<>()).size())
                        .sum();
                double submissionRate = deptStaffCount > 0
                        ? (deptWithSubmissions * 100.0 / deptStaffCount)
                        : 0.0;

                Row dataRow = summarySheet.createRow(rowIndex++);
                dataRow.createCell(0).setCellValue(dept);

                Cell c1 = dataRow.createCell(1);
                c1.setCellValue(deptStaffCount);
                c1.setCellStyle(centerStyle);

                Cell c2 = dataRow.createCell(2);
                c2.setCellValue(deptWithSubmissions);
                c2.setCellStyle(centerStyle);

                Cell c3 = dataRow.createCell(3);
                c3.setCellValue(deptWithoutSubmissions);
                c3.setCellStyle(centerStyle);

                Cell c4 = dataRow.createCell(4);
                c4.setCellValue(deptSubmissions);
                c4.setCellStyle(centerStyle);

                Cell c5 = dataRow.createCell(5);
                c5.setCellValue(String.format("%.1f%%", submissionRate));
                c5.setCellStyle(centerStyle);

                totalStaff += deptStaffCount;
                totalWithSubmissions += deptWithSubmissions;
                totalSubmissions += deptSubmissions;
            } else {
                // Show departments with no staff
                Row dataRow = summarySheet.createRow(rowIndex++);
                dataRow.createCell(0).setCellValue(dept);

                Cell c1 = dataRow.createCell(1);
                c1.setCellValue("No staff");
                c1.setCellStyle(centerStyle);

                // Leave other cells empty for no-staff departments
            }
        }

        // Total row
        rowIndex++;
        Row totalRow = summarySheet.createRow(rowIndex);
        Cell totalLabelCell = totalRow.createCell(0);
        totalLabelCell.setCellValue("TOTAL (" + departmentsWithStaff + " depts with staff)");
        totalLabelCell.setCellStyle(boldStyle);

        Cell t1 = totalRow.createCell(1);
        t1.setCellValue(totalStaff);
        t1.setCellStyle(boldStyle);

        Cell t2 = totalRow.createCell(2);
        t2.setCellValue(totalWithSubmissions);
        t2.setCellStyle(boldStyle);

        Cell t3 = totalRow.createCell(3);
        t3.setCellValue(totalStaff - totalWithSubmissions);
        t3.setCellStyle(boldStyle);

        Cell t4 = totalRow.createCell(4);
        t4.setCellValue(totalSubmissions);
        t4.setCellStyle(boldStyle);

        Cell t5 = totalRow.createCell(5);
        double overallRate = totalStaff > 0 ? (totalWithSubmissions * 100.0 / totalStaff) : 0.0;
        t5.setCellValue(String.format("%.1f%%", overallRate));
        t5.setCellStyle(boldStyle);

        // Auto-size columns
        for (int i = 0; i < 6; i++) {
            summarySheet.autoSizeColumn(i);
            summarySheet.setColumnWidth(i, summarySheet.getColumnWidth(i) + 1000);
        }

        // Move summary sheet to first position
        workbook.setSheetOrder("Summary", 0);
    }

    private void createDepartmentSheet(Workbook workbook, String sheetName,
                                       List<Staff> staffList,
                                       List<DocumentSubmission> allSubmissions,
                                       Map<UUID, List<DocumentSubmission>> submissionsByStaff,
                                       Set<String> documentNames) {

        Sheet sheet = workbook.createSheet(sheetName);

        // Create header row
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = createHeaderStyle(workbook);

        int colIndex = 0;
        createHeaderCell(headerRow, colIndex++, "Staff ID", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Staff Name", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Email Address", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Total Documents", headerStyle);
        createHeaderCell(headerRow, colIndex++, "Latest Submission", headerStyle);

        // Add document name columns for this department
        for (String documentName : documentNames) {
            createHeaderCell(headerRow, colIndex++, documentName, headerStyle);
        }

        // Create data rows
        int rowIndex = 1;
        CellStyle dateStyle = createDateStyle(workbook);
        CellStyle hyperlinkStyle = createHyperlinkStyle(workbook);
        CellStyle centerStyle = createCenterAlignedStyle(workbook);
        CellStyle notSubmittedStyle = createNotSubmittedStyle(workbook);

        for (Staff staff : staffList) {
            Row row = sheet.createRow(rowIndex++);

            List<DocumentSubmission> staffSubmissions = submissionsByStaff.getOrDefault(
                    staff.getId(),
                    new ArrayList<>()
            );

            colIndex = 0;

            // Staff ID
            row.createCell(colIndex++).setCellValue(
                    staff.getStaffIdNumber() != null ? staff.getStaffIdNumber() : "N/A"
            );

            // Staff Name
            row.createCell(colIndex++).setCellValue(
                    staff.getFullName() != null ? staff.getFullName() : "N/A"
            );

            // Email
            row.createCell(colIndex++).setCellValue(
                    staff.getEmail() != null ? staff.getEmail() : "N/A"
            );

            // Total Documents Submitted
            Cell totalCell = row.createCell(colIndex++);
            totalCell.setCellValue(staffSubmissions.size());
            totalCell.setCellStyle(centerStyle);

            // Latest Submission Date
            if (!staffSubmissions.isEmpty()) {
                String latestDate = staffSubmissions.stream()
                        .map(submission -> submission.getCreatedAt())
                        .max(java.time.LocalDateTime::compareTo)
                        .map(date -> date.format(DATE_FORMATTER))
                        .orElse("N/A");

                Cell dateCell = row.createCell(colIndex++);
                dateCell.setCellValue(latestDate);
                dateCell.setCellStyle(dateStyle);
            } else {
                Cell dateCell = row.createCell(colIndex++);
                dateCell.setCellValue("No Submissions");
                dateCell.setCellStyle(centerStyle);
            }

            // Add document URLs or status
            Map<String, DocumentSubmission> submissionMap = staffSubmissions.stream()
                    .collect(Collectors.toMap(
                            submission -> submission.getDocumentRequirement().getDocumentName(),
                            submission -> submission,
                            (existing, replacement) -> existing
                    ));

            for (String documentName : documentNames) {
                Cell cell = row.createCell(colIndex++);

                if (submissionMap.containsKey(documentName)) {
                    DocumentSubmission submission = submissionMap.get(documentName);

                    if (submission.getCloudinaryUrl() != null && !submission.getCloudinaryUrl().isEmpty()) {
                        Hyperlink hyperlink = workbook.getCreationHelper()
                                .createHyperlink(HyperlinkType.URL);
                        hyperlink.setAddress(submission.getCloudinaryUrl());

                        cell.setHyperlink(hyperlink);
                        cell.setCellValue("View Document");
                        cell.setCellStyle(hyperlinkStyle);
                    } else {
                        cell.setCellValue("Submitted");
                        cell.setCellStyle(centerStyle);
                    }
                } else {
                    cell.setCellValue("Not Submitted");
                    cell.setCellStyle(notSubmittedStyle);
                }
            }
        }

        // Auto-size columns
        int totalColumns = 5 + documentNames.size();
        for (int i = 0; i < totalColumns; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + 1000);
        }

        // Freeze header row
        sheet.createFreezePane(0, 1);
    }

    private String sanitizeSheetName(String name) {
        // Excel sheet names can't exceed 31 characters and can't contain: \ / ? * [ ]
        String sanitized = name.replaceAll("[\\\\/?*\\[\\]]", "");
        if (sanitized.length() > 31) {
            sanitized = sanitized.substring(0, 28) + "...";
        }
        return sanitized;
    }

    private void createHeaderCell(Row row, int colIndex, String value, CellStyle style) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createBoldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createCenterAlignedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHyperlinkStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createNotSubmittedStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        font.setItalic(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}