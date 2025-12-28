package com.ecommerce.store.service;

import com.ecommerce.store.dto.CartItemResponse;
import com.ecommerce.store.dto.CartResponse;
import com.ecommerce.store.model.Cart;
import com.ecommerce.store.model.CartItem;
import com.ecommerce.store.model.Item;
import com.ecommerce.store.repository.ICartRepository;
import com.ecommerce.store.repository.IItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for Cart operations.
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    
    private final ICartRepository cartRepository;
    private final IItemRepository itemRepository;
    
    @Override
    public CartResponse addItemToCart(String userId, UUID itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        // Verify item exists
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
        
        // Get or create cart
        Cart cart = cartRepository.getOrCreate(userId);
        
        // Check if item already in cart
        CartItem existingItem = cart.getItems().stream()
                .filter(ci -> ci.getItemId().equals(itemId))
                .findFirst()
                .orElse(null);
        
        if (existingItem != null) {
            // Update quantity of existing item
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            // Add new item to cart (snapshot pattern)
            CartItem newItem = new CartItem();
            newItem.setItemId(item.getItemId());
            newItem.setItemName(item.getName());
            newItem.setPrice(item.getPrice());
            newItem.setQuantity(quantity);
            cart.getItems().add(newItem);
        }
        
        // Recalculate total
        cart.setTotal(calculateTotal(cart));
        
        // Save cart
        cartRepository.save(cart);
        
        return toCartResponse(cart);
    }
    
    @Override
    public CartResponse removeItemFromCart(String userId, UUID itemId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));
        
        // Remove item
        boolean removed = cart.getItems().removeIf(ci -> ci.getItemId().equals(itemId));
        
        if (!removed) {
            throw new IllegalArgumentException("Item not found in cart: " + itemId);
        }
        
        // Recalculate total
        cart.setTotal(calculateTotal(cart));
        
        // Save cart
        cartRepository.save(cart);
        
        return toCartResponse(cart);
    }
    
    @Override
    public CartResponse updateItemQuantity(String userId, UUID itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));
        
        // Find and update item
        CartItem cartItem = cart.getItems().stream()
                .filter(ci -> ci.getItemId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart: " + itemId));
        
        cartItem.setQuantity(quantity);
        
        // Recalculate total
        cart.setTotal(calculateTotal(cart));
        
        // Save cart
        cartRepository.save(cart);
        
        return toCartResponse(cart);
    }
    
    @Override
    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.getOrCreate(userId);
        return toCartResponse(cart);
    }
    
    @Override
    public void clearCart(String userId) {
        cartRepository.delete(userId);
    }
    
    /**
     * Calculate total from cart items.
     */
    private BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Convert Cart entity to CartResponse DTO.
     */
    private CartResponse toCartResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(this::toCartItemResponse)
                .toList();
        
        int totalItems = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        
        return new CartResponse(cart.getUserId(), itemResponses, totalItems, cart.getTotal());
    }
    
    /**
     * Convert CartItem entity to CartItemResponse DTO.
     */
    private CartItemResponse toCartItemResponse(CartItem item) {
        return new CartItemResponse(
                item.getItemId(),
                item.getItemName(),
                item.getPrice(),
                item.getQuantity(),
                item.getSubtotal()
        );
    }
}
