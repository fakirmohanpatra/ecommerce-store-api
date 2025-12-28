package com.ecommerce.store.exception;

/**
 * Exception thrown when coupon validation fails.
 */
public class CouponValidationException extends RuntimeException {

    public CouponValidationException(String message) {
        super(message);
    }

    public CouponValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}