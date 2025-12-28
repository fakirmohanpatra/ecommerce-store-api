package com.ecommerce.store.repository;

import com.ecommerce.store.model.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Order operations.
 */
public interface IOrderRepository {
    
    /**
     * Find order by ID.
     */
    Optional<Order> findById(UUID orderId);
    
    /**
     * Get all orders.
     */
    List<Order> findAll();
    
    /**
     * Find all orders for a specific user.
     * Returns sorted by created date (latest first).
     */
    List<Order> findByUserId(String userId);
    
    /**
     * Save order and increment global order counter.
     * Returns the order number (Nth order in system).
     */
    int save(Order order);
    
    /**
     * Get current global order count.
     */
    int getOrderCount();
    
    /**
     * Get total number of items purchased across all orders.
     */
    int getTotalItemsPurchased();
    
    /**
     * Get total purchase amount across all orders.
     */
    BigDecimal getTotalPurchaseAmount();
    
    /**
     * Get total discount amount across all orders.
     */
    BigDecimal getTotalDiscountAmount();
    
    /**
     * Count how many orders used coupons.
     */
    long countOrdersWithCoupons();
}
