package com.ecommerce.store.repository;

import com.ecommerce.store.model.Cart;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Repository for Cart operations.
 * 
 * Responsibilities:
 * - CRUD operations for user carts
 * - Get or create cart for a user
 * - Clear cart after checkout
 * 
 * Thread-Safe: Uses ConcurrentHashMap from DataStore.
 */
@Repository
public class CartRepository implements ICartRepository {
    
    private final DataStore dataStore;
    
    public CartRepository(DataStore dataStore) {
        this.dataStore = dataStore;
    }
    
    /**
     * Find cart by user ID.
     */
    @Override
    public Optional<Cart> findByUserId(String userId) {
        return Optional.ofNullable(dataStore.carts.get(userId));
    }
    
    /**
     * Get or create cart for a user.
     * If cart doesn't exist, creates a new empty cart.
     */
    @Override
    public Cart getOrCreate(String userId) {
        return dataStore.carts.computeIfAbsent(userId, id -> {
            Cart cart = new Cart();
            cart.setUserId(id);
            cart.setItems(new ArrayList<>());
            return cart;
        });
    }
    
    /**
     * Save or update a cart.
     */
    @Override
    public Cart save(Cart cart) {
        dataStore.carts.put(cart.getUserId(), cart);
        return cart;
    }
    
    /**
     * Delete a user's cart.
     * Called after successful checkout.
     */
    @Override
    public void delete(String userId) {
        dataStore.carts.remove(userId);
    }
    
    /**
     * Check if cart exists for user.
     */
    @Override
    public boolean exists(String userId) {
        return dataStore.carts.containsKey(userId);
    }
    
    /**
     * Get total count of active carts.
     */
    @Override
    public int count() {
        return dataStore.carts.size();
    }
}
