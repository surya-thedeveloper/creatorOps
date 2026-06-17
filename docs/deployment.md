# CreatorOps Deployment Guide

## Overview

This guide covers all supported deployment modes for the CreatorOps backend.

| Mode | Profile | Database | Use Case |
|------|---------|----------|----------|
| Default | `default` | H2 in-memory | Unit tests, quick local run |
| Local PostgreSQL | `local` | PostgreSQL | Developer local environment |
| Docker Compose | `postgres` | PostgreSQL in container | Full-stack local environment |
| Production | `prod` | Managed PostgreSQL | Cloud deployment |

---

## Prerequisites

- Java 17+ (Temurin recommended)
- Maven 3.9+
- Docker + Docker Compose (for containerized modes)
- PostgreSQL 14+ (for non-Docker database modes)

---

## Option 1: Default (H2 In-Memory)

Runs with no external dependencies. Uses an in-memory H2 database.

```bash
mvn spring-boot:run
```

- Application starts at `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- Data is **ephemeral** — lost on restart

---

## Option 2: Local PostgreSQL

Requires a running PostgreSQL instance. Uses the `local` Spring profile.

### Start PostgreSQL via Docker (one-time setup)

```bash
docker run -d \
  --name creatorops-pg \
  -e POSTGRES_DB=creatorops \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine
```

### Configure environment

```bash
cp .env.example .env
# Edit .env with your DB credentials
```

### Run application

```bash
export $(cat .env | xargs)
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

- Application starts at `http://localhost:8080`
- Flyway runs all migrations on first startup
- Data is **persistent**

---

## Option 3: Docker Compose (Full Stack)

Starts both PostgreSQL and the CreatorOps backend in containers.

### Setup

```bash
# Create your .env from the template
cp .env.example .env
# Fill in JWT_SECRET (minimum 32 chars, base64 encoded):
openssl rand -base64 64
```

### Start

```bash
docker compose up --build
```

Services:
- PostgreSQL available at `localhost:5432`
- CreatorOps API at `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

### Stop

```bash
docker compose down          # Stop containers, keep data
docker compose down -v       # Stop and wipe database (destructive)
```

### Rebuild after code changes

```bash
docker compose up --build --force-recreate
```

---

## Option 4: Production Deployment

### Environment Variables (Required)

All values **must** be set. No defaults are provided for secrets.

| Variable | Description | Example / Default |
|----------|-------------|-------------------|
| `SPRING_PROFILES_ACTIVE` | Spring profile to activate | `prod` |
| `DB_HOST` | PostgreSQL host | `db.mycloud.com` |
| `DB_PORT` | PostgreSQL port | `5432` |
| `DB_NAME` | Database name | `creatorops` |
| `DB_USER` | Database user | `creatorops_user` |
| `DB_PASS` | Database password | `strong-password` |
| `JWT_SECRET` | JWT signing key (base64, min 32 chars) | `base64-encoded-secret` |
| `JWT_EXPIRATION_MS` | Access token TTL in ms | `900000` (15 min) |
| `JWT_REFRESH_EXPIRATION_MS` | Refresh token TTL in ms | `604800000` (7 days) |
| `GEMINI_API_KEY` | Google Gemini AI key | `AIza...` |
| `AI_RATE_LIMIT_CAPACITY` | AI calls per minute per user | `5` |
| `FEATURE_AI_ENABLED` | Global AI feature toggle | `true` |
| `FEATURE_AI_BRAINSTORM_ENABLED` | AI Brainstorm toggle | `true` |
| `FEATURE_AI_SCRIPT_ENABLED` | AI Script Generation toggle | `true` |
| `FEATURE_ANALYTICS_ENABLED` | Analytics Dashboard toggle | `true` |

### Build production JAR

```bash
mvn package -DskipTests -P prod
```

### Run production JAR

```bash
SPRING_PROFILES_ACTIVE=prod \
DB_HOST=your-db-host \
DB_USER=your-db-user \
DB_PASS=your-db-password \
DB_NAME=creatorops \
JWT_SECRET=your-base64-secret \
GEMINI_API_KEY=your-gemini-key \
java -jar target/creatorops-backend-*.jar
```

### Run production Docker image

```bash
docker build -t creatorops-backend:latest .

docker run -d \
  --name creatorops \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=your-db-host \
  -e DB_USER=your-db-user \
  -e DB_PASS=your-db-password \
  -e DB_NAME=creatorops \
  -e JWT_SECRET=your-base64-secret \
  -e GEMINI_API_KEY=your-gemini-key \
  creatorops-backend:latest
```

> **Note**: In production, Swagger UI is disabled (`springdoc.swagger-ui.enabled=false`).
> Only `/actuator/health` and `/actuator/info` are exposed.

---

## Database Migrations

Flyway is used for all schema management in `local`, `postgres`, and `prod` profiles.

- Migrations live in `src/main/resources/db/migration/`
- Naming convention: `V{version}__{description}.sql` (e.g., `V1__initial_schema.sql`)
- H2 profile: Flyway is **disabled** — Hibernate auto-creates schema

---

## Health Check

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" }
  }
}
```

---

## API Documentation (Swagger UI)

Available in `default`, `local`, `postgres`, and `dev` profiles:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

**Disabled in `prod` profile** for security.

---

## CI/CD

GitHub Actions CI runs on every push and pull request to `main`/`master`:

1. Checkout source
2. Set up Java 17 (Temurin)
3. Restore Maven dependency cache
4. `mvn verify` — compile, run all tests
5. Upload surefire test results as artifacts

See [.github/workflows/ci.yml](../.github/workflows/ci.yml) for configuration.
