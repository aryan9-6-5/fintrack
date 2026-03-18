# STATE.md — Live Project State
# FinTrack: Personal Finance REST API
#
# ============================================================
# OWNERSHIP RULES — read before editing this file
# ============================================================
#
# WHO UPDATES THIS FILE:
#   AI (automatic) → after every completed task in a session
#   You (manual)   → only when resuming after 24h+ break,
#                    or when you did something outside the AI
#
# WHEN IT'S WRONG:
#   If STATE.md and reality disagree → STATE.md is wrong.
#   Fix it before doing anything else. Never work from stale state.
#
# STALENESS RULE:
#   If Last Updated is more than 24h ago, treat this file as
#   potentially stale. Verify current step before proceeding.
#
# ============================================================

## Current Status

**Stage:** Stage 1 — Setup
**Step:** Step 1 — Initialize project with Spring Initializr
**Status:** Not Started
**Last Updated:** 2026-03-18 00:00
**Updated By:** AI — auto (initial generation)

---

## Last Completed

> Nothing yet. Project is starting from scratch.

---

## Current Step (in detail)

> Stage 1, Step 1 — Generate project via Spring Initializr with dependencies:
> Spring Web, Spring Data JPA, Spring Security, PostgreSQL Driver, Lombok,
> Spring Validation, Spring Boot Actuator.
> Then add manual dependencies to `pom.xml`: JJWT 0.12.x (3 artifacts),
> MapStruct 1.5.x, Springdoc OpenAPI 2.x.
> Fix annotation processor order: Lombok before MapStruct in `annotationProcessorPaths`.

---

## Next Action

> Create the full folder structure per ARCHITECTURE.md:
> `src/main/java/com/fintrack/auth/`, `transaction/`, `fraud/`,
> `common/security/`, `common/exception/`, `common/config/`.
> Create `application.yml`, `application-dev.yml`, `application-prod.yml`, `application-test.yml`.
> Create `.env.example` with all variables from ARCHITECTURE.md. Add `.env` to `.gitignore`.

---

## Blockers

> None.

---

## Open Decisions

> None. All architecture, stack, and design decisions are locked in
> PRD.md, TECH.md, ARCHITECTURE.md, AIRULES.md, DESIGN.md, and PLAN.md.

---

## Recent Changes (last 3 sessions)

| Date | What changed | Files affected |
|------|-------------|----------------|
| 2026-03-18 | All planning docs generated and locked | PRD, TECH, ARCHITECTURE, AIRULES, TESTING, PLAN, DESIGN, STATE |
| — | — | — |
| — | — | — |

---

## Environment Status

| Environment | Status | Last deployed | Notes |
|-------------|--------|---------------|-------|
| Local dev | Not set up | — | Awaiting Stage 1 setup |
| Production | Not deployed | — | AWS free tier, target Stage 6 |

---

## Quick Reference

```
Repo:     [github.com/your-username/fintrack]   ← update when created
Local:    http://localhost:8080
Swagger:  http://localhost:8080/swagger-ui.html
Health:   http://localhost:8080/actuator/health
Prod URL: [TBD — set in Stage 6]
DB local: localhost:5432/fintrack (via docker-compose)
DB prod:  [TBD — AWS RDS, set in Stage 6]
Branch:   main
```

# ============================================================
# HOW AI USES THIS FILE — read order and update protocol
# ============================================================
#
# SESSION START (AI does this automatically):
#   1. Read STATE.md
#   2. Check Last Updated — if >24h, flag as potentially stale
#   3. Say: "STATE.md: [Stage X, Step Y]. Next: [next action].
#      Has anything changed since [last updated date]?"
#   4. Wait for confirmation or correction before proceeding.
#
# SESSION END (AI does this automatically, every time):
#   Update these fields:
#   ✏️  Last Updated → current timestamp
#   ✏️  Updated By → "AI — auto"
#   ✏️  Last Completed → what was done this session
#   ✏️  Current Step → where things stand now
#   ✏️  Next Action → the very next specific thing
#   ✏️  Blockers → any new blockers found
#   ✏️  Recent Changes → add one row to the table
#
#   Then post SESSION SUMMARY before ending:
#   "✅ Done: [list]
#    📍 Now at: [stage + step]
#    ➡️  Next: [next action]
#    ⚠️  Open: [blockers or open decisions, or 'None']
#    🕐 STATE.md updated [timestamp]"
#
# IF STATE.MD IS STALE (AI detects >24h gap):
#   Say: "STATE.md was last updated [X hours/days ago].
#   Before I proceed — is [current step] still accurate,
#   or has something changed?"
# ============================================================