# ISSUES.md — Bug Tracker & Solutions Log
# FinTrack: Personal Finance REST API

> AI fills this file when issues are found and solved.
> Every resolved bug gets logged here with root cause and prevention rule.
> The Pre-Seeded Gotchas section is institutional knowledge — read it before debugging anything.

---

## Issue Format

When logging a new issue, copy this template exactly:

```
### ISSUE-[N]: [Title]
**Date:** YYYY-MM-DD
**Status:** 🔴 Open / 🟡 In Progress / ✅ Resolved / ⚪ Reference
**Category:** Bug / Gotcha / Limitation / API Quirk
**Files:** [path/to/affected/file.java]

**What happened:** [what you saw vs what you expected]
**Error:** [exact error message or stack trace excerpt]
**Root cause:** [why it happened]
**Solution:** [exactly how it was fixed, with code if helpful]
**Prevention:** [the rule that prevents this from happening again]
```

---

## Status Legend

🔴 Open — known problem, not yet fixed
🟡 In Progress — being worked on right now
✅ Resolved — fixed, root cause documented
⚪ Reference — not a bug, but a known trap to watch for

---

## Open 🔴

*None.*

---

## In Progress 🟡

*None.*

---

## Resolved ✅

*This section grows during development. Every fix gets logged here.*

---

## Known Limitations

These are intentional. AI must not attempt to "fix" them.

- **No frontend** — Swagger UI is the interface. A 404 at `/` is expected behavior, not a bug.
- **Rule-based fraud only** — `isFlagged` uses the 3x average rule. No ML model. Do not add scoring logic.
- **No real bank data** — Transactions are manually entered. No Plaid integration.
- **No email notifications** — No SMTP, no email service. Auth is JWT only.
- **No admin role** — Single user type. 403 on cross-user access is correct behavior, not a permissions bug.
- **No real-time updates** — No WebSockets. Stale data on repeated GET is expected.
- **Hibernate DDL in dev** — Schema is auto-managed in dev (`ddl-auto=update`). Missing columns in dev are a config issue, not a code bug.

---

## Pre-Seeded Gotchas ⚪

These are the most common traps for this exact stack. Read this section before Googling anything.

---

### GOTCHA-01: JJWT 0.12.x vs 0.9.x API mismatch
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Copying any Spring Boot + JWT tutorial written before 2023. They all use 0.9.x patterns.

**What you'll see:**
```
NoSuchMethodError: io.jsonwebtoken.JwtBuilder.setSubject
```
Or worse — it compiles and runs but token validation silently fails.

**Root cause:** JJWT completely rewrote its API in 0.12.x. The old fluent methods (`setSubject`, `setExpiration`, `signWith(key, SignatureAlgorithm.HS256)`) are removed.

**Correct 0.12.x pattern:**
```java
// CORRECT — 0.12.x
String token = Jwts.builder()
    .subject(username)
    .issuedAt(new Date())
    .expiration(new Date(System.currentTimeMillis() + expirationMs))
    .signWith(getSigningKey(), Jwts.SIG.HS256)
    .compact();

// Parse
Claims claims = Jwts.parser()
    .verifyWith(getSigningKey())
    .build()
    .parseSignedClaims(token)
    .getPayload();
```

**Prevention:** Never copy JWT code from a tutorial without checking the JJWT version it was written for. If it uses `setSubject` or `SignatureAlgorithm` enum — it's 0.9.x. Rewrite it.

---

### GOTCHA-02: Spring Security 6.x — `WebSecurityConfigurerAdapter` is gone
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Following any Spring Security tutorial written before Spring Boot 3.x (mid-2022).

**What you'll see:**
```
Cannot resolve symbol 'WebSecurityConfigurerAdapter'
```

**Root cause:** Spring Security 6 (which ships with Spring Boot 3.x) removed the adapter-based approach entirely. You cannot extend `WebSecurityConfigurerAdapter`.

**Correct Spring Security 6.x pattern:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/swagger-ui/**",
                                 "/v3/api-docs/**", "/actuator/health").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

**Prevention:** If any Spring Security code you're looking at extends `WebSecurityConfigurerAdapter` or calls `http.authorizeRequests()` (not `authorizeHttpRequests`) — it's pre-Spring Boot 3. Do not use it.

---

### GOTCHA-03: JWT filter not registering — requests bypass auth silently
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Creating `JwtAuthFilter` as a `@Component` without also wiring it into `SecurityConfig`.

**What you'll see:** Protected endpoints return 200 without a token. No error. Auth is simply not running.

**Root cause:** Spring creates the filter bean but doesn't automatically insert it into the security filter chain. You must explicitly add it with `addFilterBefore`.

**Solution:**
```java
// In SecurityConfig.filterChain():
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
// NOT addFilterAfter — it must run BEFORE, or token validation never happens
```

**Prevention:** After wiring the JWT filter, always test with a request that has no token. If it returns anything other than 401, the filter is not running.

---

### GOTCHA-04: Lombok and MapStruct annotation processor order
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Declaring MapStruct before Lombok in `annotationProcessorPaths` in `pom.xml`.

**What you'll see:**
```
error: No property named "X" exists in source parameter(s).
```
Or mapper methods generate with null fields even though the entity has them.

**Root cause:** MapStruct reads the getters/setters that Lombok generates. If MapStruct runs first, Lombok's generated methods don't exist yet — so MapStruct can't find the fields.

**Correct `pom.xml` order:**
```xml
<annotationProcessorPaths>
    <path>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>  <!-- Lombok FIRST -->
    </path>
    <path>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct-processor</artifactId>  <!-- MapStruct SECOND -->
    </path>
</annotationProcessorPaths>
```

**Prevention:** Order in `annotationProcessorPaths` matters. Lombok always comes before MapStruct. Check this first whenever mapper fields come back null.

---

### GOTCHA-05: `permitAll()` on Swagger UI breaks because of wrong path patterns
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Allowing `/swagger-ui.html` but forgetting the internal Springdoc paths that Swagger UI calls.

**What you'll see:** Swagger UI loads but all API calls return 401. The UI itself opens, but clicking "Try it out" fails.

**Root cause:** Springdoc serves the UI at `/swagger-ui.html` but makes internal requests to `/swagger-ui/**` and `/v3/api-docs/**`. If those paths aren't also permitted, every Swagger request gets blocked.

**Correct permit list:**
```java
.requestMatchers(
    "/api/auth/**",
    "/swagger-ui/**",       // ← not just /swagger-ui.html
    "/swagger-ui.html",
    "/v3/api-docs/**",      // ← required for Swagger to load spec
    "/actuator/health"
).permitAll()
```

**Prevention:** Always test Swagger UI end-to-end after configuring security. The UI loading is not proof that it works — click "Try it out" on a public endpoint.

---

### GOTCHA-06: `BCryptPasswordEncoder` not defined as a bean — circular dependency
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Instantiating `BCryptPasswordEncoder` with `new` inside `AuthService` instead of injecting it.

**What you'll see:**
```
The dependencies of some of the beans in the application context form a cycle:
authService → userDetailsServiceImpl → authService
```

**Root cause:** `AuthService` needs `PasswordEncoder`. `UserDetailsServiceImpl` needs `AuthRepository`. Spring Security's auth manager needs `UserDetailsService`. If `PasswordEncoder` is created inside `AuthService` instead of as a separate bean, Spring can't resolve the dependency graph.

**Solution:** Define `BCryptPasswordEncoder` as a `@Bean` in `SecurityConfig` or a separate `@Configuration` class, and inject it into `AuthService`:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Prevention:** Never use `new BCryptPasswordEncoder()` inside a service. Always inject it.

---

### GOTCHA-07: JWT secret too short — silent failure at startup or runtime
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Using a short string as the JWT secret (e.g. `"secret"`, `"mysecretkey"`).

**What you'll see:**
```
io.jsonwebtoken.security.WeakKeyException: The signing key's size is 48 bits
which is not secure enough for the HS256 algorithm.
```
Or in older JJWT versions — no error at all, but tokens are cryptographically weak.

**Root cause:** HS256 requires a minimum 256-bit (32-character) key. JJWT 0.12.x enforces this and throws at runtime.

**Prevention:** Use a minimum 32-character random string in `.env`. Never test with `"secret"` or any human-readable word. Generate with:
```bash
openssl rand -base64 32
```

---

### GOTCHA-08: `@Transactional` missing on integration tests — dirty data between tests
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Writing `@SpringBootTest` integration tests without `@Transactional` on the test class.

**What you'll see:** Tests pass individually but fail when run together. A test that creates a user fails because a user with that email already exists from a previous test.

**Root cause:** Without `@Transactional`, each test commits its data to the H2 database. Later tests run against dirty state.

**Solution:**
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional  // ← rolls back all DB changes after each test
class AuthControllerTest { ... }
```

**Prevention:** Every integration test class gets `@Transactional`. No exceptions.

---

### GOTCHA-09: `application-test.yml` not activating — tests hit the real database
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Forgetting `@ActiveProfiles("test")` on integration test classes.

**What you'll see:** Tests run slowly (they're hitting PostgreSQL instead of H2), or they fail with connection errors if PostgreSQL isn't running.

**Root cause:** Spring Boot only loads `application-test.yml` when the `test` profile is active. Without `@ActiveProfiles("test")`, the default `application-dev.yml` loads and tests try to connect to your local PostgreSQL.

**Prevention:** `@ActiveProfiles("test")` is required on every `@SpringBootTest` class. Add it to your test class template so it's never forgotten.

---

### GOTCHA-10: User ID extracted from request body instead of JWT — security hole
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Accepting `userId` as a field in `TransactionRequest` DTO instead of reading it from the `SecurityContext`.

**What you'll see:** No error. But any authenticated user can pass any `userId` in the request body and create or read transactions as another user.

**Root cause:** Trusting client-supplied identity. The JWT already proves who the user is — the server should never ask the client to also tell it.

**Correct pattern:**
```java
// In controller — get userId from SecurityContext, not request body
String username = SecurityContextHolder.getContext()
    .getAuthentication().getName();
User user = userRepository.findByEmail(username).orElseThrow();
// Pass user.getId() to service — never accept it from the request
```

**Prevention:** `TransactionRequest` DTO never contains a `userId` field. User identity always comes from the JWT via `SecurityContext`.

---

### GOTCHA-11: MapStruct mapper not generating — missing `@Mapper` config
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Defining a MapStruct interface without `componentModel = "spring"`.

**What you'll see:**
```
Consider defining a bean of type 'com.fintrack.transaction.TransactionMapper' in your configuration.
```

**Root cause:** Without `componentModel = "spring"`, MapStruct generates the implementation but does not register it as a Spring bean. It can't be injected.

**Correct:**
```java
@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionResponse toResponse(Transaction transaction);
    Transaction toEntity(TransactionRequest request);
}
```

**Prevention:** Every MapStruct `@Mapper` interface includes `componentModel = "spring"`. Without it, Spring can't inject it.

---

### GOTCHA-12: Hibernate `ddl-auto=update` adds columns but never removes them
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Renaming or removing a field from a JPA entity while using `ddl-auto=update`.

**What you'll see:** The old column stays in the database. The new column is added. Queries against the old column name break at runtime.

**Root cause:** `ddl-auto=update` is additive only. It adds new columns and tables but never drops or renames existing ones.

**Prevention:** When changing an entity field name in dev, either: (a) drop and recreate the table manually, or (b) run `docker-compose down -v` to reset the database volume. Never rely on `update` to handle renames.

---

### GOTCHA-13: Fraud flag not calculating correctly — wrong average on first transaction
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Dividing by zero when calculating the user's average on their first transaction.

**What you'll see:** `ArithmeticException: / by zero`, or the first transaction incorrectly gets flagged.

**Root cause:** Average = total / count. If count is 0 (no prior transactions), division by zero.

**Prevention:** `FraudDetectionService.isFraudulent()` must handle the zero-average case explicitly — if `userAverage` is 0 or null, return `false` immediately. This case is covered in `FraudDetectionServiceTest`.

---

### GOTCHA-14: Docker container starts but app fails — port already in use
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Running `docker-compose up` when another process is already using port 8080 or 5432.

**What you'll see:**
```
Caused by: java.net.BindException: Address already in use
```
or the PostgreSQL container exits immediately.

**Root cause:** Another app or a previous Docker container is holding the port.

**Solution:**
```bash
# Find what's using port 8080
lsof -i :8080
# Kill it, or stop existing Docker containers
docker-compose down
docker ps -a  # check for orphaned containers
```

**Prevention:** Always run `docker-compose down` before `docker-compose up` if you've had containers running before.

---

### GOTCHA-15: GitHub Actions fails — environment variables not set in CI
**Status:** ⚪ Reference
**Category:** Gotcha

**What triggers it:** Running integration tests in CI without setting `DB_URL`, `JWT_SECRET`, etc. as GitHub Actions secrets.

**What you'll see:** Build passes locally, fails in CI with `IllegalArgumentException: Could not resolve placeholder 'JWT_SECRET'`.

**Root cause:** `.env` is gitignored. CI has no access to local environment variables unless they're explicitly set as GitHub Actions secrets or environment variables in the workflow file.

**Solution:** Add all required env vars as GitHub Actions secrets (Settings → Secrets → Actions), then reference them in `ci-cd.yml`:
```yaml
env:
  JWT_SECRET: ${{ secrets.JWT_SECRET }}
  DB_URL: ${{ secrets.DB_URL }}
```

**Prevention:** Integration tests in CI need the same environment variables as local. Set them as secrets before pushing a CI pipeline that runs integration tests.

---

## Third-Party API Quirks

### Spring Boot Actuator — all endpoints exposed by default in dev
By default, Actuator exposes `/actuator/env` which can leak environment variable names (not values, but names). In production config, explicitly limit exposure:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health, info  # whitelist only — not "exclude: *"
```

### Springdoc OpenAPI — model names conflict with Java reserved words
If a DTO field is named `default`, `class`, or any Java reserved word, Springdoc will generate broken OpenAPI spec. Use alternative names (`defaultValue`, `type`, etc.).

### PostgreSQL — case-sensitive identifiers with Hibernate
Hibernate generates lowercase table and column names by default. PostgreSQL is case-sensitive when identifiers are quoted. If you mix quoted and unquoted references in SQL queries, you get `relation "User" does not exist`. Stick to lowercase naming in entities and never quote identifiers manually.

### AWS RDS free tier — connection limit on db.t3.micro
The db.t3.micro instance allows roughly 17 concurrent connections. HikariCP's default pool size is 10 — fine for a portfolio project. But if you run load tests against RDS, you'll hit connection exhaustion. For interviews, this is a talking point about the difference between dev and prod sizing.

---

## Patterns to Watch

| Pattern | What goes wrong | Prevention |
|---|---|---|
| Copying JWT tutorial code | Uses deprecated 0.9.x API, compiles but fails at runtime | Check JJWT version in every code example before using it |
| Googling Spring Security config | Returns Spring Boot 2.x / Security 5.x patterns, removed in Boot 3 | Check `authorizeRequests` vs `authorizeHttpRequests` — old API is a red flag |
| Adding `userId` to request DTOs | Any user can impersonate another user | User identity always comes from `SecurityContext`, never from the request |
| Integration tests without `@Transactional` | Tests pollute each other, fail in sequence but pass individually | `@Transactional` on every `@SpringBootTest` class, always |
| Skipping `@ActiveProfiles("test")` | Tests hit real PostgreSQL, slow or broken in CI | Add it to every integration test class |
| `ddl-auto=update` after renaming a field | Old column stays, new column added, runtime errors | Drop and recreate table after any entity field rename |
| Short JWT secret | `WeakKeyException` at runtime, or weak tokens | Minimum 32 characters, generated with `openssl rand -base64 32` |
| MapStruct without `componentModel = "spring"` | Bean injection fails at startup | Always include `componentModel = "spring"` in `@Mapper` |
| Lombok after MapStruct in processor order | Mapper generates with null fields, silent compile failure | Lombok always before MapStruct in `annotationProcessorPaths` |
| `permitAll()` only on `/swagger-ui.html` | Swagger loads but all calls return 401 | Also permit `/swagger-ui/**` and `/v3/api-docs/**` |