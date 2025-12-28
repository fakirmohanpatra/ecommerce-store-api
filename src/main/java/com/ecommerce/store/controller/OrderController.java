package com.ecommerce.store.controller;

import com.ecommerce.store.dto.CheckoutRequest;
import com.ecommerce.store.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for Order/Checkout operations.
 * 
 * API Contract:
 * - POST /api/orders/checkout - Checkout cart and create order
 * - GET  /api/orders/{userId}  - Get user's order history
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    /**
     * Checkout - Create order from cart.
     * 
     * POST /api/orders/checkout
     * 
     * Request Body:
     * {
     *   "userId": "user123",
     *   "couponCode": "SAVE10-005"  // Optional
     * }
     * 
     * Response: OrderResponse
     * - Order details with items
     * - Total amount after discount (if any)
     * - Order ID for tracking
     * 
     * Business Logic:
     * 1. Validate cart not empty
     * 2. Validate items still exist
     * 3. Apply coupon if provided (validate + calculate discount)
     * 4. Create order (snapshot of cart)
     * 5. Check if Nth order → generate new coupon
     * 6. Clear cart
     * 7. Return order confirmation
     */
    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get user's order history.
     * 
     * GET /api/orders/{userId}
     * 
     * Response: List<OrderResponse> (sorted by date, latest first)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrderHistory(@PathVariable String userId) {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
}
