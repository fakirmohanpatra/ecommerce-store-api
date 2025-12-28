package com.ecommerce.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Order.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    
    private UUID orderId;
    private String userId;
    private List<CartItemResponse> items;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private String couponCode;
    private String paymentStatus;
    private Instant createdAt;
}
