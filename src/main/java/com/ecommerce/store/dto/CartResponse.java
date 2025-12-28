package com.ecommerce.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for Cart.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    
    private String userId;
    private List<CartItemResponse> items = new ArrayList<>();
    private Integer totalItems;      // Total number of items (sum of quantities)
    private BigDecimal totalAmount;  // Total cart value
}
