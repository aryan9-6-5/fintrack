# TECH.md — Technical Stack
# FinTrack: Personal Finance REST API

> This file is the single source of truth for every technology decision on this project.
> Nothing gets installed that isn't listed here. No exceptions without updating this file first.

---

## 1. Core Stack

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Language | Java | 17 (LTS) | Core application language |
| Framework | Spring Boot | 3.x (latest stable) | REST API framework |
| Build Tool | Maven | 3.9.x | Dependency management, build lifecycle |
| Database (local) | PostgreSQL | 15.x | Primary data store, local dev |
| Database (prod) | AWS RDS PostgreSQL | 15.x, db.t3.micro | Production data store, free tier |
| ORM | Spring Data JPA + Hibernate | Spring Boot managed | Database access layer |
| Auth | Spring Security + JJWT | JJWT 0.12.x | JWT generation, validation, route protection |
| Boilerplate | Lombok | Spring Boot managed | Eliminate getter/setter/constructor noise |
| DTO Mapping | MapStruct | 1.5.x | Clean DTO ↔ Entity conversion in service layer |
| API Docs | Springdoc OpenAPI | 2.x | Swagger UI — primary interface for interviewers |
| Monitoring | Spring Boot Actuator | Spring Boot managed | `/health`, `/metrics`, `/info` endpoints |
| Testing | JUnit 5 + Mockito + Spring Boot Test | Spring Boot managed | Unit + integration test coverage |
| Containerization | Docker + docker-compose | Latest stable | Local environment parity, production packaging |
| CI/CD | GitHub Actions | — | Automated test + deploy pipeline |
| Deployment | AWS EC2 / Elastic Beanstalk | Free tier | Live public URL for interviewers |

---

## 2. Frontend

**None.** This is a backend-only project by design.

Swagger UI (served by Springdoc at `/swagger-ui.html`) is the interface. Interviewers can register a user, authenticate, and call every endpoint directly from the browser without any frontend setup.

---

## 3. Backend / API

- **Spring Boot 3.x** — application framework, auto-configuration, embedded Tomcat
- **Spring Web (spring-boot-starter-web)** — REST controller layer, request/response handling
- **Spring Data JPA (spring-boot-starter-data-jpa)** — repository pattern, query derivation
- **Hibernate** — JPA implementation, managed by Spring Boot
- **MapStruct 1.5.x** — compile-time DTO ↔ Entity mapping; keeps service layer free of manual field assignments
- **Lombok** — `@Data`, `@Builder`, `@RequiredArgsConstructor` — reduces boilerplate; works alongside MapStruct (annotation processor order matters in `pom.xml`)
- **Springdoc OpenAPI 2.x** — auto-generates OpenAPI 3 spec from controller annotations; Swagger UI at `/swagger-ui.html`
- **Spring Boot Actuator** — exposes `/actuator/health`, `/actuator/metrics`; signals production awareness to interviewers

**Note on annotation processor order in pom.xml:**
Lombok must be declared before MapStruct in the `annotationProcessorPaths` block. MapStruct processes Lombok-generated methods — wrong order breaks compilation silently.

---

## 4. Database & Storage

| Environment | Technology | Config |
|---|---|---|
| Local dev | PostgreSQL 15 via Docker | `docker-compose.yml` spins it up |
| Production | AWS RDS PostgreSQL | db.t3.micro, free tier, private subnet |

- Schema managed via **Hibernate DDL auto** in development (`spring.jpa.hibernate.ddl-auto=update`)
- Production uses `validate` — schema changes are applied manually or via migration script
- No Flyway or Liquibase in v1 (deferred to v2 — worth mentioning in interviews as a known gap)
- Connection pooling via **HikariCP** (Spring Boot default — no extra config needed)

---

## 5. Authentication

- **Spring Security** — security filter chain, route protection, stateless session config
- **JJWT 0.12.x** — JWT creation and parsing

**JJWT 0.12.x note:** The API changed significantly from 0.9.x. Most tutorials online use the old fluent builder (`Jwts.builder().setSubject(...)`). In 0.12.x the correct pattern is `Jwts.builder().subject(...).signWith(key, algorithm)`. Pin this version explicitly in `pom.xml` to avoid build-time confusion when referencing online resources.

**Auth flow:**
1. `POST /api/auth/register` — creates user, returns JWT
2. `POST /api/auth/login` — validates credentials, returns JWT
3. All other routes require `Authorization: Bearer <token>` header
4. Spring Security filter chain validates JWT on every protected request — no session, no cookies

---

## 6. State & Data Fetching

Not applicable — backend API only. No client-side state management.

---

## 7. External APIs & Services

**None in v1.** All integrations are deliberately excluded:

- No Plaid (deferred to v2)
- No Stripe or payment processor
- No email service (no notifications in v1)
- No external fraud scoring API — logic is rule-based and internal

This is intentional. Being able to explain *why* these are excluded is part of the interview signal.

---

## 8. Dev Tools & Testing

| Tool | Purpose |
|---|---|
| JUnit 5 | Unit test framework |
| Mockito | Mocking dependencies in unit tests |
| Spring Boot Test | Integration test support (`@SpringBootTest`, `@WebMvcTest`) |
| MockMvc | HTTP layer testing without a running server |
| H2 (in-memory) | Test database — swapped in for integration tests via `application-test.properties` |
| Maven Surefire Plugin | Runs tests in CI pipeline |

**Test coverage targets:**
- Service layer: unit tested with Mockito (no DB, no Spring context)
- Controller layer: tested with `@WebMvcTest` + MockMvc
- Fraud detection logic: unit tested with edge cases (exactly 3x, above 3x, below 3x)
- Auth flow: integration tested end-to-end (register → login → access protected route)

---

## 9. Infrastructure & Deployment

| Component | Technology | Notes |
|---|---|---|
| Containerization | Docker | `Dockerfile` in project root |
| Local orchestration | docker-compose | Spins up app + PostgreSQL together |
| CI/CD | GitHub Actions | Runs tests on every push; deploys on green main branch |
| Cloud platform | AWS | Free tier only |
| Compute | EC2 (t2.micro) or Elastic Beanstalk | Decision made at deploy time based on complexity |
| Database | AWS RDS PostgreSQL | db.t3.micro, free tier |
| API documentation | Swagger UI | Public-facing, no auth required to view |
| Monitoring | Spring Boot Actuator | `/actuator/health` used as health check endpoint |

**Deployment pipeline (GitHub Actions):**
1. Push to any branch → run tests
2. Push to `main` (green tests only) → build Docker image → deploy to AWS

---

## 10. Forbidden Packages ❌

These are explicitly banned. Do not add them without updating this file and getting approval.

| Package | Reason |
|---|---|
| Any frontend framework (React, Vue, Angular, Thymeleaf) | Backend-only project. Swagger is the UI. |
| Plaid SDK | V2 only. Not in scope. |
| Stripe or any payment SDK | No real money movement in v1. |
| Spring Session | Stateless JWT auth — sessions are explicitly excluded. |
| JJWT 0.9.x or older | API is incompatible with 0.12.x. Causes silent runtime failures. Pin 0.12.x only. |
| Flyway / Liquibase | Deferred to v2. Use Hibernate DDL in dev, manual migration in prod for now. |
| Redis / caching libraries | V2 feature. Adds infra complexity without interview value in v1. |
| GraphQL libraries | V2 only. |
| WebSocket dependencies | No real-time features in v1. |
| Any ML/AI library | Fraud detection is rule-based. No model inference in v1. |