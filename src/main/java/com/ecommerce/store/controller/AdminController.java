package com.ecommerce.store.controller;

import com.ecommerce.store.dto.AdminStatsResponse;
import com.ecommerce.store.dto.CouponListResponse;
import com.ecommerce.store.dto.CouponResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for Admin operations.
 * 
 * API Contract (Admin Endpoints):
 * - POST /api/admin/coupons/generate - Manually generate coupon (if needed)
 * - GET  /api/admin/coupons          - List all generated coupons
 * - GET  /api/admin/coupons/active   - Get currently active coupon
 * - GET  /api/admin/stats            - Get store statistics
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    /**
     * Get store statistics.
     * 
     * GET /api/admin/stats
     * 
     * Response:
     * {
     *   "totalItemsPurchased": 150,
     *   "totalPurchaseAmount": 12500.50,
     *   "totalDiscountAmount": 1250.00,
     *   "totalOrders": 45,
     *   "ordersWithCoupons": 8,
     *   "totalCouponsGenerated": 9,
     *   "activeCoupon": "SAVE10-045"
     * }
     * 
     * Assignment requirement: "Lists count of items purchased, total purchase 
     * amount, list of discount codes and total discount amount."
     */
    @GetMapping("/stats")
    public ResponseEntity<AdminStatsResponse> getStatistics() {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
    
    /**
     * List all generated coupon codes.
     * 
     * GET /api/admin/coupons
     * 
     * Response:
     * {
     *   "coupons": ["SAVE10-005", "SAVE10-010", "SAVE10-015"],
     *   "totalGenerated": 3
     * }
     */
    @GetMapping("/coupons")
    public ResponseEntity<CouponListResponse> getAllCoupons() {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get currently active coupon.
     * 
     * GET /api/admin/coupons/active
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
     */
    @GetMapping("/coupons/active")
    public ResponseEntity<CouponResponse> getActiveCoupon() {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
    
    /**
     * Manually generate a coupon.
     * 
     * POST /api/admin/coupons/generate
     * 
     * Note: Normally coupons auto-generate on Nth order.
     * This is for testing/admin override if needed.
     * 
     * Response: CouponResponse (newly generated coupon)
     */
    @PostMapping("/coupons/generate")
    public ResponseEntity<CouponResponse> generateCoupon() {
        
        // TODO: Implement in service layer
        return ResponseEntity.ok().build();
    }
}
