package com.ecommerce.store.controller;

import com.ecommerce.store.dto.CouponResponse;
import com.ecommerce.store.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for public Coupon operations.
 *
 * API Contract:
 * - GET /api/coupons/active - Get currently active coupon (for users to see available discount)
 */
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final AdminService adminService;

    /**
     * Get currently active coupon.
     *
     * GET /api/coupons/active
     *
     * Response:
     * {
     *   "code": "SAVE10-015",
     *   "isUsed": false,
     *   "generatedAtOrderNumber": 15,
     *   "createdAt": "2025-12-28T10:00:00"
     * }
     *
     * Or 404 if no active coupon.
     *
     * This endpoint allows users to see the current active coupon
     * that can be applied during checkout.
     */
    @GetMapping("/active")
    public ResponseEntity<CouponResponse> getActiveCoupon() {

        CouponResponse coupon = adminService.getActiveCoupon();
        if (coupon == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(coupon);
    }
}