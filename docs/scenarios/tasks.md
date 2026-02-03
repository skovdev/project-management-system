# Tasks

## Actor
Authenticated user

## Flow
1. POST /projects/{id}/tasks (create a task under a project)
2. GET /projects/{id}/tasks (list tasks for a project)
3. GET /projects/{id}/tasks/{taskId} (view task details for specified project)
4. DELETE /projects/{id}/tasks/{taskId} (delete a task for specified project)
5. PATCH /projects/{id}/tasks/{taskId} (update task details for specified project)

## Preconditions
- User is authenticated.
- Project exists and is owned by the authenticated user.

## Expected Result
- A task is created under the specified project.
- Listing tasks returns only tasks belonging to the specified project.
- Project details are returned only if the project belongs to the authenticated user.
- Task can be deleted or updated only if it belongs to the specified project owned by the authenticated user.

## Failure Cases
- Missing or invalid token → 401 Unauthorized
- Project not found or not owned by the user → 404 Not Found

## Rules
- Tasks can be created and viewed only within projects owned by the authenticated user.
