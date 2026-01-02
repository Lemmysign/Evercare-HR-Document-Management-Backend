package com.hrplatform.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private Boolean success;
    private String message;
    private String error;
    private Long timestamp;

    public static ErrorResponse of(String message, String error) {
        return ErrorResponse.builder()
                .success(false)
                .message(message)
                .error(error)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}