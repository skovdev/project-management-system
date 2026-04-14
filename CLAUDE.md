# Project Overview

* Java 21, Spring Boot 3.3.2, Spring Cloud 4.1.3
* Microservices architecture (8 services)
* Communication: OpenFeign (sync), Kafka Saga (async)
* Security: JWT via api-gateway

# Modules

* auth-service, user-service, project-service, task-service
* ai-service, api-gateway, service-discovery, config-server

# Build & Run

* Build all: mvn clean install
* Test single module: mvn test -pl {module}
* Prefer running tests only for affected modules
* Build Docker: bash build-all-docker-images.sh
* Run system: docker-compose up

# Architecture Rules

* Base package: local.pms.{service-name}
* Strict layering: controller → service → repository (no skipping)
* No business logic in controllers
* Inter-service sync: OpenFeign
* Async workflows: Kafka Saga with compensating transactions
* Each service owns its database (no cross-service DB access)

# Security Rules

* JWT enforced at api-gateway
* Downstream services must trust and validate authUserId from JWT
* All endpoints must enforce resource ownership using authUserId
* Never bypass authentication or authorization checks

# Naming Conventions

* Controllers must end with RestController
* Services must use interface + ServiceImpl
* Repositories must end with Repository
* MapStruct mappers must end with Mapper
* Kafka listeners must end with Consumer
* Kafka producers must end with Producer

# Coding Standards

* Always use DTOs for request/response
* Use MapStruct for entity ↔ DTO mapping (no manual mapping)
* Use Bean Validation (@Valid) on all controller inputs
* Global exception handling via GlobalExceptionHandler per service
* Prefer immutability
* Lombok allowed only for @RequiredArgsConstructor (avoid overuse)

# Testing

* Controllers: MockMvc
* Services: Mockito
* Repository integration tests: Testcontainers
* Do not skip tests unless explicitly requested

# Constraints — Do Not Change

* Do not modify service-discovery or config-server internals
* Do not introduce direct DB access from api-gateway
* Do not add @Transactional to controller layer
* Do not break JWT validation or ownership rules
* Keep Saga compensation logic in kafka/saga/consumer packages

# Change Scope Rules

* Do not refactor across multiple services unless explicitly requested
* Do not introduce new frameworks or libraries without approval
* Keep changes minimal and localized
* Preserve public API contracts

# Execution Rules (IMPORTANT)

* First analyze → then explain plan → then implement
* Prefer smallest working solution over large refactors
* Ask before:

    * modifying multiple services
    * changing database schema
    * altering API contracts
    * introducing new infrastructure

# Performance & Reliability

* Use Resilience4j for fault tolerance (retry, circuit breaker)
* Avoid blocking calls inside async/Kafka flows
* Ensure idempotency in Saga consumers

# AI-Service Rules

* Use OpenAI Java SDK only inside ai-service
* Do not introduce AI logic into other services

# Notes

* If unsure, ask instead of assuming
