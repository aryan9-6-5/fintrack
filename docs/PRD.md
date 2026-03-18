# PRD.md — Product Requirements Document
# FinTrack: Personal Finance REST API

---

## 1. Product Overview

**Name:** FinTrack  
**One-liner:** A production-grade personal finance REST API built to demonstrate senior backend engineering to fintech interviewers.  
**Problem:** Portfolio projects either look toy-grade or are too abstract to discuss in depth. FinTrack is built to be a 20-minute interview conversation piece — live, documented, and aligned with JPMorgan Chase's exact stack.  
**Primary User:** Engineering interviewers and recruiters at JPMorgan Chase and similar fintech/banking firms.

---

## 2. What This App IS

FinTrack is a backend REST API that simulates real fintech infrastructure. It is not a demo — it runs live on AWS and is fully accessible via Swagger. Every layer is complete: auth, business logic, persistence, containerization, CI/CD, and cloud deployment.

It handles five core functions:

1. **User Authentication** — Registration and login secured with JWT tokens.
2. **Transaction Management** — Full CRUD for income and expense records.
3. **Spending Summaries** — Category-based aggregations (e.g., total spent on Food in a given month).
4. **Fraud Detection** — Rule-based flagging: any transaction exceeding 3x the user's historical average is automatically marked as suspicious.
5. **Live Deployment** — Hosted on AWS (EC2 or Elastic Beanstalk, free tier), with a public URL interviewers can hit directly.

The project is designed to hit every keyword on a JPM backend job description — Java, Spring Boot, PostgreSQL, Docker, AWS, CI/CD, JWT, JUnit — and to support confident, detailed answers about trade-offs and design decisions.

---

## 3. What This App is NOT ⛔

These exclusions are intentional. Being able to explain *why* they're excluded is itself part of the interview signal.

- **No frontend.** Swagger/OpenAPI is the interface. No React, no dashboards, no HTML.
- **No real bank connections.** No Plaid, no Open Banking APIs, no live account data.
- **No payment processing.** No Stripe, no actual money movement of any kind.
- **No mobile app.** Backend only.
- **No ML model.** Fraud detection is rule-based (3x average threshold). No trained model, no scoring pipeline.
- **No multi-tenancy or admin roles.** Single user type. No role-based access control in v1.
- **No real-time features.** No WebSockets, no push notifications, no event streaming.

---

## 4. User Stories

**As an interviewer,**
- I can hit a live Swagger URL and call every endpoint without setting anything up locally.
- I can register a test user, log in, create transactions, and see a category summary in under 5 minutes.
- I can see a flagged transaction when I submit one that exceeds the fraud threshold.

**As the candidate,**
- I can walk through any layer of the stack — API design, DB schema, auth flow, fraud logic, deployment pipeline — and explain every decision.
- I can point to a GitHub Actions run and show that tests pass before every deployment.
- I can explain what I'd add in v2 and why I left it out of v1.

---

## 5. Success Metrics

- Live AWS URL returns a working Swagger UI with all endpoints documented.
- All five core features are reachable and functional through Swagger.
- JWT auth is enforced — unauthenticated requests to protected routes return 401.
- A transaction submitted at more than 3x the user's average is flagged in the response.
- GitHub Actions pipeline runs on every push and deploys only on green tests.
- A cold interviewer can understand the system architecture in under 10 minutes from the README alone.

---

## 6. Constraints

- **Stack lock:** Java + Spring Boot + PostgreSQL + AWS. No substitutions — the point is to match JPM's exact environment.
- **Free tier only:** AWS RDS (db.t3.micro), EC2 or Elastic Beanstalk — nothing that incurs cost beyond the free tier.
- **Rule-based fraud only:** The 3x threshold is intentional and explainable. No external scoring service.
- **No real user data:** This is a portfolio project. No PII, no real financial accounts, no compliance requirements.
- **Interview-optimized scope:** Every feature included must be explainable in depth. If it can't be defended in an interview, it doesn't belong in v1.

---

## 7. Out of Scope — V2

These are deferred, not forgotten. Mentioning them in an interview shows forward-thinking.

- React or Next.js frontend dashboard
- Plaid API integration for real bank data
- ML-based fraud scoring to replace the rule-based flag
- Admin role and multi-user management
- Redis caching for summary endpoints
- GraphQL layer on top of the REST API