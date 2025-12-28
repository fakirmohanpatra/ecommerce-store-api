package com.ecommerce.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for admin statistics.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsResponse {
    
    private Integer totalItemsPurchased;
    private BigDecimal totalPurchaseAmount;
    private BigDecimal totalDiscountAmount;
    private Integer totalOrders;
    private Long ordersWithCoupons;
    private Integer totalCouponsGenerated;
    private String activeCoupon;  // null if no active coupon
}
