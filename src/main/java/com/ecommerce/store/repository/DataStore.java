package com.ecommerce.store.repository;

import com.ecommerce.store.model.*;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Centralized in-memory data storage.
 * 
 * This class holds all the thread-safe data structures.
 * Repositories inject this to access their respective stores.
 * 
 * Design: Separation of storage from business logic.
 * - DataStore = raw storage containers
 * - Repositories = CRUD operations on specific entities
 * - Services = business logic
 */
@Component
public class DataStore {
    
    // Entity Stores
    public final ConcurrentHashMap<UUID, Item> items = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Cart> carts = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<UUID, Order> orders = new ConcurrentHashMap<>();
    
    // Coupon Management
    public volatile Coupon activeCoupon = null;
    public final List<String> generatedCoupons = Collections.synchronizedList(new ArrayList<>());
    
    // Order Counter (for Nth order logic)
    public final AtomicInteger orderCounter = new AtomicInteger(0);
    
    /**
     * Initialize seed data on startup.
     */
    @PostConstruct
    public void seedData() {
        seedItems();
    }
    
    /**
     * Seed initial items (product catalog).
     */
    private void seedItems() {
        createItem("Laptop", new BigDecimal("999.99"));
        createItem("Smartphone", new BigDecimal("699.99"));
        createItem("Wireless Headphones", new BigDecimal("199.99"));
        createItem("Smart Watch", new BigDecimal("299.99"));
        createItem("Design Patterns Book", new BigDecimal("49.99"));
        createItem("Clean Code Book", new BigDecimal("39.99"));
        createItem("The Pragmatic Programmer", new BigDecimal("44.99"));
        createItem("Coffee Maker", new BigDecimal("79.99"));
        createItem("Blender", new BigDecimal("129.99"));
        createItem("Air Fryer", new BigDecimal("89.99"));
    }
    
    private void createItem(String name, BigDecimal price) {
        Item item = new Item();
        UUID itemId = UUID.randomUUID();
        item.setItemId(itemId);
        item.setName(name);
        item.setPrice(price);
        items.put(itemId, item);
    }
    
    /**
     * Clear all data (for testing).
     */
    public void clearAll() {
        items.clear();
        carts.clear();
        orders.clear();
        orderCounter.set(0);
        activeCoupon = null;
        generatedCoupons.clear();
    }
}
