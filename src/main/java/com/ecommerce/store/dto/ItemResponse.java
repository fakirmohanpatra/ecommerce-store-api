package com.ecommerce.store.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for Item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemResponse {
    
    private UUID itemId;
    private String name;
    private BigDecimal price;
}
