package com.ecommerce.store.controller;

import com.ecommerce.store.dto.AddToCartRequest;
import com.ecommerce.store.dto.CartResponse;
import com.ecommerce.store.dto.CartItemResponse;
import com.ecommerce.store.dto.UpdateQuantityRequest;
import com.ecommerce.store.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@DisplayName("CartController Tests")
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should add item to cart successfully")
    void addItemToCart_ValidRequest_ReturnsUpdatedCart() throws Exception {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        int quantity = 2;

        AddToCartRequest request = new AddToCartRequest();
        request.setItemId(itemId);
        request.setQuantity(quantity);

        CartResponse expectedResponse = createTestCartResponse(userId, itemId);
        when(cartService.addItemToCart(eq(userId), eq(itemId), eq(quantity))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(post("/api/cart/{userId}/items", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    @DisplayName("Should return 400 for invalid quantity")
    void addItemToCart_InvalidQuantity_ReturnsBadRequest() throws Exception {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();

        AddToCartRequest request = new AddToCartRequest();
        request.setItemId(itemId);
        request.setQuantity(0); // Invalid quantity

        // When & Then
        mockMvc.perform(post("/api/cart/{userId}/items", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should get cart for user")
    void getCart_ValidUser_ReturnsCart() throws Exception {
        // Given
        String userId = "user123";
        CartResponse expectedResponse = createTestCartResponse(userId, UUID.randomUUID());
        when(cartService.getCart(userId)).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/cart/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    @DisplayName("Should remove item from cart")
    void removeItemFromCart_ValidRequest_ReturnsUpdatedCart() throws Exception {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();

        CartResponse expectedResponse = createEmptyCartResponse(userId);
        when(cartService.removeItemFromCart(userId, itemId)).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(delete("/api/cart/{userId}/items/{itemId}", userId, itemId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @DisplayName("Should update item quantity")
    void updateItemQuantity_ValidRequest_ReturnsUpdatedCart() throws Exception {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        int newQuantity = 5;

        UpdateQuantityRequest request = new UpdateQuantityRequest();
        request.setQuantity(newQuantity);

        CartResponse expectedResponse = createUpdatedCartResponse(userId, itemId, newQuantity);
        when(cartService.updateItemQuantity(eq(userId), eq(itemId), eq(newQuantity))).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(put("/api/cart/{userId}/items/{itemId}", userId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalItems").value(5));
    }

    private CartResponse createTestCartResponse(String userId, UUID itemId) {
        CartResponse response = new CartResponse();
        response.setUserId(userId);
        response.setTotalItems(2);
        response.setTotalAmount(BigDecimal.valueOf(20.00));

        CartItemResponse cartItem = new CartItemResponse();
        cartItem.setItemId(itemId);
        cartItem.setItemName("Test Item");
        cartItem.setItemPrice(BigDecimal.valueOf(10.00));
        cartItem.setQuantity(2);
        cartItem.setSubtotal(BigDecimal.valueOf(20.00));
        cartItem.setStock(15);

        response.setItems(List.of(cartItem));
        return response;
    }

    private CartResponse createUpdatedCartResponse(String userId, UUID itemId, int quantity) {
        CartResponse response = new CartResponse();
        response.setUserId(userId);
        response.setTotalItems(quantity);
        response.setTotalAmount(BigDecimal.valueOf(50.00)); // 10 * 5

        CartItemResponse cartItem = new CartItemResponse();
        cartItem.setItemId(itemId);
        cartItem.setItemName("Test Item");
        cartItem.setItemPrice(BigDecimal.valueOf(10.00));
        cartItem.setQuantity(quantity);
        cartItem.setSubtotal(BigDecimal.valueOf(50.00));
        cartItem.setStock(15);

        response.setItems(List.of(cartItem));
        return response;
    }

    private CartResponse createEmptyCartResponse(String userId) {
        CartResponse response = new CartResponse();
        response.setUserId(userId);
        response.setTotalItems(0);
        response.setTotalAmount(BigDecimal.ZERO);
        response.setItems(List.of());
        return response;
    }
}