# FinTrack: Personal Finance REST API

FinTrack is a production-grade personal finance REST API built to demonstrate senior backend engineering practices. It utilizes Java, Spring Boot, PostgreSQL, Docker, AWS, and CI/CD pipelines.

## Live Application
**Live URL:** [TBD — AWS Deployment]
**Swagger API Docs:** [TBD]/swagger-ui.html

## Local Setup

### Prerequisites
- Docker & docker-compose
- Java 17 / Maven (optional for running tests)

### Getting Started
1. Clone the repository
2. Set up environment:
   ```bash
   cp .env.example .env
   # Update JWT_SECRET and credentials if desired
   ```
3. Run with Docker:
   ```bash
   docker-compose up --build
   ```
4. Access Swagger UI for API navigation:
   http://localhost:8080/swagger-ui.html

## API Reference
The Swagger UI provides the primary interface for exploring the endpoints:
- **Authentication:** `POST /api/auth/register`, `POST /api/auth/login`
- **Transactions:** `GET`, `POST`, `PUT`, `DELETE` over `/api/transactions`
- **Fraud Engine:** Integrated automatically into transaction endpoints

### Sample API Call Sequence
1. **Register:** `POST /api/auth/register` with `{"email": "test@example.com", "password": "Password123!"}`
2. **Login:** `POST /api/auth/login`
3. **Capture JWT:** Copy the returned token, paste into Swagger's 'Authorize' button.
4. **Create Transaction:** `POST /api/transactions`
5. **Get Summary:** `GET /api/transactions/summary`
6. **Trigger Fraud:** Submit a transaction amount > 3x average to see the `isFlagged` response property set.

## Architecture Highlights
The application follows a clean feature-centric packaging model.

### Package Structure
- `auth`: Registration, login
- `transaction`: Income/Expense features, summaries
- `fraud`: Stateless rules processing
- `common`: Cross-cutting config, exception handling, and security