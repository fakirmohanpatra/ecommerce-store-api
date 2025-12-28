# Design Notes - Quick Reference

## ⚠️ CRITICAL ASSUMPTIONS (Read First!)

### Cart & Checkout Workflow
- **Checkout creates Order** (order = snapshot of cart at that moment)
- **Cart cleared AFTER order creation succeeds** (not during)
- **Payment assumed successful** (no gateway integration in scope)
- Order has `paymentStatus` field (default: PAID) for future payment layer integration
- This design allows easy payment gateway addition later without breaking existing flow

### Coupon Lifecycle (Biggest Ambiguity!)
- **ONE system-wide active coupon** at a time (not per-user)
- Generated automatically on **global Nth order** (5th, 10th, 15th...)
- **Visible to ALL users**, first-come-first-served
- **Persists until used** or replaced by next Nth coupon
- Old coupons **expire** when new one generates

### Concurrency
- `AtomicInteger` for order counter (thread-safe)
- Synchronized coupon application (prevents double-use)
- Exactly ONE order triggers Nth-coupon generation (no race conditions)

### Out of Scope
- ❌ Payment gateway integration (but paymentStatus field added for future)
- ❌ Payment failure handling (easy to add with existing paymentStatus enum)
- ❌ Order cancellation
- ❌ Order status/workflow (shipped, delivered, etc.)
- ❌ Per-user coupon eligibility tracking

**💡 Future-Proof Design**: Order model includes `paymentStatus` field for seamless payment integration later.

---

## Model Layer Decisions

### What We Built
**5 Core Models**: Item, CartItem, Cart, Order, Coupon
**1 Enum**: PaymentStatus (PENDING, PAID, FAILED)

### What We Deleted & Why
- ❌ **Admin.java** - Assignment says "auth out of scope", admin APIs don't need Admin entity
- ❌ **Client.java** - Simple `userId` string sufficient per requirements
- ❌ **OrderCounter.java** - Use `AtomicInteger` directly in store (simpler)

### Key Design Choices

#### 1. No Database Annotations
**Why**: Assignment requires in-memory storage. Using `@Entity`, `@Table` suggests database - misleading and unnecessary.

#### 2. Snapshot Pattern (CartItem, Order)
**Why**: 
- CartItem stores price/name when added (not just reference)
- Order stores copy of items (cart gets cleared after checkout)
- Prevents data inconsistency if source changes

#### 3. userId Not clientId
**Why**: API endpoints use `/cart/{userId}`. Consistency matters. "User" clearer than "Client" for B2C.

#### 4. Order Tracks Discount Info
**Why**: Admin API requirement: "total discount amount across all orders". Must store `discountAmount` and `couponCode` per order.

#### 5. No Coupon Percentage Field
**Why**: Assignment says "10% flat". Configured in `application.yml`. YAGNI - don't store constants per record.

#### 7. PaymentStatus Enum (Future-Proofing)
**Why**: 
- Assignment has no payment gateway, but real systems do
- Adding `paymentStatus` field now prevents breaking changes later
- Default to PAID for current scope (auto-payment)
- Makes cart-clearing logic explicit: "clear after PAID"
- Easy extension point: Set PENDING → call gateway → update to PAID/FAILED

#### 8. UUID for Entity IDs
**Why**: Production-ready and scalable. Globally unique without coordination. Thread-safe and works in distributed systems. Shows professional best practices.

#### 9. BigDecimal for Currency
**Why**: Standard practice. Avoids floating-point precision errors with money.

## Configuration

```yaml
app:
  coupon:
    nth-order: 5          # Every 5th order generates coupon
    discount-percentage: 10  # 10% discount
```

## Model Relationships

```
User (userId)
  ├─ has 1 Cart → contains CartItems → references Items
  └─ has N Orders → snapshot of CartItems, optional Coupon reference

System
  ├─ 1 active Coupon (single-use)
  └─ AtomicInteger orderCounter
```

## Implementation Progress

### Phase 1: Foundation Layer ✅ COMPLETED
- [x] Models (Item, Cart, CartItem, Order, Coupon, PaymentStatus)
- [x] **Refactored Repository Layer** - Separated into focused repositories
  - **DataStore**: Centralized in-memory storage (thread-safe containers)
  - **ICartRepository → CartRepository**: Cart management
  - **IItemRepository → ItemRepository**: Item CRUD operations
  - **IOrderRepository → OrderRepository**: Order operations + statistics
  - **ICouponRepository → CouponRepository**: Coupon lifecycle + validation
- [x] Seed data (10 sample items auto-loaded on startup)

**Design Benefits**:
- ✅ Interface-based: Dependency injection on interfaces, not concrete classes
- ✅ Single Responsibility: Each repository handles ONE entity
- ✅ Lightweight: Clear, focused classes (~50-100 lines each)
- ✅ Testable: Easy to mock individual repositories
- ✅ Maintainable: Changes isolated to specific repository
- ✅ Thread-safe: ConcurrentHashMap + AtomicInteger + synchronized methods

### Phase 3: REST API Layer ✅ COMPLETED (phase 3 completed before 2)
- [x] **CartController**: Cart CRUD endpoints
- [x] **ItemController**: Product listing endpoints
- [x] **OrderController**: Checkout and order history endpoints
- [x] **AdminController**: Statistics and coupon admin endpoints
- [x] All controllers wired with service layer
- [x] Request/Response DTOs properly mapped

### Phase 2: Service Layer ✅ COMPLETED
- [x] DTOs (AddToCartRequest, CartResponse, ItemResponse, OrderResponse, etc.)
- [x] **Service Interfaces**:
  - **CartService**: Cart operations (add, remove, update, get, clear)
  - **ItemService**: Product catalog operations
  - **OrderService**: Checkout and order history
  - **AdminService**: Statistics and coupon management
- [x] **Service Implementations**:
  - **CartServiceImpl**: Business logic for cart management
  - **ItemServiceImpl**: Product catalog queries
  - **OrderServiceImpl**: Checkout with coupon validation and auto-generation
  - **AdminServiceImpl**: Statistics aggregation


### Phase 4: Architecture Patterns Applied ✅
- [x] **Repository Pattern**: Interface-based repositories with implementations
- [x] **Service Layer Pattern**: Business logic separated from controllers
- [x] **DTO Pattern**: Clean separation between domain models and API contracts
- [x] **Dependency Injection**: Constructor injection using Lombok @RequiredArgsConstructor
- [x] **Clean Architecture**: Controllers → Services → Repositories → DataStore

## Repository Layer Architecture

**Separation of Concerns - Clean Architecture**:

```
DataStore (Component)
├─ Raw storage: ConcurrentHashMap + AtomicInteger
├─ Seed data initialization
└─ Shared by all repositories

ICartRepository (Interface) → CartRepository (Implementation)
├─ Cart CRUD
├─ Get-or-create pattern
└─ Clear cart after checkout

IItemRepository (Interface) → ItemRepository (Implementation)
├─ CRUD for Items
└─ Product catalog queries

IOrderRepository (Interface) → OrderRepository (Implementation)
├─ Order CRUD
├─ Order counter management
└─ Admin statistics

ICouponRepository (Interface) → CouponRepository (Implementation)
├─ Single active coupon management
├─ Generate/validate/apply
└─ Coupon history tracking
```

**Why Interface-Based Repositories**:
- Dependency Inversion Principle (DIP)
- Services depend on abstractions, not concrete implementations
- Enables easy mocking for unit tests
- Allows swapping implementations (e.g., move to JPA later)
- Single Responsibility Principle (SRP) - each class has ONE clear purpose
- Lightweight: 50-100 lines per implementation
- Easy to test, mock, and maintain

## Service Layer Architecture

**Clean Separation - Business Logic Isolated**:

```
CartService (Interface) → CartServiceImpl
├─ Add items to cart (with validation)
├─ Remove items from cart
├─ Update item quantities
├─ Get cart details
└─ Clear cart

ItemService (Interface) → ItemServiceImpl
├─ Get all items (product catalog)
└─ Get item by ID

OrderService (Interface) → OrderServiceImpl
├─ Checkout with coupon validation
├─ Calculate discounts
├─ Generate coupons on Nth order
└─ Get order history

AdminService (Interface) → AdminServiceImpl
├─ Calculate statistics
├─ Get coupon history
└─ Generate coupons manually
```

**Service Layer Responsibilities**:
- Business logic and validation
- Coordinate multiple repositories
- DTO transformations (Entity ↔ DTO)
- Transaction orchestration
- Error handling and validation

---

## In-Memory Store Structure

**Implemented in DataStore Component**:
---

## In-Memory Store Structure

**Implemented in DataStore Component**:
```java
ConcurrentHashMap<UUID, Item> items;            // Product catalog (itemId → Item)
ConcurrentHashMap<String, Cart> carts;          // User carts (userId → Cart)
ConcurrentHashMap<UUID, Order> orders;          // All orders (orderId → Order)
AtomicInteger orderCounter;                     // Global Nth order counter
volatile Coupon activeCoupon;                   // Single system-wide coupon
List<String> generatedCoupons;                  // For admin reporting
```
volatile Coupon activeCoupon;                   // Single system-wide coupon
List<String> allGeneratedCoupons;               // For admin reporting
```

**Thread-Safety Guarantees**:
- ConcurrentHashMap: Thread-safe reads/writes
- AtomicInteger: Thread-safe increment for order counting
- synchronized methods: Coupon generation/application
- volatile: Ensures activeCoupon visibility across threads

---

## Critical Design Decisions & Assumptions

### 1. Cart Clearing & Payment Status

**Decision**: Cart is cleared **AFTER** order is successfully created.

**Workflow**:
```
1. User clicks "Checkout" → checkout() API called
2. Validate cart (not empty, items exist, coupon if provided)
3. Create Order object (snapshot of cart items)
4. Save Order with paymentStatus = PAID (default for this assignment)
5. Clear user's cart
6. Return order confirmation
```

**Rationale**:
- **Separation of concerns**: Checkout = order creation, payment = separate step
- **Cart preservation**: If order creation fails (validation, system error), cart remains intact
- **Future-proof**: Easy to add payment gateway later without refactoring
- **Realistic**: Mirrors real e-commerce flow (order → payment → confirmation)

**Payment Status Field**:
- `Order.paymentStatus` enum: PENDING, PAID, FAILED
- **Default for assignment**: PAID (no actual payment gateway)
- **Future integration**: Set to PENDING, call payment API, update based on response

**Real-world payment integration (future scope)**:
```
1. Checkout → Order created with paymentStatus = PENDING
2. Call payment gateway API
3. On success: paymentStatus = PAID, clear cart
4. On failure: paymentStatus = FAILED, cart remains, user can retry
5. On timeout: paymentStatus = PENDING, user can check status later
```

**Why this is better than clearing immediately**:
- ✅ Cart survives order creation failures
- ✅ Natural extension point for payment layer
- ✅ More realistic e-commerce flow
- ✅ Better error handling (cart available for retry)
- ✅ No breaking changes when payment added

**Visual Workflow**:
```
Current Scope (No Payment Gateway):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
User clicks "Checkout"
         ↓
  Validate cart & coupon
         ↓
  Create Order (paymentStatus = PAID)
         ↓
  Order saved successfully
         ↓
  Clear cart ← Cart cleared AFTER order creation
         ↓
  Return order confirmation
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

Future Scope (With Payment Gateway):
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
User clicks "Checkout"
         ↓
  Validate cart & coupon
         ↓
  Create Order (paymentStatus = PENDING) ← Order exists but unpaid
         ↓
  Call Payment Gateway API
         ↓
    ┌────┴────┐
  SUCCESS   FAILURE
    ↓          ↓
Set PAID   Set FAILED
    ↓          ↓
Clear cart  Keep cart ← User can retry
    ↓          ↓
Return OK   Return error
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
```

---

### 2. Coupon Generation & Lifecycle (CRITICAL AMBIGUITY)

**The Assignment Says**:
> "Discount code can be requested by every user, but is made available for every nth order only. The discount code can be used only once before the next one becomes available on the next nth order."

**Our Interpretation & Decision**:

#### System-Level Coupon (Single Active Coupon Model)
- **ONE** coupon exists system-wide at any time
- **Generation**: Automatically created when system's Nth order completes (5th, 10th, 15th...)
- **Visibility**: Shown to ALL users once generated
- **Availability**: Any user can apply it to their current order
- **Expiration**: Used once → expires → next coupon generated at next Nth order

**Example Flow**:
```
Order #4 completes → No coupon
Order #5 completes → Coupon "SAVE10-005" generated, available to everyone
User A (on their 2nd order) applies "SAVE10-005" → Success! Coupon consumed.
Order #6-9 complete → No coupon available
Order #10 completes → Coupon "SAVE10-010" generated
User B ignores it → Coupon stays available
Order #15 completes → Old coupon expires, new "SAVE10-015" generated
```

**Why This Model**:
- Matches FAQ: "discount code can be requested by every user"
- Simpler concurrency: One global coupon, atomic consumption
- Fair: First-come, first-served basis
- Avoids complex per-user order tracking

---

### 3. Coupon NOT Applied on Nth Order - What Happens?

**Decision**: Coupon persists until used or replaced by next Nth-order coupon.

**Rationale**:
- 5th order generates coupon → stays available for orders 5, 6, 7, 8, 9
- If unused by order 9, it's still valid
- When 10th order completes → NEW coupon replaces old one (old expires)
- User doesn't lose opportunity if they skip applying on the generating order

**Edge Case - Concurrent Nth Orders**:
- Order counter uses `AtomicInteger.incrementAndGet()`
- Exactly ONE order will be the Nth order (thread-safe)
- That order's completion triggers coupon generation
- No duplicate coupons from race conditions

---

### 4. Coupon Validation & UX

**Problem**: Bad UX if we show all coupons to all users and validate at application time.

**Decision**: Show active coupon to ALL users; validate at checkout.

**Validation Rules**:
1. ✅ Coupon exists and matches active coupon code
2. ✅ Coupon has not been used (`coupon.isUsed() == false`)
3. ❌ Reject if already used by another order

**User Experience**:
- **Display**: "Active Discount: SAVE10-005 (10% off)" - shown to everyone
- **Application**: User enters code at checkout → validated
- **Error Messages**:
  - "Coupon code is invalid" (doesn't match active coupon)
  - "Coupon has already been used" (another user claimed it first)
  - Clear, honest feedback

**Why NOT per-user Nth-order validation**:
- Would require: "Show coupon only to user whose Nth order it is"
- Conflicts with FAQ: "can be requested by every user"
- Complex: Track order count per user, handle edge cases
- Assignment implies simpler system-wide model

---

### 5. Order Counter: Global vs Per-User

**Decision**: **Global order counter** (system-wide, not per-user).

**Rationale**:
- Assignment says "every nth order" (not "every user's nth order")
- FAQ says "available for every nth order only" (singular, system-level)
- Simpler implementation: `AtomicInteger orderCounter`
- 5th order in the SYSTEM triggers coupon, not 5th order PER USER

**Example**:
```
User A: Orders 1, 2, 3
User B: Orders 4, 5 ← This is system's 5th order → Coupon generated
User A: Order 6, 7
User B: Order 8, 9, 10 ← System's 10th order → New coupon
```

---

### 6. Concurrent Order Placement

**Scenario**: Two users click "Checkout" simultaneously near the Nth order.

**Handling**:
```java
// Thread-safe counter
int orderNumber = orderCounter.incrementAndGet();
Order order = createOrder(cart, orderNumber);

// Atomic coupon generation check
if (orderNumber % nthOrder == 0) {
    generateNewCoupon(); // Synchronized method
}
```

**Guarantees**:
- Exactly ONE order will be the Nth order (atomic increment)
- Only that order triggers coupon generation
- No race conditions, no duplicate coupons

**Edge Case - Coupon Application During Concurrent Checkouts**:
```java
synchronized(activeCoupon) {
    if (activeCoupon != null && !activeCoupon.isUsed()) {
        activeCoupon.setUsed(true);
        applyDiscount(order, activeCoupon);
    } else {
        throw new CouponAlreadyUsedException();
    }
}
```

---

### 7. What Happens to Unused Coupons?

**Decision**: Old coupons **expire** when new Nth-order coupon is generated.

**Example**:
- Order 5 → Coupon A generated
- Orders 6-9 → Coupon A still valid (not used)
- Order 10 → Coupon B generated, **Coupon A expires**
- User tries to apply Coupon A → "Invalid coupon code"

**Why**: Prevents accumulation of old coupons, keeps system simple.

---

### 8. Coupon Application Scenarios

| Scenario | Outcome | Coupon Status After |
|----------|---------|---------------------|
| Applied + Order successful | Discount given | `used=true`, expires |
| Applied but checkout fails (validation error) | No order created | Coupon remains available |
| Valid coupon, user doesn't apply | No discount | Coupon remains available |
| Applied + Payment fails (out of scope) | N/A - no payment in assignment | N/A |
| Applied + Order cancelled (out of scope) | N/A - no cancellation in assignment | N/A |

**Note**: Order cancellation and payment failure are **OUT OF SCOPE** per assignment requirements.

---

## Interview Talking Points

1. **Simplicity**: Deleted 3 unnecessary entities, kept 5 essential ones
2. **Alignment**: Every decision maps to assignment requirements
3. **Patterns**: Snapshot pattern prevents data inconsistency
4. **Standards**: BigDecimal for money, proper naming conventions
5. **Documentation**: Every model has JavaDoc explaining design rationale
6. **Ambiguity Resolution**: Documented all unclear assignment aspects with justified decisions
7. **Concurrency**: Proper thread-safety with `AtomicInteger` and `synchronized` blocks
8. **UX Consideration**: Balanced simplicity with user-friendly coupon validation
