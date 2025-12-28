package com.ecommerce.store.service;

import com.ecommerce.store.dto.AdminStatsResponse;
import com.ecommerce.store.dto.CouponListResponse;
import com.ecommerce.store.dto.CouponResponse;
import com.ecommerce.store.model.Coupon;
import com.ecommerce.store.model.Order;
import com.ecommerce.store.repository.ICouponRepository;
import com.ecommerce.store.repository.IOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AdminService Tests")
class AdminServiceTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private ICouponRepository couponRepository;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        adminService = new AdminServiceImpl(orderRepository, couponRepository);
    }

    @Test
    @DisplayName("Should return complete store statistics")
    void getStatistics_ReturnsCompleteStats() {
        // Given
        List<Order> orders = Arrays.asList(
                createTestOrder(BigDecimal.valueOf(100.00), BigDecimal.valueOf(10.00)),
                createTestOrder(BigDecimal.valueOf(200.00), BigDecimal.valueOf(20.00)),
                createTestOrder(BigDecimal.valueOf(50.00), BigDecimal.ZERO)
        );

        Coupon activeCoupon = createTestCoupon("ACTIVE10", false, 5);

        when(orderRepository.getTotalItemsPurchased()).thenReturn(15);
        when(orderRepository.getTotalPurchaseAmount()).thenReturn(BigDecimal.valueOf(350.00));
        when(orderRepository.getTotalDiscountAmount()).thenReturn(BigDecimal.valueOf(30.00));
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderRepository.countOrdersWithCoupons()).thenReturn(2L);
        when(couponRepository.getGeneratedCount()).thenReturn(3);
        when(couponRepository.getActive()).thenReturn(Optional.of(activeCoupon));

        // When
        AdminStatsResponse stats = adminService.getStatistics();

        // Then
        assertEquals(15, stats.getTotalItemsPurchased());
        assertEquals(BigDecimal.valueOf(350.00), stats.getTotalPurchaseAmount());
        assertEquals(BigDecimal.valueOf(30.00), stats.getTotalDiscountAmount());
        assertEquals(3, stats.getTotalOrders());
        assertEquals(2L, stats.getOrdersWithCoupons());
        assertEquals(3, stats.getTotalCouponsGenerated());
        assertEquals("ACTIVE10", stats.getActiveCoupon());
    }

    @Test
    @DisplayName("Should return statistics with no active coupon")
    void getStatistics_NoActiveCoupon_ReturnsNull() {
        // Given
        when(orderRepository.getTotalItemsPurchased()).thenReturn(5);
        when(orderRepository.getTotalPurchaseAmount()).thenReturn(BigDecimal.valueOf(100.00));
        when(orderRepository.getTotalDiscountAmount()).thenReturn(BigDecimal.ZERO);
        when(orderRepository.findAll()).thenReturn(List.of());
        when(orderRepository.countOrdersWithCoupons()).thenReturn(0L);
        when(couponRepository.getGeneratedCount()).thenReturn(0);
        when(couponRepository.getActive()).thenReturn(Optional.empty());

        // When
        AdminStatsResponse stats = adminService.getStatistics();

        // Then
        assertEquals(5, stats.getTotalItemsPurchased());
        assertEquals(BigDecimal.valueOf(100.00), stats.getTotalPurchaseAmount());
        assertEquals(BigDecimal.ZERO, stats.getTotalDiscountAmount());
        assertEquals(0, stats.getTotalOrders());
        assertEquals(0L, stats.getOrdersWithCoupons());
        assertEquals(0, stats.getTotalCouponsGenerated());
        assertNull(stats.getActiveCoupon());
    }

    @Test
    @DisplayName("Should return all generated coupons")
    void getAllCoupons_ReturnsAllCoupons() {
        // Given
        List<String> coupons = Arrays.asList("COUPON1", "COUPON2", "COUPON3");

        when(couponRepository.getAllGenerated()).thenReturn(coupons);

        // When
        CouponListResponse response = adminService.getAllCoupons();

        // Then
        assertEquals(coupons, response.getCoupons());
        assertEquals(3, response.getTotalGenerated());

        verify(couponRepository).getAllGenerated();
    }

    @Test
    @DisplayName("Should return empty coupon list when no coupons generated")
    void getAllCoupons_NoCoupons_ReturnsEmptyList() {
        // Given
        when(couponRepository.getAllGenerated()).thenReturn(List.of());

        // When
        CouponListResponse response = adminService.getAllCoupons();

        // Then
        assertTrue(response.getCoupons().isEmpty());
        assertEquals(0, response.getTotalGenerated());

        verify(couponRepository).getAllGenerated();
    }

    @Test
    @DisplayName("Should return active coupon")
    void getActiveCoupon_Exists_ReturnsCoupon() {
        // Given
        Coupon activeCoupon = createTestCoupon("ACTIVE10", false, 5);

        when(couponRepository.getActive()).thenReturn(Optional.of(activeCoupon));

        // When
        CouponResponse response = adminService.getActiveCoupon();

        // Then
        assertNotNull(response);
        assertEquals("ACTIVE10", response.getCode());
        assertFalse(response.isUsed());
        assertEquals(5, response.getGeneratedAtOrderNumber());

        verify(couponRepository).getActive();
    }

    @Test
    @DisplayName("Should return null when no active coupon")
    void getActiveCoupon_NoActive_ReturnsNull() {
        // Given
        when(couponRepository.getActive()).thenReturn(Optional.empty());

        // When
        CouponResponse response = adminService.getActiveCoupon();

        // Then
        assertNull(response);

        verify(couponRepository).getActive();
    }

    @Test
    @DisplayName("Should generate new coupon")
    void generateCoupon_GeneratesAndReturnsCoupon() {
        // Given
        Coupon newCoupon = createTestCoupon("NEWCOUPON", false, 10);

        when(orderRepository.getOrderCount()).thenReturn(10);
        when(couponRepository.generate(10)).thenReturn(newCoupon);

        // When
        CouponResponse response = adminService.generateCoupon();

        // Then
        assertNotNull(response);
        assertEquals("NEWCOUPON", response.getCode());
        assertFalse(response.isUsed());
        assertEquals(10, response.getGeneratedAtOrderNumber());

        verify(orderRepository).getOrderCount();
        verify(couponRepository).generate(10);
    }

    private Order createTestOrder(BigDecimal totalAmount, BigDecimal discountAmount) {
        Order order = new Order();
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        return order;
    }

    private Coupon createTestCoupon(String code, boolean used, int generatedAtOrderNumber) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setUsed(used);
        coupon.setGeneratedAtOrderNumber(generatedAtOrderNumber);
        coupon.setCreatedAt(Instant.now());
        return coupon;
    }
}