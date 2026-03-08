# BackendForge

> An AI-powered interactive learning platform for backend developers.
> Built with Java + Spring Boot. Teaches backend fundamentals through personalized learning paths, adaptive quizzes, and an AI chat assistant.

---

## What is BackendForge?

BackendForge was born from a real learning journey. While building a production-grade authentication system, the questions that naturally arose — *why refresh tokens? why hash them? why httpOnly cookies?* — turned into the foundation of the platform.

Most tutorials teach from an expert's perspective. BackendForge teaches from a **beginner's perspective** — showing the actual learning process, the questions that arise, and the real practices that production systems use.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3 |
| Security | Spring Security 6, JWT (RS256/HS256) |
| Auth | Custom JWT + Spring OAuth2 Client |
| Database | Spring Data JPA, Hibernate |
| Email | JavaMail (Gmail SMTP) |
| Frontend (planned) | React + Tailwind CSS |

---

## Architecture

```
┌─────────────────────────────────────────────────┐
│                   CLIENT                        │
│         Browser / React Frontend                │
└────────────────────┬────────────────────────────┘
                     │ HTTPS
┌────────────────────▼────────────────────────────┐
│              SPRING BOOT API                    │
│                                                 │
│  ┌─────────────┐    ┌──────────────────────┐   │
│  │ JwtAuthFilter│    │ OAuth2 Login Filter  │   │
│  └──────┬──────┘    └──────────┬───────────┘   │
│         │                      │                │
│  ┌──────▼──────────────────────▼───────────┐   │
│  │           Security Filter Chain         │   │
│  └──────────────────┬──────────────────────┘   │
│                     │                           │
│  ┌──────────────────▼──────────────────────┐   │
│  │              Controllers                │   │
│  │  AuthController  │  UserController      │   │
│  └──────────────────┬──────────────────────┘   │
│                     │                           │
│  ┌──────────────────▼──────────────────────┐   │
│  │               Services                  │   │
│  │  UserRegistrationService                │   │
│  │  RefreshTokenService                    │   │
│  │  CustomOAuthUserService                 │   │
│  └──────────────────┬──────────────────────┘   │
│                     │                           │
│  ┌──────────────────▼──────────────────────┐   │
│  │             Repository Layer            │   │
│  └──────────────────┬──────────────────────┘   │
└────────────────────┬────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────┐
│                 DATABASE                        │
│   users │ roles │ refresh_tokens │ sessions     │
└─────────────────────────────────────────────────┘
```

---

## Authentication System

BackendForge implements a production-grade, dual-entry authentication system.

### Two Entry Points, One JWT System

```
Local Login (email + password)  ─┐
                                  ├──→  YOUR JWT  ──→  All protected endpoints
OAuth Login (Google / GitHub)   ─┘
```

After authentication, both flows produce identical JWTs. The security filter chain treats them the same.

### Local Authentication Flow

```
POST /auth/register
  → validate email uniqueness
  → BCrypt hash password
  → assign ROLE_USER
  → send verification email
  → return 201

POST /auth/login
  → AuthenticationManager validates credentials
  → generate access token (15 min)
  → generate refresh token (7 days, hashed in DB)
  → set refresh token in httpOnly cookie
  → return access token in response body
```

### OAuth Authentication Flow (Google + GitHub)

```
GET /oauth2/authorization/google
  → Spring redirects to Google
  → user logs in on Google's servers (password never seen)
  → Google redirects back with auth code
  → Spring exchanges code for tokens (server-to-server)
  → CustomOAuthUserService extracts email, name, providerId
  → find existing user OR create new user in DB
  → OAuthSuccessHandler issues YOUR JWT
  → redirect to frontend with token in URL
```

### Account Linking

Same email across providers = same user account.

```
User registers with email+password using john@gmail.com
Later logs in with Google (same john@gmail.com)
→ Same account, no duplicate
```

### Token Architecture

| Token | Storage | Expiry | Purpose |
|---|---|---|---|
| Access Token | Memory (frontend) | 15 minutes | Authenticate API requests |
| Refresh Token | httpOnly Cookie | 7 days | Obtain new access token |

Refresh tokens are:
- Stored as SHA-256 hash in DB (never plaintext)
- Rotated on every use
- Revoked on reuse detection (full session invalidation)
- Tracked per device (User-Agent + IP)

### JWT Configuration

Two token service implementations are available:

- `Hs256TokenService` — HMAC-SHA256, symmetric key
- `Rs256TokenService` — RSA-SHA256, asymmetric key pair (recommended for production)

---

## API Endpoints

### Auth

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | Public | Register with email + password |
| POST | `/auth/login` | Public | Login, returns access token |
| POST | `/auth/refresh` | Cookie | Refresh access token |
| POST | `/auth/logout` | Bearer | Revoke current session |
| POST | `/auth/logout-all` | Bearer | Revoke all sessions |
| GET | `/oauth2/authorization/google` | Public | Initiate Google OAuth |
| GET | `/oauth2/authorization/github` | Public | Initiate GitHub OAuth |

### User

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/user/me` | Bearer | Get current user info |
| GET | `/user/sessions` | Bearer | List active sessions |
| DELETE | `/user/sessions/{id}` | Bearer | Revoke specific session |

### Email Verification

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/verify-email?token=` | Public | Verify email address |
| POST | `/verify-email/resend` | Bearer | Resend verification email |

---

## Security Design

### Why Short-Lived Access Tokens?

Access tokens expire in 15 minutes. If stolen via XSS, the attacker has a narrow window. The refresh token (safe in httpOnly cookie, unreachable by JavaScript) silently renews the access token.

### Why Hash Refresh Tokens?

If the database is compromised, hashed tokens are useless to an attacker — they cannot be replayed without the original value.

### Why Refresh Token Rotation?

Each use of a refresh token generates a new one and invalidates the old one. If a stolen token is used, the legitimate user's next request will detect the reuse and revoke all sessions for that user.

### Why httpOnly Cookies for Refresh Tokens?

JavaScript cannot access httpOnly cookies. XSS attacks that steal tokens from memory cannot steal the refresh token.

### Why SameSite=Strict for Local Login?

Prevents CSRF attacks — the cookie is only sent on same-site requests.

### Why SameSite=Lax for OAuth?

OAuth involves a cross-site redirect from Google back to your app. `Strict` would block the cookie during this redirect. `Lax` allows it while still protecting against most CSRF scenarios.

---

## Role Hierarchy

```
ROLE_ADMIN
    │
    └──→ ROLE_USER (inherits all USER permissions)
```

Roles are stored in a `roles` table and assigned via a `user_roles` join table. Role hierarchy is enforced via Spring Security's `RoleHierarchyImpl`.

---

## Database Schema

```
users
  id (UUID, BINARY 16)
  email (unique)
  password (nullable — null for OAuth users)
  name
  profile_picture
  bio
  provider (LOCAL / GOOGLE / GITHUB)
  provider_id
  enabled
  email_verified
  created_at
  updated_at

roles
  id
  name (ROLE_USER / ROLE_ADMIN)

user_roles
  user_id → users.id
  role_id → roles.id

refresh_tokens
  id
  session_id (UUID)
  token_hash (SHA-256)
  revoked
  expiry_date
  created_date
  user_id → users.id
  device_name
  user_agent
  ip_address

email_verification_tokens
  id
  token
  expiry_date
  user_id → users.id
```

---

## Environment Variables

```bash
# JWT
JWT_SECRET=your_secret_key

# Google OAuth
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

# GitHub OAuth
GITHUB_CLIENT_ID=your_github_client_id
GITHUB_CLIENT_SECRET=your_github_client_secret

# Email (Gmail SMTP)
MAIL_USERNAME=your_gmail
MAIL_PASSWORD=your_app_password
```

Set on macOS/Linux:
```bash
nano ~/.zshrc
# add export KEY=value lines
source ~/.zshrc
```

---

## Running Locally

```bash
# Clone
git clone https://github.com/yourusername/backendforge.git
cd backendforge

# Set environment variables (see above)

# Run
./mvnw spring-boot:run

# App starts at
http://localhost:8080
```

---

## Project Structure

```
src/main/java/com/aditya/simple_web_app/web_app/
├── Domain/
│   ├── User.java
│   ├── Role.java
│   ├── RefreshToken.java
│   ├── EmailVerificationToken.java
│   └── AuthProvider.java          (LOCAL / GOOGLE / GITHUB)
├── config/
│   ├── WebSecurityConfig.java     (SecurityFilterChain)
│   ├── OAuthSuccessHandler.java   (issues JWT after OAuth)
│   └── RoleInitializer.java
├── controller/
│   ├── AuthController.java
│   └── UserController.java
├── dto/
│   ├── OAuthUserInfo.java         (record)
│   └── ...
├── service/
│   ├── CustomUserDetails.java     (implements UserDetails + OAuth2User + OidcUser)
│   ├── CustomUserDetailsService.java
│   ├── CustomOAuthUserService.java
│   ├── OAuthUserInfoFactory.java  (normalizes Google/GitHub attributes)
│   ├── RefreshTokenService.java
│   └── UserRegistrationService.java
└── util/
    ├── JwtAuthFilter.java
    ├── TokenService.java          (interface)
    ├── Hs256TokenService.java
    └── Rs256TokenService.java
```

---

## Roadmap

- [x] JWT authentication (access + refresh tokens)
- [x] Refresh token rotation + reuse detection
- [x] Session management (per-device tracking)
- [x] Google + GitHub OAuth login
- [x] Email verification
- [x] Role hierarchy (ADMIN > USER)
- [ ] Learning agenda generator (AI)
- [ ] Adaptive MCQ quiz engine (AI)
- [ ] Article + content system
- [ ] AI chat assistant
- [ ] React + Tailwind frontend

---

## Origin

BackendForge started as a personal learning project. While building JWT authentication from scratch, the questions that came up — *why do we rotate refresh tokens? why not store them in localStorage? what's the actual difference between OAuth and OIDC?* — became the curriculum.

The goal: build a platform that teaches backend development the way it's actually learned — through building, questioning, and understanding the *why* behind every decision.

---

*Built by Aditya Debnath*
