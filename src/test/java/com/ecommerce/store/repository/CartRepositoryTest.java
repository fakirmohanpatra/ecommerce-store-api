package com.ecommerce.store.repository;

import com.ecommerce.store.model.Cart;
import com.ecommerce.store.model.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CartRepository Tests")
class CartRepositoryTest {

    private CartRepository cartRepository;
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
        cartRepository = new CartRepository(dataStore);
    }

    @Test
    @DisplayName("Should create new cart for new user")
    void getOrCreate_NewUser_CreatesCart() {
        // When
        Cart cart = cartRepository.getOrCreate("user123");

        // Then
        assertNotNull(cart);
        assertEquals("user123", cart.getUserId());
        assertTrue(cart.getItems().isEmpty());
        assertEquals(dataStore.carts.get("user123"), cart);
    }

    @Test
    @DisplayName("Should return existing cart for existing user")
    void findByUserId_ExistingUser_ReturnsExistingCart() {
        // Given
        Cart existingCart = new Cart();
        existingCart.setUserId("user123");
        dataStore.carts.put("user123", existingCart);

        // When
        Cart cart = cartRepository.findByUserId("user123").orElse(null);

        // Then
        assertEquals(existingCart, cart);
    }

    @Test
    @DisplayName("Should save cart to data store")
    void save_Cart_SavesToDataStore() {
        // Given
        Cart cart = new Cart();
        cart.setUserId("user123");

        // When
        cartRepository.save(cart);

        // Then
        assertEquals(cart, dataStore.carts.get("user123"));
    }

    @Test
    @DisplayName("Should clear cart items")
    void clear_Cart_RemovesAllItems() {
        // Given
        Cart cart = new Cart();
        cart.setUserId("user123");
        cart.getItems().add(createTestCartItem());
        cart.getItems().add(createTestCartItem());
        dataStore.carts.put("user123", cart);

        // When
        cartRepository.delete("user123");

        // Then
        assertFalse(dataStore.carts.containsKey("user123"));
    }

    @Test
    @DisplayName("Should handle clear on non-existent cart gracefully")
    void clear_NonExistentCart_DoesNothing() {
        // When
        cartRepository.delete("nonexistent");

        // Then - no exception thrown
        assertFalse(dataStore.carts.containsKey("nonexistent"));
    }

    private CartItem createTestCartItem() {
        CartItem item = new CartItem();
        item.setItemId(UUID.randomUUID());
        item.setItemName("Test Item");
        item.setPrice(BigDecimal.valueOf(10.00));
        item.setQuantity(1);
        return item;
    }
}