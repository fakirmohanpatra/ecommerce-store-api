package com.ecommerce.store.controller;

import com.ecommerce.store.dto.CouponListResponse;
import com.ecommerce.store.dto.CouponResponse;
import com.ecommerce.store.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
@DisplayName("Coupon Controller Tests")
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;

    @Test
    @DisplayName("Should return active coupon when exists")
    void getActiveCoupon_Exists_ReturnsCoupon() throws Exception {
        // Given
        CouponResponse coupon = new CouponResponse(
                "SAVE10-015",
                false,
                15,
                Instant.parse("2025-12-28T10:00:00Z")
        );
        when(adminService.getActiveCoupon()).thenReturn(coupon);

        // When & Then
        mockMvc.perform(get("/api/coupons/active"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("SAVE10-015"))
                .andExpect(jsonPath("$.used").value(false))
                .andExpect(jsonPath("$.generatedAtOrderNumber").value(15));
    }

    @Test
    @DisplayName("Should return 404 when no active coupon")
    void getActiveCoupon_None_Returns404() throws Exception {
        // Given
        when(adminService.getActiveCoupon()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/coupons/active"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return list of all coupons")
    void getAllCoupons_ReturnsList() throws Exception {
        // Given
        List<String> coupons = Arrays.asList("SAVE10-005", "SAVE10-010", "SAVE10-015");
        CouponListResponse response = new CouponListResponse(coupons, 3);
        when(adminService.getAllCoupons()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/coupons"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.coupons").isArray())
                .andExpect(jsonPath("$.coupons[0]").value("SAVE10-005"))
                .andExpect(jsonPath("$.coupons[1]").value("SAVE10-010"))
                .andExpect(jsonPath("$.coupons[2]").value("SAVE10-015"))
                .andExpect(jsonPath("$.totalGenerated").value(3));
    }

    @Test
    @DisplayName("Should return empty list when no coupons generated")
    void getAllCoupons_Empty_ReturnsEmptyList() throws Exception {
        // Given
        List<String> coupons = Arrays.asList();
        CouponListResponse response = new CouponListResponse(coupons, 0);
        when(adminService.getAllCoupons()).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/coupons"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.coupons").isArray())
                .andExpect(jsonPath("$.coupons").isEmpty())
                .andExpect(jsonPath("$.totalGenerated").value(0));
    }
}