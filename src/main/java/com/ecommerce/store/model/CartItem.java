package com.ecommerce.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents an item within a shopping cart.
 * 
 * Design Note: Uses the Snapshot Pattern.
 * When an item is added to cart, we capture its name and price at that moment.
 * This prevents inconsistencies if the original Item's price changes later.
 * Standard practice in e-commerce systems.
 * 
 * No need for separate CartItem ID - items are identified by itemId within the cart.
 * No need for cartId - CartItem belongs to Cart's items list.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    
    /**
     * Reference to the original item ID.
     * Links this cart entry to the Item it represents.
     */
    private UUID itemId;
    
    /**
     * Snapshot of item name at time of adding to cart.
     * Stored for display convenience without Item lookup.
     */
    private String itemName;
    
    /**
     * Snapshot of item price at time of adding to cart.
     * Ensures cart price doesn't change if Item's price is updated.
     */
    private BigDecimal price;
    
    /**
     * Quantity of this item in the cart.
     * Must be positive (> 0).
     */
    private int quantity;
    
    /**
     * Calculates the subtotal for this cart item.
     * 
     * @return price * quantity
     */
    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
