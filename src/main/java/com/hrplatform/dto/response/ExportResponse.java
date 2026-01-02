package com.hrplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResponse {

    private String fileName;
    private byte[] fileData;
    private String contentType;
    private Integer totalRecords;
    private String message;
}