package com.ecommerce.store.controller;

import com.ecommerce.store.dto.AddToCartRequest;
import com.ecommerce.store.dto.CartResponse;
import com.ecommerce.store.dto.UpdateQuantityRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST API for Cart operations.
 * 
 * API Contract:
 * - POST   /api/cart/{userId}/items          - Add item to cart
 * - DELETE /api/cart/{userId}/items/{itemId} - Remove item from cart
 * - PUT    /api/cart/{userId}/items/{itemId} - Update item quantity
 * - GET    /api/cart/{userId}                - Get user's cart
 * - DELETE /api/cart/{userId}                - Clear cart
 */
@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    /**
     * Add item to cart.
     * 
     * POST /api/cart/{userId}/items
     * 
     * Request Body:
     * {
     *   "itemId": "uuid",
     *   "quantity": 2
     * }
     * 
     * Response: CartResponse (full cart with all items)
     */
    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItemToCart(
            @PathVariable String userId,
            @Valid @RequestBody AddToCartRequest request) {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
    
    /**
     * Remove item from cart.
     * 
     * DELETE /api/cart/{userId}/items/{itemId}
     * 
     * Response: CartResponse (updated cart)
     */
    @DeleteMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> removeItemFromCart(
            @PathVariable String userId,
            @PathVariable UUID itemId) {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
    
    /**
     * Update item quantity in cart.
     * 
     * PUT /api/cart/{userId}/items/{itemId}
     * 
     * Request Body:
     * {
     *   "quantity": 5
     * }
     * 
     * Response: CartResponse
     */
    @PutMapping("/{userId}/items/{itemId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable String userId,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateQuantityRequest request) {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get user's cart.
     * 
     * GET /api/cart/{userId}
     * 
     * Response: CartResponse with all items and total
     */
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable String userId) {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
    
    /**
     * Clear user's cart.
     * 
     * DELETE /api/cart/{userId}
     * 
     * Response: 204 No Content
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable String userId) {
        
        // TODO: Implement in service layer
        return ResponseEntity.noContent().build();
    }
}
