# AIRULES.md — Non-Negotiable AI Rules
# FinTrack: Personal Finance REST API

> Every AI tool working on this project reads this file first.
> These rules are not suggestions. Violations block progress.

---

## 🔴 Absolute Rules

1. **Never touch code outside the requested scope.** If asked to modify `TransactionService`, only `TransactionService` changes. Adjacent files — even if the AI "notices something" — are off limits unless explicitly included in the request.

2. **Never add a dependency not listed in TECH.md.** Nothing goes into `pom.xml` without first appearing in TECH.md. If a library seems useful, stop and ask. Do not add it and explain later.

3. **Never hardcode secrets.** No passwords, JWT secrets, database credentials, or API keys in any `.java` file, `.yml` file, or any file that gets committed. All secrets reference environment variables via `${VAR_NAME}`. `.env` is gitignored. `.env.example` always stays current.

4. **Never rewrite a file that is already locked and tested.** Once a file has passing tests, it does not get restructured, "improved," or reformatted unless the request explicitly says so.

5. **Never use JJWT 0.9.x patterns.** The codebase uses JJWT 0.12.x. The old API (`Jwts.builder().setSubject(...)`, `Claims.getSubject()`, string-based signing) is gone. Any generation using deprecated patterns is a hard failure. Correct pattern only:
   ```java
   Jwts.builder()
       .subject(username)
       .issuedAt(new Date())
       .expiration(new Date(System.currentTimeMillis() + expiration))
       .signWith(getSigningKey(), Jwts.SIG.HS256)
       .compact();
   ```

6. **Never use `WebSecurityConfigurerAdapter`.** Spring Boot 3.x uses Spring Security 6.x. The adapter-based approach is removed. Security config must use the `SecurityFilterChain` bean approach only.

7. **Never swallow exceptions silently.** Empty catch blocks are banned. Every caught exception either gets rethrown, logged with meaningful context, or delegated to `GlobalExceptionHandler`. This pattern never appears:
   ```java
   } catch (Exception e) {
       // nothing here
   }
   ```

---

## 🟡 Code Quality Rules

8. **Max 200 lines per class.** If a class approaches this, it is doing too much. Split responsibilities before continuing.

9. **Constructor injection only.** Never use field injection (`@Autowired` on a field). Constructor injection makes dependencies explicit and Mockito-mockable:
   ```java
   // Correct
   private final TransactionRepository repo;
   public TransactionService(TransactionRepository repo) { this.repo = repo; }

   // Never
   @Autowired
   private TransactionRepository repo;
   ```

10. **Never use `new` inside a Spring-managed class for dependencies.** If a class needs another service or repository, it receives it via constructor injection. `new FraudDetectionService()` inside `TransactionService` is a unit test killer.

11. **Every async or I/O operation gets proper exception handling.** No unchecked exceptions propagating to the controller raw — they go through `GlobalExceptionHandler` with a meaningful message and HTTP status.

12. **No business logic in controllers.** Controllers parse the request, call a service method, and return a response DTO. Conditionals, calculations, and data transformations belong in the service layer.

13. **No direct repository calls from controllers.** Data access goes Controller → Service → Repository, always. No skipping layers.

14. **Every method does one thing.** If a method name requires "and" to describe it, it needs to be split.

15. **Verify method signatures against pinned library versions before generating.** JJWT 0.12.x, Spring Boot 3.x, MapStruct 1.5.x — if unsure whether a method exists in the pinned version, say so rather than generating a plausible-looking call that fails at runtime.

---

## 🔵 Architecture Rules

16. **Every file has exactly one correct home per ARCHITECTURE.md.** Before creating any file, check the file placement table. No exceptions, no "it made sense here."

17. **Feature-owned classes stay in their feature package.** `TransactionController`, `TransactionService`, `TransactionRepository`, `Transaction` entity, and all transaction DTOs live in `com.fintrack.transaction`. They do not get moved to `common/` for any reason.

18. **`common/` is for cross-cutting concerns only.** A class goes in `common/` only if it is genuinely used by two or more feature packages. Security config, JWT utilities, exception handling, and OpenAPI config belong here. Feature logic does not.

19. **Services never return JPA entities.** The service layer maps all outgoing data to response DTOs via MapStruct before returning to the controller. Raw entities never reach the controller or the HTTP response.

20. **`FraudDetectionService` stays stateless.** It takes inputs (transaction amount, user average), returns a boolean or flag, and calls no repository. No `@Autowired` repository inside this class. If fraud logic ever needs DB access, stop and discuss the design change.

21. **DTOs live in `{feature}/dto/`.** Request objects, response objects, and category summary shapes all live in the feature's `dto/` subdirectory. No DTO goes in `common/` unless multiple features use the exact same shape.

22. **Test files mirror the main package structure.** `TransactionServiceTest.java` lives at `src/test/java/com/fintrack/transaction/`. No flat `/tests` dump at the project root.

---

## 🛡️ Security Rules

23. **JWT filter must be inserted before `UsernamePasswordAuthenticationFilter` in the security chain.** Wrong position = JWT validation skipped. Every `SecurityConfig` generation must include:
    ```java
    .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
    ```

24. **`permitAll()` applies only to explicitly public routes.** The only routes that bypass authentication are `POST /api/auth/register` and `POST /api/auth/login`. Every other route requires a valid JWT. Never use `.anyRequest().permitAll()`.

25. **Passwords are never stored in plaintext.** `BCryptPasswordEncoder` is used for all password hashing. No MD5, no SHA-1, no raw storage.

26. **JWT secret key must be at least 256 bits (32 characters) for HS256.** Shorter keys are rejected by JJWT 0.12.x at runtime. The key comes from `${JWT_SECRET}` — never hardcoded.

27. **User data is always scoped to the authenticated user.** When fetching transactions, the repository query filters by the JWT-derived user ID. A user can never retrieve, update, or delete another user's transactions — enforce this at the service layer, not just the controller.

28. **No PII in logs.** Never log passwords, JWT tokens, or full transaction details at any log level. Log user IDs and operation names only.

29. **CORS config must be explicit.** Never use `.cors().disable()` or allow all origins in production config. Define allowed origins explicitly in `SecurityConfig`.

30. **Actuator endpoints are secured.** `/actuator/health` may be public. All other actuator endpoints (`/actuator/metrics`, `/actuator/env`, etc.) require authentication or are disabled in production config.

---

## 🎨 UI/UX Rules

Not applicable — this project has no frontend. Swagger UI is the interface.

**Swagger-specific rules:**
31. Every endpoint has an `@Operation` annotation with a plain-English summary an interviewer can read.
32. Every request/response DTO has `@Schema` annotations on fields that need explanation.
33. The Swagger UI JWT auth header input is configured in `OpenApiConfig` so interviewers can authenticate without reading code.

---

## 🔄 Workflow Rules

34. **Read PRD.md, TECH.md, and ARCHITECTURE.md before generating any code.** Not a suggestion — this is the first step of every session.

35. **State scope before writing code.** Before generating a file, say: "I am generating `TransactionService.java`. This touches: `Transaction.java`, `TransactionRepository.java`, `FraudDetectionService.java`. Nothing else." If the actual output touches something not listed, explain why before doing it.

36. **Ask before adding anything that isn't explicitly requested.** A missing null check, an extra validation, an additional endpoint — ask first. Do not add it and mention it at the end.

37. **Flag version ambiguity immediately.** If a code pattern could apply to multiple library versions (JJWT, Spring Security, MapStruct), say which version the generated code targets and cite the API.

38. **Never present plausible-but-unverified code as correct.** If unsure whether a Spring Data query derivation, JJWT method, or Mockito pattern works with the pinned version, say "I believe this is correct for version X — verify before committing" rather than stating it confidently.

39. **One file at a time in complex areas.** Auth and security config are generated one class at a time. No "here's the whole security package" dumps.

40. **After generating a file, state what test should be written next.** Do not wait to be asked.

---

## ⚙️ Project-Specific Rules

41. **The 3x fraud rule is rule-based and intentionally simple.** Do not upgrade it to ML scoring, external API calls, or weighted averages without an explicit request. The simplicity is a feature — the candidate can explain the trade-off and describe what v2 would look like.

42. **Every fraud flag is recorded on the transaction, not in a separate table.** A boolean `isFlagged` field on the `Transaction` entity is sufficient for v1. No separate `FraudAlert` entity unless explicitly requested.

43. **Category is a plain string field on `Transaction`, not a separate normalized entity.** No `Category` table, no foreign key to a lookup table. A simple `String category` field covers v1. Normalization is a v2 decision worth discussing in interviews.

44. **All API endpoints are prefixed with `/api/`.** No exceptions. `AuthController` exposes `/api/auth/**`, `TransactionController` exposes `/api/transactions/**`.

45. **The project must run end-to-end with `docker-compose up`.** Any change that breaks local Docker setup is a blocker. Never generate config that requires manual environment setup steps not documented in README.

46. **GitHub Actions pipeline runs tests before any deployment step.** A failed test must block deployment. Never configure the pipeline to skip tests or deploy on a failed build.

47. **The README is treated as interviewer-facing documentation.** It must always contain: the live Swagger URL, local setup steps, a brief architecture summary, and a sample API call sequence. Keep it current.