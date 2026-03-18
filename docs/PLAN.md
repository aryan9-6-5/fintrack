# PLAN.md — Project Plan & Progress Tracker
# FinTrack: Personal Finance REST API

> AI reads the Current Status block first every session.
> Update it last before closing. Never skip this step.

---

## 📍 Current Status

**Stage:** 1 — Setup
**Step:** Not started — beginning from scratch
**Updated:** 2026-03-18
**Blockers:** None

---

## Stage Overview

| Stage | Name | Status |
|---|---|---|
| 1 | Setup — repo, config, DB, CI scaffold | ⬜ Not Started |
| 2 | Core — entities, security, JWT, base wiring | ⬜ Not Started |
| 3 | Features — auth, transactions, fraud, summaries | ⬜ Not Started |
| 4 | Security Hardening | ⬜ Not Started |
| 5 | Testing | ⬜ Not Started |
| 6 | Polish & Deploy | ⬜ Not Started |

---

## Stage 1: Setup

**Goal:** A running Spring Boot app connected to PostgreSQL, containerized, with CI pipeline active. No feature code yet.

- [ ] Generate project via Spring Initializr with dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL Driver, Lombok, Validation, Actuator
- [ ] Add manual dependencies to `pom.xml`: JJWT 0.12.x (3 artifacts), MapStruct 1.5.x, Springdoc OpenAPI 2.x
- [ ] Fix annotation processor order in `pom.xml`: Lombok declared before MapStruct in `annotationProcessorPaths`
- [ ] Create folder structure exactly per ARCHITECTURE.md: `auth/`, `transaction/`, `fraud/`, `common/security/`, `common/exception/`, `common/config/`
- [ ] Create `application.yml`, `application-dev.yml`, `application-prod.yml`, `application-test.yml`
- [ ] Create `.env.example` with all variables from ARCHITECTURE.md environment table
- [ ] Create `.env` locally (gitignored) with real dev values
- [ ] Add `.env` to `.gitignore` — verify it does not appear in `git status`
- [ ] Create `docker-compose.yml` — spins up app + PostgreSQL 15
- [ ] Create `Dockerfile` — multi-stage build (build with Maven, run with JRE)
- [ ] Verify: `docker-compose up` starts both containers, app connects to DB, no errors
- [ ] Verify: `GET /actuator/health` returns `{"status":"UP"}`
- [ ] Create `.github/workflows/ci-cd.yml` — runs `mvn test` on every push
- [ ] Push to GitHub — confirm GitHub Actions run appears and passes (empty test suite passes)
- [ ] Create `README.md` skeleton with sections: Overview, Local Setup, API Reference, Architecture, Live URL (placeholder)

**Stage 1 is done when:** `docker-compose up` runs clean, `/actuator/health` responds, and GitHub Actions shows a green build.

---

## Stage 2: Core Architecture

**Goal:** Database schema live, Spring Security configured, JWT working end to end. No feature endpoints yet — just the foundation every feature builds on.

### 2a. Database & Entities

- [ ] Create `User.java` entity in `auth/` — fields: `id` (Long), `email` (unique), `password` (hashed), `createdAt`
- [ ] Create `Transaction.java` entity in `transaction/` — fields: `id`, `userId` (FK to User), `amount` (BigDecimal), `type` (INCOME/EXPENSE enum), `category` (String), `description` (String, nullable), `isFlagged` (boolean, default false), `createdAt`
- [ ] Create `TransactionType.java` enum in `transaction/` — values: `INCOME`, `EXPENSE`
- [ ] Verify Hibernate creates schema correctly: `spring.jpa.hibernate.ddl-auto=update`, check tables appear in DB
- [ ] Connect to local PostgreSQL via DBeaver or psql and confirm `users` and `transactions` tables exist with correct columns

### 2b. Spring Security & JWT

- [ ] Create `JwtUtil.java` in `common/security/` — methods: `generateToken(username)`, `extractUsername(token)`, `isTokenValid(token, userDetails)`
- [ ] Use JJWT 0.12.x API only — `Jwts.builder().subject(...).signWith(key, Jwts.SIG.HS256).compact()`
- [ ] Create `JwtAuthFilter.java` in `common/security/` — extends `OncePerRequestFilter`, extracts Bearer token, validates, sets `SecurityContextHolder`
- [ ] Create `UserDetailsServiceImpl.java` in `common/security/` — loads user by email from `AuthRepository`
- [ ] Create `SecurityConfig.java` in `common/security/` — `SecurityFilterChain` bean, stateless session, CSRF disabled, `permitAll` only on `/api/auth/**`, all other routes require auth, JWT filter inserted before `UsernamePasswordAuthenticationFilter`
- [ ] Write `JwtUtilTest.java` — test token generation, extraction, expiry, tampered token rejection
- [ ] Verify: unauthenticated `GET /api/transactions` returns 401 (endpoint doesn't exist yet — expect 401 not 404)

### 2c. Exception Handling & OpenAPI

- [ ] Create `ApiErrorResponse.java` in `common/exception/` — fields: `status`, `message`, `timestamp`
- [ ] Create `ResourceNotFoundException.java` and `UnauthorizedException.java` in `common/exception/`
- [ ] Create `GlobalExceptionHandler.java` in `common/exception/` — `@ControllerAdvice`, catches all custom exceptions + `MethodArgumentNotValidException`, returns `ApiErrorResponse`
- [ ] Create `OpenApiConfig.java` in `common/config/` — adds JWT Bearer auth scheme to Swagger UI so interviewers can authenticate without reading code
- [ ] Verify: Swagger UI loads at `/swagger-ui.html` with JWT auth button visible

**Stage 2 is done when:** Schema exists in DB, JWT round-trip works in isolation (unit test passes), unauthenticated requests return 401, Swagger UI loads with auth configured.

---

## Stage 3: Features

### 3a. Auth — Register & Login

**Depends on:** Stage 2 complete

- [ ] Create `RegisterRequest.java` and `LoginRequest.java` DTOs in `auth/dto/` — add Bean Validation annotations (`@NotBlank`, `@Email`)
- [ ] Create `AuthResponse.java` DTO in `auth/dto/` — field: `token` (String)
- [ ] Create `AuthRepository.java` in `auth/` — `findByEmail(String email)` method
- [ ] Create `AuthService.java` in `auth/` — `register(RegisterRequest)` hashes password with BCrypt, saves user, returns JWT; `login(LoginRequest)` validates credentials, returns JWT
- [ ] Create `AuthController.java` in `auth/` — `POST /api/auth/register`, `POST /api/auth/login`, both `@Operation` annotated for Swagger
- [ ] Write `AuthServiceTest.java` — test register success, duplicate email, login success, wrong password
- [ ] Write `AuthControllerTest.java` — integration test full register → login → JWT returned flow
- [ ] Manual Swagger check: register → copy token → click Authorize → login confirms same user

**Acceptance criteria:** A cold tester can register, log in, receive a JWT, and authenticate subsequent requests through Swagger with zero setup.

---

### 3b. Transaction CRUD

**Depends on:** 3a (auth) complete — all endpoints require valid JWT

- [ ] Create `TransactionRequest.java` DTO — fields: `amount`, `type`, `category`, `description`; add `@NotNull`, `@Positive` validation
- [ ] Create `TransactionResponse.java` DTO — all fields including `isFlagged`, `createdAt`
- [ ] Create MapStruct mapper `TransactionMapper.java` — `toResponse(Transaction)`, `toEntity(TransactionRequest)`
- [ ] Create `TransactionRepository.java` — `findAllByUserId(Long userId)`, `findByIdAndUserId(Long id, Long userId)`
- [ ] Create `TransactionService.java` — `create`, `getAll`, `getById`, `update`, `delete` — all scoped to authenticated user ID; calls `FraudDetectionService` on create/update
- [ ] Create `TransactionController.java` — `POST /api/transactions`, `GET /api/transactions`, `GET /api/transactions/{id}`, `PUT /api/transactions/{id}`, `DELETE /api/transactions/{id}`; extract user ID from `SecurityContext`, never from request body
- [ ] Write `TransactionServiceTest.java` — test each CRUD method, verify user scoping (user A cannot get user B's transaction)
- [ ] Write `TransactionControllerTest.java` — integration test each endpoint with valid JWT, invalid JWT, and wrong-user scenarios
- [ ] Manual Swagger check: create 3 transactions, retrieve all, update one, delete one

**Acceptance criteria:** Full CRUD works authenticated. Unauthenticated requests return 401. Cross-user access returns 403.

---

### 3c. Fraud Detection

**Depends on:** 3b (transaction CRUD) — `FraudDetectionService` is called by `TransactionService`

- [ ] Create `FraudDetectionService.java` in `fraud/` — single public method `isFraudulent(BigDecimal amount, BigDecimal userAverage)` returns boolean; stateless, no repository, no `@Autowired` dependencies
- [ ] Implement rule: `amount.compareTo(userAverage.multiply(BigDecimal.valueOf(3))) >= 0` → flagged; handle zero average (no flag)
- [ ] Wire into `TransactionService.create()` — calculate user's historical average from repository, call `FraudDetectionService`, set `isFlagged` on entity before save
- [ ] `isFlagged` appears in `TransactionResponse` DTO
- [ ] Write `FraudDetectionServiceTest.java` — test: below 3x (no flag), exactly 3x (flag), above 3x (flag), zero average (no flag), first transaction (no flag)
- [ ] Manual Swagger check: create several normal transactions, then one large one — confirm `isFlagged: true` in response

**Acceptance criteria:** `isFlagged: true` appears on any transaction at or above 3x the user's historical average. First transaction is never flagged. Zero-average edge case handled without error.

---

### 3d. Category Summary

**Depends on:** 3b (transactions exist to summarize)

- [ ] Create `CategorySummaryResponse.java` DTO in `transaction/dto/` — fields: `category` (String), `total` (BigDecimal), `transactionCount` (int)
- [ ] Add custom query to `TransactionRepository` — `@Query` that groups by category, sums amounts, filters by userId and optional month/year
- [ ] Add `getSummary(Long userId, Integer month, Integer year)` to `TransactionService`
- [ ] Add `GET /api/transactions/summary` to `TransactionController` — optional query params `?month=3&year=2026`
- [ ] Write unit test for summary calculation in `TransactionServiceTest`
- [ ] Write integration test for summary endpoint in `TransactionControllerTest`
- [ ] Manual Swagger check: create transactions across 3 categories, hit summary endpoint, verify totals

**Acceptance criteria:** Summary endpoint returns per-category totals for the authenticated user only. Optional month/year filter works. Empty result set returns `[]` not 500.

---

## Stage 4: Security Hardening

**Goal:** Close every gap an interviewer could probe. These are not optional polish items.

- [ ] Confirm `SecurityConfig` has no `anyRequest().permitAll()` — only `/api/auth/**` is open
- [ ] Confirm JWT filter is inserted at `addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)` — not after
- [ ] Confirm JWT secret in `.env` is at least 32 characters (256-bit minimum for HS256)
- [ ] Confirm passwords stored as BCrypt hash — query `users` table directly and verify hash format (`$2a$...`)
- [ ] Confirm no PII in any log statement — grep codebase for `log.*email`, `log.*password`, `log.*token`
- [ ] Confirm all transaction queries filter by `userId` — no query returns all rows across users
- [ ] Confirm `application-prod.yml` contains zero hardcoded secrets — all values via `${VAR_NAME}`
- [ ] Confirm `.env` is in `.gitignore` and does not appear in `git log` history
- [ ] Confirm Actuator: `/actuator/health` is public, `/actuator/env` is disabled or secured in prod config
- [ ] Confirm CORS config is explicit — no wildcard `*` origin in production profile
- [ ] Confirm `GlobalExceptionHandler` returns consistent `ApiErrorResponse` shape — not raw Spring error objects
- [ ] Run `mvn dependency:check` — no known CVEs in declared dependencies

**Stage 4 is done when:** Every box above is checked and confirmed by direct inspection, not assumption.

---

## Stage 5: Testing

**Goal:** All tests written, all passing, coverage gate met, CI green.

- [ ] `FraudDetectionServiceTest` — all 5 boundary cases covered (below, exactly at, above 3x, zero average, first transaction)
- [ ] `JwtUtilTest` — token generation, extraction, expiry, tampered token
- [ ] `AuthServiceTest` — register success, duplicate email, login success, wrong password
- [ ] `TransactionServiceTest` — CRUD methods, user scoping, fraud flag trigger
- [ ] `AuthControllerTest` — register → login → JWT, unauthenticated 401, wrong password 401
- [ ] `TransactionControllerTest` — all endpoints with valid JWT, invalid JWT, cross-user 403
- [ ] Run `mvn test jacoco:report` — confirm `fraud` and `transaction/service` packages at 80%+ line coverage
- [ ] Run full test suite — zero failures, zero skipped tests
- [ ] Push to GitHub — GitHub Actions shows green on all test jobs
- [ ] Verify H2 test profile activates correctly — confirm no test touches the dev PostgreSQL database

**Stage 5 is done when:** `mvn test` exits 0, coverage report meets threshold, CI is green.

---

## Stage 6: Polish & Deploy

**Goal:** Live AWS URL, Swagger accessible, README complete. Every v1 done-enough box checked.

### Documentation
- [ ] README: add Architecture section with package structure summary
- [ ] README: add Sample API Call Sequence (register → login → create transaction → get summary → see fraud flag)
- [ ] README: add Local Setup section (clone → copy `.env.example` → `docker-compose up` → open Swagger)
- [ ] Add `@Operation` and `@ApiResponse` annotations to every controller method — Swagger must be self-explanatory
- [ ] Add `@Schema` descriptions to all request/response DTO fields

### AWS Deployment
- [ ] Create AWS RDS PostgreSQL instance — db.t3.micro, free tier, note connection string
- [ ] Set production environment variables on EC2/Elastic Beanstalk: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `SPRING_PROFILES_ACTIVE=prod`
- [ ] Build production Docker image: `docker build -t fintrack .`
- [ ] Deploy to EC2 or Elastic Beanstalk — confirm app starts and connects to RDS
- [ ] Configure GitHub Actions deploy step — triggers on green merge to `main`, deploys to AWS
- [ ] Get live URL — add to README as primary link
- [ ] Cold test: open live Swagger URL in incognito, register, login, run all 5 features

### Final V1 Checklist
- [ ] Live AWS URL responds (not localhost)
- [ ] Swagger UI loads at that URL — cold, no setup
- [ ] Register + login returns JWT
- [ ] Transaction CRUD works authenticated
- [ ] Category summary returns correct totals
- [ ] Fraud flag appears on qualifying transactions
- [ ] `/actuator/health` returns `{"status":"UP"}`
- [ ] JWT enforced — unauthenticated requests return 401
- [ ] User data scoped — no cross-user data leakage
- [ ] All tests green in CI
- [ ] GitHub Actions pipeline visible and passing
- [ ] README contains live URL + setup steps + sample call sequence
- [ ] `docker-compose up` works on a fresh machine

**V1 is shipped when every box above is checked. Partial is not done.**