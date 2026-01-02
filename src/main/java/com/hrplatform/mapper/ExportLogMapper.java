package com.hrplatform.mapper;

import com.hrplatform.dto.request.ExportFilterRequest;
import com.hrplatform.entity.ExportLog;
import org.springframework.stereotype.Component;

@Component
public class ExportLogMapper {

    public ExportLog toEntity(
            String hrUserEmail,
            ExportFilterRequest request,
            Integer totalRecords,
            String departmentName) {

        return ExportLog.builder()
                .hrUserEmail(hrUserEmail)
                .departmentFilter(departmentName)
                .submissionStatusFilter(request.getSubmissionStatus())
                .dateRangeStart(request.getDateRangeStart())
                .dateRangeEnd(request.getDateRangeEnd())
                .totalRecordsExported(totalRecords)
                .build();
    }
}