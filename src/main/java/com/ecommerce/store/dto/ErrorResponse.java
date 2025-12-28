package com.ecommerce.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for error responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    
    private String message;
    private String errorCode;
    private Instant timestamp;
    
    public ErrorResponse(String message) {
        this.message = message;
        this.timestamp = Instant.now();
    }
}
