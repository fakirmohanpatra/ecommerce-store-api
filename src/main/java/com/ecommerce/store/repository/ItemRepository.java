package com.ecommerce.store.repository;

import com.ecommerce.store.model.Item;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Item (product catalog) operations.
 * 
 * Responsibilities:
 * - CRUD operations for items
 * - Query items by ID or list all
 * 
 * Thread-Safe: Uses ConcurrentHashMap from DataStore.
 */
@Repository
public class ItemRepository implements IItemRepository {
    
    private final DataStore dataStore;
    
    public ItemRepository(DataStore dataStore) {
        this.dataStore = dataStore;
    }
    
    /**
     * Find item by ID.
     */
    @Override
    public Optional<Item> findById(UUID itemId) {
        return Optional.ofNullable(dataStore.items.get(itemId));
    }
    
    /**
     * Get all items.
     */
    @Override
    public List<Item> findAll() {
        return new ArrayList<>(dataStore.items.values());
    }
    
    /**
     * Save or update an item.
     */
    @Override
    public Item save(Item item) {
        if (item.getItemId() == null) {
            item.setItemId(UUID.randomUUID());
        }
        dataStore.items.put(item.getItemId(), item);
        return item;
    }
    
    /**
     * Delete an item.
     */
    @Override
    public void delete(UUID itemId) {
        dataStore.items.remove(itemId);
    }
    
    /**
     * Check if item exists.
     */
    @Override
    public boolean exists(UUID itemId) {
        return dataStore.items.containsKey(itemId);
    }
    
    /**
     * Get total count of items.
     */
    @Override
    public int count() {
        return dataStore.items.size();
    }
}
