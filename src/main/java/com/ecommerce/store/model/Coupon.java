package com.ecommerce.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a discount coupon.
 * 
 * ═══════════════════════════════════════════════════════════════
 * CRITICAL DESIGN DECISIONS (Resolving Assignment Ambiguity)
 * ═══════════════════════════════════════════════════════════════
 * 
 * 1. SYSTEM-WIDE SINGLE COUPON MODEL
 *    - ONE active coupon exists across entire system at any time
 *    - NOT per-user coupons (interpretation of FAQ answer)
 *    - Available to ALL users on first-come-first-served basis
 * 
 * 2. COUPON GENERATION
 *    - Triggered by GLOBAL order counter (5th, 10th, 15th order in SYSTEM)
 *    - NOT per-user order count (5th order of ANY user, not each user's 5th)
 *    - Generated automatically after Nth order completes
 * 
 * 3. COUPON LIFECYCLE & PERSISTENCE
 *    - Generated on Nth order → Available immediately to all users
 *    - Persists until: (a) Used by someone, OR (b) Next Nth order generates new coupon
 *    - Example: Coupon from order #5 stays valid through orders 6-9
 *    - When order #10 completes → Old coupon EXPIRES, new one generated
 *    - If user doesn't apply on 5th order → They can still use it on 6th-9th orders
 * 
 * 4. COUPON EXPIRATION SCENARIOS
 *    Scenario A: User applies coupon, order succeeds → Coupon marked used, expires
 *    Scenario B: User doesn't apply coupon → Stays available until next Nth order
 *    Scenario C: New Nth order happens → Old unused coupon expires, replaced by new one
 *    
 *    OUT OF SCOPE (per assignment):
 *    - Payment failure (no payment gateway integration)
 *    - Order cancellation (orders are immutable once created)
 * 
 * 5. CONCURRENCY HANDLING
 *    - Coupon application synchronized to prevent double-use
 *    - If two users try to apply same coupon simultaneously:
 *      → First checkout succeeds, second gets "Coupon already used" error
 *    - Order counter uses AtomicInteger for thread-safe Nth-order detection
 * 
 * 6. USER EXPERIENCE IMPLICATIONS
 *    - UI shows active coupon to ALL users (transparent availability)
 *    - No confusing "this coupon is only for your Nth order" logic
 *    - Clear validation: "Coupon used" or "Invalid code"
 *    - Fair: Anyone can grab the active coupon if fast enough
 * 
 * 7. DISCOUNT PERCENTAGE
 *    - Assignment says "10% flat discount"
 *    - Constant configured in application.yml (not stored per coupon)
 *    - YAGNI: If variable discounts needed later, add field then
 * 
 * RATIONALE FOR THESE DECISIONS:
 * - FAQ states: "discount code can be requested by every user" → System-wide model
 * - FAQ states: "available for every nth order only" (singular) → Global counter
 * - Assignment doesn't mention per-user tracking → Simpler implementation
 * - Thread-safety naturally handled with single coupon + synchronization
 * 
 * See DESIGN_NOTES.md for full decision rationale and edge cases.
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
    private Instant createdAt;
    
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
