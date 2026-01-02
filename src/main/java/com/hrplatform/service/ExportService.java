package com.hrplatform.service;

import com.hrplatform.dto.request.ExportFilterRequest;
import com.hrplatform.dto.response.ExportResponse;

public interface ExportService {

    ExportResponse exportToExcel(ExportFilterRequest request, String hrUserEmail);
}