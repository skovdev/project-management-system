# Authorization and Authentication

## Actor
New user

## Flow
1. POST /sign-up (register a new user)
2. POST /sign-in (log in a user and receive an access token)

## Preconditions
- User is not authenticated before sign-up or sign-in.

## Expected Result
- A new user account is created after successful sign-up.
- Successful sign-in returns an access token.
- The access token is required for all protected endpoints.

## Failure Cases
- Invalid credentials during sign-in → 401 Unauthorized
- Attempt to sign up with an existing email → 409 Conflict (or 400 Bad Request)

## Rules
- Authenticated user can access protected resources using a valid access token.
- Unauthenticated users can access only authentication endpoints.
