package com.hrplatform.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudinarySignatureResponse {

    private String signature;
    private Long timestamp;
    private String apiKey;
    private String cloudName;
    private String folder;
    private String publicId;
}