package com.ecommerce.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for cart item (item + quantity + subtotal).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    
    private UUID itemId;
    private String itemName;
    private BigDecimal itemPrice;
    private Integer quantity;
    private BigDecimal subtotal;  // itemPrice * quantity
}
