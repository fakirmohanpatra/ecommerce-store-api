package com.ecommerce.store.service;

import com.ecommerce.store.dto.CartItemResponse;
import com.ecommerce.store.dto.CartResponse;
import com.ecommerce.store.model.Cart;
import com.ecommerce.store.model.CartItem;
import com.ecommerce.store.model.Item;
import com.ecommerce.store.repository.ICartRepository;
import com.ecommerce.store.repository.IItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CartService Tests")
class CartServiceTest {

    @Mock
    private ICartRepository cartRepository;

    @Mock
    private IItemRepository itemRepository;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        cartService = new CartServiceImpl(cartRepository, itemRepository);
    }

    @Test
    @DisplayName("Should add new item to empty cart")
    void addItemToCart_NewItem_EmptyCart_AddsSuccessfully() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        int quantity = 2;

        Item item = createTestItem(itemId, "Test Item", BigDecimal.valueOf(10.00));
        item.setStock(5); // Set initial stock
        Cart emptyCart = createEmptyCart(userId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(cartRepository.getOrCreate(userId)).thenReturn(emptyCart);
        when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);

        // When
        CartResponse response = cartService.addItemToCart(userId, itemId, quantity);

        // Then
        assertEquals(userId, response.getUserId());
        assertEquals(1, response.getItems().size());
        assertEquals(quantity, response.getTotalItems());
        assertEquals(BigDecimal.valueOf(20.00), response.getTotalAmount());

        CartItemResponse cartItem = response.getItems().get(0);
        assertEquals(itemId, cartItem.getItemId());
        assertEquals("Test Item", cartItem.getItemName());
        assertEquals(BigDecimal.valueOf(10.00), cartItem.getItemPrice());
        assertEquals(quantity, cartItem.getQuantity());
        assertEquals(BigDecimal.valueOf(20.00), cartItem.getSubtotal());

        verify(cartRepository).save(emptyCart);
        verify(itemRepository).decreaseStock(itemId);
    }

    @Test
    @DisplayName("Should add quantity to existing item in cart")
    void addItemToCart_ExistingItem_IncreasesQuantity() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        int existingQuantity = 1;
        int additionalQuantity = 3;

        Item item = createTestItem(itemId, "Test Item", BigDecimal.valueOf(15.00));
        item.setStock(10); // Set sufficient stock
        Cart cart = createCartWithItem(userId, itemId, "Test Item", BigDecimal.valueOf(15.00), existingQuantity);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(cartRepository.getOrCreate(userId)).thenReturn(cart);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        CartResponse response = cartService.addItemToCart(userId, itemId, additionalQuantity);

        // Then
        assertEquals(userId, response.getUserId());
        assertEquals(1, response.getItems().size());
        assertEquals(4, response.getTotalItems()); // 1 + 3
        assertEquals(BigDecimal.valueOf(60.00), response.getTotalAmount()); // 15 * 4

        verify(cartRepository).save(cart);
        verify(itemRepository).decreaseStock(itemId);
    }

    @Test
    @DisplayName("Should throw exception when adding item with zero quantity")
    void addItemToCart_ZeroQuantity_ThrowsException() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        int quantity = 0;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.addItemToCart(userId, itemId, quantity));
        assertEquals("Quantity must be positive", exception.getMessage());

        verifyNoInteractions(itemRepository);
        verifyNoInteractions(cartRepository);
    }

    @Test
    @DisplayName("Should throw exception when adding out of stock item")
    void addItemToCart_OutOfStockItem_ThrowsException() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        int quantity = 1;

        Item item = createTestItem(itemId, "Out of Stock Item", BigDecimal.valueOf(10.00));
        item.setStock(0); // Out of stock

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.addItemToCart(userId, itemId, quantity));
        assertEquals("Item is out of stock: " + itemId, exception.getMessage());

        verify(itemRepository).findById(itemId);
        verifyNoInteractions(cartRepository);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    @DisplayName("Should throw exception when adding non-existent item")
    void addItemToCart_ItemNotFound_ThrowsException() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        int quantity = 1;

        when(itemRepository.findById(itemId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.addItemToCart(userId, itemId, quantity));
        assertEquals("Item not found: " + itemId, exception.getMessage());

        verify(itemRepository).findById(itemId);
        verifyNoInteractions(cartRepository);
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    @DisplayName("Should remove item from cart")
    void removeItemFromCart_ItemExists_RemovesSuccessfully() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();

        Cart cart = createCartWithItem(userId, itemId, "Test Item", BigDecimal.valueOf(10.00), 2);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        CartResponse response = cartService.removeItemFromCart(userId, itemId);

        // Then
        assertEquals(userId, response.getUserId());
        assertEquals(0, response.getItems().size());
        assertEquals(0, response.getTotalItems());
        assertEquals(BigDecimal.ZERO, response.getTotalAmount());

        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Should throw exception when removing item from non-existent cart")
    void removeItemFromCart_CartNotFound_ThrowsException() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.removeItemFromCart(userId, itemId));
        assertEquals("Cart not found for user: " + userId, exception.getMessage());

        verify(cartRepository).findByUserId(userId);
        verifyNoMoreInteractions(cartRepository);
    }

    @Test
    @DisplayName("Should throw exception when removing non-existent item from cart")
    void removeItemFromCart_ItemNotInCart_ThrowsException() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        UUID differentItemId = UUID.randomUUID();

        Cart cart = createCartWithItem(userId, differentItemId, "Different Item", BigDecimal.valueOf(10.00), 1);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.removeItemFromCart(userId, itemId));
        assertEquals("Item not found in cart: " + itemId, exception.getMessage());

        verify(cartRepository).findByUserId(userId);
        verifyNoMoreInteractions(cartRepository);
    }

    @Test
    @DisplayName("Should update item quantity in cart")
    void updateItemQuantity_ItemExists_UpdatesSuccessfully() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        int newQuantity = 5;

        Cart cart = createCartWithItem(userId, itemId, "Test Item", BigDecimal.valueOf(10.00), 2);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When
        CartResponse response = cartService.updateItemQuantity(userId, itemId, newQuantity);

        // Then
        assertEquals(userId, response.getUserId());
        assertEquals(1, response.getItems().size());
        assertEquals(newQuantity, response.getTotalItems());
        assertEquals(BigDecimal.valueOf(50.00), response.getTotalAmount()); // 10 * 5

        verify(cartRepository).save(cart);
    }

    @Test
    @DisplayName("Should throw exception when updating quantity to zero")
    void updateItemQuantity_ZeroQuantity_ThrowsException() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        int quantity = 0;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.updateItemQuantity(userId, itemId, quantity));
        assertEquals("Quantity must be positive", exception.getMessage());

        verifyNoInteractions(cartRepository);
    }

    @Test
    @DisplayName("Should get cart for user")
    void getCart_UserExists_ReturnsCart() {
        // Given
        String userId = "user123";
        Cart cart = createCartWithItem(userId, UUID.randomUUID(), "Test Item", BigDecimal.valueOf(10.00), 1);

        when(cartRepository.getOrCreate(userId)).thenReturn(cart);

        // When
        CartResponse response = cartService.getCart(userId);

        // Then
        assertEquals(userId, response.getUserId());
        assertEquals(1, response.getItems().size());
        assertEquals(1, response.getTotalItems());
        assertEquals(BigDecimal.valueOf(10.00), response.getTotalAmount());

        verify(cartRepository).getOrCreate(userId);
    }

    @Test
    @DisplayName("Should handle concurrent cart operations safely")
    void addItemToCart_ConcurrentAccess_HandlesSafely() {
        // Given - Test thread safety with concurrent operations
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        Item item = createTestItem(itemId, "Test Item", BigDecimal.valueOf(10.00));
        Cart cart = createEmptyCart(userId);

        when(itemRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(cartRepository.getOrCreate(userId)).thenReturn(cart);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        // When - Simulate concurrent additions (in real scenario, this would be handled by the repository)
        cartService.addItemToCart(userId, itemId, 1);
        cartService.addItemToCart(userId, itemId, 2);

        // Then - Should handle the operations without corruption
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity()); // 1 + 2
        assertEquals(BigDecimal.valueOf(30.00), cart.getTotal());

        verify(cartRepository, times(2)).save(cart);
    }

    @Test
    @DisplayName("Should validate negative quantities are rejected")
    void addItemToCart_NegativeQuantity_ThrowsException() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        int quantity = -1;

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cartService.addItemToCart(userId, itemId, quantity));
        assertEquals("Quantity must be positive", exception.getMessage());

        verifyNoInteractions(itemRepository);
        verifyNoInteractions(cartRepository);
    }

    private Item createTestItem(UUID itemId, String name, BigDecimal price) {
        Item item = new Item();
        item.setItemId(itemId);
        item.setName(name);
        item.setPrice(price);
        item.setStock(10); // Default stock for tests
        return item;
    }

    private Cart createEmptyCart(String userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setTotal(BigDecimal.ZERO);
        return cart;
    }

    private Cart createCartWithItem(String userId, UUID itemId, String itemName, BigDecimal price, int quantity) {
        Cart cart = createEmptyCart(userId);

        CartItem cartItem = new CartItem();
        cartItem.setItemId(itemId);
        cartItem.setItemName(itemName);
        cartItem.setPrice(price);
        cartItem.setQuantity(quantity);

        cart.getItems().add(cartItem);
        cart.setTotal(price.multiply(BigDecimal.valueOf(quantity)));

        return cart;
    }
}