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

## Tech Stack (Planned)
- Java 17
- Spring Boot
- Spring Web (REST APIs)
- Swagger / OpenAPI
- JUnit 5 & Mockito
