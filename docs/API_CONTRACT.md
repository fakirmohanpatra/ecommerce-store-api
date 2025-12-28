# E-Commerce Store API Contract

**Version:** 1.0  
**Base URL:** `http://localhost:8080`  
**Last Updated:** December 28, 2025

This document serves as the official API contract for frontend integration. All endpoints, request/response formats, and business rules are documented here.

---

## Table of Contents

1. [General Information](#general-information)
2. [Item/Product APIs](#itemproduct-apis)
3. [Cart APIs](#cart-apis)
4. [Coupon APIs](#coupon-apis)
5. [Order/Checkout APIs](#ordercheckout-apis)
6. [Admin APIs](#admin-apis)
7. [Error Responses](#error-responses)
8. [Data Models](#data-models)

---

## General Information

### Content Type
- **Request:** `application/json`
- **Response:** `application/json`

### Authentication
- Currently no authentication (userId is passed as string parameter)
- Future: JWT/OAuth2 tokens can be added

### User ID Format
- Type: `string`
- Example: `"user123"`, `"alice"`, `"bob"``, `"fmp"`
- Any string value is accepted (authentication out of scope)

### Item ID Format
- Type: `UUID`
- Example: `"3fa85f64-5717-4562-b3fc-2c963f66afa6"`
- Must be valid UUID from available items

### HTTP Status Codes
- `200 OK` - Successful GET/POST/PUT request
- `204 No Content` - Successful DELETE with no response body
- `400 Bad Request` - Validation error or malformed JSON
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Unexpected server error

---

## Item/Product APIs

### 1. Get All Items (Product Catalog)

Retrieve the complete product catalog with all available items.

**Endpoint:** `GET /api/items`

**Request:**
```http
GET /api/items HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Response:** `200 OK`
```json
[
  {
    "itemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "name": "Laptop",
    "price": 999.99,
    "stock": 10
  },
  {
    "itemId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
    "name": "Smartphone",
    "price": 699.99,
    "stock": 25
  }
]
```

**Frontend Usage:**
- Display product catalog
- Use `itemId` for add-to-cart operations
- Show `name` and `price` to users

---

### 2. Get Item by ID

Retrieve details of a specific item.

**Endpoint:** `GET /api/items/{itemId}`

**Path Parameters:**
- `itemId` (UUID, required) - Item identifier

**Request:**
```http
GET /api/items/3fa85f64-5717-4562-b3fc-2c963f66afa6 HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Response:** `200 OK`
```json
{
  "itemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "name": "Laptop",
  "price": 999.99,
  "stock": 10
}
```

**Error Response:** `404 Not Found` (if item doesn't exist)

---

## Cart APIs

### 1. Add Item to Cart

Add an item to user's shopping cart or increase quantity if already exists.

**Endpoint:** `POST /api/cart/{userId}/items`

**Path Parameters:**
- `userId` (string, required) - User identifier

**Request Body:**
```json
{
  "itemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "quantity": 2
}
```

**Validation Rules:**
- `itemId` - Required, must be valid UUID of existing item
- `quantity` - Required, must be >= 1

**Request:**
```http
POST /api/cart/user123/items HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "itemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "quantity": 2
}
```

**Response:** `200 OK`
```json
{
  "userId": "user123",
  "items": [
    {
      "itemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "itemName": "Laptop",
      "itemPrice": 999.99,
      "quantity": 2,
      "subtotal": 1999.98,
      "stock": 10
    }
  ],
  "totalItems": 2,
  "totalAmount": 1999.98
}
```

**Business Logic:**
- Creates new cart if user doesn't have one
- If item already in cart, quantity is **added** (not replaced)
- Price snapshot taken at add time (price changes don't affect existing cart items)

---

### 2. Remove Item from Cart

Remove an item completely from the cart.

**Endpoint:** `DELETE /api/cart/{userId}/items/{itemId}`

**Path Parameters:**
- `userId` (string, required)
- `itemId` (UUID, required)

**Request:**
```http
DELETE /api/cart/user123/items/3fa85f64-5717-4562-b3fc-2c963f66afa6 HTTP/1.1
Host: localhost:8080
```

**Response:** `200 OK`
```json
{
  "userId": "user123",
  "items": [],
  "totalItems": 0,
  "totalAmount": 0.00
}
```

**Note:** Returns updated cart (may be empty)

---

### 3. Update Item Quantity

Update the quantity of an existing cart item.

**Endpoint:** `PUT /api/cart/{userId}/items/{itemId}`

**Path Parameters:**
- `userId` (string, required)
- `itemId` (UUID, required)

**Request Body:**
```json
{
  "quantity": 5
}
```

**Validation:**
- `quantity` - Required, must be >= 1

**Request:**
```http
PUT /api/cart/user123/items/3fa85f64-5717-4562-b3fc-2c963f66afa6 HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "quantity": 5
}
```

**Response:** `200 OK`
```json
{
  "userId": "user123",
  "items": [
    {
      "itemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "itemName": "Laptop",
      "itemPrice": 999.99,
      "quantity": 5,
      "subtotal": 4999.95,
      "stock": 10
    }
  ],
  "totalItems": 5,
  "totalAmount": 4999.95
}
```

**Note:** This **replaces** the quantity (doesn't add to it)

---

### 4. Get User's Cart

Retrieve the current state of user's cart.

**Endpoint:** `GET /api/cart/{userId}`

**Path Parameters:**
- `userId` (string, required)

**Request:**
```http
GET /api/cart/user123 HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Response:** `200 OK`
```json
{
  "userId": "user123",
  "items": [
    {
      "itemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "name": "Laptop",
      "quantity": 2,
      "pricePerUnit": 999.99,
      "subtotal": 1999.98
    },
    {
      "itemId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "name": "Smartphone",
      "quantity": 1,
      "pricePerUnit": 699.99,
      "subtotal": 699.99
    }
  ],
  "totalItems": 3,
  "totalAmount": 2699.97
}
```

**Empty Cart Response:**
```json
{
  "userId": "user123",
  "items": [],
  "totalItems": 0,
  "totalAmount": 0.00
}
```

---

### 5. Clear Cart

Remove all items from user's cart.

**Endpoint:** `DELETE /api/cart/{userId}`

**Path Parameters:**
- `userId` (string, required)

**Request:**
```http
DELETE /api/cart/user123 HTTP/1.1
Host: localhost:8080
```

**Response:** `204 No Content`

**Note:** No response body. Cart is emptied.

---

## Coupon APIs

### 1. Get Active Coupon

Get the currently active coupon that can be applied during checkout.

**Endpoint:** `GET /api/coupons/active`

**Response (200 OK):**
```json
{
  "code": "SAVE10-015",
  "isUsed": false,
  "generatedAtOrderNumber": 15,
  "createdAt": "2025-12-28T10:00:00.000Z"
}
```

**Response (404 Not Found):** No active coupon available.

**Note:** This endpoint allows users to see the current active coupon code for display in the UI.

---

## Order/Checkout APIs

### 1. Checkout (Create Order)

Convert cart to order, apply coupon discount (if provided), and generate new coupon on Nth order.

**Endpoint:** `POST /api/orders/checkout`

**Request Body:**
```json
{
  "userId": "user123",
  "couponCode": "SAVE10-005"
}
```

**Validation Rules:**
- `userId` - Required, must not be blank
- `couponCode` - Optional (empty string or null = no coupon)

**Request (Without Coupon):**
```http
POST /api/orders/checkout HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "userId": "user123",
  "couponCode": ""
}
```

**Response:** `200 OK`
```json
{
  "orderId": "550e8400-e29b-41d4-a716-446655440000",
  "userId": "user123",
  "items": [
    {
      "itemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "itemName": "Laptop",
      "itemPrice": 999.99,
      "quantity": 2,
      "subtotal": 1999.98,
      "stock": 10
    }
  ],
  "totalAmount": 1999.98,
  "discountAmount": 0.00,
  "couponCode": null,
  "paymentStatus": "PAID",
  "createdAt": "2025-12-28T10:30:00.123Z"
}
```

**Request (With Coupon):**
```http
POST /api/orders/checkout HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "userId": "user123",
  "couponCode": "SAVE10-005"
}
```

**Response:** `200 OK`
```json
{
  "orderId": "660e8400-e29b-41d4-a716-446655440000",
  "userId": "user123",
  "items": [
    {
      "itemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "itemName": "Laptop",
      "itemPrice": 999.99,
      "quantity": 1,
      "subtotal": 999.99,
      "stock": 10
    }
  ],
  "totalAmount": 899.99,
  "discountAmount": 100.00,
  "couponCode": "SAVE10-005",
  "paymentStatus": "PAID",
  "createdAt": "2025-12-28T10:35:00.456Z"
}
```

**Business Logic:**
1. Validate cart is not empty
2. Validate all items still exist
3. If coupon provided:
   - Validate coupon exists and is not used
   - Calculate 10% discount on subtotal
   - Mark coupon as used
4. Create order (snapshot of cart with frozen prices)
5. Check if this is Nth order (default: every 5th order):
   - Generate new coupon: `SAVE10-{orderNumber}`
   - Example: Order #10 → generates `SAVE10-010`
6. Clear user's cart
7. Return order confirmation

**Error Responses:**

Empty Cart:
```json
{
  "message": "Cart is empty",
  "errorCode": "INVALID_ARGUMENT",
  "timestamp": "2025-12-28T10:00:00.000Z"
}
```

Invalid Coupon:
```json
{
  "message": "Invalid coupon code: SAVE10-005. Please check the active coupon code.",
  "errorCode": "COUPON_INVALID",
  "timestamp": "2025-12-28T10:00:00.000Z"
}
```

No Active Coupon:
```json
{
  "message": "No active coupon available. Coupons are generated every 5th order.",
  "errorCode": "COUPON_INVALID",
  "timestamp": "2025-12-28T10:00:00.000Z"
}
```

Coupon Already Used:
```json
{
  "message": "Coupon code already used: SAVE10-005. Each coupon can only be used once.",
  "errorCode": "COUPON_INVALID",
  "timestamp": "2025-12-28T10:00:00.000Z"
}
```

Item No Longer Available:
```json
{
  "message": "Item {itemId} not found",
  "errorCode": "RUNTIME_ERROR",
  "timestamp": "2025-12-28T10:00:00.000Z"
}
```

---

### 2. Get Order History

Retrieve all orders for a user, sorted by date (latest first).

**Endpoint:** `GET /api/orders/{userId}`

**Path Parameters:**
- `userId` (string, required)

**Request:**
```http
GET /api/orders/user123 HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Response:** `200 OK`
```json
[
  {
    "orderId": "660e8400-e29b-41d4-a716-446655440000",
    "userId": "user123",
    "items": [
      {
        "itemId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "itemName": "Laptop",
        "itemPrice": 999.99,
        "quantity": 1,
        "subtotal": 999.99,
        "stock": 10
      }
    ],
    "totalAmount": 899.99,
    "discountAmount": 100.00,
    "couponCode": "SAVE10-005",
    "paymentStatus": "PAID",
    "createdAt": "2025-12-28T10:35:00.456Z"
  },
  {
    "orderId": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "user123",
    "items": [
      {
        "itemId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
        "itemName": "Smartphone",
        "itemPrice": 699.99,
        "quantity": 2,
        "subtotal": 1399.98,
        "stock": 25
      }
    ],
    "totalAmount": 1399.98,
    "discountAmount": 0.00,
    "couponCode": null,
    "paymentStatus": "PAID",
    "createdAt": "2025-12-28T09:30:00.123Z"
  }
]
```

**Empty History:**
```json
[]
```

---

## Admin APIs

### 1. Get Store Statistics

Get aggregate statistics for the entire store.

**Endpoint:** `GET /api/admin/stats`

**Request:**
```http
GET /api/admin/stats HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Response:** `200 OK`
```json
{
  "totalItemsPurchased": 150,
  "totalPurchaseAmount": 12500.50,
  "totalDiscountAmount": 1250.00,
  "totalOrders": 45,
  "ordersWithCoupons": 8,
  "totalCouponsGenerated": 9,
  "activeCoupon": "SAVE10-045"
}
```

**Field Descriptions:**
- `totalItemsPurchased` - Sum of quantities across all orders
- `totalPurchaseAmount` - Sum of all order totals (after discounts)
- `totalDiscountAmount` - Sum of all discounts applied
- `totalOrders` - Total number of completed orders
- `ordersWithCoupons` - Number of orders that used a coupon
- `totalCouponsGenerated` - Number of coupons generated
- `activeCoupon` - Current unused coupon (null if all used)

---

### 2. List All Coupons

Get list of all generated coupon codes.

**Endpoint:** `GET /api/admin/coupons`

**Request:**
```http
GET /api/admin/coupons HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Response:** `200 OK`
```json
{
  "coupons": ["SAVE10-005", "SAVE10-010", "SAVE10-015", "SAVE10-020"],
  "totalGenerated": 4
}
```

**Empty List:**
```json
{
  "coupons": [],
  "totalGenerated": 0
}
```

---

### 3. Get Active Coupon

Get the currently active (unused) coupon.

**Endpoint:** `GET /api/admin/coupons/active`

**Request:**
```http
GET /api/admin/coupons/active HTTP/1.1
Host: localhost:8080
Accept: application/json
```

**Response:** `200 OK`
```json
{
  "code": "SAVE10-015",
  "isUsed": false,
  "generatedAtOrderNumber": 15,
  "createdAt": "2025-12-28T10:00:00.123Z"
}
```

**No Active Coupon:** `404 Not Found`

---

### 4. Manually Generate Coupon

Admin endpoint to manually generate a coupon (for testing or special cases).

**Endpoint:** `POST /api/admin/coupons/generate`

**Request:**
```http
POST /api/admin/coupons/generate HTTP/1.1
Host: localhost:8080
```

**Response:** `200 OK`
```json
{
  "code": "SAVE10-000",
  "isUsed": false,
  "generatedAtOrderNumber": 0,
  "createdAt": "2025-12-28T11:00:00.789Z"
}
```

**Note:** Manual coupons use order number 0 to distinguish from auto-generated ones.

---

## Error Responses

All errors follow a consistent format:

```json
{
  "message": "Description of what went wrong",
  "errorCode": "ERROR_TYPE",
  "timestamp": "2025-12-28T10:00:00.123Z"
}
```

### Error Codes

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `VALIDATION_ERROR` | 400 | Request validation failed (missing required fields, invalid format) |
| `INVALID_JSON` | 400 | Malformed JSON in request body |
| `INVALID_ARGUMENT` | 400 | Business logic validation failed (empty cart, item not found, etc.) |
| `RUNTIME_ERROR` | 400 | Runtime exception during processing |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

### Common Error Scenarios

**Malformed JSON:**
```json
{
  "message": "Invalid request format. Please check your JSON syntax.",
  "errorCode": "INVALID_JSON",
  "timestamp": "2025-12-28T10:00:00.000Z"
}
```

**Missing Required Field:**
```json
{
  "message": "User ID is required",
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "2025-12-28T10:00:00.000Z"
}
```

**Item Not Found:**
```json
{
  "message": "Item not found: 3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "errorCode": "RUNTIME_ERROR",
  "timestamp": "2025-12-28T10:00:00.000Z"
}
```

**Empty Cart Checkout:**
```json
{
  "message": "Cart is empty",
  "errorCode": "INVALID_ARGUMENT",
  "timestamp": "2025-12-28T10:00:00.000Z"
}
```

**Invalid or Used Coupon:**
```json
{
  "message": "Coupon SAVE10-005 not found or already used",
  "errorCode": "RUNTIME_ERROR",
  "timestamp": "2025-12-28T10:00:00.000Z"
}
```

---

## Data Models

### ItemResponse
```typescript
{
  itemId: string;        // UUID
  name: string;
  price: number;         // Decimal, 2 decimal places
}
```

### CartItemResponse
```typescript
{
  itemId: string;        // UUID
  name: string;
  quantity: number;      // Integer >= 1
  pricePerUnit: number;  // Price at time of adding to cart
  subtotal: number;      // quantity × pricePerUnit
}
```

### CartResponse
```typescript
{
  userId: string;
  items: CartItemResponse[];
  totalItems: number;    // Sum of all quantities
  totalAmount: number;   // Sum of all subtotals
}
```

### OrderResponse
```typescript
{
  orderId: string;           // UUID
  userId: string;
  orderNumber: number;       // Sequential counter (1, 2, 3...)
  items: CartItemResponse[]; // Snapshot of cart at checkout
  subtotal: number;          // Total before discount
  discountAmount: number;    // Discount applied (0 if no coupon)
  totalAmount: number;       // Subtotal - discount
  couponCode: string | null; // Coupon used (null if none)
  paymentStatus: "PAID";     // Always PAID for now
  createdAt: string;         // ISO 8601 timestamp
}
```

### CouponResponse
```typescript
{
  code: string;                   // Format: "SAVE10-{orderNumber}"
  isUsed: boolean;
  generatedAtOrderNumber: number; // Order that triggered generation
  createdAt: string;              // ISO 8601 timestamp
}
```

### AdminStatsResponse
```typescript
{
  totalItemsPurchased: number;    // Total item quantity sold
  totalPurchaseAmount: number;    // Total revenue (after discounts)
  totalDiscountAmount: number;    // Total discounts given
  totalOrders: number;            // Number of orders
  ordersWithCoupons: number;      // Orders that used coupons
  totalCouponsGenerated: number;  // Coupons generated
  activeCoupon: string | null;    // Current unused coupon
}
```

### ErrorResponse
```typescript
{
  message: string;
  errorCode: string;
  timestamp: string;  // ISO 8601
}
```

---

## Business Rules Reference

### Coupon Generation
- **Trigger:** Every Nth order (configurable, default: 5th)
- **Format:** `SAVE10-{orderNumber}` (e.g., `SAVE10-005`, `SAVE10-010`)
- **Discount:** 10% of cart subtotal (configurable)
- **Usage:** Single-use only
- **Example:** Orders 5, 10, 15, 20 each generate a coupon

### Price Snapshot
- When item added to cart, current price is **frozen**
- Future price changes don't affect existing cart items
- Order stores prices from cart snapshot

### Cart Behavior
- **Add Item:** Increases quantity if item already exists
- **Update Quantity:** Replaces quantity (doesn't add)
- **Checkout:** Clears cart after successful order creation

### Order Numbering
- Sequential counter starting at 1
- Global across all users
- Used for coupon generation logic

---

## Frontend Integration Examples

### TypeScript/JavaScript Service Example

```typescript
const API_BASE_URL = 'http://localhost:8080';

class EcommerceService {
  
  // Get all items
  async getAllItems(): Promise<ItemResponse[]> {
    const response = await fetch(`${API_BASE_URL}/api/items`);
    return response.json();
  }
  
  // Add to cart
  async addToCart(userId: string, itemId: string, quantity: number): Promise<CartResponse> {
    const response = await fetch(`${API_BASE_URL}/api/cart/${userId}/items`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ itemId, quantity })
    });
    return response.json();
  }
  
  // Checkout
  async checkout(userId: string, couponCode?: string): Promise<OrderResponse> {
    const response = await fetch(`${API_BASE_URL}/api/orders/checkout`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ 
        userId, 
        couponCode: couponCode || "" 
      })
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message);
    }
    
    return response.json();
  }
  
  // Get cart
  async getCart(userId: string): Promise<CartResponse> {
    const response = await fetch(`${API_BASE_URL}/api/cart/${userId}`);
    return response.json();
  }
  
  // Get order history
  async getOrderHistory(userId: string): Promise<OrderResponse[]> {
    const response = await fetch(`${API_BASE_URL}/api/orders/${userId}`);
    return response.json();
  }
  
  // Get admin stats
  async getAdminStats(): Promise<AdminStatsResponse> {
    const response = await fetch(`${API_BASE_URL}/api/admin/stats`);
    return response.json();
  }
}
```

### React Hook Example

```typescript
function useCart(userId: string) {
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [loading, setLoading] = useState(false);
  
  const refreshCart = async () => {
    setLoading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/api/cart/${userId}`);
      const data = await response.json();
      setCart(data);
    } finally {
      setLoading(false);
    }
  };
  
  const addItem = async (itemId: string, quantity: number) => {
    const response = await fetch(`${API_BASE_URL}/api/cart/${userId}/items`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ itemId, quantity })
    });
    const data = await response.json();
    setCart(data);
  };
  
  return { cart, loading, refreshCart, addItem };
}
```

---

## Testing Checklist

### Smoke Tests
- [ ] GET /api/items returns 10 seed items
- [ ] POST /api/cart/{userId}/items adds item
- [ ] GET /api/cart/{userId} shows cart
- [ ] POST /api/orders/checkout creates order
- [ ] Cart is cleared after checkout
- [ ] 5th order generates coupon SAVE10-005
- [ ] Coupon applies 10% discount on 6th order
- [ ] Coupon cannot be reused (error on 7th order)
- [ ] GET /api/admin/stats shows correct aggregates

### Edge Cases
- [ ] Checkout with empty cart → error
- [ ] Add non-existent item → error
- [ ] Use invalid coupon → error
- [ ] Update quantity to 0 → error
- [ ] Malformed JSON → clear error message
- [ ] Remove item not in cart → no error
- [ ] Multiple users have independent carts

---

## Configuration

Application properties (`application.yml`):

```yaml
app:
  coupon:
    nth-order: 5          # Generate coupon every Nth order
    discount-percentage: 10  # Discount percentage (10%)
```

---

## Notes for Frontend Developers

1. **User IDs:** Any string works ("user123", "alice"). No registration required.

2. **Item UUIDs:** Call `GET /api/items` first to get valid UUIDs for testing.

3. **Timestamps:** All timestamps are ISO 8601 format in UTC.

4. **Currency:** All amounts use 2 decimal places (standard for USD).

5. **Coupon Display:** Show active coupon from `GET /api/admin/coupons/active` on checkout page.

6. **Error Handling:** Always check `response.ok` and display `error.message` to users.

7. **Cart Total:** Frontend should display `totalAmount` from CartResponse (backend calculates).

8. **Order Confirmation:** After checkout, display OrderResponse details including discount applied.

9. **CORS:** If frontend runs on different domain, backend CORS may need configuration.

10. **State Management:** Cart should refresh after add/update/remove operations.

---

## Revision History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-12-28 | Initial API contract document |

---

**End of API Contract**
