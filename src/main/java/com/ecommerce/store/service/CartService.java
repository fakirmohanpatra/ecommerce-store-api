package com.ecommerce.store.service;

import com.ecommerce.store.dto.CartResponse;

import java.util.UUID;

/**
 * Service interface for Cart operations.
 */
public interface CartService {
    
    /**
     * Add item to user's cart.
     * 
     * @param userId User identifier
     * @param itemId Item identifier
     * @param quantity Quantity to add
     * @return Updated cart
     */
    CartResponse addItemToCart(String userId, UUID itemId, int quantity);
    
    /**
     * Remove item from user's cart.
     * 
     * @param userId User identifier
     * @param itemId Item identifier
     * @return Updated cart
     */
    CartResponse removeItemFromCart(String userId, UUID itemId);
    
    /**
     * Update item quantity in cart.
     * 
     * @param userId User identifier
     * @param itemId Item identifier
     * @param quantity New quantity
     * @return Updated cart
     */
    CartResponse updateItemQuantity(String userId, UUID itemId, int quantity);
    
    /**
     * Get user's cart.
     * 
     * @param userId User identifier
     * @return Cart with items and total
     */
    CartResponse getCart(String userId);
    
    /**
     * Clear user's cart.
     * 
     * @param userId User identifier
     */
    void clearCart(String userId);
}
