# SKILLS.md — AI Workflow Framework
# FinTrack: Personal Finance REST API

> This is the operating manual for every AI tool on this project.
> Read this before starting any session. The Standard Prompts section is the most useful part.

---

## 1. AI Tool Roster

| Tool | Role | Use for | Don't use for |
|---|---|---|---|
| **Claude** | Thinking, decisions, doc population | Architecture decisions, planning, doc generation, understanding tradeoffs, writing AIRULES/PRD/PLAN | Generating actual files, running code, creating file structures |
| **Antigravity** | Code generation, file creation, execution | Creating Java files, running `mvn test`, building Docker, executing terminal commands, writing tests | High-level design decisions, stack choices |
| **DeepSeek** | Code review | Reviewing generated code against AIRULES.md, catching security holes, checking patterns | First-pass generation, doc writing |
| **ChatGPT** | Quick concept questions | "What is X?", "How does Y work?", fast concept clarification when stuck | This project's specific decisions — it doesn't know your docs |

**Rule:** Decisions and thinking happen in Claude. Code and execution happen in Antigravity. Review happens in DeepSeek. Concept questions go to ChatGPT. Never use ChatGPT for project-specific decisions — it doesn't have your context.

---

## 2. Problem-Solving Framework

Every task follows this sequence. No skipping steps.

**1. Understand**
Read the task. Identify which PLAN.md stage and step this belongs to. If it's not in PLAN.md, ask whether it should be before starting.

**2. Locate**
Identify every file that will be created or changed. Check ARCHITECTURE.md for correct file locations. State them before writing any code.

**3. Check ISSUES.md and GOTCHA.md**
Before writing anything in `common/security/` or auth-related code — read GOTCHA.md first. Five minutes here saves three hours of debugging.

**4. Plan**
For anything beyond a single method — state the approach in one paragraph before generating. Include: what pattern is being used, what dependencies are needed, what tests will be written.

**5. Implement**
Generate one file at a time. Never dump a whole package at once. Start with the deepest dependency (entity → repository → service → controller).

**6. Self-review**
Run the self-review checklist in Section 6 before presenting anything as done.

**7. Document**
After every session: update STATE.md, log any new issues to ISSUES.md, mark PLAN.md checkboxes completed.

---

## 3. Code Generation Templates

Copy these skeletons when generating new files. They enforce ARCHITECTURE.md structure and AIRULES.md patterns.

### Entity Template

```java
package com.fintrack.{feature};

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "{table_name}")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class {EntityName} {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // fields here

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### Repository Template

```java
package com.fintrack.{feature};

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface {Feature}Repository extends JpaRepository<{Entity}, Long> {

    // Custom queries go here
    // Example: List<Transaction> findAllByUserId(Long userId);
    // Example: Optional<Transaction> findByIdAndUserId(Long id, Long userId);
}
```

### Service Template

```java
package com.fintrack.{feature};

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor  // constructor injection via Lombok — never @Autowired on fields
public class {Feature}Service {

    private final {Feature}Repository {feature}Repository;
    // inject other dependencies here

    public {ResponseDto} create({RequestDto} request, Long userId) {
        // 1. Build entity from request DTO
        // 2. Apply business logic
        // 3. Save via repository
        // 4. Map to response DTO via MapStruct mapper
        // 5. Return response DTO — never return the entity
    }
}
```

### Controller Template

```java
package com.fintrack.{feature};

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/{feature}s")
@RequiredArgsConstructor
@Tag(name = "{Feature}", description = "{Feature} management endpoints")
public class {Feature}Controller {

    private final {Feature}Service {feature}Service;

    @PostMapping
    @Operation(summary = "Create a new {feature}")
    @ApiResponse(responseCode = "201", description = "{Feature} created")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<{Feature}Response> create(@Valid @RequestBody {Feature}Request request) {
        // Get user identity from JWT — never from request body
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body({feature}Service.create(request, username));
    }
}
```

### MapStruct Mapper Template

```java
package com.fintrack.{feature};

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.fintrack.{feature}.dto.{Feature}Request;
import com.fintrack.{feature}.dto.{Feature}Response;

@Mapper(componentModel = "spring")  // ← always required for Spring injection
public interface {Feature}Mapper {

    {Feature}Response toResponse({Entity} entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    {Entity} toEntity({Feature}Request request);
}
```

### Unit Test Template

```java
package com.fintrack.{feature};

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)  // ← unit test: no Spring context, no DB
class {Feature}ServiceTest {

    @Mock
    private {Feature}Repository {feature}Repository;

    @InjectMocks
    private {Feature}Service {feature}Service;

    @Test
    @DisplayName("Should [expected behavior] when [condition]")
    void should{Behavior}When{Condition}() {
        // Arrange
        // Act
        // Assert — use AssertJ (assertThat), not JUnit assertEquals
    }
}
```

### Integration Test Template

```java
package com.fintrack.{feature};

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")   // ← activates H2, not real PostgreSQL
@Transactional            // ← rolls back all DB changes after each test
class {Feature}ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/{feature}s — should return 201 on success")
    void shouldCreate{Feature}Successfully() throws Exception {
        // Arrange: build request object
        // Act: perform mockMvc request
        // Assert: check status, response body fields
    }
}
```

---

## 4. Debugging Protocol

Follow these steps in order. Do not skip to step 4 before completing steps 1-3.

```
1. CHECK GOTCHA.md
   Does this error appear in the gotchas list?
   If yes → follow the documented solution exactly before trying anything else.

2. CHECK RECENT GIT CHANGES
   git diff HEAD~1
   What changed in the last commit? Is this error new since that change?

3. ISOLATE
   Can you reproduce it with the smallest possible test?
   Write a failing unit test that reproduces the bug before fixing it.

4. READ THE FULL STACK TRACE
   Not just the first line. The root cause is almost always at the bottom.
   Look for "Caused by:" — that's the real error.

5. VERIFY VERSIONS
   Is the error related to JJWT, Spring Security, or MapStruct?
   Check you're using the 0.12.x / Spring Boot 3.x / 1.5.x APIs.
   Old tutorial code looks correct but uses removed methods.

6. FIX AND LOG
   Fix the issue. Write the test that would have caught it.
   Log it in ISSUES.md with root cause and prevention rule.
   Update GOTCHA.md if it's a reusable gotcha.
```

**Rule:** If you're still stuck after 20 minutes, paste the full stack trace and the relevant file into Claude with the message: "I've checked GOTCHA.md and recent changes. Here's what I'm seeing: [error]. Here's the file: [code]."

---

## 5. Standard Prompts

### a) SESSION START — Short (paste this every time, under 30 seconds)

```
Project: FinTrack — Java/Spring Boot REST API, PostgreSQL, JWT, Docker, AWS
Docs: PRD.md, TECH.md, ARCHITECTURE.md, AIRULES.md, TESTING.md, PLAN.md, DESIGN.md, ISSUES.md, GOTCHA.md, STATE.md

Read STATE.md first. Tell me the current stage and next action.
Then wait for my instruction.

Key rules:
- Never touch files outside the requested scope
- Never add dependencies not in TECH.md
- Services never return entities — always DTOs
- User identity always from SecurityContext, never request body
- JJWT 0.12.x API only — check GOTCHA.md before writing any JWT or Security code
- Write tests alongside every service and controller file
```

---

### b) SESSION START — Full (for complex sessions or after a long break)

```
Project: FinTrack — Personal Finance REST API
Stack: Java 17, Spring Boot 3.x, PostgreSQL, Spring Security 6 + JJWT 0.12.x,
       MapStruct, Lombok, Springdoc OpenAPI, Docker, GitHub Actions, AWS

Package structure (hybrid — feature + common):
  com.fintrack.auth/          — registration, login, JWT
  com.fintrack.transaction/   — CRUD, category summary, fraud trigger
  com.fintrack.fraud/         — stateless fraud detection service
  com.fintrack.common/        — security, exception handling, config

Critical rules:
1. Never touch files outside requested scope
2. Never add dependencies not in TECH.md
3. JJWT 0.12.x API: .subject() not .setSubject(), Jwts.SIG.HS256 not SignatureAlgorithm
4. Spring Security 6.x: SecurityFilterChain bean only, no WebSecurityConfigurerAdapter
5. JWT filter: addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
6. Services never return entities — map to DTOs via MapStruct
7. User identity always from SecurityContext, never request body
8. Constructor injection only — no @Autowired on fields
9. Tests alongside every file — @Transactional on all integration tests
10. Read GOTCHA.md before writing any auth or security code

Read STATE.md. Confirm current stage and next action. Wait for my instruction.
```

---

### c) NEW FEATURE

```
Implement [feature name] from PLAN.md Stage [X] Step [Y].

Files to create:
- [list files per ARCHITECTURE.md]

Follow these patterns:
- Entity in com.fintrack.{feature}/
- DTOs in com.fintrack.{feature}/dto/
- Mapper: @Mapper(componentModel = "spring")
- Service: constructor injection, returns DTOs only
- Controller: user ID from SecurityContext only
- Tests: unit test for service, integration test for controller

Start with [first file]. Show me that file, then wait before moving to the next.
```

---

### d) BUG REPORT

```
There's a bug in [ClassName.java].

Error:
[paste exact error message and relevant stack trace lines]

Expected: [what should happen]
Actual: [what is happening]

Files involved: [list]
Recent changes: [what changed before this appeared, or "nothing — appeared fresh"]

Check GOTCHA.md first. If it's there, follow the documented solution.
If not, diagnose and fix. Then log it in ISSUES.md with root cause and prevention rule.
```

---

### e) DEEPSEEK CODE REVIEW

```
Review this Spring Boot code for a personal finance REST API.

Stack: Java 17, Spring Boot 3.x, Spring Security 6.x, JJWT 0.12.x, PostgreSQL, MapStruct, Lombok

Review against these rules:
1. JJWT: must use 0.12.x API (.subject(), Jwts.SIG.HS256) — flag any 0.9.x patterns
2. Spring Security: must use SecurityFilterChain, not WebSecurityConfigurerAdapter
3. JWT filter: must be addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
4. No field injection (@Autowired on fields) — constructor injection only
5. Services must return DTOs, not entities
6. User ID must come from SecurityContext, not request body
7. No hardcoded secrets
8. No empty catch blocks
9. No new dependencies outside: Spring Boot starter, JJWT 0.12.x, MapStruct, Lombok, Springdoc

Output format:
- CRITICAL: security holes or broken patterns (must fix before merge)
- WARNING: code quality issues (should fix)
- NOTE: suggestions (optional)
- PASS: if the file looks clean

[paste code here]
```

---

### f) CHATGPT CONCEPT QUESTION

```
Quick concept question — no project context needed.

[Your question here]

Keep it short. I just need to understand [concept], not implement it yet.
```

---

### g) SESSION END

```
Session complete. Please do the following:

1. Update STATE.md:
   - Last Completed: [what was done]
   - Current Step: [where we are now]
   - Next Action: [specific next thing]
   - Recent Changes: add a row to the table
   - Last Updated: today's date and time

2. Mark completed items in PLAN.md with ✅

3. If any new bugs or gotchas were found: log them in ISSUES.md

4. Post the session summary:
   ✅ Done: [list]
   📍 Now at: [stage + step]
   ➡️  Next: [next action]
   ⚠️  Open: [blockers or 'None']
   🕐 STATE.md updated [timestamp]
```

---

## 6. Self-Review Checklist

Antigravity runs this before presenting any code as done.

```
SCOPE
□ Does this change only touch the files listed in the task?
□ No unrequested "improvements" to other files?

ARCHITECTURE
□ Every file is in the correct package per ARCHITECTURE.md?
□ No controller calling a repository directly?
□ Service returns a DTO, not an entity?
□ User ID sourced from SecurityContext, not request body?

SECURITY
□ No hardcoded secrets, passwords, or JWT keys?
□ No empty catch blocks?
□ BCrypt used for password hashing?

DEPENDENCIES
□ No new entries in pom.xml that aren't in TECH.md?
□ All imports use the correct version APIs?
  (JJWT 0.12.x: .subject() not .setSubject())
  (Spring Security 6.x: authorizeHttpRequests not authorizeRequests)

TESTING
□ Unit test written for every new service method?
□ Integration test written for every new controller endpoint?
□ @ActiveProfiles("test") on every @SpringBootTest class?
□ @Transactional on every integration test class?
□ @Mapper(componentModel = "spring") on every MapStruct interface?

COMPLETENESS
□ @Operation annotation on every controller method?
□ Lombok annotation processor order correct in pom.xml?
□ .env.example updated if new environment variables added?
```

---

## 7. Definition of Done

### Feature done
- All PLAN.md checklist items for this feature are checked
- Unit tests written and passing for all service methods
- Integration tests written and passing for all controller endpoints
- Manual Swagger check complete (register → authenticate → call endpoint → verify response)
- No TODO comments left in code
- STATE.md updated

### Bug fix done
- A failing test that reproduces the bug exists and now passes
- Root cause is documented in ISSUES.md
- Prevention rule added to ISSUES.md or GOTCHA.md
- No other tests broken by the fix

### Refactor done
- All existing tests still pass — no behavior changed
- File is in the correct location per ARCHITECTURE.md
- No new dependencies introduced
- STATE.md updated with what changed

---

## 8. Escalation Rules

Antigravity stops and asks Claude before proceeding when:

- **A new dependency seems needed** — something not in TECH.md appears necessary. Stop. Ask before adding anything to pom.xml.
- **A file would land outside ARCHITECTURE.md structure** — the right location isn't clear. Stop. Ask where it belongs.
- **Security config needs changing** — any change to `SecurityConfig.java`, `JwtAuthFilter.java`, or `JwtUtil.java`. Stop. Confirm the change is intentional.
- **An entity would be returned from a service** — the mapper doesn't exist yet or the DTO shape isn't clear. Stop. Ask before returning raw entities.
- **A test would require mocking the class under test** — that's a sign the design is wrong. Stop. Ask about refactoring instead.
- **Two approaches exist and the tradeoff isn't obvious** — don't pick silently. State both options in one sentence each and ask which to use.
- **Any step in PLAN.md is ambiguous** — the checklist item could mean more than one thing. Stop. Ask for clarification before writing code.

**The rule:** A wrong assumption that gets coded costs 30 minutes. A 30-second question costs 30 seconds.