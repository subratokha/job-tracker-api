# Job Tracker API

Spring Boot REST API for registering users, authenticating with JWTs, and managing job application records owned by the authenticated user.

## Stack

- Java 21
- Spring Boot 4.0.6
- Spring Web MVC
- Spring Security
- Spring Data JPA / Hibernate
- PostgreSQL for normal runtime
- H2 in-memory database for tests
- Maven Wrapper
- Docker Compose
- JUnit 5, Mockito, MockMvc, Spring Security Test, Data JPA Test

## Application Model

The application has two persisted entities:

- `User`, stored in `users`
- `JobApplication`, stored in `job_applications`

`JobApplication` has a required many-to-one relationship to `User` through `user_id`. Application records are always resolved through the current authenticated user.

Supported application statuses are:

```text
SAVED
APPLIED
INTERVIEWING
OFFERED
REJECTED
```

New users are assigned `ROLE_USER`. `ROLE_ADMIN` exists in the enum but is not used by a controller-specific authorization rule in the current code.

## API

Base URL when running locally: `http://localhost:8080`

### Authentication

| Method | Path | Auth | Success |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | Public | `201 Created` |
| `POST` | `/auth/login` | Public | `200 OK` with JWT |

Register request:

```json
{
  "email": "user@example.com",
  "password": "password1",
  "firstName": "Ada",
  "lastName": "Lovelace"
}
```

Validation:

- `email` is required and must be a valid email address.
- `password` is required and must be 8 to 12 characters.
- `firstName` is required.
- `lastName` is optional.

Login request:

```json
{
  "email": "user@example.com",
  "password": "password1"
}
```

Login response:

```json
{
  "token": "<jwt>"
}
```

### Job Applications

All `/applications` endpoints require:

```http
Authorization: Bearer <jwt>
```

| Method | Path | Success |
| --- | --- | --- |
| `GET` | `/applications` | `200 OK` with current user's applications |
| `POST` | `/applications` | `201 Created` with `Location: /applications/{id}` |
| `GET` | `/applications/{id}` | `200 OK` |
| `PUT` | `/applications/{id}` | `204 No Content` |
| `DELETE` | `/applications/{id}` | `204 No Content` |

Create/update request:

```json
{
  "companyName": "Example Corp",
  "jobTitle": "Backend Engineer",
  "jobUrl": "https://example.com/jobs/backend-engineer",
  "contactName": "Recruiter Name",
  "dateApplied": "2026-01-02",
  "lastFollowUpDate": "2026-01-09",
  "status": "APPLIED",
  "notes": "Applied via careers page"
}
```

Validation:

- `companyName` is required.
- `jobTitle` is required.
- `dateApplied` is required.
- `status` is required.
- `jobUrl`, `contactName`, `lastFollowUpDate`, and `notes` are optional.

Response shape:

```json
{
  "id": 1,
  "companyName": "Example Corp",
  "jobTitle": "Backend Engineer",
  "jobUrl": "https://example.com/jobs/backend-engineer",
  "contactName": "Recruiter Name",
  "dateApplied": "2026-01-02",
  "lastFollowUpDate": "2026-01-09",
  "status": "APPLIED",
  "notes": "Applied via careers page"
}
```

## Authentication and Authorization

Security is stateless. CSRF is disabled and sessions use `SessionCreationPolicy.STATELESS`.

Public endpoints:

- `/auth/register`
- `/auth/login`

All other endpoints require authentication.

JWT configuration:
- **Secret**: Injected from `JWT_SECRET` environment variable (defaults to `change-me-in-production` if not set)
- **Expiration**: 3600000ms (1 hour)

The JWT subject is the user's email address. `JwtAuthFilter` reads the `Authorization: Bearer ...` header, extracts the email, loads the user through `CustomUserDetailsService`, validates the token, and sets the Spring Security authentication. **Always set `JWT_SECRET` in production through environment variables, not in source code.**

Job application ownership is enforced in `JobApplicationService` with repository methods that include the authenticated user's id:

```java
List<JobApplication> findAllByUserId(Long userId);
Optional<JobApplication> findByIdAndUserId(Long id, Long userId);
```

## Error Responses

Handled errors return:

```json
{
  "status": 400,
  "message": "field: validation message",
  "timestamp": "2026-05-26T12:00:00"
}
```

Mapped exceptions/statuses:

- Validation failures: `400 Bad Request`
- Duplicate registration email: `409 Conflict`
- Invalid credentials: `401 Unauthorized`
- Missing/invalid authenticated user: `401 Unauthorized`
- Missing job application for the current user: `404 Not Found`
- Unhandled exception: `500 Internal Server Error`

## Configuration

Runtime configuration is **environment-driven** via Spring placeholders (all required):

- `SPRING_DATASOURCE_URL`: PostgreSQL connection string (e.g., `jdbc:postgresql://localhost:5432/jobtracker`)
- `SPRING_DATASOURCE_USERNAME`: Database user (e.g., `postgres`)
- `SPRING_DATASOURCE_PASSWORD`: Database password (must be set, never default)
- `JWT_SECRET`: Signing key for JWT tokens (must be set, never default)

Test configuration uses H2 (configured in `src/test/resources/application.properties`):

```properties
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
spring.jpa.hibernate.ddl-auto=create-drop
```

## Prerequisites

**For Docker (recommended):**
- Docker and Docker Compose
- No other dependencies needed

**For local development (without Docker):**
- Java 21
- Maven (provided via `./mvnw`)
- PostgreSQL 16

## Run with Docker Compose

The application uses a multi-stage Dockerfile: the build stage compiles with Maven, the runtime stage runs the JAR with only the JRE.

### Setup

1. Create `.env` file from example (`.env.example` is a template, not real values):
```bash
cp .env.example .env
```

2. Edit `.env` with your actual configuration (used by both postgres and app services):
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/jobtracker
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your-actual-secure-password
JWT_SECRET=your-actual-secure-jwt-secret-key
```

**Never commit `.env` to version control.** All environment variables are injected from `.env` into both the PostgreSQL container and the application container.

3. Start services (Dockerfile builds automatically):
```bash
docker-compose up --build
```

The API will be available at `http://localhost:8080`. PostgreSQL data persists in the named volume `postgres-data`.

**Note:** The Dockerfile internally runs `mvn clean package`; you don't need Maven locally.

## Run Locally (without Docker)

### Setup

1. Start PostgreSQL with expected database:
```bash
createdb jobtracker  # or via your PostgreSQL client
```

2. Create `.env` file for local development (use your actual database password and JWT secret):
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/jobtracker
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your-actual-db-password
JWT_SECRET=your-actual-jwt-secret-key
```

3. Load environment variables and start the application:
```bash
export $(cat .env | xargs)
./mvnw spring-boot:run
```

The API listens on port `8080`.

### Configuration Hierarchy

Environment variables override `application.properties`. The application uses sensible defaults so it works out-of-the-box for local development:

- `application.properties`: Safe defaults for localhost PostgreSQL and placeholder JWT secret
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`: Database config
- `JWT_SECRET`: JWT signing key (must be set in production)
- `.env` file: Loaded manually in local dev, or injected via docker-compose in containerized mode
- `docker-compose.yml`: Container environment sourced from `.env`

## Tests

Run the full test suite:

```bash
./mvnw test
```

## API Documentation

Interactive API documentation available at:
http://localhost:8080/swagger-ui/index.html

The repository contains:

- Controller tests for authentication and job application endpoints using `@WebMvcTest`, MockMvc, and Spring Security Test.
- Service tests for registration, login, current-user lookup, JWT generation behavior, and job application ownership logic using Mockito.
- Repository tests using `@DataJpaTest` and H2.
- JWT service tests for token generation, subject extraction, expiration, validation, and invalid token handling.
- A Spring context load test.

## Project Layout

```text
src/main/java/com/jobtracker/api
|-- controller      HTTP controllers
|-- dto             Request/response records
|-- exception       Custom exceptions and global exception handler
|-- model           JPA entities and enums
|-- repository      Spring Data JPA repositories
|-- security        JWT filter/service and Spring Security configuration
`-- service         Business logic

src/main/resources/application.properties
src/test/resources/application.properties
docker-compose.yml
Dockerfile
pom.xml
```
