# ARCHITECTURE.md — Project Architecture
# FinTrack: Personal Finance REST API

> This file is the file placement law. Every file has one correct home.
> When in doubt about where something goes, check here first.

---

## 1. Folder Structure

```
fintrack/
│
├── src/
│   ├── main/
│   │   ├── java/com/fintrack/
│   │   │   │
│   │   │   ├── auth/                         # Feature: registration, login, JWT
│   │   │   │   ├── AuthController.java        # REST endpoints: /api/auth/**
│   │   │   │   ├── AuthService.java           # Business logic: register, login
│   │   │   │   ├── AuthRepository.java        # DB access: find user by email
│   │   │   │   ├── User.java                  # User entity
│   │   │   │   └── dto/
│   │   │   │       ├── RegisterRequest.java
│   │   │   │       ├── LoginRequest.java
│   │   │   │       └── AuthResponse.java       # Returns JWT token
│   │   │   │
│   │   │   ├── transaction/                   # Feature: CRUD, summaries, fraud flag
│   │   │   │   ├── TransactionController.java  # REST endpoints: /api/transactions/**
│   │   │   │   ├── TransactionService.java     # Business logic + fraud flag trigger
│   │   │   │   ├── TransactionRepository.java  # DB access + custom summary queries
│   │   │   │   ├── Transaction.java            # Transaction entity
│   │   │   │   └── dto/
│   │   │   │       ├── TransactionRequest.java
│   │   │   │       ├── TransactionResponse.java
│   │   │   │       └── CategorySummaryResponse.java
│   │   │   │
│   │   │   ├── fraud/                         # Feature: fraud detection rule engine
│   │   │   │   └── FraudDetectionService.java  # Stateless service: evaluates 3x rule
│   │   │   │
│   │   │   └── common/                        # Cross-cutting concerns (no feature owns these)
│   │   │       ├── security/
│   │   │       │   ├── JwtUtil.java            # Token generation + parsing (JJWT 0.12.x)
│   │   │       │   ├── JwtAuthFilter.java      # OncePerRequestFilter — validates JWT
│   │   │       │   ├── SecurityConfig.java     # Spring Security filter chain config
│   │   │       │   └── UserDetailsServiceImpl.java  # Loads user for Spring Security
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java  # @ControllerAdvice — all error responses
│   │   │       │   ├── ResourceNotFoundException.java
│   │   │       │   ├── UnauthorizedException.java
│   │   │       │   └── ApiErrorResponse.java   # Standard error response shape
│   │   │       └── config/
│   │   │           └── OpenApiConfig.java      # Swagger/OpenAPI customization + JWT auth header
│   │   │
│   │   └── resources/
│   │       ├── application.yml                 # Base config (all environments)
│   │       ├── application-dev.yml             # Local dev overrides
│   │       └── application-prod.yml            # Production overrides (no secrets here)
│   │
│   └── test/
│       └── java/com/fintrack/
│           ├── auth/
│           │   ├── AuthControllerTest.java     # @WebMvcTest — HTTP layer
│           │   └── AuthServiceTest.java        # Unit test with Mockito
│           ├── transaction/
│           │   ├── TransactionControllerTest.java
│           │   └── TransactionServiceTest.java
│           ├── fraud/
│           │   └── FraudDetectionServiceTest.java  # Edge cases: exactly 3x, above, below
│           └── resources/
│               └── application-test.yml        # H2 in-memory DB for tests
│
├── .github/
│   └── workflows/
│       └── ci-cd.yml                          # GitHub Actions: test → build → deploy
│
├── docker-compose.yml                         # Local: app + PostgreSQL
├── Dockerfile                                 # Production image
├── pom.xml                                    # Maven dependencies
├── .env.example                               # Template — committed to repo
├── .env                                       # Actual secrets — gitignored
└── README.md                                  # Setup guide + Swagger URL for interviewers
```

---

## 2. File Placement Rules

| Type of file | Goes in | Example |
|---|---|---|
| REST controller | `{feature}/` | `transaction/TransactionController.java` |
| Business logic | `{feature}/` | `transaction/TransactionService.java` |
| Database access | `{feature}/` | `transaction/TransactionRepository.java` |
| JPA entity | `{feature}/` | `transaction/Transaction.java` |
| Request/response DTOs | `{feature}/dto/` | `transaction/dto/TransactionRequest.java` |
| JWT logic | `common/security/` | `common/security/JwtUtil.java` |
| Spring Security config | `common/security/` | `common/security/SecurityConfig.java` |
| Exception classes | `common/exception/` | `common/exception/ResourceNotFoundException.java` |
| Global error handler | `common/exception/` | `common/exception/GlobalExceptionHandler.java` |
| App-level config beans | `common/config/` | `common/config/OpenApiConfig.java` |
| Application properties | `src/main/resources/` | `application.yml`, `application-dev.yml` |
| Unit tests | Mirror of main, same package | `test/java/com/fintrack/transaction/TransactionServiceTest.java` |
| Integration tests | Mirror of main, same package | `test/java/com/fintrack/auth/AuthControllerTest.java` |
| Test config | `test/resources/` | `application-test.yml` |
| CI/CD pipeline | `.github/workflows/` | `ci-cd.yml` |

**Rule:** If a class serves only one feature, it lives in that feature's package. If it serves two or more features, it lives in `common/`.

---

## 3. Naming Conventions

| Thing | Convention | Example |
|---|---|---|
| Classes | PascalCase | `TransactionService`, `JwtAuthFilter` |
| Methods | camelCase | `getUserAverage()`, `isFraudulent()` |
| Variables | camelCase | `transactionAmount`, `userId` |
| Constants | UPPER_SNAKE_CASE | `JWT_EXPIRATION_MS` |
| Packages | lowercase, singular | `transaction`, `auth`, `fraud` |
| DTOs | Suffix with `Request` or `Response` | `TransactionRequest`, `AuthResponse` |
| Entities | Plain noun, no suffix | `Transaction`, `User` |
| Repositories | Suffix with `Repository` | `TransactionRepository` |
| Tests | Suffix with `Test` | `FraudDetectionServiceTest` |
| Config files | `application-{profile}.yml` | `application-dev.yml` |
| Endpoints | kebab-case, plural nouns | `/api/transactions`, `/api/auth` |

---

## 4. Data Flow

Every request follows the same path. No skipping layers.

```
HTTP Request
    │
    ▼
JwtAuthFilter          (common/security) — validates token, sets SecurityContext
    │
    ▼
Controller             ({feature}/) — parses request, calls service, returns response DTO
    │
    ▼
Service                ({feature}/) — business logic, calls repository + FraudDetectionService
    │
    ├──▶ FraudDetectionService   (fraud/) — stateless rule evaluation, returns boolean
    │
    ▼
Repository             ({feature}/) — Spring Data JPA, talks to PostgreSQL
    │
    ▼
Database               (PostgreSQL — local or AWS RDS)
    │
    ▼
Response mapped via MapStruct → DTO → JSON → HTTP Response
```

**Rules:**
- Controllers never touch the repository directly.
- Services never return entities — always map to DTOs before returning.
- FraudDetectionService is stateless and pure — no repository calls, no side effects.
- GlobalExceptionHandler catches anything thrown from any layer and formats it consistently.

---

## 5. Path Aliases

Not applicable — Java uses fully qualified package imports, not path aliases.

Package structure is flat enough that imports are always readable:
```java
import com.fintrack.transaction.dto.TransactionResponse;
import com.fintrack.common.security.JwtUtil;
```

No import aliasing needed.

---

## 6. Environment Variables

| Variable | Purpose | Environment | Secret? |
|---|---|---|---|
| `DB_URL` | PostgreSQL JDBC connection string | dev + prod | No |
| `DB_USERNAME` | Database username | dev + prod | Yes |
| `DB_PASSWORD` | Database password | dev + prod | Yes |
| `JWT_SECRET` | HMAC signing key for JWT tokens | dev + prod | Yes |
| `JWT_EXPIRATION_MS` | Token validity window in milliseconds | dev + prod | No |
| `SPRING_PROFILES_ACTIVE` | Activates `dev` or `prod` config | dev + prod | No |
| `SERVER_PORT` | Port override (default 8080) | prod | No |

**Rules:**
- `.env` is gitignored. Never commit real secrets.
- `.env.example` is committed with placeholder values so any dev can onboard without asking.
- Production secrets are set as environment variables on the AWS instance — not in any file.
- `application-prod.yml` references variables via `${VAR_NAME}` — no hardcoded values.

---

## 7. Key Architectural Decisions

**1. Hybrid package structure (feature + common)**
Feature packages (`auth/`, `transaction/`, `fraud/`) group everything related to one domain. `common/` holds cross-cutting concerns that no single feature owns. This mirrors microservice boundary thinking — each feature package could be extracted into its own service with minimal refactoring.

**2. Stateless JWT authentication**
No server-side sessions. Every request carries a signed JWT. Spring Security validates it on entry via `JwtAuthFilter`. This scales horizontally — any instance can validate any token without shared state.

**3. FraudDetectionService is stateless and isolated**
The fraud rule (3x average) is pure logic. It takes inputs, returns a result, touches no database. This makes it trivially testable and replaceable — swap the rule for an ML model in v2 without touching `TransactionService`.

**4. DTOs at every layer boundary**
Entities never leave the service layer. Controllers receive request DTOs, pass primitives or request objects to services, and return response DTOs. MapStruct handles the mapping at compile time — no runtime reflection, no manual field assignments.

**5. GlobalExceptionHandler for all error responses**
All exceptions are caught in one place (`@ControllerAdvice`). Every error response has the same shape: `{ status, message, timestamp }`. No error formatting logic scattered across controllers.

**6. H2 for tests, PostgreSQL for everything else**
Test profile swaps to H2 in-memory via `application-test.yml`. No Docker required to run tests. CI pipeline runs tests without spinning up a database container.

**7. application.yml over application.properties**
YAML supports hierarchy and is easier to read for nested config (datasource, jpa, security settings). One base file + profile-specific overrides keeps config DRY.