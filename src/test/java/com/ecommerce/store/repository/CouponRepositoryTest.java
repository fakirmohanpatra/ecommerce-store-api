package com.ecommerce.store.repository;

import com.ecommerce.store.model.Coupon;
import com.ecommerce.store.model.CouponValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CouponRepository Tests")
class CouponRepositoryTest {

    private CouponRepository couponRepository;
    private DataStore dataStore;

    @BeforeEach
    void setUp() {
        dataStore = new DataStore();
        couponRepository = new CouponRepository(dataStore);
    }

    @Test
    @DisplayName("Should return empty when no active coupon exists")
    void getActive_NoCoupon_ReturnsEmpty() {
        // When
        Optional<Coupon> result = couponRepository.getActive();

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should return active coupon when one exists")
    void getActive_CouponExists_ReturnsCoupon() {
        // Given
        Coupon coupon = createTestCoupon("TEST001", 5);
        dataStore.activeCoupon = coupon;

        // When
        Optional<Coupon> result = couponRepository.getActive();

        // Then
        assertTrue(result.isPresent());
        assertEquals("TEST001", result.get().getCode());
        assertEquals(5, result.get().getGeneratedAtOrderNumber());
    }

    @Test
    @DisplayName("Should generate new coupon and replace existing one")
    void generate_NewCoupon_ReplacesExisting() {
        // Given
        Coupon existingCoupon = createTestCoupon("OLD001", 5);
        dataStore.activeCoupon = existingCoupon;

        // When
        Coupon newCoupon = couponRepository.generate(10);

        // Then
        assertNotNull(newCoupon);
        assertEquals("SAVE10-010", newCoupon.getCode());
        assertEquals(10, newCoupon.getGeneratedAtOrderNumber());
        assertFalse(newCoupon.isUsed());
        assertNotNull(newCoupon.getCreatedAt());

        // Verify old coupon is replaced
        assertEquals(newCoupon, dataStore.activeCoupon);
        assertTrue(dataStore.generatedCoupons.contains("SAVE10-010"));
    }

    @Test
    @DisplayName("Should validate and use coupon successfully")
    void validateAndUse_ValidCoupon_ReturnsValid() {
        // Given
        Coupon coupon = createTestCoupon("VALID001", 5);
        dataStore.activeCoupon = coupon;

        // When
        CouponValidationResult result = couponRepository.validateAndUse("VALID001");

        // Then
        assertEquals(CouponValidationResult.VALID, result);
        assertTrue(dataStore.activeCoupon.isUsed());
    }

    @Test
    @DisplayName("Should return NO_ACTIVE_COUPON when no coupon exists")
    void validateAndUse_NoActiveCoupon_ReturnsNoActiveCoupon() {
        // When
        CouponValidationResult result = couponRepository.validateAndUse("ANYCODE");

        // Then
        assertEquals(CouponValidationResult.NO_ACTIVE_COUPON, result);
    }

    @Test
    @DisplayName("Should return INVALID_CODE when coupon code doesn't match")
    void validateAndUse_WrongCode_ReturnsInvalidCode() {
        // Given
        Coupon coupon = createTestCoupon("VALID001", 5);
        dataStore.activeCoupon = coupon;

        // When
        CouponValidationResult result = couponRepository.validateAndUse("WRONG001");

        // Then
        assertEquals(CouponValidationResult.INVALID_CODE, result);
        assertFalse(dataStore.activeCoupon.isUsed());
    }

    @Test
    @DisplayName("Should return ALREADY_USED when coupon is already used")
    void validateAndUse_AlreadyUsed_ReturnsAlreadyUsed() {
        // Given
        Coupon coupon = createTestCoupon("USED001", 5);
        coupon.setUsed(true);
        dataStore.activeCoupon = coupon;

        // When
        CouponValidationResult result = couponRepository.validateAndUse("USED001");

        // Then
        assertEquals(CouponValidationResult.ALREADY_USED, result);
    }

    @Test
    @DisplayName("Should return true for valid unused coupon")
    void isValid_ValidCoupon_ReturnsTrue() {
        // Given
        Coupon coupon = createTestCoupon("VALID001", 5);
        dataStore.activeCoupon = coupon;

        // When
        boolean result = couponRepository.isValid("VALID001");

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when no active coupon")
    void isValid_NoActiveCoupon_ReturnsFalse() {
        // When
        boolean result = couponRepository.isValid("ANYCODE");

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false for wrong code")
    void isValid_WrongCode_ReturnsFalse() {
        // Given
        Coupon coupon = createTestCoupon("VALID001", 5);
        dataStore.activeCoupon = coupon;

        // When
        boolean result = couponRepository.isValid("WRONG001");

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false for used coupon")
    void isValid_UsedCoupon_ReturnsFalse() {
        // Given
        Coupon coupon = createTestCoupon("USED001", 5);
        coupon.setUsed(true);
        dataStore.activeCoupon = coupon;

        // When
        boolean result = couponRepository.isValid("USED001");

        // Then
        assertFalse(result);
    }

    @Test
    @DisplayName("Should get all generated coupon codes")
    void getAllGenerated_ReturnsCouponCodes() {
        // Given
        dataStore.generatedCoupons.add("CODE001");
        dataStore.generatedCoupons.add("CODE002");
        dataStore.generatedCoupons.add("CODE003");

        // When
        var result = couponRepository.getAllGenerated();

        // Then
        assertEquals(3, result.size());
        assertTrue(result.contains("CODE001"));
        assertTrue(result.contains("CODE002"));
        assertTrue(result.contains("CODE003"));
    }

    @Test
    @DisplayName("Should handle injection attacks and special characters in coupon codes")
    void validateAndUse_SpecialCharactersInCode_HandlesSafely() {
        // Given - Test with potentially dangerous input
        Coupon coupon = createTestCoupon("SAFE-CODE-123", 5);
        dataStore.activeCoupon = coupon;

        // Test various special characters that could be used in injection attacks
        String[] maliciousCodes = {"SAFE-CODE-123<script>", "SAFE-CODE-123' OR '1'='1", "SAFE-CODE-123; DROP TABLE;"};

        for (String maliciousCode : maliciousCodes) {
            // When
            CouponValidationResult result = couponRepository.validateAndUse(maliciousCode);

            // Then - Should return INVALID_CODE, not crash or allow injection
            assertEquals(CouponValidationResult.INVALID_CODE, result,
                "Should safely handle malicious input: " + maliciousCode);
        }

        // Original coupon should remain unused
        assertFalse(dataStore.activeCoupon.isUsed());
    }

    @Test
    @DisplayName("Should handle whitespace in coupon codes")
    void validateAndUse_WhitespaceInCode_HandlesProperly() {
        // Given - Test with whitespace handling
        Coupon coupon = createTestCoupon("VALID-CODE", 5);
        dataStore.activeCoupon = coupon;

        // Test various whitespace scenarios
        String[] whitespaceCodes = {" VALID-CODE ", "  VALID-CODE", "VALID-CODE  ", "\tVALID-CODE\n"};

        for (String whitespaceCode : whitespaceCodes) {
            // When
            CouponValidationResult result = couponRepository.validateAndUse(whitespaceCode);

            // Then - Should return INVALID_CODE for codes with extra whitespace
            // (assuming the system doesn't trim input - if it does, this test would need adjustment)
            assertEquals(CouponValidationResult.INVALID_CODE, result,
                "Should handle whitespace input properly: '" + whitespaceCode + "'");
        }

        // Original coupon should remain unused
        assertFalse(dataStore.activeCoupon.isUsed());
    }

    private Coupon createTestCoupon(String code, int orderNumber) {
        Coupon coupon = new Coupon();
        coupon.setCode(code);
        coupon.setUsed(false);
        coupon.setGeneratedAtOrderNumber(orderNumber);
        coupon.setCreatedAt(Instant.now());
        return coupon;
    }
}