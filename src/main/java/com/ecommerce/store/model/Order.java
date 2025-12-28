package com.ecommerce.store.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a completed order.
 * 
 * Design Note: Orders are IMMUTABLE after creation (as per assumptions).
 * Order captures a snapshot of cart items at checkout time.
 * This prevents data inconsistency if cart is modified or cleared later.
 * 
 * Cart Clearing Workflow:
 * 1. Checkout called → Order created (cart snapshot taken)
 * 2. Order saved with paymentStatus = PAID (default, no gateway in scope)
 * 3. Cart cleared AFTER order creation succeeds
 * 4. If order creation fails, cart remains intact for retry
 * 
 * Future Payment Integration:
 * - Create order with paymentStatus = PENDING
 * - Call payment gateway
 * - Update paymentStatus based on response (PAID/FAILED)
 * - Clear cart only on PAID status
 * - This design allows seamless payment layer addition
 * 
 * Orders track discount information for admin reporting requirements:
 * - Admin API needs "total discount amount" across all orders
 * - Must know which orders used coupons
 * 
 * No status field - orders don't have workflow (paid/shipped/etc.)
 * Assignment scope is just cart → checkout → order creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    /**
     * Unique order identifier.
     * Using UUID for production-ready, globally unique IDs.
     * Thread-safe and scalable for distributed systems.
     */
    private UUID orderId;
    
    /**
     * Identifier of the user who placed this order.
     * Note: Changed from "clientId" to "userId" for consistency.
     */
    private String userId;
    
    /**
     * Snapshot of cart items at checkout time.
     * Independent copy - not a reference to Cart object.
     * Cart gets cleared after checkout, but order preserves items.
     */
    private List<CartItem> items = new ArrayList<>();
    
    /**
     * Final order total after applying discount (if any).
     * This is what the user pays.
     */
    private BigDecimal totalAmount;
    
    /**
     * Amount discounted from the order.
     * Set to 0 if no coupon was applied.
     * Required for admin reporting: "total discount amount".
     */
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    /**
     * Coupon code that was applied to this order.
     * Null if no coupon was used.
     * Useful for tracking and debugging.
     */
    private String couponCode;
    
    /**
     * Timestamp when the order was created.
     * Using Instant to represent absolute point in time (UTC-based).
     */
    private Instant createdAt;
    
    /**
     * Payment status of this order.
     * 
     * Default: PAID (assignment has no payment gateway)
     * Future: Will be PENDING initially, updated after payment gateway response
     * 
     * Why we added this:
     * - Enables future payment integration without breaking changes
     * - Makes the cart clearing logic clearer (clear after PAID)
     * - Allows proper error handling (FAILED orders keep cart intact)
     */
    private PaymentStatus paymentStatus = PaymentStatus.PAID;
    
    /**
     * Checks if this order used a discount coupon.
     * 
     * @return true if a coupon was applied, false otherwise
     */
    public boolean hasCouponApplied() {
        return couponCode != null && !couponCode.isEmpty() 
            && discountAmount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Checks if payment for this order is complete.
     * 
     * @return true if paymentStatus is PAID
     */
    public boolean isPaymentComplete() {
        return paymentStatus == PaymentStatus.PAID;
    }
}
