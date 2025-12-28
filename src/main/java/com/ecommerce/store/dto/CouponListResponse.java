package com.ecommerce.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for list of coupons.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponListResponse {
    
    private List<String> coupons;
    private Integer totalGenerated;
}
