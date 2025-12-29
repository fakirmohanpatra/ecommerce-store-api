# Ecommerce Store API - Design Decisions

## Critical Assumptions

### Business Rules
- **Single System-Wide Coupon**: One active coupon at a time, available to all users (first-come-first-served)
- **Auto-Generated Coupons**: Every 5th order triggers new coupon generation
- **Stock Reduced on Checkout**: Stock is validated during cart operations but only reduced after successful order placement
- **Payment Always Succeeds**: No payment gateway - orders default to PAID status
- **In-Memory Storage**: No database required

### Technical Constraints
- Multiple users can checkout simultaneously
- Stock levels must prevent overselling
- Coupon usage must be atomic (no double-usage)
- Order numbering must be sequential

## Design Decisions & Rationale

### 1. Data Model Choices

**Decision**: UUID for all entity IDs
**Why**:
- Globally unique without coordination
- Thread-safe and works in distributed systems
- Shows production-ready best practices

**Decision**: BigDecimal for monetary values
**Why**:
- Prevents floating-point precision errors
- Industry standard for financial calculations

**Decision**: Snapshot pattern for CartItem/Order
**Why**:
- Prevents data inconsistency if source items change
- Orders remain accurate even if product details are updated later

### 2. Thread Safety Strategy

**Decision**: ConcurrentHashMap for data storage
**Why**:
- Thread-safe reads/writes without external synchronization
- Better performance than synchronized HashMap
- Handles concurrent access from multiple users

**Decision**: AtomicInteger for order counter
**Why**:
- Thread-safe increment operations
- No need for synchronized blocks
- Lightweight and efficient

**Decision**: Synchronized methods for coupon validation
**Why**:
- Prevents race conditions in coupon usage
- Ensures exactly one user can use a coupon
- Simple and effective for single JVM

### 3. Architecture Pattern

**Decision**: Repository pattern with interfaces
**Why**:
- Clean separation of concerns
- Easy to test with mocks
- Future database migration without changing service layer

**Decision**: Service layer for business logic
**Why**:
- Controllers handle HTTP, services handle business rules
- Centralized validation and error handling
- Reusable across different controllers

### 4. Coupon System Design

**Decision**: System-wide single coupon (not per-user)
**Why**:
- Simpler concurrency model
- Matches assignment requirements
- Realistic for promotional campaigns
- Avoids complex per-user tracking

**Decision**: No expiration time on coupons
**Why**:
- Simpler implementation
- Coupons expire naturally when used or replaced
- No need for background cleanup jobs

### 5. Inventory Management

**Decision**: Stock validation at cart, reduction at checkout
**Why**:
- Stock checked for availability when adding to cart (validation only)
- Actual stock reduction happens after successful order placement
- Prevents overselling via checkout-time validation
- Allows users to browse and add items without immediately locking inventory

### 6. Payment Integration

**Decision**: PaymentStatus enum with default PAID
**Why**:
- Future-proofs for payment gateway addition
- No breaking changes when payment layer is added
- Shows architectural foresight

## Application Flow

### Product Catalog
```
GET /api/items
├── Fetch all items from repository
├── Filter out-of-stock items
└── Return available products with current stock
```

### Cart Operations
```
POST /api/cart (Add Item)
├── Validate item exists and has sufficient stock
├── Create CartItem snapshot with current price/name
├── Add to user's cart (or update quantity if already exists)
└── Return updated cart with totals

GET /api/cart/{userId}
├── Retrieve user's cart items
├── Calculate subtotal
├── Apply coupon discount if valid
└── Return cart with final totals
```

### Checkout Process
```
POST /api/orders/checkout
├── Validate cart is not empty
├── Re-validate all items exist and have sufficient stock
├── Calculate subtotal and apply coupon (if provided) - synchronized check
├── Create Order with CartItem snapshots
├── Set paymentStatus = PAID
├── Save order with auto-generated ID
├── Decrease stock for all ordered items
├── Check if Nth order → generate new coupon
├── Clear user's cart
└── Return order confirmation
```

### Coupon Lifecycle
```
Generation:
├── Increment global order counter
├── If orderNumber % 5 == 0 → create new coupon
├── Replace any existing active coupon
└── Format: "SAVE10-{orderNumber}"

Usage:
├── Synchronized validation (exists + not used)
├── Mark as used (prevents double-usage)
├── Calculate 10% discount
├── Store discount details in order
└── Coupon becomes inactive
```

## Configuration
```yaml
app:
  coupon:
    nth-order: 5          # Every Nth order generates coupon
    discount-percentage: 10  # Fixed discount percentage
```
├── Global order counter increments on each checkout
├── if (orderNumber % 5 == 0) → Generate new coupon
├── Replace any existing active coupon
└── Format: "SAVE10-{orderNumber}"

Usage Logic:
├── Validate coupon exists and not used
├── Mark as used (synchronized)
├── Apply 10% discount to order subtotal
└── Store discount details in order
```

## Key Design Decisions

### Data Models
- **UUID for IDs**: Globally unique, no coordination needed
- **BigDecimal for Money**: Prevents floating-point precision errors
- **Snapshot Pattern**: CartItem/Order stores item data at creation time
- **PaymentStatus Enum**: Future-proof for payment gateway integration

### Thread Safety
- **ConcurrentHashMap**: Thread-safe data storage for items, carts, orders, coupons
- **AtomicInteger**: Thread-safe order counter
- **Synchronized Methods**: Coupon validation and usage (prevents double-use)
- **Stock Operations**: Rely on ConcurrentHashMap's thread-safety (no additional synchronization)

### Repository Pattern
- **Interface-Based**: Dependency injection on contracts
- **Single Responsibility**: One repository per entity
- **In-Memory Implementation**: ConcurrentHashMap with thread-safe operations

### Service Layer
- **Business Logic Isolation**: Controllers handle HTTP, services handle logic
- **Validation**: All business rules enforced here
- **Error Handling**: Custom exceptions with meaningful messages

### Configuration
```yaml
app:
  coupon:
    nth-order: 5
    discount-percentage: 10
```

## Architecture Overview

```
REST API Layer
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
In-Memory DataStore (Thread-Safe Storage)
```

## Out of Scope
- Payment gateway integration
- User authentication/authorization
- Order status workflow (shipped, delivered)
- Database persistence
- Advanced inventory features (reservations, backorders)

## 🌐 Frontend Integration Architecture
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

---
