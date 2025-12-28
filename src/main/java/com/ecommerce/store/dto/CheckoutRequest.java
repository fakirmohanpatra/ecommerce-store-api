package com.ecommerce.store.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for checkout.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    /**
     * Optional coupon code.
     * If provided, will be validated and applied to order.
     */
    private String couponCode;
}
