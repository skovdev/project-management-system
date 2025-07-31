# Project Management System

Pet project for self-education, designed to simulate a real-world project management platform.

## Overview

The **Project Management System** is a microservices-based application built with Java, Spring Boot, Docker, and a variety of cloud-native technologies.  
It demonstrates authentication, user management, project and task tracking, service discovery, centralized configuration, and communication between services.

## Architecture

- **Backend Microservices**:
  - `auth-service`: Manages authentication and JWT token issuance.
  - `user-service`: Manages users' personal data and roles.
  - `project-service`: Handles project-related operations.
  - `task-service`: Manages tasks linked to projects.
  - `ai-service`: Provides AI-powered assistance to enhance project management experiences.
- **Infrastructure**:
  - `api-gateway`: Entry point for all client requests (via Spring Cloud Gateway).
  - `service-discovery`: Service registry and discovery (Eureka).
  - `config-server`: Centralized configuration server for microservices.
  - `postgresql`: Database for persisting data.
- **Frontend**:
  - `app-client`: Client application for interacting with the backend.

## Technologies

- Java 21
- Spring Boot
- Spring Cloud
- Spring Security with JWT
- PostgreSQL
- Docker, Docker Compose, Docker Swarm
- Shell scripts for deployment automation

## Author

Created by [Stanislav Kovalenko](https://github.com/skovdev) for educational purposes and personal growth.
