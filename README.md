# Ecommerce Store API

This project implements a simple ecommerce backend where users can:
- Add items to a cart
- Checkout and place orders
- Apply discount coupons generated on every Nth order

The system is implemented using in-memory data stores and exposes REST APIs.

---

## Features
- Cart management (add/remove items)
- Checkout with optional discount code
- Automatic coupon generation on every Nth successful order
- Admin reporting APIs

---

## Assumptions
- In-memory storage is used (no database)
- Single store (no multi-tenant support)
- Users are identified via `userId` (passed as API parameter, no authentication required)
- Each user has one active cart. The cart is maintained until checkout or explicit item removal, and is cleared upon successful checkout.
- One active discount code at a time
- Discount code provides a flat 10% off the entire order
- Discount code is single-use
- Authentication and authorization are out of scope

---

## Tech Stack
- **Language**: Java 17
- **Framework**: Spring Boot 4.0.1
- **Build Tool**: Maven
- **API Documentation**: Springdoc OpenAPI (Swagger UI)
- **Testing**: JUnit 5, Mockito
- **In-Memory Storage**: ConcurrentHashMap, AtomicInteger

---

## How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+ (or use system Maven)

### Steps
1. **Clone the repository**
   ```bash
   git clone https://github.com/fakirmohanpatra/ecommerce-store-api.git
   cd ecommerce-store-api
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8080`
   - Swagger UI: `http://localhost:8080/swagger-ui.html`
   - API Docs: `http://localhost:8080/api-docs`

### Run Tests
```bash
mvn test
```

---

## Configuration

The application can be configured via `application.yml`:

```yaml
app:
  coupon:
    nth-order: 5          # Generate coupon every 5th order
    discount-percentage: 10  # 10% discount
```
