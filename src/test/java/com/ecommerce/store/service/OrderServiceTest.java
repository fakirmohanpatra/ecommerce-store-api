package com.ecommerce.store.service;

import com.ecommerce.store.dto.OrderResponse;
import com.ecommerce.store.exception.CouponValidationException;
import com.ecommerce.store.model.*;
import com.ecommerce.store.repository.ICartRepository;
import com.ecommerce.store.repository.ICouponRepository;
import com.ecommerce.store.repository.IItemRepository;
import com.ecommerce.store.repository.IOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("OrderService Tests")
class OrderServiceTest {

    @Mock
    private ICartRepository cartRepository;

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private ICouponRepository couponRepository;

    @Mock
    private IItemRepository itemRepository;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderService = new OrderServiceImpl(cartRepository, orderRepository, couponRepository, itemRepository);

        // Set default configuration values
        ReflectionTestUtils.setField(orderService, "nthOrder", 5);
        ReflectionTestUtils.setField(orderService, "discountPercentage", 10);
    }

    @Test
    @DisplayName("Should successfully checkout without coupon")
    void checkout_NoCoupon_Successful() {
        // Given
        String userId = "user123";
        Cart cart = createCartWithItems(userId, BigDecimal.valueOf(100.00));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(itemRepository.exists(any(UUID.class))).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(1);

        // When
        OrderResponse response = orderService.checkout(userId, null);

        // Then
        assertEquals(userId, response.getUserId());
        assertEquals(BigDecimal.valueOf(100.00), response.getTotalAmount());
        assertEquals(BigDecimal.ZERO, response.getDiscountAmount());
        assertNull(response.getCouponCode());
        assertEquals("PAID", response.getPaymentStatus());

        verify(cartRepository).delete(userId);
        verify(orderRepository).save(any(Order.class));
        verify(couponRepository, never()).validateAndUse(anyString());
    }

    @Test
    @DisplayName("Should successfully checkout with valid coupon")
    void checkout_ValidCoupon_AppliesDiscount() {
        // Given
        String userId = "user123";
        String couponCode = "VALID10";
        Cart cart = createCartWithItems(userId, BigDecimal.valueOf(100.00));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(itemRepository.exists(any(UUID.class))).thenReturn(true);
        when(couponRepository.validateAndUse(couponCode)).thenReturn(CouponValidationResult.VALID);
        when(orderRepository.save(any(Order.class))).thenReturn(1);

        // When
        OrderResponse response = orderService.checkout(userId, couponCode);

        // Then
        assertEquals(userId, response.getUserId());
        assertEquals(0, BigDecimal.valueOf(90.00).compareTo(response.getTotalAmount())); // 100 - 10% discount
        assertEquals(0, BigDecimal.valueOf(10.00).compareTo(response.getDiscountAmount()));
        assertEquals(couponCode, response.getCouponCode());

        verify(couponRepository).validateAndUse(couponCode);
        verify(cartRepository).delete(userId);
    }

    @Test
    @DisplayName("Should generate new coupon on nth order")
    void checkout_NthOrder_GeneratesCoupon() {
        // Given
        String userId = "user123";
        Cart cart = createCartWithItems(userId, BigDecimal.valueOf(50.00));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(itemRepository.exists(any(UUID.class))).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(5); // 5th order

        // When
        orderService.checkout(userId, null);

        // Then
        verify(couponRepository).generate(5);
    }

    @Test
    @DisplayName("Should not generate coupon on non-nth order")
    void checkout_NonNthOrder_NoCouponGenerated() {
        // Given
        String userId = "user123";
        Cart cart = createCartWithItems(userId, BigDecimal.valueOf(50.00));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(itemRepository.exists(any(UUID.class))).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenReturn(3); // 3rd order

        // When
        orderService.checkout(userId, null);

        // Then
        verify(couponRepository, never()).generate(anyInt());
    }

    @Test
    @DisplayName("Should throw exception when cart not found")
    void checkout_CartNotFound_ThrowsException() {
        // Given
        String userId = "user123";

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.checkout(userId, null));
        assertEquals("Cart not found for user: " + userId, exception.getMessage());

        verify(cartRepository).findByUserId(userId);
        verifyNoMoreInteractions(orderRepository, couponRepository, itemRepository);
    }

    @Test
    @DisplayName("Should throw exception when cart is empty")
    void checkout_EmptyCart_ThrowsException() {
        // Given
        String userId = "user123";
        Cart emptyCart = createEmptyCart(userId);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(emptyCart));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.checkout(userId, null));
        assertEquals("Cannot checkout with empty cart", exception.getMessage());

        verify(cartRepository).findByUserId(userId);
        verifyNoMoreInteractions(orderRepository, couponRepository, itemRepository);
    }

    @Test
    @DisplayName("Should throw exception when item no longer exists")
    void checkout_ItemNotAvailable_ThrowsException() {
        // Given
        String userId = "user123";
        UUID itemId = UUID.randomUUID();
        Cart cart = createCartWithItems(userId, BigDecimal.valueOf(50.00));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(itemRepository.exists(itemId)).thenReturn(false);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.checkout(userId, null));
        assertTrue(exception.getMessage().startsWith("Item no longer available:"));

        verify(itemRepository).exists(any(UUID.class));
        verifyNoMoreInteractions(orderRepository, couponRepository);
    }

    @Test
    @DisplayName("Should throw exception for invalid coupon")
    void checkout_InvalidCoupon_ThrowsException() {
        // Given
        String userId = "user123";
        String couponCode = "INVALID";
        Cart cart = createCartWithItems(userId, BigDecimal.valueOf(100.00));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(itemRepository.exists(any(UUID.class))).thenReturn(true);
        when(couponRepository.validateAndUse(couponCode)).thenReturn(CouponValidationResult.INVALID_CODE);

        // When & Then
        CouponValidationException exception = assertThrows(CouponValidationException.class,
                () -> orderService.checkout(userId, couponCode));
        assertTrue(exception.getMessage().contains("Invalid coupon code"));

        verify(couponRepository).validateAndUse(couponCode);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("Should throw exception for already used coupon")
    void checkout_UsedCoupon_ThrowsException() {
        // Given
        String userId = "user123";
        String couponCode = "USED";
        Cart cart = createCartWithItems(userId, BigDecimal.valueOf(100.00));

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(itemRepository.exists(any(UUID.class))).thenReturn(true);
        when(couponRepository.validateAndUse(couponCode)).thenReturn(CouponValidationResult.ALREADY_USED);

        // When & Then
        CouponValidationException exception = assertThrows(CouponValidationException.class,
                () -> orderService.checkout(userId, couponCode));
        assertTrue(exception.getMessage().contains("already used"));

        verify(couponRepository).validateAndUse(couponCode);
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    @DisplayName("Should return order history for user")
    void getOrderHistory_UserHasOrders_ReturnsHistory() {
        // Given
        String userId = "user123";
        Order order1 = createTestOrder(userId, BigDecimal.valueOf(50.00));
        Order order2 = createTestOrder(userId, BigDecimal.valueOf(75.00));

        when(orderRepository.findByUserId(userId)).thenReturn(List.of(order1, order2));

        // When
        List<OrderResponse> history = orderService.getOrderHistory(userId);

        // Then
        assertEquals(2, history.size());
        assertEquals(order1.getOrderId(), history.get(0).getOrderId());
        assertEquals(order2.getOrderId(), history.get(1).getOrderId());

        verify(orderRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should return empty history when user has no orders")
    void getOrderHistory_NoOrders_ReturnsEmptyList() {
        // Given
        String userId = "user123";

        when(orderRepository.findByUserId(userId)).thenReturn(List.of());

        // When
        List<OrderResponse> history = orderService.getOrderHistory(userId);

        // Then
        assertTrue(history.isEmpty());
        verify(orderRepository).findByUserId(userId);
    }

    private Cart createEmptyCart(String userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setItems(new ArrayList<>());
        cart.setTotal(BigDecimal.ZERO);
        return cart;
    }

    private Cart createCartWithItems(String userId, BigDecimal total) {
        Cart cart = createEmptyCart(userId);
        cart.setTotal(total);

        // Add a dummy cart item
        CartItem item = new CartItem();
        item.setItemId(UUID.randomUUID());
        item.setItemName("Test Item");
        item.setPrice(total);
        item.setQuantity(1);
        cart.getItems().add(item);

        return cart;
    }

    private Order createTestOrder(String userId, BigDecimal totalAmount) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setItems(new ArrayList<>());
        return order;
    }
}