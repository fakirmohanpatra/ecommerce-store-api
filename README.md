# 🛍️ Ecommerce Store API

A comprehensive, production-ready ecommerce backend system built with Spring Boot that enables seamless shopping experiences with intelligent coupon management and real-time inventory tracking.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.13-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.6+-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

## 📋 Table of Contents

- [🎯 Project Overview](#-project-overview)
- [🏗️ System Architecture](#️-system-architecture)
- [✨ Key Features](#-key-features)
- [🛠️ Technology Stack](#️-technology-stack)
- [🚀 Quick Start](#-quick-start)
- [📚 API Documentation](#-api-documentation)
- [🔧 Configuration](#-configuration)
- [🧪 Testing](#-testing)
- [🌐 Frontend Integration](#-frontend-integration)
- [📊 Business Logic](#-business-logic)
- [🤝 Contributing](#-contributing)

---

## 🎯 Project Overview

### Vision
This project implements a modern ecommerce platform where customers can browse products, manage shopping carts, and complete purchases with an intelligent discount system that rewards customer loyalty through automated coupon generation.

### Core Business Problem
Traditional ecommerce systems often lack sophisticated discount management. This platform addresses the challenge by implementing an automated coupon system that generates discounts based on system-wide order milestones, creating a gamified shopping experience that encourages repeat purchases.

### Key Business Rules
- **Smart Cart Management**: Real-time inventory validation and stock tracking
- **Automated Rewards**: Every Nth order triggers a system-wide discount coupon
- **One-Time Use Coupons**: Ensures fair distribution and prevents abuse
- **Thread-Safe Operations**: Handles concurrent users without data corruption
- **Admin Oversight**: Comprehensive reporting and manual coupon generation capabilities

---

## 🏗️ System Architecture

### High-Level Design

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   REST API      │    │   In-Memory     │
│   (React/Angular│◄──►│   Controllers   │◄──►│   Data Store    │
│   /Vue.js)      │    │   Services       │    │   (Thread-Safe)│
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Admin Portal  │
                       │   (Statistics & │
                       │   Management)   │
                       └─────────────────┘
```

### Component Architecture

#### **Data Layer**
- **DataStore**: Thread-safe in-memory storage using `ConcurrentHashMap` and `AtomicInteger`
- **Repositories**: Clean abstraction layer with interface-based design
- **Thread Safety**: Synchronized operations for concurrent access

#### **Business Logic Layer**
- **Services**: Core business logic with validation and error handling
- **Domain Models**: Rich business objects with proper encapsulation
- **Validation**: Comprehensive input validation and business rule enforcement

#### **API Layer**
- **Controllers**: RESTful endpoints with proper HTTP semantics
- **DTOs**: Data transfer objects for clean API contracts
- **Exception Handling**: Global exception handling with meaningful error responses

#### **Cross-Cutting Concerns**
- **Configuration**: Externalized configuration with Spring profiles
- **Security**: CORS configuration for frontend integration
- **Documentation**: Auto-generated OpenAPI/Swagger documentation

### Data Flow

1. **Product Discovery** → Items API returns available products with stock levels
2. **Cart Management** → Add/Update/Remove items with real-time stock validation
3. **Checkout Process** → Validate cart, apply coupons, create order, update inventory
4. **Coupon Generation** → Automatic coupon creation on Nth order milestone
5. **Admin Reporting** → Real-time statistics and system management

---

## ✨ Key Features

### 🛒 Shopping Experience
- **Product Catalog**: Browse available items with real-time stock information
- **Smart Cart**: Add, update, and remove items with automatic stock validation
- **Checkout Flow**: Seamless order placement with optional discount application
- **Order History**: Complete purchase history for users

### 🎫 Intelligent Coupon System
- **Automated Generation**: Coupons created every Nth system order (configurable)
- **One-Time Use**: Each coupon can only be used once, ensuring fair distribution
- **System-Wide Rewards**: Global order counter creates community-driven incentives
- **Manual Override**: Admin capability to generate coupons when needed

### 📈 Admin Dashboard
- **Real-Time Statistics**: Total sales, items purchased, discount tracking
- **Coupon Management**: View all generated coupons and active discount codes
- **System Monitoring**: Order counts, revenue analytics, and performance metrics

### 🔒 Enterprise-Grade Features
- **Thread Safety**: Concurrent user operations without data corruption
- **Input Validation**: Comprehensive validation with meaningful error messages
- **Error Handling**: Graceful error responses and recovery mechanisms
- **Configurable**: Externalized configuration for different environments

---

## 🛠️ Technology Stack

### Backend
- **Runtime**: Java 17 (LTS)
- **Framework**: Spring Boot 3.4.13
- **Build Tool**: Maven 3.6+
- **API Documentation**: SpringDoc OpenAPI 2.7.0 (Swagger UI)
- **Validation**: Bean Validation (JSR-303)
- **Testing**: JUnit 5, Mockito

### Data Storage
- **Primary Store**: In-Memory (ConcurrentHashMap, AtomicInteger)
- **Thread Safety**: Synchronized collections for concurrent access
- **No External Dependencies**: Zero database configuration required

### Development Tools
- **IDE**: VS Code / IntelliJ IDEA
- **Version Control**: Git
- **API Testing**: Postman / REST Client
- **Documentation**: Markdown

### Frontend (Optional)
- **Framework**: Angular 17+
- **Language**: TypeScript
- **HTTP Client**: Angular HttpClient
- **Build Tool**: Angular CLI
- **Styling**: SCSS/CSS

---

## 🚀 Quick Start

### Prerequisites
- **Java**: JDK 17 or higher
- **Maven**: 3.6+ (or use included Maven wrapper)
- **Git**: For cloning the repository
- **Node.js**: 18+ and npm (for frontend development)
- **Angular CLI**: `npm install -g @angular/cli` (for frontend development)

### Installation & Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/fakirmohanpatra/ecommerce-store-api.git
   cd ecommerce-store-api
   ```

2. **Build the Application**
   ```bash
   # Using Maven wrapper (recommended)
   ./mvnw clean install

   # Or using system Maven
   mvn clean install
   ```

3. **Run the Application**
   ```bash
   # Using Maven wrapper
   ./mvnw spring-boot:run

   # Or using system Maven
   mvn spring-boot:run
   ```

4. **Verify Installation**
   - **API Base URL**: http://localhost:8080
   - **Swagger UI**: http://localhost:8080/swagger-ui/index.html
   - **API Documentation**: http://localhost:8080/v3/api-docs

### Sample API Calls

```bash
# Get all available items
curl http://localhost:8080/api/items

# Add item to cart
curl -X POST http://localhost:8080/api/cart \
  -H "Content-Type: application/json" \
  -d '{"userId": "user123", "itemId": "item-uuid", "quantity": 1}'

# Checkout with coupon
curl -X POST http://localhost:8080/api/orders/checkout \
  -H "Content-Type: application/json" \
  -d '{"userId": "user123", "couponCode": "SAVE10-005"}'
```

---

## 📚 API Documentation

### Core Endpoints

#### **Product Management**
- `GET /api/items` - Browse available products
- `GET /api/items/{id}` - Get specific product details

#### **Cart Operations**
- `GET /api/cart/{userId}` - View user's cart
- `POST /api/cart` - Add item to cart
- `PUT /api/cart/quantity` - Update item quantity
- `DELETE /api/cart/{userId}/items/{itemId}` - Remove item from cart

#### **Order Management**
- `POST /api/orders/checkout` - Complete purchase
- `GET /api/orders/history/{userId}` - View order history

#### **Coupon System**
- `GET /api/coupons/active` - Get current active coupon
- `POST /api/coupons/validate` - Validate coupon code

#### **Admin Operations**
- `GET /api/admin/stats` - System statistics
- `GET /api/admin/coupons` - All generated coupons
- `POST /api/admin/coupons/generate` - Manual coupon generation

### Response Format
```json
{
  "success": true,
  "data": { ... },
  "message": "Operation completed successfully",
  "timestamp": "2025-12-28T10:30:00Z"
}
```

### Error Handling
```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_STOCK",
    "message": "Not enough stock available",
    "details": { ... }
  },
  "timestamp": "2025-12-28T10:30:00Z"
}
```

---

## 🔧 Configuration

### Application Properties

```yaml
# Server Configuration
server:
  port: 8080

# Application Settings
spring:
  application:
    name: ecommerce-store-api

# Business Rules Configuration
app:
  coupon:
    nth-order: 5                    # Generate coupon every Nth order
    discount-percentage: 10         # Discount percentage (10%)

# CORS Configuration (for frontend integration)
cors:
  allowed-origins: http://localhost:4200,https://yourdomain.com
```

### Environment Variables
```bash
# Override default configurations
export SERVER_PORT=9090
export APP_COUPON_NTH_ORDER=10
export APP_COUPON_DISCOUNT_PERCENTAGE=15
```

### Profiles
```bash
# Development profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Production profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

---

## 🧪 Testing

### Run Test Suite
```bash
# Run all tests
mvn test

# Run with coverage report
mvn test jacoco:report

# Run specific test class
mvn test -Dtest=OrderServiceTest

# Run integration tests only
mvn test -Dtest="*IT"
```

### Test Structure
```
src/test/java/
├── controller/     # API endpoint tests
├── service/        # Business logic tests
├── repository/     # Data access tests
└── integration/    # End-to-end tests
```

### Test Coverage
- **Unit Tests**: 96+ test cases covering all business logic
- **Integration Tests**: Full API workflow testing
- **Concurrency Tests**: Thread-safety validation
- **Edge Case Coverage**: Error scenarios and boundary conditions

---

## 🌐 Frontend Integration

### Integration Architecture

```
Frontend App (Angular)
        │
        ▼ HTTP Requests
    REST API (Spring Boot)
        │
        ▼ Business Logic
    In-Memory Data Store
```

### CORS Configuration
The API is pre-configured for Angular frontend integration:

```yaml
cors:
  allowed-origins: http://localhost:4200,https://yourdomain.com
```

### Frontend Integration Steps

1. **Angular Project Setup**
   ```bash
   # Create new Angular project
   ng new ecommerce-frontend --routing --style=scss

   # Navigate to project
   cd ecommerce-frontend

   # Install dependencies
   npm install
   ```

2. **Configure API Base URL**
   ```typescript
   // src/environments/environment.ts
   export const environment = {
     production: false,
     apiUrl: 'http://localhost:8080'
   };

   // src/environments/environment.prod.ts
   export const environment = {
     production: true,
     apiUrl: 'https://your-api-domain.com'
   };
   ```

3. **Create API Service**
   ```typescript
   // src/app/services/api.service.ts
   import { Injectable } from '@angular/core';
   import { HttpClient, HttpErrorResponse } from '@angular/common/http';
   import { Observable, throwError } from 'rxjs';
   import { catchError } from 'rxjs/operators';
   import { environment } from '../../environments/environment';

   @Injectable({
     providedIn: 'root'
   })
   export class ApiService {
     private apiUrl = environment.apiUrl;

     constructor(private http: HttpClient) { }

     // Fetch all products
     getProducts(): Observable<any> {
       return this.http.get(`${this.apiUrl}/api/items`)
         .pipe(catchError(this.handleError));
     }

     // Add item to cart
     addToCart(userId: string, itemId: string, quantity: number): Observable<any> {
       return this.http.post(`${this.apiUrl}/api/cart`, {
         userId,
         itemId,
         quantity
       }).pipe(catchError(this.handleError));
     }

     // Get user cart
     getCart(userId: string): Observable<any> {
       return this.http.get(`${this.apiUrl}/api/cart/${userId}`)
         .pipe(catchError(this.handleError));
     }

     // Checkout
     checkout(userId: string, couponCode?: string): Observable<any> {
       return this.http.post(`${this.apiUrl}/api/orders/checkout`, {
         userId,
         couponCode
       }).pipe(catchError(this.handleError));
     }

     // Get active coupon
     getActiveCoupon(): Observable<any> {
       return this.http.get(`${this.apiUrl}/api/coupons/active`)
         .pipe(catchError(this.handleError));
     }

     private handleError(error: HttpErrorResponse) {
       let errorMessage = 'An unknown error occurred!';

       if (error.error instanceof ErrorEvent) {
         // Client-side error
         errorMessage = error.error.message;
       } else {
         // Server-side error
         errorMessage = error.error?.message || `Error Code: ${error.status}`;
       }

       console.error('API Error:', errorMessage);
       return throwError(() => new Error(errorMessage));
     }
   }
   ```

4. **Implement Components**
   ```typescript
   // src/app/components/product-list/product-list.component.ts
   import { Component, OnInit } from '@angular/core';
   import { ApiService } from '../../services/api.service';

   @Component({
     selector: 'app-product-list',
     templateUrl: './product-list.component.html',
     styleUrls: ['./product-list.component.scss']
   })
   export class ProductListComponent implements OnInit {
     products: any[] = [];
     loading = false;
     error: string | null = null;

     constructor(private apiService: ApiService) { }

     ngOnInit(): void {
       this.loadProducts();
     }

     loadProducts(): void {
       this.loading = true;
       this.error = null;

       this.apiService.getProducts().subscribe({
         next: (data) => {
           this.products = data;
           this.loading = false;
         },
         error: (error) => {
           this.error = error.message;
           this.loading = false;
         }
       });
     }

     addToCart(product: any): void {
       const userId = 'user123'; // In real app, get from auth service
       const quantity = 1;

       this.apiService.addToCart(userId, product.id, quantity).subscribe({
         next: () => {
           alert(`${product.name} added to cart!`);
         },
         error: (error) => {
           alert(`Failed to add to cart: ${error.message}`);
         }
       });
     }
   }
   ```

   ```html
   <!-- src/app/components/product-list/product-list.component.html -->
   <div class="product-list">
     <h2>Available Products</h2>

     <div *ngIf="loading" class="loading">Loading products...</div>

     <div *ngIf="error" class="error">
       Error loading products: {{ error }}
       <button (click)="loadProducts()">Retry</button>
     </div>

     <div *ngIf="!loading && !error" class="products-grid">
       <div *ngFor="let product of products" class="product-card">
         <h3>{{ product.name }}</h3>
         <p class="price">${{ product.price }}</p>
         <p class="stock">Stock: {{ product.stock }}</p>
         <button
           (click)="addToCart(product)"
           [disabled]="product.stock === 0"
           class="add-to-cart-btn">
           {{ product.stock > 0 ? 'Add to Cart' : 'Out of Stock' }}
         </button>
       </div>
     </div>
   </div>
   ```

5. **Create Shopping Cart Component**
   ```typescript
   // src/app/components/shopping-cart/shopping-cart.component.ts
   import { Component, OnInit } from '@angular/core';
   import { ApiService } from '../../services/api.service';

   @Component({
     selector: 'app-shopping-cart',
     templateUrl: './shopping-cart.component.html',
     styleUrls: ['./shopping-cart.component.scss']
   })
   export class ShoppingCartComponent implements OnInit {
     cart: any = null;
     couponCode = '';
     activeCoupon: any = null;
     loading = false;

     constructor(private apiService: ApiService) { }

     ngOnInit(): void {
       this.loadCart();
       this.loadActiveCoupon();
     }

     loadCart(): void {
       const userId = 'user123'; // In real app, get from auth service
       this.loading = true;

       this.apiService.getCart(userId).subscribe({
         next: (data) => {
           this.cart = data;
           this.loading = false;
         },
         error: (error) => {
           console.error('Failed to load cart:', error);
           this.loading = false;
         }
       });
     }

     loadActiveCoupon(): void {
       this.apiService.getActiveCoupon().subscribe({
         next: (data) => {
           this.activeCoupon = data;
         },
         error: (error) => {
           console.log('No active coupon available');
           this.activeCoupon = null;
         }
       });
     }

     checkout(): void {
       if (!this.cart || this.cart.items.length === 0) {
         alert('Your cart is empty!');
         return;
       }

       const userId = 'user123'; // In real app, get from auth service
       const couponToUse = this.couponCode.trim() || null;

       this.loading = true;

       this.apiService.checkout(userId, couponToUse).subscribe({
         next: (order) => {
           alert(`Order placed successfully! Total: $${order.totalAmount}`);
           this.cart = null;
           this.couponCode = '';
           this.loading = false;
         },
         error: (error) => {
           alert(`Checkout failed: ${error.message}`);
           this.loading = false;
         }
       });
     }
   }
   ```

   ```html
   <!-- src/app/components/shopping-cart/shopping-cart.component.html -->
   <div class="shopping-cart">
     <h2>Shopping Cart</h2>

     <div *ngIf="loading" class="loading">Processing...</div>

     <div *ngIf="!loading && !cart" class="empty-cart">
       <p>Your cart is empty</p>
     </div>

     <div *ngIf="!loading && cart">
       <div class="cart-items">
         <div *ngFor="let item of cart.items" class="cart-item">
           <h4>{{ item.itemName }}</h4>
           <p>Quantity: {{ item.quantity }}</p>
           <p>Price: ${{ item.price }}</p>
           <p>Subtotal: ${{ item.subtotal }}</p>
         </div>
       </div>

       <div class="cart-summary">
         <p><strong>Total: ${{ cart.total }}</strong></p>

         <div *ngIf="activeCoupon" class="coupon-info">
           <p>🎫 Active Coupon: <strong>{{ activeCoupon.code }}</strong></p>
           <small>{{ activeCoupon.discountPercentage }}% off your order!</small>
         </div>

         <div class="coupon-input">
           <input
             type="text"
             [(ngModel)]="couponCode"
             placeholder="Enter coupon code (optional)"
             class="coupon-input-field">
         </div>

         <button
           (click)="checkout()"
           [disabled]="cart.items.length === 0"
           class="checkout-btn">
           Checkout {{ couponCode ? 'with Coupon' : '' }}
         </button>
       </div>
     </div>
   </div>
   ```

### Running the Angular Frontend

```bash
# Start Angular development server
ng serve

# Or with custom port
ng serve --port 4200

# Build for production
ng build --prod
```

### Angular CLI Commands Reference

```bash
# Generate components
ng generate component components/product-list
ng generate component components/shopping-cart

# Generate services
ng generate service services/api

# Generate guards (for future auth)
ng generate guard guards/auth
```

---

## 📊 Business Logic

### Coupon Generation Algorithm
```java
// Automatic coupon generation on Nth order
if (orderNumber % nthOrder == 0) {
    String couponCode = String.format("SAVE10-%03d", orderNumber);
    couponRepository.generate(orderNumber);
}
```

### Stock Management Rules
- **Validation**: Check stock before adding to cart
- **Reservation**: Decrease stock immediately upon cart addition
- **Thread Safety**: Synchronized stock operations
- **No Overselling**: Prevent negative stock levels

### Order Processing Flow
1. **Validate Cart**: Check all items exist and have sufficient stock
2. **Apply Coupon**: Validate and apply discount if coupon provided
3. **Calculate Total**: Compute final amount with discounts
4. **Create Order**: Snapshot cart as order record
5. **Update Inventory**: Decrease stock levels
6. **Generate Coupon**: Check if Nth order milestone reached
7. **Clear Cart**: Remove all items from user's cart

---

## 🤝 Contributing

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make your changes with proper tests
4. Run the test suite: `mvn test`
5. Commit your changes: `git commit -am 'Add new feature'`
6. Push to the branch: `git push origin feature/your-feature`
7. Submit a pull request

### Code Standards
- **Java**: Follow Spring Boot conventions
- **Testing**: Minimum 80% code coverage
- **Documentation**: Update README and API docs for changes
- **Commits**: Use conventional commit messages

### Testing Guidelines
- Write unit tests for all new business logic
- Include integration tests for API changes
- Test edge cases and error scenarios
- Ensure thread-safety for concurrent operations

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙋 Support

For questions or issues:
- Create an issue in the GitHub repository
- Check the [API Documentation](docs/API_CONTRACT.md) for detailed specifications
- Review [Design Notes](docs/DESIGN_NOTES.md) for implementation details

---

**Built with ❤️ using Spring Boot | Java 17 | Maven**
