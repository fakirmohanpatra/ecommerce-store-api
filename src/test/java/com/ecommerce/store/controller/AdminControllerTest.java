package com.ecommerce.store.controller;

import com.ecommerce.store.dto.AdminStatsResponse;
import com.ecommerce.store.dto.CouponListResponse;
import com.ecommerce.store.dto.CouponResponse;
import com.ecommerce.store.service.AdminService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@DisplayName("AdminController Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Test
    @DisplayName("Should return admin statistics")
    void getStatistics_ReturnsStatistics_Successfully() throws Exception {
        // Given
        AdminStatsResponse expectedStats = createTestStats();
        when(adminService.getStatistics()).thenReturn(expectedStats);

        // When & Then
        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalOrders").value(10))
                .andExpect(jsonPath("$.totalPurchaseAmount").value(500.00))
                .andExpect(jsonPath("$.totalItemsPurchased").value(25));
    }

    @Test
    @DisplayName("Should generate new coupon")
    void generateCoupon_ReturnsGeneratedCoupon_Successfully() throws Exception {
        // Given
        CouponResponse expectedCoupon = createTestCoupon();
        when(adminService.generateCoupon()).thenReturn(expectedCoupon);

        // When & Then
        mockMvc.perform(post("/api/admin/coupons/generate"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("SAVE10-001"));
    }

    @Test
    @DisplayName("Should return all coupons")
    void getAllCoupons_ReturnsCouponList_Successfully() throws Exception {
        // Given
        CouponListResponse expectedResponse = createTestCouponList();
        when(adminService.getAllCoupons()).thenReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/admin/coupons"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.coupons[0]").value("SAVE10-001"))
                .andExpect(jsonPath("$.coupons[1]").value("SAVE10-005"))
                .andExpect(jsonPath("$.totalGenerated").value(2));
    }

    @Test
    @DisplayName("Should return active coupon when exists")
    void getActiveCoupon_Exists_ReturnsCoupon() throws Exception {
        // Given
        CouponResponse expectedCoupon = createTestCoupon();
        when(adminService.getActiveCoupon()).thenReturn(expectedCoupon);

        // When & Then
        mockMvc.perform(get("/api/admin/coupons/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("SAVE10-001"));
    }

    @Test
    @DisplayName("Should return 404 when no active coupon")
    void getActiveCoupon_NoneExists_ReturnsNotFound() throws Exception {
        // Given
        when(adminService.getActiveCoupon()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/admin/coupons/active"))
                .andExpect(status().isNotFound());
    }

    private AdminStatsResponse createTestStats() {
        AdminStatsResponse stats = new AdminStatsResponse();
        stats.setTotalOrders(10);
        stats.setTotalPurchaseAmount(BigDecimal.valueOf(500.00));
        stats.setTotalItemsPurchased(25);
        stats.setActiveCoupon("SAVE10-010");
        return stats;
    }

    private CouponResponse createTestCoupon() {
        CouponResponse coupon = new CouponResponse();
        coupon.setCode("SAVE10-001");
        coupon.setUsed(false);
        coupon.setGeneratedAtOrderNumber(1);
        coupon.setCreatedAt(Instant.now());
        return coupon;
    }

    private CouponListResponse createTestCouponList() {
        CouponListResponse response = new CouponListResponse();
        response.setCoupons(List.of("SAVE10-001", "SAVE10-005"));
        response.setTotalGenerated(2);
        return response;
    }
}