package com.ecommerce.store.controller;

import com.ecommerce.store.dto.AddToCartRequest;
import com.ecommerce.store.dto.CartResponse;
import com.ecommerce.store.dto.UpdateQuantityRequest;
import com.ecommerce.store.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class CartController {
    
    private final CartService cartService;
    
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
        
        CartResponse response = cartService.addItemToCart(
                userId, 
                request.getItemId(), 
                request.getQuantity()
        );
        return ResponseEntity.ok(response);
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
        
        CartResponse response = cartService.removeItemFromCart(userId, itemId);
        return ResponseEntity.ok(response);
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
        
        CartResponse response = cartService.updateItemQuantity(
                userId, 
                itemId, 
                request.getQuantity()
        );
        return ResponseEntity.ok(response);
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
        
        CartResponse response = cartService.getCart(userId);
        return ResponseEntity.ok(response);
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
        
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
