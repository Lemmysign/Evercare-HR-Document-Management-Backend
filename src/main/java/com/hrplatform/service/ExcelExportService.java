package com.hrplatform.service;

import com.hrplatform.dto.request.ExportFilterRequest;

public interface ExcelExportService {

    byte[] exportAllSubmissions(String generatedBy);

    // ✅ ADD THIS NEW METHOD
    byte[] exportFilteredSubmissions(ExportFilterRequest filter, String generatedBy);
}