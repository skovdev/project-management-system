# API v1

## Authentication

- **Sign up (register a new user)**  
  `POST /api/v1/auth/sign-up`

- **Sign in (log in user)**  
  `POST /api/v1/auth/sign-in`

---

## Projects

- **Find all my projects**  
  `GET /api/v1/projects`

- **Create a new project**  
  `POST /api/v1/projects`

- **Find project details by project identifier**  
  `GET /api/v1/projects/{projectId}`

## Tasks

- **Create a new task**  
  `POST /api/v1/projects/{projectId}/tasks`

- **Get all tasks in project**  
  `GET /api/v1/projects/{projectId}/tasks`

- **Get task details**  
  `GET /api/v1/projects/{projectId}/tasks/{taskId}`

- **Delete a task**  
  `DELETE /api/v1/projects/{projectId}/tasks/{taskId}`

- **Update task details**  
  `PATCH /api/v1/projects/{projectId}/tasks/{taskId}`
