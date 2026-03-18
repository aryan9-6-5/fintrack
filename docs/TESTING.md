# TESTING.md — Test Strategy & Standards
# FinTrack: Personal Finance REST API

> Every AI tool reads this before writing or skipping tests.
> "It compiles" is not done. "It passes tests" is done.

---

## 1. Testing Philosophy

**Test everything that can break in production and would be embarrassing to explain in an interview.**

That means: auth flows, fraud logic, transaction operations, and any security boundary. Config boilerplate and framework wiring do not need tests. Business logic and security rules always do.

Code is not "done" until the relevant tests in this document pass. AI writes tests alongside every file it generates — no exceptions for the critical paths defined below.

---

## 2. Test Pyramid

```
        [ Integration Tests ]
       /api routes + DB operations
      ────────────────────────────
         [ Unit Tests ]          ← heaviest layer
    services, fraud logic, JWT util
```

**No E2E tests in v1.** Swagger UI is the interface — there is no browser flow to automate. Manual Swagger testing covers what E2E would normally handle.

| Layer | Count target | Rationale |
|---|---|---|
| Unit | High — every service method, every fraud edge case | Fast, isolated, no infrastructure needed |
| Integration | Medium — every controller endpoint, full auth flow | Catches Spring Security misconfig, DB query errors |
| E2E | None in v1 | No frontend, no browser — Swagger covers this manually |

**Coverage target:** 80%+ on `service` and `fraud` packages. Controller layer covered by integration tests. Config and DTOs exempt.

---

## 3. Unit Tests

### What must always have unit tests

| Class | What to test |
|---|---|
| `FraudDetectionService` | Exactly at 3x (boundary), above 3x (flag), below 3x (no flag), zero average, first transaction |
| `TransactionService` | Create transaction (fraud flag path + normal path), get by user, category summary calculation, delete authorization |
| `AuthService` | Register (success, duplicate email), login (success, wrong password, user not found) |
| `JwtUtil` | Token generation, token parsing, expired token rejection, tampered token rejection |

### What does NOT need unit tests

- JPA repository interfaces (Spring Data generates the implementation — test via integration tests)
- DTOs and entity classes (no logic)
- `SecurityConfig` (tested via integration tests — behavior matters, not wiring)
- `GlobalExceptionHandler` (tested as a side effect of controller integration tests)
- `OpenApiConfig` (no logic)

### File naming and location

```
src/test/java/com/fintrack/{feature}/{ClassName}Test.java

Examples:
  src/test/java/com/fintrack/fraud/FraudDetectionServiceTest.java
  src/test/java/com/fintrack/transaction/TransactionServiceTest.java
  src/test/java/com/fintrack/auth/AuthServiceTest.java
  src/test/java/com/fintrack/common/security/JwtUtilTest.java
```

### Unit test template

```java
package com.fintrack.fraud;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

    // Inject mocks if FraudDetectionService ever needs dependencies
    // Currently stateless — no mocks needed
    private FraudDetectionService fraudDetectionService;

    @BeforeEach
    void setUp() {
        fraudDetectionService = new FraudDetectionService();
    }

    @Test
    @DisplayName("Should flag transaction exactly at 3x average")
    void shouldFlagTransactionAtExactly3xAverage() {
        double userAverage = 100.0;
        double transactionAmount = 300.0;

        boolean result = fraudDetectionService.isFraudulent(transactionAmount, userAverage);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should flag transaction above 3x average")
    void shouldFlagTransactionAbove3xAverage() {
        double userAverage = 100.0;
        double transactionAmount = 301.0;

        boolean result = fraudDetectionService.isFraudulent(transactionAmount, userAverage);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should not flag transaction below 3x average")
    void shouldNotFlagTransactionBelow3xAverage() {
        double userAverage = 100.0;
        double transactionAmount = 299.99;

        boolean result = fraudDetectionService.isFraudulent(transactionAmount, userAverage);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should handle zero average without division error")
    void shouldHandleZeroAverage() {
        double userAverage = 0.0;
        double transactionAmount = 500.0;

        // First transaction — no average yet. Should not flag.
        boolean result = fraudDetectionService.isFraudulent(transactionAmount, userAverage);

        assertThat(result).isFalse();
    }
}
```

---

## 4. Integration Tests

### What must have integration tests

| Flow | Test class | What to verify |
|---|---|---|
| Register → Login → Access protected route | `AuthControllerTest` | Full JWT round trip works end to end |
| Unauthenticated request to protected route | `AuthControllerTest` | Returns 401, not 403, not 200 |
| Create transaction (authenticated) | `TransactionControllerTest` | 201 response, persisted to DB, fraud flag set correctly |
| Get transactions (only own data) | `TransactionControllerTest` | User A cannot retrieve User B's transactions |
| Category summary | `TransactionControllerTest` | Aggregation returns correct totals per category |
| Invalid JWT (tampered/expired) | `AuthControllerTest` | Returns 401 on every protected route |

### Test profile setup

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
  security:
    # Use a fixed test secret — short enough to be clear it's test-only
    # Never reuse this value outside tests
jwt:
  secret: test-secret-key-minimum-32-chars-long
  expiration-ms: 3600000
```

### Integration test template

```java
package com.fintrack.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.auth.dto.LoginRequest;
import com.fintrack.auth.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional  // rolls back after each test — no leftover data
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/register — should return JWT on success")
    void shouldRegisterAndReturnToken() throws Exception {
        RegisterRequest request = new RegisterRequest("test@example.com", "SecurePass123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/login — should reject wrong password with 401")
    void shouldRejectWrongPassword() throws Exception {
        // First register
        RegisterRequest register = new RegisterRequest("user@example.com", "CorrectPass123!");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)));

        // Then try wrong password
        LoginRequest login = new LoginRequest("user@example.com", "WrongPass!");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/transactions — should return 401 without token")
    void shouldRejectUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isUnauthorized());
    }
}
```

---

## 5. E2E Tests

**Not applicable for v1.** There is no frontend or browser flow to automate.

Manual Swagger testing (see Section 6) covers what E2E would normally handle. If a React frontend is added in v2, Playwright will be added here.

---

## 6. Manual Testing Checklist

Run this before every PR and every deploy. Check every box or do not merge.

### Auth
- [ ] `POST /api/auth/register` with a new email returns a JWT token
- [ ] `POST /api/auth/login` with correct credentials returns a JWT token
- [ ] `POST /api/auth/login` with wrong password returns 401
- [ ] Any protected endpoint called without `Authorization: Bearer <token>` returns 401
- [ ] Any protected endpoint called with a tampered token returns 401
- [ ] Any protected endpoint called with an expired token returns 401

### Transactions
- [ ] `POST /api/transactions` creates a transaction and returns 201
- [ ] `GET /api/transactions` returns only the authenticated user's transactions
- [ ] `PUT /api/transactions/{id}` updates the correct transaction
- [ ] `DELETE /api/transactions/{id}` removes the transaction
- [ ] `DELETE /api/transactions/{id}` with another user's transaction ID returns 403

### Fraud Detection
- [ ] A transaction with amount > 3x user average returns `"flagged": true`
- [ ] A transaction with amount exactly = 3x user average returns `"flagged": true`
- [ ] A transaction with amount < 3x user average returns `"flagged": false`
- [ ] First transaction (no average yet) does not get flagged

### Category Summary
- [ ] `GET /api/transactions/summary` returns correct totals per category
- [ ] Summary reflects only the authenticated user's transactions
- [ ] Filtering by month returns only that month's data (if supported)

### Infrastructure
- [ ] `GET /actuator/health` returns `{"status": "UP"}`
- [ ] Swagger UI loads at `/swagger-ui.html`
- [ ] All 5 feature areas are visible and callable in Swagger
- [ ] Docker: `docker-compose up` starts the app and DB with no manual steps
- [ ] README setup steps produce a running app on a fresh machine

---

## 7. AI Testing Rules

### AI must always write tests when:
- Generating any service class method
- Generating `FraudDetectionService` or any change to fraud logic
- Generating `JwtUtil` or any change to token handling
- Generating any controller endpoint
- Fixing a bug (write a failing test that reproduces it first, then fix)

### AI may skip tests when:
- Generating DTOs, entities, or config classes with no logic
- Generating `application.yml` or `docker-compose.yml`
- Explicitly told "generate the file only, I'll write tests separately"

### AI must never:
- Mark a service method or controller endpoint "done" without a corresponding test
- Mock the class under test (mock dependencies, not the subject)
- Write tests that only verify the mock was called — verify the actual output
- Use `@SpringBootTest` for pure unit tests (use `@ExtendWith(MockitoExtension.class)`)
- Write a test that passes vacuously (empty assertion, `assertTrue(true)`)
- Skip the boundary case for fraud detection (exactly 3x must always be tested)

### Self-check before saying "done":

```
□ Does every new service method have a unit test?
□ Does every new controller endpoint have an integration test?
□ Are fraud detection boundary cases covered (below, exactly at, above 3x)?
□ Is the auth round-trip tested (register → login → access protected route)?
□ Does `@Transactional` appear on every integration test class?
□ Are tests in the correct mirrored package location?
□ Does the test profile (H2) activate correctly (`@ActiveProfiles("test")`)?
```

---

## 8. Test Commands

```bash
# Run all tests
mvn test

# Run tests for a specific class
mvn test -Dtest=FraudDetectionServiceTest

# Run tests for a specific package
mvn test -Dtest="com.fintrack.fraud.*"

# Run with coverage report (generates to target/site/jacoco)
mvn test jacoco:report

# Run only integration tests (if tagged with @Tag("integration"))
mvn test -Dgroups=integration

# Skip tests (only for local build speed — never in CI)
mvn package -DskipTests

# Run with test profile explicitly
mvn test -Dspring.profiles.active=test
```

---

## 9. CI Integration

| Trigger | Tests that run | Blocks merge/deploy? |
|---|---|---|
| Every push to any branch | All unit tests | Yes — build fails |
| Pull request to `main` | All unit tests + all integration tests | Yes — PR cannot merge |
| Merge to `main` (green) | Full test suite + coverage check | Yes — deploy blocked if red |
| Pre-deploy to AWS | Full test suite | Yes — deploy blocked if any test fails |

**Coverage gate:** CI fails if line coverage on `com.fintrack.fraud` or `com.fintrack.transaction.service` drops below 80%. Configured via JaCoCo Maven plugin in `pom.xml`.