package com.ecommerce.store.repository;

import com.ecommerce.store.model.Order;
import com.ecommerce.store.model.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderRepository Tests")
class OrderRepositoryTest {

    private OrderRepository orderRepository;
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
        orderRepository = new OrderRepository(dataStore);
    }

    @Test
    @DisplayName("Should save order and increment counter")
    void save_Order_SavesAndIncrementsCounter() {
        // Given
        Order order = createTestOrder("user123", BigDecimal.valueOf(99.99));

        // When
        orderRepository.save(order);

        // Then
        assertEquals(order, dataStore.orders.get(order.getOrderId()));
        assertEquals(1, dataStore.orderCounter.get());
    }

    @Test
    @DisplayName("Should return next order number")
    void getNextOrderNumber_IncrementsCounter() {
        // When
        Order order1 = createTestOrder("user123", BigDecimal.valueOf(99.99));
        Order order2 = createTestOrder("user456", BigDecimal.valueOf(50.00));

        int orderNumber1 = orderRepository.save(order1);
        int orderNumber2 = orderRepository.save(order2);

        // Then
        assertEquals(1, orderNumber1);
        assertEquals(2, orderNumber2);
        assertEquals(2, dataStore.orderCounter.get());
    }

    @Test
    @DisplayName("Should find orders by user ID")
    void findByUserId_UserHasOrders_ReturnsOrders() {
        // Given
        Order order1 = createTestOrder("user123", BigDecimal.valueOf(50.00));
        Order order2 = createTestOrder("user123", BigDecimal.valueOf(75.00));
        Order order3 = createTestOrder("user456", BigDecimal.valueOf(25.00));

        dataStore.orders.put(order1.getOrderId(), order1);
        dataStore.orders.put(order2.getOrderId(), order2);
        dataStore.orders.put(order3.getOrderId(), order3);

        // When
        List<Order> userOrders = orderRepository.findByUserId("user123");

        // Then
        assertEquals(2, userOrders.size());
        assertTrue(userOrders.contains(order1));
        assertTrue(userOrders.contains(order2));
    }

    @Test
    @DisplayName("Should return empty list when user has no orders")
    void findByUserId_UserHasNoOrders_ReturnsEmptyList() {
        // When
        List<Order> userOrders = orderRepository.findByUserId("user123");

        // Then
        assertTrue(userOrders.isEmpty());
    }

    @Test
    @DisplayName("Should return all orders")
    void findAll_ReturnsAllOrders() {
        // Given
        Order order1 = createTestOrder("user123", BigDecimal.valueOf(50.00));
        Order order2 = createTestOrder("user456", BigDecimal.valueOf(75.00));

        dataStore.orders.put(order1.getOrderId(), order1);
        dataStore.orders.put(order2.getOrderId(), order2);

        // When
        List<Order> allOrders = orderRepository.findAll();

        // Then
        assertEquals(2, allOrders.size());
        assertTrue(allOrders.contains(order1));
        assertTrue(allOrders.contains(order2));
    }

    @Test
    @DisplayName("Should calculate total discount amount across all orders")
    void getTotalDiscountAmount_CalculatesCorrectly() {
        // Given
        Order order1 = createTestOrder("user123", BigDecimal.valueOf(100.00));
        order1.setDiscountAmount(BigDecimal.valueOf(10.00));

        Order order2 = createTestOrder("user456", BigDecimal.valueOf(200.00));
        order2.setDiscountAmount(BigDecimal.valueOf(20.00));

        Order order3 = createTestOrder("user789", BigDecimal.valueOf(50.00));
        order3.setDiscountAmount(BigDecimal.ZERO);

        dataStore.orders.put(order1.getOrderId(), order1);
        dataStore.orders.put(order2.getOrderId(), order2);
        dataStore.orders.put(order3.getOrderId(), order3);

        // When
        BigDecimal totalDiscount = orderRepository.getTotalDiscountAmount();

        // Then
        assertEquals(BigDecimal.valueOf(30.00), totalDiscount);
    }

    @Test
    @DisplayName("Should return zero when no orders exist")
    void getTotalDiscountAmount_NoOrders_ReturnsZero() {
        // When
        BigDecimal totalDiscount = orderRepository.getTotalDiscountAmount();

        // Then
        assertEquals(BigDecimal.ZERO, totalDiscount);
    }

    @Test
    @DisplayName("Should return zero when orders have no discounts")
    void getTotalDiscountAmount_NoDiscounts_ReturnsZero() {
        // Given
        Order order1 = createTestOrder("user123", BigDecimal.valueOf(100.00));
        order1.setDiscountAmount(BigDecimal.ZERO);

        Order order2 = createTestOrder("user456", BigDecimal.valueOf(200.00));
        order2.setDiscountAmount(BigDecimal.ZERO);

        dataStore.orders.put(order1.getOrderId(), order1);
        dataStore.orders.put(order2.getOrderId(), order2);

        // When
        BigDecimal totalDiscount = orderRepository.getTotalDiscountAmount();

        // Then
        assertEquals(BigDecimal.ZERO, totalDiscount);
    }

    private Order createTestOrder(String userId, BigDecimal totalAmount) {
        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setCreatedAt(Instant.now());
        return order;
    }
}