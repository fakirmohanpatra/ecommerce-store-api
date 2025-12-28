package com.ecommerce.store.model;

/**
 * Represents the payment status of an order.
 * 
 * Design Decision: Added for future payment gateway integration.
 * Current assignment scope has no payment processing, so all orders default to PAID.
 * 
 * When payment gateway is integrated:
 * - PENDING: Order created, awaiting payment confirmation
 * - PAID: Payment successfully processed
 * - FAILED: Payment attempt failed, order may be retried
 */
public enum PaymentStatus {
    /**
     * Payment is awaiting confirmation.
     * Used when payment gateway integration is added.
     */
    PENDING,
    
    /**
     * Payment successfully completed.
     * Default status for current assignment (no gateway).
     */
    PAID,
    
    /**
     * Payment failed or was declined.
     * User may retry checkout with same cart.
     */
    FAILED
}
