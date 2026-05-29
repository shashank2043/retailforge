# Smart Retail Billing & Inventory System (RetailForge)

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.x-brightgreen)](https://spring.io/projects/spring-boot)
[![Microservices](https://img.shields.io/badge/Architecture-Microservices-blue)](https://microservices.io/)
[![Java](https://img.shields.io/badge/Java-21%2B-orange)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/Database-MySQL-blue)](https://www.mysql.com/)
[![Kafka](https://img.shields.io/badge/Messaging-Apache%20Kafka-black)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Cache-Redis-red)](https://redis.io/)
[![Keycloak](https://img.shields.io/badge/Security-Keycloak-yellow)](https://www.keycloak.org/)
[![Zipkin](https://img.shields.io/badge/Tracing-Zipkin-orange)](https://zipkin.io/)

RetailForge is an enterprise-grade, scalable retail backend platform designed for supermarkets, retail chains, and wholesale stores. It leverages a microservices architecture to handle high-volume billing, real-time inventory synchronization, and complex sales analytics.

## 🚀 Key Features

- **Barcode-based Billing:** Rapid POS checkout with sub-50ms product lookup using Redis caching.
- **Real-time Inventory Management:** Atomic stock updates with optimistic locking to prevent overselling across multiple counters.
- **GST-Compliant Invoicing:** Automated PDF generation of tax invoices with detailed GST breakdowns.
- **Event-Driven Architecture:** Asynchronous communication via Kafka for decoupled service interactions.
- **Distributed Tracing:** Full request lifecycle visibility across microservices using Micrometer Tracing and Zipkin.
- **Sales Analytics:** Real-time dashboards for revenue, tax collection, and product performance trends.
- **Distributed Consistency:** Implementation of the Saga Pattern to maintain data integrity across services.
- **Secure Access Control:** Centralized authentication and role-based authorization (RBAC) via Keycloak.
- **Resilient Infrastructure:** Centralized configuration, service discovery, and circuit breaker patterns.

## 🏗 System Architecture

The system follows a microservices architecture pattern, with each service responsible for a specific business domain.

### Core Services

| Service | Port | Responsibility |
| :--- | :--- | :--- |
| **Eureka Server** | 8761 | Service registration and discovery. |
| **Config Server** | 8888 | Centralized external configuration management. |
| **API Gateway** | 8081 | Central entry point, JWT validation, and rate limiting. |
| **Auth Service** | - | Identity management and RBAC via Keycloak. |
| **Product Service** | 8082 | Product catalog, categories, and barcode management. |
| **Inventory Service** | 8083 | Stock tracking, warehouse management, and low-stock alerts. |
| **Billing Service** | 8084 | POS billing, order processing, and invoice generation. |
| **Notification Service** | 8085 | Email and SMS notifications for orders and alerts. |
| **Analytics Service** | 8086 | Real-time sales metrics and reporting. |

## 🛠 Tech Stack

- **Backend:** Spring Boot, Spring Cloud (Gateway, Eureka, Config)
- **Messaging:** Apache Kafka
- **Caching:** Redis
- **Database:** MySQL
- **Identity Provider:** Keycloak (OIDC/JWT)
- **Distributed Tracing:** Zipkin
- **Containerization:** Docker & Docker Compose
- **Monitoring:** Prometheus & Grafana
- **Build Tool:** Gradle

## 🚦 Prerequisites

Ensure you have the following installed and running on their default ports:
- **Java 21+**
- **MySQL** (Default: 3306)
- **Redis** (Default: 6379)
- **Apache Kafka** (Default: 9092)
- **Keycloak** (Default: 8080)
- **Zipkin** (Default: 9411)
- **Gradle**

## 🏁 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/shashank2043/retailforge.git
cd retailforge
```

### 2. Infrastructure Setup
Ensure MySQL, Redis, Kafka, and Keycloak are active. Create the necessary databases for each microservice if not already handled by migration scripts.

### 3. Build the services
```bash
./gradlew build
```

### 4. Run all services
On Windows, you can use the provided interactive bootstrapper:
```bash
start-all.bat
```
Alternatively, start the services in the following order:
1. `eureka-server`
2. `config-server`
3. `api-gateway`
4. Other microservices (`product`, `inventory`, `billing`, etc.)

## 📖 Documentation

- [Detailed Project Specification](ProjectDocument.md)
- [User Stories & Acceptance Criteria](UserStories.md)

## 🔒 Security

Authentication and Authorization are managed through Keycloak. 
- Roles: `ADMIN`, `CASHIER`, `STORE_MANAGER`.
- API requests must include a valid Bearer JWT token in the `Authorization` header.

## 📈 Monitoring

- **Eureka Dashboard:** [http://localhost:8761](http://localhost:8761)
- **Zipkin Tracing:** [http://localhost:9411](http://localhost:9411)
- **Prometheus Metrics:** [http://localhost:9090](http://localhost:9090) (if configured)
- **Grafana Visualization:** [http://localhost:3000](http://localhost:3000) (if configured)

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
