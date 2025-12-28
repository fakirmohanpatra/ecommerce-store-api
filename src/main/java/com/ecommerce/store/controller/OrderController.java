package com.ecommerce.store.controller;

import com.ecommerce.store.dto.CheckoutRequest;
import com.ecommerce.store.dto.OrderResponse;
import com.ecommerce.store.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OrderController {
    
    private final OrderService orderService;
    
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
        
        OrderResponse response = orderService.checkout(
                request.getUserId(), 
                request.getCouponCode()
        );
        return ResponseEntity.ok(response);
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
        
        List<OrderResponse> orders = orderService.getOrderHistory(userId);
        return ResponseEntity.ok(orders);
    }
}
