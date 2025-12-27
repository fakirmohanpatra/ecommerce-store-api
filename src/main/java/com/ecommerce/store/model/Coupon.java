package com.ecommerce.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Represents a discount coupon.
 * 
 * Design Note: Assignment specifies "one active discount code at a time".
 * System maintains a single active coupon that gets replaced when a new one is generated.
 * 
 * Coupon Generation Rules:
 * - Generated automatically on every Nth successful order (N=5 by default)
 * - Each coupon is single-use only
 * - Once used, it becomes invalid
 * - New coupon generated on next Nth order
 * 
 * Discount Percentage:
 * - Assignment says "10% flat discount"
 * - This is constant, configured in application.yml
 * - No need to store percentage per coupon (YAGNI principle)
 * - If requirements change to variable discounts, we can add it later
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coupon {
    
    /**
     * Unique coupon code.
     * Format example: "DISC10-abc123" (prefix + random suffix).
     * Users provide this code during checkout.
     */
    private String code;
    
    /**
     * Flag indicating if this coupon has been used.
     * Single-use enforcement: once true, cannot be used again.
     * System tracks the currently active coupon; once used, it's marked true.
     */
    private boolean isUsed;
    
    /**
     * The order number that triggered this coupon's generation.
     * Example: If N=5, and this coupon was generated on the 10th order,
     * generatedAtOrderNumber = 10.
     * Useful for audit trail and debugging.
     */
    private int generatedAtOrderNumber;
    
    /**
     * Timestamp when this coupon was created.
     * Useful for displaying "valid until" or audit purposes.
     */
    private LocalDateTime createdAt;
    
    /**
     * Checks if this coupon is valid for use.
     * A coupon is valid if it hasn't been used yet.
     * 
     * @return true if coupon can be applied, false otherwise
     */
    public boolean isValid() {
        return !isUsed;
    }
    
    /**
     * Marks this coupon as used.
     * Should be called during checkout when coupon is successfully applied.
     */
    public void markAsUsed() {
        this.isUsed = true;
    }
}
