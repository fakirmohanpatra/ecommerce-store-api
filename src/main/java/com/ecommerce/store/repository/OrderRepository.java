package com.ecommerce.store.repository;

import com.ecommerce.store.model.CartItem;
import com.ecommerce.store.model.Order;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Order operations.
 * 
 * Responsibilities:
 * - CRUD operations for orders
 * - Manage global order counter (for Nth order logic)
 * - Query orders by user or ID
 * - Admin statistics
 * 
 * Thread-Safe: Uses ConcurrentHashMap and AtomicInteger from DataStore.
 */
@Repository
public class OrderRepository implements IOrderRepository {
    
    private final DataStore dataStore;
    
    public OrderRepository(DataStore dataStore) {
        this.dataStore = dataStore;
    }
    
    /**
     * Find order by ID.
     */
    @Override
    public Optional<Order> findById(UUID orderId) {
        return Optional.ofNullable(dataStore.orders.get(orderId));
    }
    
    /**
     * Get all orders.
     */
    @Override
    public List<Order> findAll() {
        return new ArrayList<>(dataStore.orders.values());
    }
    
    /**
     * Find all orders for a specific user.
     * Returns sorted by created date (latest first).
     */
    @Override
    public List<Order> findByUserId(String userId) {
        return dataStore.orders.values().stream()
                .filter(order -> order.getUserId().equals(userId))
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .toList();
    }
    
    /**
     * Save order and increment global order counter.
     * Returns the order number (Nth order in system).
     */
    @Override
    public int save(Order order) {
        if (order.getOrderId() == null) {
            order.setOrderId(UUID.randomUUID());
        }
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(Instant.now());
        }
        
        dataStore.orders.put(order.getOrderId(), order);
        
        // Increment and return order number (thread-safe)
        return dataStore.orderCounter.incrementAndGet();
    }
    
    /**
     * Get current global order count.
     */
    @Override
    public int getOrderCount() {
        return dataStore.orderCounter.get();
    }
    
    // ═══════════════════════════════════════════════════════════
    // Admin Statistics
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Get total number of items purchased across all orders.
     */
    @Override
    public int getTotalItemsPurchased() {
        return dataStore.orders.values().stream()
                .mapToInt(order -> order.getItems().stream()
                        .mapToInt(CartItem::getQuantity)
                        .sum())
                .sum();
    }
    
    /**
     * Get total purchase amount across all orders.
     */
    @Override
    public BigDecimal getTotalPurchaseAmount() {
        return dataStore.orders.values().stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Get total discount amount across all orders.
     */
    @Override
    public BigDecimal getTotalDiscountAmount() {
        return dataStore.orders.values().stream()
                .map(Order::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Count how many orders used coupons.
     */
    @Override
    public long countOrdersWithCoupons() {
        return dataStore.orders.values().stream()
                .filter(Order::hasCouponApplied)
                .count();
    }
}
