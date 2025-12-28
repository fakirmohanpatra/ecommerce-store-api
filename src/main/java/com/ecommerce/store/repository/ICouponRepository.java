package com.ecommerce.store.repository;

import com.ecommerce.store.model.Coupon;
import com.ecommerce.store.model.CouponValidationResult;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Coupon operations.
 */
public interface ICouponRepository {
    
    /**
     * Get the currently active coupon.
     */
    Optional<Coupon> getActive();
    
    /**
     * Generate a new coupon.
     * Old coupon (if any) is replaced/expired.
     * 
     * @param orderNumber The Nth order that triggered this coupon
     * @return the newly generated coupon
     */
    Coupon generate(int orderNumber);
    
    /**
     * Validate and mark coupon as used.
     * 
     * @param couponCode The code to validate
     * @return VALID if successfully validated and marked used, otherwise the failure reason
     */
    CouponValidationResult validateAndUse(String couponCode);
    
    /**
     * Check if a coupon code is valid (exists and not used).
     */
    boolean isValid(String couponCode);
    
    /**
     * Get all generated coupon codes (for admin reporting).
     */
    List<String> getAllGenerated();
    
    /**
     * Get count of all generated coupons.
     */
    int getGeneratedCount();
}
