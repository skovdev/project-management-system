# Project Management System

A pet project for self-education, simulating a real-world project management platform built with microservices.

## Overview

The **Project Management System** is a microservices-based application built with Java 21, Spring Boot 3, Spring Cloud, Kafka, and PostgreSQL. It demonstrates JWT authentication, user management, project and task tracking, AI-assisted features, service discovery, centralized configuration, synchronous (OpenFeign) and asynchronous (Kafka Saga) inter-service communication, and fault tolerance via Resilience4j.

## Architecture

```
app-client (Angular)
       │
       ▼
  api-gateway  ──── JWT validation ──► downstream services
       │
       ├── auth-service      (authentication, token issuance)
       ├── user-service       (user profile management)
       ├── project-service    (project CRUD)
       ├── task-service       (task CRUD, linked to projects)
       └── ai-service         (AI-powered project assistance)

Infrastructure:
  service-discovery  (Eureka)
  config-server      (Spring Cloud Config)
  postgresql         (shared DB host, each service owns its schema)
  kafka              (async Saga workflows)
```

### Services

| Service | Responsibility | Port |
|---|---|---|
| `api-gateway` | Entry point, JWT enforcement, load balancing | 8762 |
| `service-discovery` | Eureka service registry | 8761 |
| `config-server` | Centralized configuration | 8888 |
| `auth-service` | Registration, login, JWT issuance | — |
| `user-service` | User profile data and roles | — |
| `project-service` | Project management | — |
| `task-service` | Task management within projects | — |
| `ai-service` | AI-powered assistance (OpenAI) | — |
| `postgresql` | Persistence | 5432 |
| `kafka` | Async messaging (KRaft mode) | 9092 |

## Technology Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.3.2, Spring Cloud 4.1.3 |
| Security | Spring Security 6, JWT (jjwt 0.12.6) |
| Persistence | Spring Data JPA, PostgreSQL 42.7.4 |
| Messaging | Apache Kafka (Spring Cloud Stream) |
| Service Mesh | Eureka, Spring Cloud Config, Spring Cloud Gateway |
| Sync Communication | OpenFeign |
| Fault Tolerance | Resilience4j 2.3.0 |
| Mapping | MapStruct 1.6.2 |
| API Docs | SpringDoc OpenAPI 2.6.0 |
| AI | OpenAI Java SDK 2.12.0 |
| Testing | JUnit 5, Mockito, Testcontainers 1.21.0 |
| Build | Maven |
| Deployment | Docker, Docker Compose, Docker Swarm |

## API Reference (v1)

All endpoints are routed through `api-gateway` at port `8762`. JWT is required for all endpoints except auth.

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/auth/sign-up` | Register a new user |
| `POST` | `/api/v1/auth/sign-in` | Log in and receive a JWT |

### Projects

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/projects` | Create a new project |
| `GET` | `/api/v1/projects` | List all projects for the authenticated user |
| `GET` | `/api/v1/projects/{projectId}` | Get project by ID |

### Tasks

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/projects/{projectId}/tasks` | Create a task in a project |
| `GET` | `/api/v1/projects/{projectId}/tasks` | List all tasks in a project |
| `GET` | `/api/v1/tasks/{taskId}` | Get task by ID |
| `PUT` | `/api/v1/tasks/{taskId}` | Update a task |
| `DELETE` | `/api/v1/tasks/{taskId}` | Delete a task |

### Users

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/users/{id}` | Get user profile |
| `PUT` | `/api/v1/users/{id}` | Update user profile |
| `DELETE` | `/api/v1/users/{id}` | Delete user account |

## Build & Run

### Prerequisites

- Java 21
- Maven
- Docker & Docker Compose

### Build all Docker images

```bash
bash build-all-docker-images.sh
```

### Run the system

```bash
docker-compose up
```

### Build all modules (without Docker)

```bash
mvn clean install
```

### Run tests for a single module

```bash
mvn test -pl auth-service
```

## Project Structure

```
project-management-system/
├── api-gateway/
├── auth-service/
├── user-service/
├── project-service/
├── task-service/
├── ai-service/
├── service-discovery/
├── config-server/
├── app-client/          # Angular frontend
├── postgresql/          # DB Dockerfile & init scripts
├── spring-config/       # Externalized config files
├── docs/                # API docs and scenarios
├── postman/             # Postman collection
├── docker-compose.yml
└── build-all-docker-images.sh
```

## Author

Created by [Stanislav Kovalenko](https://github.com/skovdev) for educational purposes and personal growth.
