package com.ecommerce.store.controller;

import com.ecommerce.store.dto.*;
import com.ecommerce.store.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("Order Controller Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    @Test
    @DisplayName("Should successfully checkout without coupon")
    void checkout_NoCoupon_Successful() throws Exception {
        // Given
        String userId = "user123";
        CheckoutRequest request = new CheckoutRequest(userId, null);

        UUID orderId = UUID.randomUUID();
        List<CartItemResponse> items = Arrays.asList(
                new CartItemResponse(UUID.randomUUID(), "Laptop", BigDecimal.valueOf(999.99), 1, BigDecimal.valueOf(999.99), 10)
        );
        OrderResponse response = new OrderResponse(
                orderId, userId, items, BigDecimal.valueOf(999.99),
                BigDecimal.ZERO, null, "PAID", Instant.now()
        );

        when(orderService.checkout(userId, null)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.totalAmount").value(999.99))
                .andExpect(jsonPath("$.discountAmount").value(0.00))
                .andExpect(jsonPath("$.couponCode").isEmpty())
                .andExpect(jsonPath("$.paymentStatus").value("PAID"))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    @DisplayName("Should successfully checkout with valid coupon")
    void checkout_WithValidCoupon_Successful() throws Exception {
        // Given
        String userId = "user123";
        String couponCode = "SAVE10-005";
        CheckoutRequest request = new CheckoutRequest(userId, couponCode);

        UUID orderId = UUID.randomUUID();
        List<CartItemResponse> items = Arrays.asList(
                new CartItemResponse(UUID.randomUUID(), "Laptop", BigDecimal.valueOf(999.99), 1, BigDecimal.valueOf(999.99), 10)
        );
        OrderResponse response = new OrderResponse(
                orderId, userId, items, BigDecimal.valueOf(899.99),
                BigDecimal.valueOf(100.00), couponCode, "PAID", Instant.now()
        );

        when(orderService.checkout(userId, couponCode)).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.totalAmount").value(899.99))
                .andExpect(jsonPath("$.discountAmount").value(100.00))
                .andExpect(jsonPath("$.couponCode").value(couponCode));
    }

    @Test
    @DisplayName("Should return 400 for missing userId")
    void checkout_MissingUserId_Returns400() throws Exception {
        // Given
        CheckoutRequest request = new CheckoutRequest("", "SAVE10-005");

        // When & Then
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for null userId")
    void checkout_NullUserId_Returns400() throws Exception {
        // Given
        CheckoutRequest request = new CheckoutRequest(null, "SAVE10-005");

        // When & Then
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 for invalid JSON")
    void checkout_InvalidJson_Returns400() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/orders/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return order history for user")
    void getOrderHistory_UserHasOrders_ReturnsHistory() throws Exception {
        // Given
        String userId = "user123";
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        List<CartItemResponse> items1 = Arrays.asList(
                new CartItemResponse(UUID.randomUUID(), "Laptop", BigDecimal.valueOf(999.99), 1, BigDecimal.valueOf(999.99), 10)
        );
        List<CartItemResponse> items2 = Arrays.asList(
                new CartItemResponse(UUID.randomUUID(), "Smartphone", BigDecimal.valueOf(699.99), 1, BigDecimal.valueOf(699.99), 25)
        );

        List<OrderResponse> orders = Arrays.asList(
                new OrderResponse(orderId1, userId, items1, BigDecimal.valueOf(999.99),
                        BigDecimal.ZERO, null, "PAID", Instant.now().minusSeconds(3600)),
                new OrderResponse(orderId2, userId, items2, BigDecimal.valueOf(699.99),
                        BigDecimal.ZERO, null, "PAID", Instant.now())
        );

        when(orderService.getOrderHistory(userId)).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/orders/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].orderId").value(orderId1.toString()))
                .andExpect(jsonPath("$[0].totalAmount").value(999.99))
                .andExpect(jsonPath("$[1].orderId").value(orderId2.toString()))
                .andExpect(jsonPath("$[1].totalAmount").value(699.99));
    }

    @Test
    @DisplayName("Should return empty list when user has no orders")
    void getOrderHistory_UserHasNoOrders_ReturnsEmptyList() throws Exception {
        // Given
        String userId = "user123";
        List<OrderResponse> emptyOrders = Arrays.asList();
        when(orderService.getOrderHistory(userId)).thenReturn(emptyOrders);

        // When & Then
        mockMvc.perform(get("/api/orders/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.length()").value(0));
    }
}