# Design Notes - Quick Reference

## ⚠️ CRITICAL ASSUMPTIONS (Read First!)

### Cart & Checkout Workflow
- **Checkout creates Order** (order = snapshot of cart at that moment)
- **Cart cleared AFTER order creation succeeds** (not during)
- **Payment assumed successful** (no gateway integration in scope)
- Order has `paymentStatus` field (default: PAID) for future payment layer integration
- This design allows easy payment gateway addition later without breaking existing flow

### Inventory Management (Stock Control)
- **Stock Validation**: Items must have sufficient stock before being added to cart
- **Stock Decrease**: Stock is decreased only AFTER successful cart addition (not during validation)
- **Out-of-Stock Prevention**: Cannot add items with zero or insufficient stock to cart
- **Thread-Safe Operations**: Stock decrease is synchronized to prevent race conditions
- **No Stock Reservation**: Stock is not reserved during cart operations (immediate decrease on add)
- **Future-Proof**: Design allows easy addition of stock reservation/rollback for payment failures

### Coupon Lifecycle (Biggest Ambiguity!) - DETAILED

#### **Who Generates Coupons?**
- **System Auto-Generation**: Coupons are generated automatically by the system (OrderServiceImpl during checkout)
- **Admin Manual Generation**: Admin can also manually generate coupons via `POST /api/admin/coupons/generate`
- **NOT User-Generated**: Users cannot create their own coupons

#### **When Are Coupons Created?**
- **Auto-Generation Trigger**: Automatically created when the **Nth order** completes checkout (default: every 5th order)
  - Example: Order #5, #10, #15, #20... each triggers a new coupon
  - Uses **global system order counter** (not per-user)
- **Manual Trigger**: Admin can force-generate at any time (replaces existing active coupon)
- **Timing**: Created AFTER order is successfully saved (not during validation)

#### **How Long Are Coupons Active?**
- **No Fixed Expiration Time**: Coupons don't have a time-based expiry (no "valid for 24 hours")
- **Persists Until**:
  1. **Used by any user** during checkout → Marked as `used=true`, becomes inactive
  2. **Replaced by next Nth-order coupon** → Old coupon expires, new one becomes active
- **Example Timeline**:
  ```
  Order #5  → Coupon "SAVE10-005" generated (active)
  Order #6  → "SAVE10-005" still active (not used)
  Order #7  → User applies "SAVE10-005" → Marked used, expires
  Order #8-9 → No active coupon
  Order #10 → Coupon "SAVE10-010" generated (active)
  ```

#### **What Happens If Coupon Is NOT Used?**
- **Scenario 1: Next Nth Order Before Use**
  - Old coupon gets **replaced/expired** when new one generates
  - Example: "SAVE10-005" unused → Order #10 completes → "SAVE10-005" expires, "SAVE10-010" becomes active
- **Scenario 2: Remains Unused Between Nth Orders**
  - Coupon stays active and available to ALL users
  - Example: "SAVE10-010" generated → Orders 11-14 happen → Coupon still available
- **No Accumulation**: Only ONE coupon active at a time (old ones don't stack)

#### **What Happens When Coupon IS Used?**
- **Marked as Used**: `isUsed` flag set to `true` (in CouponRepository.validateAndUse())
- **Becomes Inactive**: Cannot be used by another user
- **Discount Applied**: 10% discount calculated on order subtotal
- **Stored in Order**: Order records `couponCode` and `discountAmount` for audit/reporting
- **Next Availability**: No active coupon until next Nth order generates a new one

#### **Auto-Generation Details**
- **Logic**: `if (orderNumber % nthOrder == 0)` in OrderServiceImpl.checkout()
- **Code Format**: `SAVE10-{orderNumber}` (e.g., "SAVE10-005", "SAVE10-010")
- **Thread-Safe**: Uses `AtomicInteger` for order counter + synchronized coupon methods
- **Race Condition Protection**: Exactly ONE order triggers generation (no duplicates)

#### **Key Assumption: ONE System-Wide Coupon**
- **NOT per-user coupons** (every user doesn't get their own 5th-order coupon)
- **Global visibility**: All users see the same active coupon
- **First-come-first-served**: Whoever checks out first with the code gets the discount
- **Why This Design?**
  - Simpler concurrency (single coupon to synchronize)
  - Matches assignment FAQ: "can be requested by every user"
  - Avoids complex per-user order tracking
  - Realistic for promotional campaigns (limited-use codes)

### Concurrency
- `AtomicInteger` for order counter (thread-safe)
- Synchronized coupon application (prevents double-use)
- Synchronized stock decrease operations (prevents overselling)
- Exactly ONE order triggers Nth-coupon generation (no race conditions)

### Out of Scope
- ❌ Payment gateway integration (but paymentStatus field added for future)
- ❌ Payment failure handling (easy to add with existing paymentStatus enum)
- ❌ Order cancellation
- ❌ Order status/workflow (shipped, delivered, etc.)
- ❌ Per-user coupon eligibility tracking
- ❌ Advanced inventory features (stock reservation, backorders, low-stock alerts)

**💡 Future-Proof Design**: 
- Order model includes `paymentStatus` field for seamless payment integration later
- Item model includes `stock` field for inventory management expansion

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

#### 4. Item Model with Inventory Management
**Why**:
- **Stock Field**: `int stock` tracks available inventory (default: 0)
- **isOutOfStock() Method**: Returns `true` if `stock <= 0`
- **Thread-Safe Stock Decrease**: `decreaseStock()` method in repository prevents race conditions
- **Validation Before Cart Addition**: CartService checks stock availability before allowing items to be added
- **Immediate Stock Decrease**: Stock reduced immediately on successful cart addition (no reservation system)

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
  - **CartServiceImpl**: Business logic for cart management + stock validation
  - **ItemServiceImpl**: Product catalog queries
  - **OrderServiceImpl**: Checkout with coupon validation and auto-generation
  - **AdminServiceImpl**: Statistics aggregation
- [x] **Inventory Management**: Stock validation and decrease on cart operations


### Phase 4: Architecture Patterns Applied ✅
- [x] **Repository Pattern**: Interface-based repositories with implementations
- [x] **Service Layer Pattern**: Business logic separated from controllers
- [x] **DTO Pattern**: Clean separation between domain models and API contracts
- [x] **Dependency Injection**: Constructor injection using Lombok @RequiredArgsConstructor
- [x] **Clean Architecture**: Controllers → Services → Repositories → DataStore
- [x] **Security Testing**: Injection attack protection validated in unit tests

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
├─ Product catalog queries
└─ Stock management (decreaseStock with thread-safety)

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
├─ Add items to cart (with validation + stock check)
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

#### Detailed Coupon States & Lifecycle

**State 1: NO_ACTIVE_COUPON (Initial State)**
- **When**: System starts, or after a coupon is used and before next Nth order
- **User Experience**: GET /api/coupons/active returns 404
- **What Happens**: Users can checkout without coupon (no discount)
- **Why This State Exists**: Natural gap between coupons being consumed and regenerated

**State 2: ACTIVE & UNUSED (Available)**
- **When**: Nth order completes, coupon generated (`isUsed=false`)
- **User Experience**: Coupon code visible to all users, can be applied at checkout
- **Duration**: Persists until used OR replaced by next Nth order
- **Example**: Order #5 generates "SAVE10-005" → Available through orders 6-9 (if unused)
- **Why This State Exists**: Gives window of opportunity for users to claim discount

**State 3: ACTIVE & USED (Consumed)**
- **When**: User successfully applies coupon during checkout (`isUsed=true`)
- **User Experience**: Returns ALREADY_USED error to subsequent users
- **What Happens**: Discount applied to that one order, coupon becomes inactive
- **Why This State Exists**: Enforces single-use constraint, prevents fraud

**State 4: EXPIRED (Replaced by New Coupon)**
- **When**: New Nth order generates new coupon (old one discarded)
- **User Experience**: Old code returns INVALID_CODE error
- **Example**: "SAVE10-005" expires when Order #10 generates "SAVE10-010"
- **Why This State Exists**: Prevents accumulation, keeps system simple

#### Coupon Validation Results - Are All 4 States Needed?

**Current Enum: CouponValidationResult**
```java
VALID,              // ✅ Coupon exists, not used, correct code
NO_ACTIVE_COUPON,   // ❌ No coupon in system
INVALID_CODE,       // ❌ Code doesn't match active coupon
ALREADY_USED        // ❌ Coupon exists but already consumed
```

**Analysis: YES, All 4 States Are Necessary**

| State | User Scenario | Error Message Needed | Can We Merge? |
|-------|---------------|---------------------|---------------|
| **VALID** | Happy path - discount applied | None (success) | No - core success case |
| **NO_ACTIVE_COUPON** | User tries coupon when none exists (e.g., order #3, no coupon yet) | "No active coupon available" | ❌ Can't merge - different from invalid code |
| **INVALID_CODE** | User types wrong code, or uses expired code (e.g., tries "SAVE10-005" when "SAVE10-010" is active) | "Invalid coupon code" | ❌ Can't merge - user typo vs no coupon are different |
| **ALREADY_USED** | User tries to use coupon that another user just consumed | "Coupon has already been used" | ❌ Can't merge - critical feedback for race conditions |

**Why We Can't Simplify to 2 States (VALID/INVALID):**
- **UX Clarity**: "No coupon exists" vs "Wrong code" vs "Someone else used it" are distinct user errors
- **Debugging**: Helps admins/developers diagnose issues (system state vs user input error)
- **Security**: Distinguishes between expired coupons and never-existed coupons (prevents guessing)
- **Concurrency Feedback**: ALREADY_USED specifically addresses race conditions (two users checking out simultaneously)

**Recommendation: Keep All 4 States** ✅

**Example Flow**:
```
Order #4 completes → NO_ACTIVE_COUPON (no discount yet)
Order #5 completes → Coupon "SAVE10-005" generated, available to everyone
User A (on their 2nd order) applies "SAVE10-005" → VALID → Discount applied → ALREADY_USED
User B tries "SAVE10-005" → ALREADY_USED error
Order #6-9 complete → NO_ACTIVE_COUPON (coupon was used)
Order #10 completes → Coupon "SAVE10-010" generated
User C tries "SAVE10-005" → INVALID_CODE (old/expired)
User C tries "SAVE10-010" → VALID → Discount applied
Order #15 completes → "SAVE10-010" expires, new "SAVE10-015" generated
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

### 8. Coupon Application Scenarios & Edge Cases

**Complete State Transition Table**:

| Scenario | Starting State | User Action | Validation Result | Order Created? | Coupon State After | Why This Decision |
|----------|----------------|-------------|-------------------|----------------|-------------------|-------------------|
| **Happy Path** | ACTIVE & UNUSED | Apply valid code | VALID | ✅ Yes (with discount) | USED | Core requirement - single-use discount |
| **No Coupon Generated Yet** | NO_ACTIVE_COUPON | Tries any code | NO_ACTIVE_COUPON | ✅ Yes (no discount) | NO_ACTIVE_COUPON | Allow checkout without coupon (graceful degradation) |
| **Wrong Code** | ACTIVE & UNUSED | Apply invalid code | INVALID_CODE | ❌ No (validation fails) | ACTIVE & UNUSED | Preserve coupon for correct user (typo protection) |
| **Race Condition** | ACTIVE & UNUSED | Two users apply simultaneously | VALID (1st user) / ALREADY_USED (2nd user) | ✅ Yes for 1st / ❌ No for 2nd | USED after 1st | Synchronized validation prevents double-use |
| **Expired Coupon** | EXPIRED (replaced) | Apply old code | INVALID_CODE | ❌ No | EXPIRED | Old codes don't work after new generation |
| **Checkout Without Coupon** | Any state | Empty/null coupon | N/A (not validated) | ✅ Yes (no discount) | Unchanged | Coupon is optional at checkout |
| **Unused Until Next Nth** | ACTIVE & UNUSED | No one applies | N/A | ✅ Orders continue | EXPIRED (replaced by new) | Prevents coupon accumulation |

**Edge Cases & Assumptions Explained**:

#### 1. **Checkout Fails After Coupon Validation**
- **Scenario**: Coupon validated (marked used), but order creation fails (e.g., database error)
- **Current Behavior**: Coupon stays marked as used (not rolled back)
- **Why**: Assignment has no database/transactions, rollback is out of scope
- **Future Improvement**: Wrap in transaction or use try-catch to revert coupon state

#### 2. **Nth Order User Doesn't Get Automatic Discount**
- **Scenario**: Order #5 completes → Coupon generated → But that order itself doesn't get discount
- **Why**: Coupon becomes available AFTER order #5 succeeds (for orders 6-9)
- **Alternative Considered**: Auto-apply to Nth order (rejected - violates "user applies" requirement)

#### 3. **Coupon Discount > Order Total**
- **Scenario**: Order subtotal $50, 10% discount = $5 off → Total $45 ✅
- **Edge Case**: What if rounding error makes total negative?
- **Handling**: `BigDecimal` math prevents this, but added check: `totalAmount = max(subtotal - discount, 0)`

#### 4. **Multiple Concurrent Nth Orders**
- **Scenario**: Two orders complete simultaneously, both are the 10th order
- **Handling**: `AtomicInteger.incrementAndGet()` ensures exactly ONE is #10
- **Result**: Only one order triggers coupon generation (no duplicates)

#### 5. **Payment Failure (OUT OF SCOPE)**
- **Assignment**: No payment gateway integration
- **Current**: Order created → paymentStatus = PAID → Cart cleared → Coupon consumed (if applied)
- **If Payment Was Added**: Set paymentStatus = PENDING → Call gateway → On failure: Keep cart, restore coupon? (NOT IMPLEMENTED)

#### 6. **Order Cancellation (OUT OF SCOPE)**
- **Assignment**: Orders are immutable once created
- **If Cancellation Was Added**: Should coupon be restored? New requirement discussion needed

#### 7. **Admin Manual Coupon Generation**
- **Scenario**: Admin generates coupon outside Nth-order cycle
- **Behavior**: Replaces any existing active coupon (old one expires)
- **Why**: Admin override capability (e.g., marketing campaign)

**Why These Decisions?**
- **Simplicity**: In-memory, no transactions → Keep logic straightforward
- **Scope Adherence**: Focus on assignment requirements, avoid over-engineering
- **Concurrency**: Thread-safe operations prevent race conditions
- **User Experience**: Clear error messages, graceful degradation
- **Future-Proof**: Payment status field allows easy gateway integration later

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

---

## 🧪 Controller Testing Implementation

### Test Coverage Summary
- **AdminControllerTest**: 7 tests covering admin stats and coupon generation
- **CartControllerTest**: 8 tests covering cart operations (add, update, remove, get)
- **ItemControllerTest**: 4 tests covering item catalog endpoints
- **OrderControllerTest**: 8 tests covering checkout and order history
- **Total**: 27 unit tests with 100% controller endpoint coverage

### Testing Architecture
- **Framework**: Spring Boot `@WebMvcTest` for isolated controller testing
- **HTTP Testing**: MockMvc for simulating HTTP requests/responses
- **Mocking**: Mockito `@MockBean` for service layer isolation
- **Assertions**: JSON Path assertions for response validation
- **Coverage**: Success scenarios, error handling, edge cases, and validation

### Test Structure Pattern
Each controller test follows this consistent pattern:
1. **Setup**: Mock service responses with realistic data
2. **Execute**: Perform HTTP request via MockMvc
3. **Verify**: Assert HTTP status, content type, and JSON response structure
4. **Edge Cases**: Test validation errors, empty responses, and invalid inputs

### Key Testing Features
- **DTO Validation**: Tests ensure proper request/response mapping
- **Error Handling**: Comprehensive coverage of 400/404 error scenarios
- **JSON Serialization**: Validates ObjectMapper configuration
- **Service Isolation**: Pure controller logic testing without database dependencies
- **Descriptive Names**: `@DisplayName` annotations for clear test documentation
- **Stock Information**: All response DTOs now include current stock levels for better UX

### Stock Information Enhancement
- **ItemResponse**: Added `stock` field to show available inventory for each product
- **CartItemResponse**: Added `stock` field to show current availability in cart and order views
- **Real-time Updates**: Stock levels fetched from database for accurate, current information
- **User Experience**: Users can see available stock when browsing items and managing cart
- **API Consistency**: Stock field included in all item-related response DTOs

### Validation Results
- ✅ All 27 controller tests pass
- ✅ No integration test failures
- ✅ Clean build with no compilation errors
- ✅ Consistent test patterns across all controllers
