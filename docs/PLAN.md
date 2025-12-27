# Implementation Plan - Ecommerce Store API

## Core Assumptions (Documented in README)

### Business Assumptions
- Single store (not multi-merchant)
- Prices are fixed and known at item creation time
- Cart belongs to a user (identified via `userId` in request)
- One active discount code at a time
- Discount is 10% flat, applied to entire cart total
- Discount code:
  - Generated only on every Nth successful order
  - Usable once only
  - Becomes invalid immediately after use
- Orders are immutable after checkout
- No concurrency edge cases need to be handled perfectly (since in-memory store)

### Technical Assumptions
- No persistent DB → In-memory data structures
- APIs are stateless; cart stored in memory mapped by `userId`
- Authentication/authorization is out of scope
- Currency handling simplified (use BigDecimal)

---

## High-Level Design - 4 Domains

1. **Cart** - Shopping cart management
2. **Order** - Order processing and storage
3. **Discount / Coupon** - Coupon generation and validation
4. **Admin Reporting** - Analytics and reporting

---

## Implementation Steps

### Phase 1: Project Setup
- [ ] Bootstrap Spring Boot project (Java 17)
  - Spring Web
  - Spring Validation
  - Springdoc OpenAPI (Swagger)
  - Lombok (optional)
- [ ] Configure `application.yml`
  - Set `N = 5` (configurable)
  - Server port, app name, etc.
- [ ] Setup project structure (packages)
  ```
  com.ecommerce.store
    ├── controller
    ├── service
    ├── model
    ├── repository (in-memory)
    ├── dto
    ├── exception
    └── config
  ```
- [ ] Add `.gitignore` for Java/Maven/Gradle
- [ ] Initial commit: `feat: bootstrap spring boot project`

---

### Phase 2: Core Models & Data Structures

#### Models to Define
- [ ] `Item` model
  ```java
  - String itemId
  - String name
  - BigDecimal price
  ```

- [ ] `CartItem` model
  ```java
  - String itemId
  - String itemName
  - BigDecimal price
  - int quantity
  ```

- [ ] `Cart` model
  ```java
  - String userId
  - List<CartItem> items
  - BigDecimal total
  ```

- [ ] `Order` model
  ```java
  - String orderId
  - String userId
  - List<CartItem> items
  - BigDecimal totalAmount
  - BigDecimal discountAmount
  - boolean couponApplied
  - LocalDateTime createdAt
  ```

- [ ] `Coupon` model
  ```java
  - String code
  - boolean isUsed
  - int generatedAtOrderNumber
  - LocalDateTime createdAt
  ```

#### In-Memory Stores
- [ ] Create `InMemoryStore` class
  ```java
  - ConcurrentHashMap<String, Cart> cartStore
  - ConcurrentHashMap<String, Order> orderStore
  - AtomicInteger orderCounter
  - Coupon activeCoupon
  - List<String> allGeneratedCoupons
  ```

- [ ] Commit: `feat: define core models and in-memory data structures`

---

### Phase 3: Cart APIs

- [ ] **POST** `/api/cart/{userId}/items` - Add item to cart
  - Request body: `{ "itemId": "...", "name": "...", "price": 99.99, "quantity": 2 }`
  - Create cart if doesn't exist
  - Add/update item in cart
  - Recalculate total

- [ ] **DELETE** `/api/cart/{userId}/items/{itemId}` - Remove item from cart
  - Remove item
  - Recalculate total

- [ ] **GET** `/api/cart/{userId}` - Get cart details
  - Return cart with items and total

- [ ] Create `CartController`, `CartService`
- [ ] Add request/response DTOs
- [ ] Commit: `feat: implement cart add/remove APIs`

---

### Phase 4: Checkout Flow

- [ ] **POST** `/api/checkout` - Checkout and place order
  - Request body:
    ```json
    {
      "userId": "u123",
      "couponCode": "DISC10" (optional)
    }
    ```
  - Validate cart exists and not empty
  - Validate coupon if provided:
    - Exists
    - Not used
    - Is the active coupon
  - Calculate discount (10% if valid coupon)
  - Create order
  - Increment order counter
  - Mark coupon as used
  - Check if Nth order → generate new coupon
  - Clear cart
  - Return order details

- [ ] Create `CheckoutController`, `CheckoutService`
- [ ] Create `CouponService` for coupon logic
- [ ] Add exception handling (InvalidCouponException, etc.)
- [ ] Commit: `feat: implement checkout flow`

---

### Phase 5: Coupon Generation Logic

- [ ] Implement coupon generation in `CouponService`
  - Generate unique code (e.g., "DISC10-" + UUID)
  - Mark as unused
  - Set generated order number
  - Store as active coupon
  - Add to all generated coupons list

- [ ] Logic for Nth order trigger
  - `if (orderCounter.get() % N == 0)`
  - Generate new coupon

- [ ] Commit: `feat: add coupon generation logic for nth order`

---

### Phase 6: Admin APIs

- [ ] **POST** `/api/admin/coupon/generate` - Manual coupon generation
  - For testing purposes
  - Generate coupon manually
  - Return coupon details

- [ ] **GET** `/api/admin/report` - Analytics report
  - Calculate:
    - Total items purchased (sum of all order items)
    - Total purchase amount (sum of all order totals)
    - Total discount amount (sum of all discounts applied)
    - List of all generated discount codes
  - Return report JSON

- [ ] Create `AdminController`, `AdminService`
- [ ] Commit: `feat: implement admin reporting APIs`

---

### Phase 7: API Documentation

- [ ] Configure Swagger/OpenAPI
- [ ] Add `@Operation`, `@ApiResponse` annotations
- [ ] Test Swagger UI at `/swagger-ui.html`
- [ ] Commit: `docs: add swagger API documentation`

---

### Phase 8: Testing

- [ ] Unit tests for `CouponService`
  - Test coupon generation
  - Test coupon validation
  - Test single-use enforcement
  - Test Nth order trigger

- [ ] Unit tests for `CheckoutService`
  - Checkout without coupon
  - Checkout with valid coupon
  - Checkout with invalid coupon
  - Checkout with already-used coupon
  - Discount calculation

- [ ] Unit tests for `CartService`
  - Add item
  - Remove item
  - Cart total calculation

- [ ] Integration tests for controllers (optional)

- [ ] Commit: `test: add unit tests for core business logic`

---

### Phase 9: Documentation & Polish

- [ ] Update README.md with:
  - Problem statement
  - Assumptions
  - Tech stack
  - API endpoints list
  - How to run
  - Sample requests/responses
  - Known limitations

- [ ] Create Postman collection
  - Add all API requests
  - Add example requests
  - Export and commit

- [ ] Add code comments where needed
- [ ] Commit: `docs: update README and add postman collection`

---

### Phase 10: Final Review

- [ ] Code cleanup
- [ ] Verify all APIs work
- [ ] Test full flow end-to-end
- [ ] Check commit history is clean
- [ ] Push to GitHub
- [ ] Verify GitHub README displays correctly

---

## Git Commit Strategy

```
1. feat: bootstrap spring boot project
2. feat: define core models and in-memory data structures
3. feat: implement cart add/remove APIs
4. feat: implement checkout flow
5. feat: add coupon generation logic for nth order
6. feat: implement admin reporting APIs
7. docs: add swagger API documentation
8. test: add unit tests for core business logic
9. docs: update README and add postman collection
10. chore: final cleanup and polish
```

---

## API Endpoints Summary

### Cart APIs
- `POST /api/cart/{userId}/items` - Add item
- `DELETE /api/cart/{userId}/items/{itemId}` - Remove item
- `GET /api/cart/{userId}` - Get cart

### Checkout API
- `POST /api/checkout` - Checkout and place order

### Admin APIs
- `POST /api/admin/coupon/generate` - Generate coupon manually
- `GET /api/admin/report` - Get analytics report

---

## Tech Stack

- **Language**: Java 17
- **Framework**: Spring Boot 3.x
- **Build Tool**: Maven/Gradle
- **API Documentation**: Springdoc OpenAPI (Swagger)
- **Testing**: JUnit 5, Mockito
- **In-Memory Storage**: ConcurrentHashMap, AtomicInteger

---

## Configuration

### application.yml
```yaml
server:
  port: 8080

app:
  coupon:
    nth-order: 5  # Generate coupon on every 5th order
    discount-percentage: 10  # 10% discount

spring:
  application:
    name: ecommerce-store-api
```

---

## Progress Tracker

- [ ] Phase 1: Project Setup
- [ ] Phase 2: Core Models
- [ ] Phase 3: Cart APIs
- [ ] Phase 4: Checkout Flow
- [ ] Phase 5: Coupon Logic
- [ ] Phase 6: Admin APIs
- [ ] Phase 7: API Documentation
- [ ] Phase 8: Testing
- [ ] Phase 9: Documentation
- [ ] Phase 10: Final Review
