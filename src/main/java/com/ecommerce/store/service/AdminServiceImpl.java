package com.ecommerce.store.service;

import com.ecommerce.store.dto.AdminStatsResponse;
import com.ecommerce.store.dto.CouponListResponse;
import com.ecommerce.store.dto.CouponResponse;
import com.ecommerce.store.model.Coupon;
import com.ecommerce.store.repository.ICouponRepository;
import com.ecommerce.store.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service implementation for Admin operations.
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    
    private final IOrderRepository orderRepository;
    private final ICouponRepository couponRepository;
    
    @Override
    public AdminStatsResponse getStatistics() {
        int totalItemsPurchased = orderRepository.getTotalItemsPurchased();
        BigDecimal totalPurchaseAmount = orderRepository.getTotalPurchaseAmount();
        BigDecimal totalDiscountAmount = orderRepository.getTotalDiscountAmount();
        int totalOrders = orderRepository.findAll().size();
        long ordersWithCoupons = orderRepository.countOrdersWithCoupons();
        int totalCouponsGenerated = couponRepository.getGeneratedCount();
        
        // Get active coupon code
        String activeCouponCode = couponRepository.getActive()
                .map(Coupon::getCode)
                .orElse(null);
        
        return new AdminStatsResponse(
                totalItemsPurchased,
                totalPurchaseAmount,
                totalDiscountAmount,
                totalOrders,
                ordersWithCoupons,
                totalCouponsGenerated,
                activeCouponCode
        );
    }
    
    @Override
    public CouponListResponse getAllCoupons() {
        List<String> coupons = couponRepository.getAllGenerated();
        return new CouponListResponse(coupons, coupons.size());
    }
    
    @Override
    public CouponResponse getActiveCoupon() {
        return couponRepository.getActive()
                .map(this::toCouponResponse)
                .orElse(null);
    }
    
    @Override
    public CouponResponse generateCoupon() {
        // Generate coupon with current order count
        int currentOrderCount = orderRepository.getOrderCount();
        Coupon coupon = couponRepository.generate(currentOrderCount);
        return toCouponResponse(coupon);
    }
    
    /**
     * Convert Coupon entity to CouponResponse DTO.
     */
    private CouponResponse toCouponResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getCode(),
                coupon.isUsed(),
                coupon.getGeneratedAtOrderNumber(),
                coupon.getCreatedAt()
        );
    }
}
