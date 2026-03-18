# CONSTRAINTS.md — Project Constraints & Guardrails
# FinTrack: Personal Finance REST API

> Every AI tool reads this before suggesting anything with cost, complexity, or infrastructure implications.
> If it costs money, adds a new service, or requires a new environment variable — check here first.

---

## 1. Budget Constraints

**Monthly ceiling: $0. Free tier only, always.**

AWS free tier runs for 12 months from account creation. After that, shut down or migrate to Railway/Render (permanently free for portfolio projects). No exceptions to the zero-cost rule without explicit approval.

### Approved free-tier services

| Service | Free limit | What happens at limit |
|---|---|---|
| AWS EC2 t2.micro | 750 hrs/month | Billing starts — shut down immediately |
| AWS RDS db.t3.micro | 750 hrs/month | Billing starts — shut down immediately |
| AWS RDS Storage | 20 GB | Storage billing starts — won't hit this |
| AWS Data Transfer | 1 GB/month outbound | Small overage charges — Swagger + API calls stay well under |
| GitHub | Unlimited public repos | N/A |
| GitHub Actions | 2,000 min/month (public repos: unlimited) | N/A for public repo |
| Docker Hub | 1 free private repo | Use public repo or local image only |

### Requires approval before adding

- Any AWS service not listed above (S3, SQS, Lambda, CloudFront, etc.)
- Any third-party SaaS with a paid tier (even if free tier exists)
- Any service requiring a credit card on file

### Off-limits entirely

- Plaid, Stripe, or any payment/banking API — V2 only, not in scope
- Redis, ElastiCache, or any caching service — V2 only
- Any ML/AI API (OpenAI, AWS Bedrock, etc.) — not in this project
- AWS RDS instance types above db.t3.micro
- AWS EC2 instance types above t2.micro
- Any service with no free tier

---

## 2. Infrastructure Constraints

**Deployment target:** AWS EC2 (t2.micro) or Elastic Beanstalk — free tier only.

| Constraint | Limit | Rule |
|---|---|---|
| Compute | 1 vCPU, 1GB RAM (t2.micro) | No memory-heavy operations. No in-memory caching. |
| Database | AWS RDS db.t3.micro, 20GB storage | ~17 concurrent connections max. HikariCP pool default (10) is fine. |
| Database (local) | PostgreSQL via Docker | `docker-compose up` only. No local PostgreSQL installation required. |
| Containerization | Docker + docker-compose | Already approved and in TECH.md. No new container tooling. |
| Microservices | None | Single deployable JAR. No service split without explicit approval. |
| Background jobs | None | No async job queues, no scheduled tasks beyond what Spring Boot provides natively. |

---

## 3. Performance Constraints

This is a backend API — no bundle size concerns. Targets are for API response times.

| Metric | Target | Notes |
|---|---|---|
| API response time (p95) | < 500ms | On free-tier RDS — realistic, not aggressive |
| `/actuator/health` response | < 200ms | Used as health check — must be fast |
| Startup time | < 30 seconds | Spring Boot on t2.micro — acceptable |
| DB query time | < 100ms | All queries filter by `userId` — indexed |

**No Lighthouse targets** — no frontend.
**No bundle size targets** — no frontend.

---

## 4. Complexity Constraints

- **No new external services** without updating TECH.md and CONSTRAINTS.md first.
- **No new environment variables** without updating `.env.example` in the same commit.
- **No new Maven dependencies** without updating TECH.md first — check the Forbidden Packages list.
- **No microservices split** — single Spring Boot application, single deployable JAR, always.
- **No background job framework** (Quartz, Spring Batch, etc.) — not in scope for v1.
- **No caching layer** (Redis, Caffeine, Ehcache) — V2 only. If a query is slow, optimize the SQL first.
- **No GraphQL** — V2 only. REST only in v1.
- **No WebSockets or SSE** — no real-time features in v1.
- **No additional AWS services** — EC2 + RDS is the complete infrastructure. Nothing else.
- **Schema changes** require manually dropping/recreating the table in dev (Hibernate `ddl-auto=update` is additive only — see GOTCHA.md).

---

## 5. API & Rate Limit Awareness

| Service | Free limit | At-limit behavior | Fallback |
|---|---|---|---|
| AWS EC2 t2.micro | 750 hrs/month | Instance billed at on-demand rate | Shut down instance immediately |
| AWS RDS db.t3.micro | 750 hrs/month | Instance billed at on-demand rate | Shut down instance immediately |
| GitHub Actions | Unlimited (public repo) | N/A | N/A |
| Docker Hub | 100 pulls/6hrs (anonymous) | Pull throttled | Use authenticated pull or local image |

**No third-party APIs in v1.** Fraud detection is internal rule logic. No external scoring service. No rate limit exposure.

---

## 6. Approved vs Requires Approval

| Can add without asking | Requires approval first |
|---|---|
| New Spring Boot starter already in TECH.md | Any dependency not in TECH.md |
| New `application-{profile}.yml` config file | Any new AWS service |
| New DTO class in existing feature package | Any paid SaaS integration |
| New `@Query` method on existing repository | Any new environment variable (must update `.env.example`) |
| New unit or integration test | Any change to `SecurityConfig.java` scope |
| New `@Operation` annotation on existing endpoint | Any new Docker container in `docker-compose.yml` |
| Bug fixes to existing files | Any schema change requiring data migration |
| README updates | Any new GitHub Actions job or workflow file |

---

## 7. Escalation Rule

When an action would violate a constraint, AI responds with exactly this format:

> ⚠️ CONSTRAINT: [proposed action] would [cost/complexity implication].
> This exceeds [specific constraint from this doc].
> Constrained alternative: [what can be done within approved limits].
> Shall I proceed with the alternative?

**Example:**
> ⚠️ CONSTRAINT: Adding Redis for summary caching would require a new AWS ElastiCache instance (~$13/month) and a new dependency outside TECH.md.
> This exceeds the $0 budget ceiling and the no-new-services rule.
> Constrained alternative: Optimize the existing PostgreSQL summary query with an index on `(user_id, category, created_at)`.
> Shall I proceed with the alternative?