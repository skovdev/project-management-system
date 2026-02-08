# API v1

## Authentication

- **Sign up (register a new user)**  
  `POST /api/v1/auth/sign-up`

- **Sign in (log in user)**  
  `POST /api/v1/auth/sign-in`

---

## Projects

- **Create a new project**  
  `POST /api/v1/projects`

- **Find all my projects**  
  `GET /api/v1/projects`

- **Find project details by project identifier**  
  `GET /api/v1/projects/{projectId}`

## Tasks

- **Create a new task in a project**  
  `POST /api/v1/projects/{projectId}/tasks`

- **Find all tasks in a project**  
  `GET /api/v1/projects/{projectId}/tasks`

- **Find task details**  
  `GET /api/v1/tasks/{taskId}`

- **Update task details**  
  `PUT /api/v1/tasks/{taskId}`

- **Delete a task**  
  `DELETE /api/v1/tasks/{taskId}`
