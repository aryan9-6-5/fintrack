# PLAN.md — Project Plan & Progress Tracker
# FinTrack: Personal Finance REST API

> AI reads the Current Status block first every session.
> Update it last before closing. Never skip this step.

---

## 📍 Current Status

**Stage:** 1 — All stages 1–4 complete
**Step:** Stage 4 Security Hardening — Done
**Updated:** 2026-03-18
**Blockers:** None

---

## Stage Overview

| Stage | Name | Status |
|---|---|---|
| 1 | Setup — repo, config, DB, CI scaffold | ✅ Done |
| 2 | Core — entities, security, JWT, base wiring | ✅ Done |
| 3 | Features — auth, transactions, fraud, summaries | ✅ Done |
| 4 | Security Hardening | ✅ Done |
| 5 | Testing | ⬜ Not Started |
| 6 | Polish & Deploy | ⬜ Not Started |

---

## Stage 1: Setup

**Goal:** A running Spring Boot app connected to PostgreSQL, containerized, with CI pipeline active. No feature code yet.

- [x] Generate project via Spring Initializr with dependencies: Spring Web, Spring Data JPA, Spring Security, PostgreSQL Driver, Lombok, Validation, Actuator
- [x] Add manual dependencies to `pom.xml`: JJWT 0.12.x (3 artifacts), MapStruct 1.5.x, Springdoc OpenAPI 2.x
- [x] Fix annotation processor order in `pom.xml`: Lombok declared before MapStruct in `annotationProcessorPaths`
- [x] Create folder structure exactly per ARCHITECTURE.md: `auth/`, `transaction/`, `fraud/`, `common/security/`, `common/exception/`, `common/config/`
- [x] Create `application.yml`, `application-dev.yml`, `application-prod.yml`, `application-test.yml`
- [x] Create `.env.example` with all variables from ARCHITECTURE.md environment table
- [x] Create `.env` locally (gitignored) with real dev values
- [x] Add `.env` to `.gitignore` — verify it does not appear in `git status`
- [x] Create `docker-compose.yml` — spins up app + PostgreSQL 15
- [x] Create `Dockerfile` — multi-stage build (build with Maven, run with JRE)
- [x] Verify: `docker-compose up` starts both containers, app connects to DB, no errors
- [x] Verify: `GET /actuator/health` returns `{"status":"UP"}`
- [x] Create `.github/workflows/ci-cd.yml` — runs `mvn test` on every push
- [x] Push to GitHub — confirm GitHub Actions run appears and passes (empty test suite passes)
- [x] Create `README.md` skeleton with sections: Overview, Local Setup, API Reference, Architecture, Live URL (placeholder)

**Stage 1 is done when:** `docker-compose up` runs clean, `/actuator/health` responds, and GitHub Actions shows a green build.

---

## Stage 2: Core Architecture

**Goal:** Database schema live, Spring Security configured, JWT working end to end. No feature endpoints yet — just the foundation every feature builds on.

### 2a. Database & Entities

- [x] Create `User.java` entity in `auth/` — fields: `id` (Long), `email` (unique), `password` (hashed), `createdAt`
- [x] Create `Transaction.java` entity in `transaction/` — fields: `id`, `userId` (FK to User), `amount` (BigDecimal), `type` (INCOME/EXPENSE enum), `category` (String), `description` (String, nullable), `isFlagged` (boolean, default false), `createdAt`
- [x] Create `TransactionType.java` enum in `transaction/` — values: `INCOME`, `EXPENSE`
- [x] Verify Hibernate creates schema correctly: `spring.jpa.hibernate.ddl-auto=update`, check tables appear in DB
- [x] Connect to local PostgreSQL via DBeaver or psql and confirm `users` and `transactions` tables exist with correct columns

### 2b. Spring Security & JWT

- [x] Create `JwtUtil.java` in `common/security/` — methods: `generateToken(username)`, `extractUsername(token)`, `isTokenValid(token, userDetails)`
- [x] Use JJWT 0.12.x API only — `Jwts.builder().subject(...).signWith(key, Jwts.SIG.HS256).compact()`
- [x] Create `JwtAuthFilter.java` in `common/security/` — extends `OncePerRequestFilter`, extracts Bearer token, validates, sets `SecurityContextHolder`
- [x] Create `UserDetailsServiceImpl.java` in `common/security/` — loads user by email from `AuthRepository`
- [x] Create `SecurityConfig.java` in `common/security/` — `SecurityFilterChain` bean, stateless session, CSRF disabled, `permitAll` only on `/api/auth/**`, all other routes require auth, JWT filter inserted before `UsernamePasswordAuthenticationFilter`
- [x] Write `JwtUtilTest.java` — test token generation, extraction, expiry, tampered token rejection
- [x] Verify: unauthenticated `GET /api/transactions` returns 401 (endpoint doesn't exist yet — expect 401 not 404)

### 2c. Exception Handling & OpenAPI

- [x] Create `ApiErrorResponse.java` in `common/exception/` — fields: `status`, `message`, `timestamp`
- [x] Create `ResourceNotFoundException.java` and `UnauthorizedException.java` in `common/exception/`
- [x] Create `GlobalExceptionHandler.java` in `common/exception/` — `@ControllerAdvice`, catches all custom exceptions + `MethodArgumentNotValidException`, returns `ApiErrorResponse`
- [x] Create `OpenApiConfig.java` in `common/config/` — adds JWT Bearer auth scheme to Swagger UI so interviewers can authenticate without reading code
- [x] Verify: Swagger UI loads at `/swagger-ui.html` with JWT auth button visible

**Stage 2 is done when:** Schema exists in DB, JWT round-trip works in isolation (unit test passes), unauthenticated requests return 401, Swagger UI loads with auth configured.

---

## Stage 3: Features

### 3a. Auth — Register & Login

**Depends on:** Stage 2 complete

- [x] Create `RegisterRequest.java` and `LoginRequest.java` DTOs in `auth/dto/` — add Bean Validation annotations (`@NotBlank`, `@Email`)
- [x] Create `AuthResponse.java` DTO in `auth/dto/` — field: `token` (String)
- [x] Create `AuthRepository.java` in `auth/` — `findByEmail(String email)` method
- [x] Create `AuthService.java` in `auth/` — `register(RegisterRequest)` hashes password with BCrypt, saves user, returns JWT; `login(LoginRequest)` validates credentials, returns JWT
- [x] Create `AuthController.java` in `auth/` — `POST /api/auth/register`, `POST /api/auth/login`, both `@Operation` annotated for Swagger
- [x] Write `AuthServiceTest.java` — test register success, duplicate email, login success, wrong password, user not found
- [x] Write `AuthControllerTest.java` — integration test full register → login → JWT returned flow, 401 responses
- [x] Manual Swagger check: register → copy token → click Authorize → login confirms same user

**Acceptance criteria:** A cold tester can register, log in, receive a JWT, and authenticate subsequent requests through Swagger with zero setup.

---

### 3b. Transaction CRUD

**Depends on:** 3a (auth) complete — all endpoints require valid JWT

- [x] Create `TransactionRequest.java` DTO — fields: `amount`, `type`, `category`, `description`; add `@NotNull`, `@Positive` validation
- [x] Create `TransactionResponse.java` DTO — all fields including `isFlagged`, `createdAt`
- [x] Create MapStruct mapper `TransactionMapper.java` — `toResponse(Transaction)`, `toEntity(TransactionRequest)`
- [x] Create `TransactionRepository.java` — `findAllByUserId(Long userId)`, `findByIdAndUserId(Long id, Long userId)`
- [x] Create `TransactionService.java` — `create`, `getAll`, `getById`, `update`, `delete` — all scoped to authenticated user ID; calls `FraudDetectionService` on create/update
- [x] Create `TransactionController.java` — `POST /api/transactions`, `GET /api/transactions`, `GET /api/transactions/{id}`, `PUT /api/transactions/{id}`, `DELETE /api/transactions/{id}`; extract user ID from `SecurityContext`, never from request body
- [x] Write `TransactionServiceTest.java` — test each CRUD method, verify user scoping (user A cannot get user B's transaction)
- [x] Write `TransactionControllerTest.java` — integration test each endpoint with valid JWT, invalid JWT, and wrong-user scenarios
- [x] Manual Swagger check: create 3 transactions, retrieve all, update one, delete one

**Acceptance criteria:** Full CRUD works authenticated. Unauthenticated requests return 401. Cross-user access returns 403.

---

### 3c. Fraud Detection

**Depends on:** 3b (transaction CRUD) — `FraudDetectionService` is called by `TransactionService`

- [x] Create `FraudDetectionService.java` in `fraud/` — single public method `isFraudulent(BigDecimal amount, BigDecimal userAverage)` returns boolean; stateless, no repository, no `@Autowired` dependencies
- [x] Implement rule: `amount.compareTo(userAverage.multiply(BigDecimal.valueOf(3))) >= 0` → flagged; handle zero average (no flag)
- [x] Wire into `TransactionService.create()` — calculate user's historical average from repository, call `FraudDetectionService`, set `isFlagged` on entity before save
- [x] `isFlagged` appears in `TransactionResponse` DTO
- [x] Write `FraudDetectionServiceTest.java` — test: below 3x (no flag), exactly 3x (flag), above 3x (flag), zero average (no flag), first transaction (no flag)
- [x] Manual Swagger check: create several normal transactions, then one large one — confirm `isFlagged: true` in response

**Acceptance criteria:** `isFlagged: true` appears on any transaction at or above 3x the user's historical average. First transaction is never flagged. Zero-average edge case handled without error.

---

### 3d. Category Summary

**Depends on:** 3b (transactions exist to summarize)

- [x] Create `CategorySummaryResponse.java` DTO in `transaction/dto/` — fields: `category` (String), `total` (BigDecimal), `transactionCount` (int)
- [x] Add custom query to `TransactionRepository` — `@Query` that groups by category, sums amounts, filters by userId and optional month/year
- [x] Add `getSummary(Long userId, Integer month, Integer year)` to `TransactionService`
- [x] Add `GET /api/transactions/summary` to `TransactionController` — optional query params `?month=3&year=2026`
- [x] Write unit test for summary calculation in `TransactionServiceTest`
- [x] Write integration test for summary endpoint in `TransactionControllerTest`
- [x] Manual Swagger check: create transactions across 3 categories, hit summary endpoint, verify totals

**Acceptance criteria:** Summary endpoint returns per-category totals for the authenticated user only. Optional month/year filter works. Empty result set returns `[]` not 500.

---

## Stage 4: Security Hardening

**Goal:** Close every gap an interviewer could probe. These are not optional polish items.

- [x] Confirm `SecurityConfig` has no `anyRequest().permitAll()` — only `/api/auth/**` is open
- [x] Confirm JWT filter is inserted at `addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)` — not after
- [x] Confirm JWT secret in `.env` is at least 32 characters (256-bit minimum for HS256)
- [x] Confirm passwords stored as BCrypt hash — query `users` table directly and verify hash format (`$2a$...`)
- [x] Confirm no PII in any log statement — grep codebase for `log.*email`, `log.*password`, `log.*token`
- [x] Confirm all transaction queries filter by `userId` — no query returns all rows across users
- [x] Confirm `application-prod.yml` contains zero hardcoded secrets — all values via `${VAR_NAME}`
- [x] Confirm `.env` is in `.gitignore` and does not appear in `git log` history
- [x] Confirm Actuator: `/actuator/health` is public, `/actuator/env` is disabled or secured in prod config
- [x] Confirm CORS config is explicit — no wildcard `*` origin in production profile
- [x] Confirm `GlobalExceptionHandler` returns consistent `ApiErrorResponse` shape — not raw Spring error objects
- [x] Run `mvn dependency:check` — no known CVEs in declared dependencies

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