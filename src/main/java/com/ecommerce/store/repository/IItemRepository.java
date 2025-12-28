package com.ecommerce.store.repository;

import com.ecommerce.store.model.Item;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Item (product catalog) operations.
 */
public interface IItemRepository {
    
    /**
     * Find item by ID.
     */
    Optional<Item> findById(UUID itemId);
    
    /**
     * Get all items.
     */
    List<Item> findAll();
    
    /**
     * Save or update an item.
     */
    Item save(Item item);
    
    /**
     * Delete an item.
     */
    void delete(UUID itemId);
    
    /**
     * Check if item exists.
     */
    boolean exists(UUID itemId);
    
    /**
     * Get total count of items.
     */
    int count();
}
