package com.ecommerce.store.service;

import com.ecommerce.store.dto.ItemResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Item (product catalog) operations.
 */
public interface ItemService {
    
    /**
     * Get all items in the catalog.
     * 
     * @return List of all items
     */
    List<ItemResponse> getAllItems();
    
    /**
     * Get item by ID.
     * 
     * @param itemId Item identifier
     * @return Item details
     */
    ItemResponse getItemById(UUID itemId);
}
