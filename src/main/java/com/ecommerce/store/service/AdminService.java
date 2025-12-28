package com.ecommerce.store.service;

import com.ecommerce.store.dto.AdminStatsResponse;
import com.ecommerce.store.dto.CouponListResponse;
import com.ecommerce.store.dto.CouponResponse;

/**
 * Service interface for Admin operations.
 */
public interface AdminService {
    
    /**
     * Get store statistics.
     * 
     * Includes:
     * - Total items purchased
     * - Total purchase amount
     * - Total discount amount
     * - Total orders
     * - Orders with coupons
     * - Total coupons generated
     * - Active coupon code
     * 
     * @return Store statistics
     */
    AdminStatsResponse getStatistics();
    
    /**
     * List all generated coupon codes.
     * 
     * @return List of all coupons and count
     */
    CouponListResponse getAllCoupons();
    
    /**
     * Get currently active coupon.
     * 
     * @return Active coupon or null if none exists
     */
    CouponResponse getActiveCoupon();
    
    /**
     * Manually generate a coupon.
     * For testing/admin override.
     * 
     * @return Newly generated coupon
     */
    CouponResponse generateCoupon();
}
