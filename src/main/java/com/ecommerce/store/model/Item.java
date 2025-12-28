package com.ecommerce.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents a product/item in the store.
 * 
 * Design Note: This is a simple value object representing product information.
 * Items are passed in API requests when adding to cart (no pre-populated catalog).
 * Kept minimal with only essential fields: id, name, price, and stock.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    
    /**
     * Unique identifier for the item.
     * Using UUID for globally unique, thread-safe ID generation.
     * Production-ready and scalable for distributed systems.
     */
    private UUID itemId;
    
    /**
     * Name/title of the item.
     */
    private String name;
    
    /**
     * Price of the item.
     * Using BigDecimal to avoid floating-point precision issues with currency.
     */
    private BigDecimal price;
    
    /**
     * Available stock quantity.
     * When stock reaches 0, item is considered out of stock.
     */
    private int stock = 0;
    
    /**
     * Check if item is out of stock.
     */
    public boolean isOutOfStock() {
        return stock <= 0;
    }
}
