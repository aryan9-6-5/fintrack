# FinTrack

Production-grade personal finance REST API built with Java 17 and Spring Boot 3. Handles user authentication, transaction management, category-based spending summaries, and automatic fraud detection — deployed live on AWS.

**Live API:** `[Swagger URL — added after AWS deployment]`

---

## What it does

- **JWT Authentication** — secure registration and login, every protected route requires a valid token
- **Transaction Management** — full CRUD for income and expense records, scoped per user
- **Spending Summaries** — category-based breakdowns (e.g. total spent on Food this month)
- **Fraud Detection** — automatically flags any transaction exceeding 3× the user's historical average
- **Production Monitoring** — `/actuator/health` endpoint, Docker-ready, CI/CD via GitHub Actions

---

## Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Database | PostgreSQL 15 (local via Docker, production via AWS RDS) |
| Auth | Spring Security 6 + JJWT 0.12.x |
| ORM | Spring Data JPA + Hibernate |
| DTO Mapping | MapStruct |
| API Docs | Springdoc OpenAPI (Swagger UI) |
| Monitoring | Spring Boot Actuator |
| Container | Docker + docker-compose |
| CI/CD | GitHub Actions |
| Deployment | AWS EC2 + RDS (free tier) |

---

## Local setup

**Requirements:** Java 17, Docker, Maven

```bash
git clone https://github.com/aryan9-6-5/fintrack.git
cd fintrack
cp .env.example .env        # fill in your values
docker-compose up           # starts app + PostgreSQL
```

Swagger UI opens at `http://localhost:8080/swagger-ui.html`

---

## Sample API call sequence

**1. Register**
```bash
POST /api/auth/register
{
  "email": "test@example.com",
  "password": "SecurePass123!"
}
```

**2. Login — copy the token**
```bash
POST /api/auth/login
{
  "email": "test@example.com",
  "password": "SecurePass123!"
}
```

**3. Create a transaction (paste token in Swagger Authorize)**
```bash
POST /api/transactions
{
  "amount": 85.00,
  "type": "EXPENSE",
  "category": "Food",
  "description": "Grocery run"
}
```

**4. Trigger the fraud detector**
```bash
POST /api/transactions
{
  "amount": 9999.00,
  "type": "EXPENSE",
  "category": "Other",
  "description": "Large purchase"
}
# Response includes: "isFlagged": true
```

**5. Get category summary**
```bash
GET /api/transactions/summary?month=3&year=2026
```

---

## Architecture

```
src/main/java/com/fintrack/
├── auth/           # Registration, login, JWT
├── transaction/    # CRUD, summaries, fraud trigger
├── fraud/          # Stateless rule engine (3× average = flagged)
└── common/
    ├── security/   # JWT filter, Spring Security config
    ├── exception/  # Global error handler
    └── config/     # OpenAPI / Swagger config
```

Hybrid package structure — features own their code, cross-cutting concerns live in `common/`. Each feature package could be extracted into a microservice with minimal refactoring.

---

## Running tests

```bash
mvn test                          # all tests
mvn test jacoco:report            # with coverage report
mvn test -Dtest=FraudDetectionServiceTest   # single class
```

Tests use H2 in-memory database — no Docker required to run the test suite.

---

## Deployment

Deployed on AWS free tier:
- **Compute:** EC2 t2.micro
- **Database:** RDS PostgreSQL db.t3.micro
- **CI/CD:** GitHub Actions — tests run on every push, deploy on green merge to `main`

```bash
docker build -t fintrack .
# deploy via GitHub Actions on push to main
```