package com.hrplatform.service;

import com.hrplatform.entity.DocumentSubmission;
import com.hrplatform.entity.Staff;

import java.util.List;

public interface ExcelExportService {

    byte[] generateExcelFile(List<Staff> staffList, List<DocumentSubmission> allSubmissions);
}