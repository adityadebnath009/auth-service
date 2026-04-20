# BackendForge Project Outline

## 1. Project Summary

BackendForge is a full-stack learning platform centered on backend development concepts, with a strong emphasis on production-style authentication and account security.

At the moment, the repository is primarily organized around:

- a Spring Boot backend API
- a React frontend client
- a small secondary Spring Boot sample app in `DeveloperSecurity/`
- generated PDF summaries and utility scripts

## 2. High-Level Architecture

```text
backendforge/
├── src/main/java/...                    # Main Spring Boot backend
├── src/main/resources/                 # Backend configuration
├── backendforge-ui/                    # React + Vite frontend
├── DeveloperSecurity/                  # Separate sample Spring Boot app
├── scripts/                            # Helper scripts
├── output/pdf/                         # Generated PDF documentation
└── tmp/pdfs/                           # Rendered PDF preview images
```

### Runtime shape

```text
React client
    |
    | HTTP + Bearer token + refresh cookie
    v
Spring Boot API
    |
    +-- Spring Security
    +-- JWT authentication
    +-- OAuth2 login
    +-- JPA repositories
    v
MySQL
```

## 3. Main Backend Application

Entry point:

- `src/main/java/com/aditya/simple_web_app/web_app/WebAppApplication.java`

Core responsibilities:

- user registration and login
- JWT access token and refresh token handling
- Google and GitHub OAuth login
- email verification
- session management
- role-based authorization
- workspace creation and lookup
- legacy developer CRUD endpoints

### 3.1 Backend package map

#### `auth/`

This is the core domain of the current backend.

- `Domain/`
  - entities such as `User`, `Role`, `RefreshToken`, `EmailVerificationToken`, and `AuthProvider`
- `controller/`
  - `AuthController`: register, verify, login, refresh, logout, session endpoints
  - `UserController`: current authenticated user endpoint
  - `HomeController`: role-protected sample endpoints
- `dto/`
  - request and response payloads such as login, registration, sessions, paginated responses, and API errors
- `repository/`
  - JPA repositories for users, roles, refresh tokens, developers, and verification tokens
- `service/`
  - business logic for user registration, token lifecycle, OAuth user loading, user lookup, workspace creation, and developer operations
- `util/`
  - token service abstractions and implementations, JWT filter, event listeners, and refresh-token cleanup helpers

#### `config/`

Cross-cutting application configuration.

- `WebSecurityConfig`
  - configures stateless security, CORS, route access rules, OAuth2 login, JWT filter ordering, and role hierarchy
- `OAuthSuccessHandler`
  - finalizes OAuth logins and redirects the user back to the frontend
- `RoleInitializer`
  - seeds core roles
- `AsyncConfig`
  - async task executor support
- `StripeConfig`
  - Stripe client/config property support

#### `platform/workspace/`

Workspace domain for each user.

- `domain/Workspace`
- `repository/WorkspaceRepository`
- `dto/`
  - `PlanType`
  - `WorkspaceResponseDTO`
- `controller/WorkspaceController`

Behavior:

- a default workspace is created for a new user
- workspace slugs are generated from the user name or email
- authenticated users can fetch their current workspace

#### `legacy/`

Older developer-management API that still exists in the codebase.

- `DeveloperController`
- request/response records for create, update, and patch flows
- `Developer` model

Behavior:

- CRUD-style endpoints for developers
- method-level security
- paginated list response support

#### `common/`

Shared exception handling.

- custom exceptions such as `BadRequestException`, `ApiException`, and `ResourceNotFoundException`
- `GlobalHandleApiException` for centralized API error responses

#### `mapper/`

- `DeveloperMapper`

Used to map legacy developer entities into response DTOs.

## 4. Backend Request Flows

### Local auth flow

1. Client sends `POST /auth/register` or `POST /auth/login`
2. Spring Security authenticates credentials
3. Access token is returned in the response body
4. Refresh token is stored as an HTTP-only cookie
5. Later 401s trigger `POST /auth/refresh`
6. Refresh token rotation issues a new access token and cookie

### OAuth flow

1. Frontend redirects to `/oauth2/authorization/google` or `/oauth2/authorization/github`
2. Spring Security completes the provider login
3. `CustomOAuthUserService` resolves or creates the user
4. `OAuthSuccessHandler` issues the app token and redirects to the frontend callback

### Session management flow

1. Refresh tokens are stored per session in the database
2. `/auth/sessions` returns active sessions
3. `/auth/sessions/{sessionId}` revokes one session
4. `/auth/logout-all` revokes all refresh tokens for the user

## 5. Backend Configuration

Primary config file:

- `src/main/resources/application.properties`

Configured integrations and settings include:

- MySQL datasource
- JPA / Hibernate
- JWT secrets and token durations
- SMTP mail
- Google OAuth
- GitHub OAuth
- Redis host and port
- Stripe keys and price IDs

## 6. Frontend Application

Frontend root:

- `backendforge-ui/`

Stack:

- React 19
- Vite
- React Router
- Axios
- Framer Motion
- Lucide React

### 6.1 Frontend structure

- `src/main.jsx`
  - React bootstrap
- `src/App.jsx`
  - route setup and top-level auth gating
- `src/api.js`
  - Axios instance with:
  - bearer token injection from `localStorage`
  - refresh-on-401 behavior using cookies
- `src/components/Navbar.jsx`
  - top navigation
- `src/pages/`
  - `Login.jsx`: email/password + OAuth login entry
  - `Register.jsx`: account creation UI
  - `Dashboard.jsx`: active sessions dashboard
  - `Profile.jsx`: user profile and logout-all action
  - `OAuthCallback.jsx`: frontend callback handler after provider auth
- `src/index.css`
  - shared visual styling

### 6.2 Frontend responsibilities

- store the access token in `localStorage`
- send authenticated API requests to `http://localhost:8080`
- rely on browser-managed HTTP-only refresh cookies
- redirect unauthenticated users to login
- display sessions, profile data, and auth state

## 7. Secondary Project: `DeveloperSecurity/`

This is a separate Gradle-based Spring Boot application inside the same repository.

Current shape:

- standalone entry point
- its own Gradle wrapper and build files
- its own `application.properties`
- a minimal test class

At the moment, it reads more like an experimental or learning subproject than part of the main runtime path.

## 8. Scripts and Generated Assets

### Scripts

- `scripts/generate_project_summary_pdfs.py`
  - generates project summary PDFs

### Generated output

- `output/pdf/backendforge_project_summary.pdf`
- `output/pdf/backendforge_flow_guide.pdf`
- `tmp/pdfs/*.png`

These appear to be documentation artifacts rather than application runtime assets.

## 9. Build and Tooling

### Backend

- Maven wrapper: `mvnw`
- Java version: 21
- Spring Boot: 3.5.x

### Frontend

- npm + Vite
- ESLint for linting

### Secondary app

- Gradle wrapper inside `DeveloperSecurity/`

## 10. Recommended Mental Model

If you are new to this repository, the easiest way to think about it is:

1. `src/main/java/.../auth` is the heart of the system
2. `config/` defines how security and integrations are wired
3. `platform/workspace/` adds user workspace behavior on top of auth
4. `legacy/` contains an older secured CRUD slice
5. `backendforge-ui/` is the client for authentication and account/session UX
6. `DeveloperSecurity/` is separate from the main application flow

## 11. Best Starting Points For Exploration

If someone wants to understand the code quickly, start here:

- `README.md`
- `src/main/java/com/aditya/simple_web_app/web_app/config/WebSecurityConfig.java`
- `src/main/java/com/aditya/simple_web_app/web_app/auth/controller/AuthController.java`
- `src/main/java/com/aditya/simple_web_app/web_app/auth/service/UserRegistrationService.java`
- `src/main/java/com/aditya/simple_web_app/web_app/auth/service/RefreshTokenService.java`
- `backendforge-ui/src/App.jsx`
- `backendforge-ui/src/api.js`

