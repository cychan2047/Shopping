# Online Shopping Backend - Microservices Project

This project is the backend for an online shopping website, built using a modern microservices architecture. It includes four core services that work together to handle user accounts, product inventory, order processing, and payments. The entire system is containerized with Docker for easy, "one-click" deployment.

---

## Architecture Overview

The application is composed of four independent microservices. They communicate with each other using both synchronous (REST API) and asynchronous (event-driven) methods.

* **Account Service**: Manages all user-related data, including registration, updates, and authentication. It acts as the central authentication server, issuing JWTs for secure communication.
* **Item Service**: Manages the product catalog and inventory. It provides details about items and tracks available stock.
* **Order Service**: Orchestrates the creation and management of orders. It communicates with the Item Service to verify stock and publishes events to Kafka when an order is created or cancelled.
* **Payment Service**: Handles all payment-related logic. It listens for order creation events from Kafka to initiate payments and ensures idempotency to prevent duplicate charges.

---

## Tech Stack & Tools

This project utilizes a polyglot persistence approach, using the best database technology for each service's specific needs.

| Technology                  | Purpose                                                                    |
| :-------------------------- | :------------------------------------------------------------------------- |
| **Spring Boot** | Core framework for building all microservices.                             |
| **Spring Security** | Handles user authentication and secures endpoints with JWT.                |
| **Spring Data** | Simplifies database access for each service.                               |
| **Spring Cloud OpenFeign** | Used for declarative, synchronous REST API calls between services.         |
| **Kafka** | Used for asynchronous, event-driven communication between services.        |
| **MySQL** | Relational database for the Account and Payment services.                  |
| **MongoDB** | NoSQL document store for the Item Service.                                 |
| **Cassandra** | NoSQL wide-column store for the Order Service.                             |
| **Swagger (OpenAPI)** | Provides interactive API documentation for each microservice.              |
| **JUnit 5 & Mockito** | Used for unit and integration testing.                                     |
| **Jacoco** | Measures and reports test coverage.                                        |
| **Maven** | Project build and dependency management.                                   |
| **Docker & Docker Compose** | Containerizes all services and dependencies for a "one-click" run.         |

---

## 🚀 How to Run the Project

The entire application stack is managed by Docker Compose, making it easy to run with a single command.

### Prerequisites
* [Docker Desktop](https://www.docker.com/products/docker-desktop/) must be installed and running on your machine.

### "One-Click" Start
1.  Clone this repository to your local machine.
2.  Open a terminal and navigate to the root directory of the project (where the `docker-compose.yml` file is located).
3.  Run the following command:
    ```bash
    docker-compose up --build
    ```
This command will:
* Build the Docker images for all four microservices.
* Start containers for all services and their database dependencies (MySQL, MongoDB, Cassandra, and Kafka).
* Connect all containers to a shared network.

---

## Accessing Services & API Documentation

Once all containers are running, you can interact with each service through its dedicated port and view its interactive API documentation via Swagger.

* **Account Service**:
    * **Port**: `8081`
    * **Swagger UI**: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
* **Item Service**:
    * **Port**: `8082`
    * **Swagger UI**: [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)
* **Order Service**:
    * **Port**: `8083`
    * **Swagger UI**: [http://localhost:8083/swagger-ui.html](http://localhost:8083/swagger-ui.html)
* **Payment Service**:
    * **Port**: `8084`
    * **Swagger UI**: [http://localhost:8084/swagger-ui.html](http://localhost:8084/swagger-ui.html)