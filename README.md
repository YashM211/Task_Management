# Task Management System

A stateless REST API for managing tasks and collaboration between
registered users. The application supports account registration,
JWT-based authentication, task ownership and assignment, task-status
updates, and comments.

> **Project status:** This is an individual backend portfolio project
> demonstrating layered application design, API contracts,
> authentication and authorization, relational persistence, and
> documented engineering trade-offs. It is not presented as a
> production-ready service. Known limitations and planned improvements
> are listed below.

## Table of contents

-   [Features](#features)
-   [Architecture](#architecture)
-   [Domain model](#domain-model)
-   [Technology stack](#technology-stack)
-   [Getting started](#getting-started)
-   [API reference](#api-reference)
-   [Example workflow](#example-workflow)
-   [Response examples](#response-examples)
-   [Testing](#testing)
-   [Known limitations](#known-limitations)
-   [Roadmap](#roadmap)
-   [License](#license)

## Features

-   **Account registration:** validates email and password input,
    normalizes email addresses to lowercase, rejects duplicate accounts,
    and stores BCrypt password hashes.
-   **Stateless authentication:** exchanges HTTP Basic credentials for
    an RSA-signed JWT and accepts the token as a bearer credential on
    protected routes.
-   **Task management:** creates tasks and lists them in descending ID
    order, with optional author and assignee filters.
-   **Task assignment:** allows a task's author to assign or unassign a
    registered user.
-   **Task lifecycle:** allows the author or assigned user to change a
    task's status.
-   **Comments:** allows authenticated users to add comments to tasks
    and retrieve comment history.
-   **Request validation:** uses Jakarta Bean Validation for request
    payloads.
-   **Centralized error handling:** maps validation and domain failures
    to HTTP responses through a global exception handler.

## Architecture

The application is a single Spring Boot service organized around feature
packages (`auth`, `user`, and `task`) with shared security,
configuration, and exception-handling components.

``` mermaid
flowchart LR
    Client["API client"] --> Security["Spring Security filter chain"]

    subgraph Application["Spring Boot application"]
        Security --> Controllers["REST controllers"]
        Controllers --> DTOs["Request / response records"]
        Controllers --> Services["Application services"]
        Services --> Repositories["Spring Data repositories"]
        Security --> UserDetails["CustomUserDetailsService"]
        UserDetails --> UserRepository["UserRepository"]
        Security --> Decoder["JWT decoder"]
        AuthController["AuthController"] --> Encoder["JWT encoder"]
        Encoder --> RSA["Runtime RSA-2048 key pair"]
    end

    Repositories --> H2[("File-backed H2 database")]
    UserRepository --> H2
```

### Request lifecycle

1.  The Spring Security filter chain authenticates the request using
    HTTP Basic credentials or a bearer JWT.
2.  The controller validates the incoming payload and obtains caller
    information from the authenticated principal when required.
3.  The service layer coordinates the operation and applies task
    ownership and assignment rules.
4.  Spring Data repositories load and persist JPA entities.
5.  Controllers return explicit response records rather than exposing
    JPA entities directly.
6.  `GlobalExceptionHandler` translates validation and domain errors
    into HTTP responses.

### Authentication flow

``` mermaid
sequenceDiagram
    actor Client
    participant Security as Spring Security
    participant Users as UserService
    participant DB as H2
    participant Auth as AuthController
    participant JWT as Nimbus JWT encoder/decoder
    participant Tasks as Task API

    Client->>Security: POST /api/accounts
    Security->>Users: Registration request
    Users->>Users: Normalize email and hash password
    Users->>DB: Persist user
    DB-->>Users: Saved user
    Users-->>Client: Account response

    Client->>Security: POST /api/auth/token (Basic credentials)
    Security->>DB: Load user
    DB-->>Security: Password hash and role
    Security->>Security: Verify password
    Security->>Auth: Authenticated principal
    Auth->>JWT: Sign claims with runtime RSA private key
    JWT-->>Client: JWT response

    Client->>Security: Protected request (Bearer JWT)
    Security->>JWT: Verify signature and expiration
    JWT-->>Security: Authenticated subject
    Security->>Tasks: Forward authenticated request
    Tasks->>DB: Execute task operation
    DB-->>Tasks: Persisted state
    Tasks-->>Client: API response
```

The issued token currently expires **60 seconds** after creation.
Although `jwt.expiration-minutes=60` exists in configuration, token
generation does not yet use that property. This discrepancy is
documented as a known limitation.

## Domain model

``` mermaid
erDiagram
    APP_USER ||--o{ TASK : authors
    APP_USER o|--o{ TASK : assigned_to
    APP_USER ||--o{ COMMENT : writes
    TASK ||--o{ COMMENT : contains

    APP_USER {
        Long id PK
        String email UK
        String password
        String role
    }

    TASK {
        Long id PK
        String title
        String description
        String status
        Long author_id FK
        Long assignee_id FK
    }

    COMMENT {
        Long id PK
        String text
        Long task_id FK
        Long author_id FK
    }
```

  -----------------------------------------------------------------------
  Entity                  Responsibility          Constraints / behavior
  ----------------------- ----------------------- -----------------------
  `AppUser`               Authentication identity Unique email, BCrypt
                          and task participant    password hash, default
                                                  `USER` role

  `Task`                  Work item created by an Required title,
                          author                  description, author,
                                                  and initial `CREATED`
                                                  status; optional
                                                  assignee

  `Comment`               Discussion attached to  Belongs to one task and
                          a task                  one author
  -----------------------------------------------------------------------

Supported task statuses are `CREATED`, `IN_PROGRESS`, and `COMPLETED`.
The API uses the `TaskStatus` enum, while the entity currently persists
the status as a string. Persisting the enum explicitly is a planned
type-safety improvement.

## Technology stack

  -----------------------------------------------------------------------
  Area                                Technology
  ----------------------------------- -----------------------------------
  Language                            Java 25

  Framework                           Spring Boot 4.0.6, Spring Web

  Build                               Gradle 9.5.1 wrapper

  Security                            Spring Security, HTTP Basic, OAuth2
                                      Resource Server, Nimbus JWT,
                                      RSA-2048, BCrypt

  API contracts                       Java records, Jakarta Bean
                                      Validation

  Persistence                         Spring Data JPA, Hibernate,
                                      file-backed H2

  Error handling                      `@RestControllerAdvice`, structured
                                      `ErrorResponse` record

  Tests                               JUnit 5, Spring Boot Test
  -----------------------------------------------------------------------

## Getting started

### Prerequisites

-   JDK 25
-   Port `8080` available locally

The repository includes the Gradle wrapper, so a separate Gradle
installation is not required. No external database is needed for the
current H2-backed configuration.

### 1. Run the tests

**macOS / Linux**

``` bash
./gradlew test
```

**Windows PowerShell**

``` powershell
.\gradlew.bat test
```

### 2. Start the application

**macOS / Linux**

``` bash
./gradlew bootRun
```

**Windows PowerShell**

``` powershell
.\gradlew.bat bootRun
```

The API starts at `http://localhost:8080`.

### 3. Inspect the development database

The H2 Console is available at `http://localhost:8080/h2-console`.

  Setting    Value
  ---------- --------------------------
  JDBC URL   `jdbc:h2:file:../tms_db`
  Username   `sa`
  Password   `sa`

> **Development only:** The H2 Console, default credentials, and
> `spring.jpa.hibernate.ddl-auto=update` are local-development settings.
> They are not a production database configuration.

## API reference

Account registration is public. Other routes require authentication. The
intended client flow is to obtain a JWT using HTTP Basic credentials,
then use the bearer token for subsequent requests.

  --------------------------------------------------------------------------------------
  Method            Endpoint                         Authentication    Authorization /
                                                                       behavior
  ----------------- -------------------------------- ----------------- -----------------
  `POST`            `/api/accounts`                  Public            Register an
                                                                       account

  `GET`             `/api/auth`                      Basic or Bearer   Return
                                                                       authenticated
                                                                       principal /
                                                                       authority details

  `POST`            `/api/auth/token`                Basic or Bearer   Issue a JWT
                                                                       (currently valid
                                                                       for 60 seconds)

  `GET`             `/api/tasks`                     Basic or Bearer   List tasks;
                                                                       optionally filter
                                                                       by `author`
                                                                       and/or `assignee`

  `POST`            `/api/tasks`                     Basic or Bearer   Create a task
                                                                       owned by the
                                                                       caller

  `PUT`             `/api/tasks/{taskId}/assign`     Basic or Bearer   Assign or
                                                                       unassign a user;
                                                                       task author only

  `PUT`             `/api/tasks/{taskId}/status`     Basic or Bearer   Set status; task
                                                                       author or
                                                                       assignee

  `PUT`             `/api/tasks/{taskId}/comments`   Basic or Bearer   Add a comment

  `GET`             `/api/tasks/{taskId}/comments`   Basic or Bearer   Retrieve comments
                                                                       for a task
  --------------------------------------------------------------------------------------

Valid task statuses:

``` text
CREATED
IN_PROGRESS
COMPLETED
```

Task filters can be used individually or together:

``` http
GET /api/tasks?author=alice@example.com
GET /api/tasks?assignee=bob@example.com
GET /api/tasks?author=alice@example.com&assignee=bob@example.com
```

## Example workflow

The following Bash example uses `curl` and `jq`. The JWT currently
expires after 60 seconds, so complete the protected requests promptly or
obtain a fresh token.

``` bash
BASE_URL="http://localhost:8080"

# 1. Register two users.
curl -s -X POST "$BASE_URL/api/accounts" \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"password123"}' | jq

curl -s -X POST "$BASE_URL/api/accounts" \
  -H "Content-Type: application/json" \
  -d '{"email":"bob@example.com","password":"password123"}' | jq

# 2. Exchange Alice's Basic credentials for a JWT.
TOKEN=$(curl -s -X POST "$BASE_URL/api/auth/token" \
  -u "alice@example.com:password123" | jq -r '.token')

# 3. Create a task and capture its ID.
TASK_ID=$(curl -s -X POST "$BASE_URL/api/tasks" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Document the API","description":"Publish architecture and usage details"}' \
  | jq -r '.id')

# 4. List tasks.
curl -s "$BASE_URL/api/tasks" \
  -H "Authorization: Bearer $TOKEN" | jq

# 5. Assign the task to Bob. Only the author can assign it.
curl -s -X PUT "$BASE_URL/api/tasks/$TASK_ID/assign" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"assignee":"bob@example.com"}' | jq

# 6. Update the task status.
curl -s -X PUT "$BASE_URL/api/tasks/$TASK_ID/status" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"status":"IN_PROGRESS"}' | jq

# 7. Add and retrieve a comment.
curl -s -X PUT "$BASE_URL/api/tasks/$TASK_ID/comments" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"text":"Architecture section drafted"}'

curl -s "$BASE_URL/api/tasks/$TASK_ID/comments" \
  -H "Authorization: Bearer $TOKEN" | jq
```

The current implementation returns `200 OK` for registration, token
issuance, task creation, updates, and comment creation. It does not yet
return `201 Created` for newly created resources.

## Response examples

### Successful account registration

``` json
{
  "id": 1,
  "email": "alice@example.com"
}
```

### Successful task creation

``` json
{
  "id": "1",
  "title": "Document the API",
  "description": "Publish architecture and usage details",
  "status": "CREATED",
  "author": "alice@example.com",
  "assignee": "none"
}
```

### Validation failure --- `400 Bad Request`

``` json
{
  "status": 400,
  "error": "Validation failed",
  "details": {
    "email": "Invalid email"
  },
  "timestamp": "2026-07-29T12:00:00"
}
```

### Duplicate account --- `409 Conflict`

``` json
{
  "status": 409,
  "error": "Username already exists",
  "details": {
    "username": "alice@example.com"
  },
  "timestamp": "2026-07-29T12:00:00"
}
```

### Missing task --- `404 Not Found`

``` json
{
  "status": 404,
  "error": "Task not found",
  "details": {
    "message": "Task not found"
  },
  "timestamp": "2026-07-29T12:00:00"
}
```

### Unauthorized assignment --- `403 Forbidden`

``` json
{
  "status": 403,
  "error": "User is not allowed to assign this task",
  "details": {
    "message": "User is not allowed to assign this task"
  },
  "timestamp": "2026-07-29T12:00:00"
}
```

An invalid task status currently returns a plain-text `400` response
rather than the shared JSON error structure:

``` text
Invalid value. Accepted values are: [CREATED, IN_PROGRESS, COMPLETED]
```

## Testing

The current Gradle test task passes. At the time this README was
written, the tracked test suite contains one Spring Boot context smoke
test:

``` text
TaskManagementSystemApplicationTests.contextLoads()
```

This verifies that the application context and dependency graph can
start. It does **not** demonstrate business-rule, security, or endpoint
test coverage. No test-coverage percentage is claimed.

Recommended next test layers:

1.  **Service unit tests:** email normalization, duplicate-account
    rejection, task ownership, assignment permissions, status
    authorization, and missing entities.
2.  **Controller tests:** response status codes, JSON contracts,
    validation errors, authentication requirements, and filter
    parameters.
3.  **Repository tests:** author/assignee filter combinations, ordering,
    and entity relationships against H2.
4.  **Security integration tests:** Basic-to-JWT issuance, signature
    validation, token expiration, and forbidden operations.

## Known limitations

The following limitations are present in the current implementation and
are intentionally documented:

-   **Ephemeral signing keys:** a new RSA-2048 key pair is generated on
    every application start. Restarting the application invalidates
    previously issued tokens.
-   **JWT lifetime mismatch:** the token currently expires after 60
    seconds; the configured `jwt.expiration-minutes=60` property is not
    wired into token generation.
-   **Authentication scope:** both Basic and bearer authentication are
    accepted on protected endpoints. Basic authentication should be
    limited to token issuance.
-   **Authorization scope:** task and comment visibility is broader than
    a participant-only model. Comment access is not yet restricted to
    the task author or assignee.
-   **Service / HTTP coupling:** task authorization rules use
    `ResponseStatusException` in the service layer.
-   **Persistence model:** H2 and `ddl-auto=update` are used for local
    development. A production-oriented deployment would need a managed
    database and versioned schema migrations.
-   **Error response consistency:** malformed request bodies and some
    authentication failures do not yet use the same error response
    structure as other failures.
-   **HTTP creation semantics:** creation endpoints currently return
    `200 OK` rather than `201 Created`.

## Engineering roadmap

-   [ ] Persist RSA signing keys securely and define a key-rotation
    strategy.
-   [ ] Make JWT expiration configurable through
    `jwt.expiration-minutes`.
-   [ ] Restrict Basic authentication to the token endpoint.
-   [ ] Tighten task and comment visibility rules.
-   [ ] Replace HTTP-specific service exceptions with domain exceptions
    or method-level authorization.
-   [ ] Persist `TaskStatus` explicitly as a string enum.
-   [ ] Replace unsafe `Optional.get()` usage.
-   [ ] Move comment persistence components into the task feature
    package.
-   [ ] Standardize validation, malformed-body, and authentication error
    responses.
-   [ ] Add PostgreSQL support and Flyway migrations.
-   [ ] Publish OpenAPI documentation and request examples.
-   [ ] Expand automated tests and add a GitHub Actions build.
-   [ ] Add container packaging, health checks, and environment-specific
    configuration.

## License

No license file is currently included. Until a license is added, treat
this repository as source-available for portfolio review and do not
assume that redistribution or reuse is permitted.
