# Modules

This document describes the main logical modules of the system and
their responsibilities. Modules are defined by behavior and ownership,
not by implementation details.

---

## Authentication Module

**Responsibility**
- User registration and authentication
- Issuing and validating access tokens

**Main capabilities**
- Register a new user
- Authenticate a user and issue an access token
- Validate access tokens for protected operations

**Related scenarios**
- [authorization-authentication.md](authorization-authentication.md)

---

## Projects Module

**Responsibility**
- Managing user-owned projects
- Enforcing project ownership and access rules

**Main capabilities**
- Create a project
- List projects owned by a user
- Retrieve project details for the owner

**Related scenarios**
- [projects.md](scenarios/projects.md)

---

## Tasks Module

**Responsibility**
- Managing tasks within projects
- Enforcing task access through project ownership

**Main capabilities**
- Create tasks under a project
- List tasks for a project
- Retrieve task details for a project
- Update an existing task
- Delete an existing task

**Related scenarios**
- [tasks.md](scenarios/tasks.md)