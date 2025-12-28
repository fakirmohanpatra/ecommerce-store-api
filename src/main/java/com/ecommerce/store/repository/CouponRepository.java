package com.ecommerce.store.repository;

import com.ecommerce.store.model.Coupon;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Coupon operations.
 * 
 * Responsibilities:
 * - Manage single active coupon (system-wide)
 * - Generate new coupons
 * - Validate and apply coupons
 * - Track coupon history for admin reporting
 * 
 * Thread-Safe: Synchronized methods prevent concurrent coupon application.
 */
@Repository
public class CouponRepository implements ICouponRepository {
    
    private final DataStore dataStore;
    
    public CouponRepository(DataStore dataStore) {
        this.dataStore = dataStore;
    }
    
    /**
     * Get the currently active coupon.
     */
    @Override
    public synchronized Optional<Coupon> getActive() {
        return Optional.ofNullable(dataStore.activeCoupon);
    }
    
    /**
     * Generate a new coupon.
     * Old coupon (if any) is replaced/expired.
     * 
     * @param orderNumber The Nth order that triggered this coupon
     * @return the newly generated coupon
     */
    @Override
    public synchronized Coupon generate(int orderNumber) {
        Coupon newCoupon = new Coupon();
        newCoupon.setCode(generateCode(orderNumber));
        newCoupon.setUsed(false);
        newCoupon.setGeneratedAtOrderNumber(orderNumber);
        newCoupon.setCreatedAt(Instant.now());
        
        // Replace old coupon (old one expires)
        dataStore.activeCoupon = newCoupon;
        
        // Track for admin reporting
        dataStore.generatedCoupons.add(newCoupon.getCode());
        
        return newCoupon;
    }
    
    /**
     * Validate and mark coupon as used.
     * 
     * @param couponCode The code to validate
     * @return true if valid and successfully marked used, false otherwise
     * 
     * Thread-safe: Prevents double-use by concurrent checkouts.
     */
    @Override
    public synchronized boolean validateAndUse(String couponCode) {
        if (dataStore.activeCoupon == null) {
            return false; // No active coupon
        }
        
        if (!dataStore.activeCoupon.getCode().equals(couponCode)) {
            return false; // Wrong code
        }
        
        if (dataStore.activeCoupon.isUsed()) {
            return false; // Already used
        }
        
        // Mark as used (consume the coupon)
        dataStore.activeCoupon.setUsed(true);
        return true;
    }
    
    /**
     * Check if a coupon code is valid (exists and not used).
     */
    @Override
    public synchronized boolean isValid(String couponCode) {
        return dataStore.activeCoupon != null
                && dataStore.activeCoupon.getCode().equals(couponCode)
                && !dataStore.activeCoupon.isUsed();
    }
    
    /**
     * Get all generated coupon codes (for admin reporting).
     */
    @Override
    public List<String> getAllGenerated() {
        return new ArrayList<>(dataStore.generatedCoupons);
    }
    
    /**
     * Get count of all generated coupons.
     */
    @Override
    public int getGeneratedCount() {
        return dataStore.generatedCoupons.size();
    }
    
    /**
     * Generate coupon code format: SAVE10-XXX
     * XXX is zero-padded order number.
     */
    private String generateCode(int orderNumber) {
        return String.format("SAVE10-%03d", orderNumber);
    }
}
