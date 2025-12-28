package com.ecommerce.store.model;

/**
 * Result of coupon validation operation.
 */
public enum CouponValidationResult {
    VALID,
    NO_ACTIVE_COUPON,
    INVALID_CODE,
    ALREADY_USED
}