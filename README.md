# FinTrack API 📊

Production-grade personal finance REST API with JWT authentication, PostgreSQL persistence, and automated fraud detection.

**Live Swagger UI:** [http://18.234.231.225:8080/swagger-ui.html](http://18.234.231.225:8080/swagger-ui.html)

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) 
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) 
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white) 
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white) 
![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white) 
![GitHub Actions](https://img.shields.io/badge/github%20actions-%232671E5.svg?style=for-the-badge&logo=githubactions&logoColor=white)

## What It Does
- **Secure Authentication**: Stateless JWT-based registration and login flows.
- **Transaction Management**: Full CRUD for income and expenses with strict user data isolation.
- **Fraud Detection**: Automated flagging of suspicious transactions (3x historical average).
- **Category Summaries**: Per-month and per-year financial breakdown by category.
- **Production-Ready Ops**: Actuator health monitoring, Swagger documentation, and automated CI/CD.

## Quick Start
1. **Clone & Environment**:
   ```bash
   git clone https://github.com/aryan9-6-5/fintrack.git
   cp .env.example .env # Set your JWT_SECRET (min 32 chars)
   ```
2. **Launch Containers**:
   ```bash
   docker-compose up --build
   ```
3. **Explore API**:
   Open [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) locally.

## Sample API Call Sequence
1. **Register**: `POST /api/auth/register` with email and password.
2. **Login**: `POST /api/auth/login`.
3. **Authorize**: Copy the `token`, click **Authorize** in Swagger, and paste it.
4. **Create Data**: Post multiple transactions to establish a historical average.
5. **Get Summary**: `GET /api/transactions/summary` to see totals grouped by category.
6. **Trigger Fraud**: Post a transaction > 3x your average to see `isFlagged: true` in the response.

## Architecture
Following a clean, feature-centric package structure:
```text
com.fintrack
├── auth         # Registration, user management, and JWT generation
├── transaction  # Core financial logic, CRUD, and summary queries
├── fraud        # Stateless fraud detection rules engine
├── common
│   ├── security # Spring Security 6 config and JWT filters
│   ├── exception# Global error handling and shared DTOs
│   └── config   # OpenAPI/Swagger and Spring configs
```

## Running Tests
Ensure high reliability with the JaCoCo coverage gate (80% minimum):
```bash
mvn clean test jacoco:report
```
Access the report at `target/site/jacoco/index.html`.

## Deployment Notes
- **Hosting**: AWS EC2 with Docker.
- **Database**: PostgreSQL (RDS).
- **CI/CD**: GitHub Actions automates testing and compilation.
- **Health**: [http://18.234.231.225:8080/actuator/health](http://18.234.231.225:8080/actuator/health)