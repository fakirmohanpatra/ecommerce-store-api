package com.ecommerce.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user's shopping cart.
 * 
 * Design Note: Each user (identified by userId) has one active cart.
 * Cart persists until checkout or explicit item removal.
 * Cart is cleared upon successful checkout.
 * 
 * The cart is mutable - items can be added, removed, and quantities updated.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    
    /**
     * Identifier of the user who owns this cart.
     * Using String for simplicity (e.g., "user123").
     * Note: Changed from "clientId" to "userId" for consistency with API endpoints.
     */
    private String userId;
    
    /**
     * List of items in the cart.
     * Initialized as empty list to avoid null checks.
     */
    private List<CartItem> items = new ArrayList<>();
    
    /**
     * Total value of all items in the cart.
     * Calculated by summing all CartItem subtotals.
     * Should be recalculated whenever items change.
     */
    private BigDecimal total = BigDecimal.ZERO;
    
    /**
     * Constructor with userId only (for new empty carts).
     */
    public Cart(String userId) {
        this.userId = userId;
        this.items = new ArrayList<>();
        this.total = BigDecimal.ZERO;
    }
    
    /**
     * Recalculates the cart total based on current items.
     * Should be called after any modification to items.
     */
    public void recalculateTotal() {
        this.total = items.stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
