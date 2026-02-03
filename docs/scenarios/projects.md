# Projects

## Actor
Authenticated user

## Flow
1. POST /projects (create a project)
2. GET /projects (list my projects)
3. GET /projects/{id} (view project details)

## Preconditions
- User is authenticated.

## Expected Result
- A project is created and owned by the authenticated user.
- Listing projects returns only projects owned by the authenticated user.
- Project details are returned only if the project belongs to the authenticated user.

## Failure Cases
- Missing or invalid token → 401 Unauthorized
- Project not found or not owned by the user → 404 Not Found

## Rules
- User can see only their own projects.
