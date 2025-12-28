package com.ecommerce.store.repository;

import com.ecommerce.store.model.Cart;

import java.util.Optional;

/**
 * Repository interface for Cart operations.
 */
public interface ICartRepository {
    
    /**
     * Find cart by user ID.
     */
    Optional<Cart> findByUserId(String userId);
    
    /**
     * Get or create cart for a user.
     * If cart doesn't exist, creates a new empty cart.
     */
    Cart getOrCreate(String userId);
    
    /**
     * Save or update a cart.
     */
    Cart save(Cart cart);
    
    /**
     * Delete a user's cart.
     * Called after successful checkout.
     */
    void delete(String userId);
    
    /**
     * Check if cart exists for user.
     */
    boolean exists(String userId);
    
    /**
     * Get total count of active carts.
     */
    int count();
}
