package com.ecommerce.store.service;

import com.ecommerce.store.dto.CartItemResponse;
import com.ecommerce.store.dto.OrderResponse;
import com.ecommerce.store.exception.CouponValidationException;
import com.ecommerce.store.model.*;
import com.ecommerce.store.repository.ICartRepository;
import com.ecommerce.store.repository.ICouponRepository;
import com.ecommerce.store.repository.IItemRepository;
import com.ecommerce.store.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Service implementation for Order/Checkout operations.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    
    private final ICartRepository cartRepository;
    private final IOrderRepository orderRepository;
    private final ICouponRepository couponRepository;
    private final IItemRepository itemRepository;
    
    @Value("${app.coupon.nth-order:5}")
    private int nthOrder;
    
    @Value("${app.coupon.discount-percentage:10}")
    private int discountPercentage;
    
    @Override
    public OrderResponse checkout(String userId, String couponCode) {
        // 1. Validate cart exists and not empty
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));
        
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot checkout with empty cart");
        }
        
        // 2. Validate all items still exist and have sufficient stock
        for (CartItem cartItem : cart.getItems()) {
            if (!itemRepository.exists(cartItem.getItemId())) {
                throw new IllegalArgumentException("Item no longer available: " + cartItem.getItemId());
            }
            
            Item item = itemRepository.findById(cartItem.getItemId()).orElseThrow();
            if (item.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for item: " + cartItem.getItemId() + 
                    ". Requested: " + cartItem.getQuantity() + ", Available: " + item.getStock());
            }
        }
        
        // 3. Calculate amounts
        BigDecimal subtotal = cart.getTotal();
        BigDecimal discountAmount = BigDecimal.ZERO;
        String appliedCouponCode = null;
        
        // 4. Apply coupon if provided
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            CouponValidationResult validationResult = couponRepository.validateAndUse(couponCode);
            if (validationResult != CouponValidationResult.VALID) {
                String errorMessage = switch (validationResult) {
                    case NO_ACTIVE_COUPON -> "No active coupon available.";
                    case INVALID_CODE -> "Invalid coupon code: " + couponCode + ". Please check the active coupon code.";
                    case ALREADY_USED -> "Coupon code already used: " + couponCode;
                    default -> "Invalid coupon code: " + couponCode;
                };
                throw new CouponValidationException(errorMessage);
            }
            
            // Calculate discount
            discountAmount = subtotal
                    .multiply(BigDecimal.valueOf(discountPercentage))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            appliedCouponCode = couponCode;
        }
        
        BigDecimal totalAmount = subtotal.subtract(discountAmount);
        
        // 5. Create order (snapshot of cart)
        Order order = new Order();
        order.setUserId(userId);
        order.setItems(copyCartItems(cart.getItems()));
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setCouponCode(appliedCouponCode);
        order.setPaymentStatus(PaymentStatus.PAID);
        
        // 6. Save order and get order number
        int orderNumber = orderRepository.save(order);
        
        // 7. Decrease stock for all ordered items
        for (CartItem cartItem : cart.getItems()) {
            itemRepository.decreaseStock(cartItem.getItemId(), cartItem.getQuantity());
        }
        
        // 8. Check if Nth order → generate new coupon
        if (orderNumber % nthOrder == 0) {
            couponRepository.generate(orderNumber);
        }
        
        // 8. Clear cart
        cartRepository.delete(userId);
        
        // 9. Return order response
        return toOrderResponse(order);
    }
    
    @Override
    public List<OrderResponse> getOrderHistory(String userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::toOrderResponse)
                .toList();
    }
    
    /**
     * Copy cart items to order items (deep copy for snapshot).
     */
    private List<CartItem> copyCartItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(ci -> new CartItem(
                        ci.getItemId(),
                        ci.getItemName(),
                        ci.getPrice(),
                        ci.getQuantity()
                ))
                .toList();
    }
    
    /**
     * Convert Order entity to OrderResponse DTO.
     */
    private OrderResponse toOrderResponse(Order order) {
        List<CartItemResponse> items = order.getItems().stream()
                .map(this::toCartItemResponse)
                .toList();
        
        return new OrderResponse(
                order.getOrderId(),
                order.getUserId(),
                items,
                order.getTotalAmount(),
                order.getDiscountAmount(),
                order.getCouponCode(),
                order.getPaymentStatus().name(),
                order.getCreatedAt()
        );
    }
    
    /**
     * Convert CartItem to CartItemResponse DTO.
     */
    private CartItemResponse toCartItemResponse(CartItem item) {
        // Fetch current stock from item repository (stock at time of order, not current)
        // For order history, we show the stock level that was available when the order was placed
        int stockAtOrderTime = itemRepository.findById(item.getItemId())
                .map(Item::getStock)
                .orElse(0); // Default to 0 if item not found
        
        return new CartItemResponse(
                item.getItemId(),
                item.getItemName(),
                item.getPrice(),
                item.getQuantity(),
                item.getSubtotal(),
                stockAtOrderTime
        );
    }
}
