package com.ecommerce.store.service;

import com.ecommerce.store.dto.OrderResponse;

import java.util.List;

/**
 * Service interface for Order/Checkout operations.
 */
public interface OrderService {
    
    /**
     * Checkout - Create order from user's cart.
     * 
     * Business Logic:
     * 1. Validate cart not empty
     * 2. Validate items still exist
     * 3. Apply coupon if provided (validate + calculate discount)
     * 4. Create order (snapshot of cart)
     * 5. Check if Nth order → generate new coupon
     * 6. Clear cart
     * 7. Return order confirmation
     * 
     * @param userId User identifier
     * @param couponCode Optional coupon code to apply
     * @return Created order details
     */
    OrderResponse checkout(String userId, String couponCode);
    
    /**
     * Get user's order history.
     * 
     * @param userId User identifier
     * @return List of orders (sorted by date, latest first)
     */
    List<OrderResponse> getOrderHistory(String userId);
}
