package com.ecommerce.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response DTO for Coupon.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponResponse {
    
    private String code;
    private boolean isUsed;
    private Integer generatedAtOrderNumber;
    private Instant createdAt;
}
