# Smart Retail Billing & Inventory System

## Enterprise Microservices Backend Project Specification

---

# 1. Project Overview

## Project Name
Smart Retail Billing & Inventory System

## Domain
Retail Chain Management / Point of Sale (POS)

## Objective
To build a scalable, enterprise-grade retail backend platform capable of handling:

- Barcode-based billing
- Multi-store inventory management
- Real-time stock synchronization
- GST invoice generation
- Sales analytics
- Role-based secure access
- Event-driven communication
- Distributed system consistency

The system is designed for supermarkets, retail chains, mini-marts, and wholesale stores.

---

# 2. Business Problem Statement

Retail stores commonly face:

- Inventory mismatch between branches
- Slow billing systems
- Lack of centralized analytics
- Stock shortages
- Manual invoice management
- Poor scalability
- No real-time synchronization

This project solves these problems using a distributed microservices architecture.

---

# 3. System Architecture

## Architecture Style
Microservices Architecture

## Communication Types

| Type | Usage |
|---|---|
| REST APIs | Synchronous communication |
| Kafka/RabbitMQ | Asynchronous event-driven communication |
| Redis | Caching |
| MySQL | Persistent storage |

---

# 4. Core Microservices

---

## 4.1 API Gateway Service

### Responsibilities
- Central entry point
- Route requests to services
- JWT validation
- Rate limiting
- Request logging
- Load balancing

### Technologies
- Spring Cloud Gateway
- Redis Rate Limiter
- Keycloak Integration

### Endpoints

| Endpoint | Route |
|---|---|
| `/auth/**` | Auth Service |
| `/products/**` | Product Service |
| `/inventory/**` | Inventory Service |
| `/orders/**` | Billing Service |
| `/analytics/**` | Analytics Service |

---

## 4.2 Auth Service

### Responsibilities
- User authentication via Keycloak OIDC
- JWT token management and signature validation
- Centralized role-based authorization (RBAC) in Keycloak
- User profile management

### Roles
- ADMIN
- CASHIER
- STORE_MANAGER

### Features
- Login
- Refresh token
- User registration
- Role assignment

### Technologies
- Keycloak (Authentication & Authorization Provider)
- Spring Security (OAuth2 Resource Server)
- JWT

---

## 4.3 Product Service

### Responsibilities
- Product catalog management
- Barcode management
- Product search
- Category management

### Features
- Add/Edit/Delete products
- Barcode lookup
- Product pricing
- GST percentage handling

### Database Tables
- products
- categories
- barcodes

### Sample Product Fields

| Field | Type |
|---|---|
| id | UUID |
| barcode | String |
| name | String |
| price | Decimal |
| gstPercentage | Decimal |
| categoryId | UUID |

---

## 4.4 Inventory Service

### Responsibilities
- Store inventory tracking
- Stock updates
- Low stock monitoring
- Warehouse synchronization

### Features
- Add stock
- Reduce stock after billing
- Transfer stock between stores
- Inventory audit logs

### Event Handling

Consumes:
- `ORDER_CREATED`
- `PRODUCT_UPDATED`

Publishes:
- `LOW_STOCK_ALERT`
- `STOCK_UPDATED`

### Database Tables
- inventory
- stock_transactions
- warehouses

---

## 4.5 Billing / Order Service

### Responsibilities
- POS billing
- GST invoice generation
- Order creation
- Payment tracking

### Features
- Barcode-based cart
- Multiple payment methods
- Invoice PDF generation
- Order history

### Payment Methods
- Cash
- UPI
- Card

### Event Publishing

Publishes:
- `ORDER_CREATED`
- `PAYMENT_COMPLETED`

### Database Tables
- orders
- order_items
- invoices
- payments

---

## 4.6 Notification Service

### Responsibilities
- Send notifications
- Alert management
- Email/SMS integrations

### Features
- Low stock alerts
- Invoice notifications
- Daily report emails

### Event Consumption

Consumes:
- `LOW_STOCK_ALERT`
- `ORDER_CREATED`

### Notification Channels
- Email
- SMS
- Push Notifications

---

## 4.7 Analytics Service

### Responsibilities
- Daily sales analytics
- Revenue calculations
- Product performance analysis

### Features
- Best-selling products
- Store-wise revenue
- Sales trends
- Tax reports

### Technologies
- MySQL
- Redis
- Kafka Consumers

### Reports

| Report | Description |
|---|---|
| Daily Sales | Total sales/day |
| GST Report | Tax collected |
| Product Trends | Best sellers |
| Store Revenue | Revenue per branch |

---

# 5. High-Level Workflow

## Billing Workflow

1. Cashier scans barcode
2. Product Service returns product details
3. Billing Service creates order
4. Inventory Service reduces stock
5. Invoice generated
6. Analytics updated
7. Notification sent

---

# 6. Distributed System Concepts

---

## 6.1 Distributed Transactions

### Problem
Order creation and inventory deduction occur in different services.

### Solution
Saga Pattern

### Flow
1. Order Created
2. Inventory Reserved
3. Payment Completed
4. Order Confirmed

### Compensation
If inventory fails:
- Cancel order
- Rollback payment

---

## 6.2 Event-Driven Communication

### Broker
Kafka / RabbitMQ

### Events

| Event | Producer | Consumer |
|---|---|---|
| ORDER_CREATED | Billing | Inventory, Analytics |
| LOW_STOCK_ALERT | Inventory | Notification |
| PRODUCT_UPDATED | Product | Inventory |

---

## 6.3 Caching

### Redis Usage
- Product lookup cache
- Frequently scanned barcodes
- User sessions
- Rate limiting

### Benefits
- Faster billing
- Reduced DB load
- Better scalability

---

## 6.4 Inventory Consistency

### Challenges
- Multiple billing counters
- Simultaneous purchases
- Overselling

### Solutions
- Optimistic locking
- Row versioning
- Atomic stock updates

---

# 7. Security Requirements

## Authentication & Authorization (Keycloak)
Centralized identity, authentication, and authorization are managed by **Keycloak** (acting as the Identity Provider).

### Authentication
- OpenID Connect (OIDC) token-based flow.
- Clients authenticate against Keycloak to receive signed JWT (JSON Web Tokens).

### Authorization
- Role-Based Access Control (RBAC) configured and managed directly within Keycloak client scopes/roles.
- Keycloak roles (`ADMIN`, `CASHIER`, `STORE_MANAGER`) are embedded in the access token.
- Microservices validate JWT signatures and extract roles to enforce path-based and method-based access control.

### Access Matrix

| Feature | Admin | Cashier |
|---|---|---|
| Manage Products | Yes | No |
| Billing | Yes | Yes |
| Analytics | Yes | No |
| User Management | Yes | No |

---

# 8. Database Design

## Databases Per Service

| Service | Database |
|---|---|
| Product | MySQL |
| Inventory | MySQL |
| Billing | MySQL |
| Analytics | MySQL |

### Design Principle
Database-per-service pattern.

---

# 9. Non-Functional Requirements

| Requirement | Goal |
|---|---|
| Availability | 99.9% |
| Scalability | Horizontal |
| Security | JWT + HTTPS |
| Response Time | < 300ms |
| Fault Tolerance | Retry + Circuit Breaker |

---

# 10. DevOps & Deployment

## Containerization
Docker

## Orchestration
Docker Compose / Kubernetes

## CI/CD
GitHub Actions / Jenkins

## Monitoring
- Prometheus
- Grafana

## Logging
- ELK Stack
- Loki

---

# 11. Suggested Folder Structure

```text
smart-retail-system/
│
├── api-gateway/
├── auth-service/
├── product-service/
├── inventory-service/
├── billing-service/
├── notification-service/
├── analytics-service/
│
├── docker-compose.yml
├── kafka/
├── monitoring/
└── docs/

# 12. API Examples

## Product API

### Add Product

```http
POST /products
```

### Get Product by Barcode

```http
GET /products/barcode/{barcode}
```

---

## Billing API

### Create Order

```http
POST /orders
```

### Generate Invoice

```http
GET /orders/{id}/invoice
```

---

# 13. Deliverables

---

# 5-Day Development Plan

---

## Day 1 — Core Backend Setup

### Deliverables
- Microservices project setup
- Eureka Discovery Server
- API Gateway configuration
- MySQL setup
- Keycloak authentication integration
- Common configuration setup
- Docker base configuration

---

## Day 2 — Product & Inventory Services

### Deliverables
- Product Service CRUD APIs
- Barcode support implementation
- Category management
- Inventory Service setup
- Stock tracking APIs
- Inventory database design
- Redis caching integration

---

## Day 3 — Billing & Order Management

### Deliverables
- POS/Billing APIs
- Barcode billing workflow
- Order management APIs
- GST invoice generation
- Payment handling module
- Inventory deduction logic
- Role-based access implementation

---

## Day 4 — Event-Driven Communication & Analytics

### Deliverables
- Kafka/RabbitMQ setup
- Async communication between services
- Saga pattern implementation
- Analytics Service development
- Daily sales reporting
- Revenue analytics
- Low stock event processing
- Notification Service integration

---

## Day 5 — DevOps, Monitoring & Finalization

### Deliverables
- Dockerization of all services
- Docker Compose setup
- CI/CD pipeline configuration
- Prometheus & Grafana monitoring
- Centralized logging setup
- API testing & bug fixing
- Performance optimization
- Final project documentation

---
# 14. Optional Advanced Features

- AI-based sales prediction
- Demand forecasting
- QR-code billing
- Multi-tenant retail chains
- Offline billing sync
- Voice-assisted billing
- Fraud detection

---

# 15. Recommended Tech Stack

| Layer | Technology |
|---|---|
| Backend | Spring Boot |
| API Gateway | Spring Cloud Gateway |
| Discovery | Eureka |
| Messaging | Kafka / RabbitMQ |
| Authentication | Keycloak |
| Database | MySQL |
| Cache | Redis |
| Monitoring | Prometheus + Grafana |
| Logging | ELK Stack |
| Containerization | Docker |
| CI/CD | GitHub Actions |

---

# 16. Resume Description

Developed an enterprise-grade Smart Retail Billing & Inventory System using Spring Boot microservices architecture with API Gateway, Eureka, Kafka, Redis, MySQL, and Keycloak. Implemented barcode-based billing, inventory consistency management, event-driven communication, distributed transactions using Saga Pattern, GST invoice generation, analytics dashboards, and secure role-based authentication.

---

# 17. Expected Learning Outcomes

By completing this project, you will learn:

- Enterprise microservices architecture
- Event-driven systems
- Distributed transactions
- Spring Cloud ecosystem
- Secure authentication
- Real-time inventory consistency
- Redis caching
- Kafka integration
- Docker deployment
- Monitoring and observability