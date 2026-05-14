# JobOps

A personal job-search operating system for Artem Sutulov. Not a job board scraper — a manual-first CRM + AI assistant that helps decide which jobs are worth applying to, tracks the funnel, reminds what to do next, and generates copy-paste-ready texts.

## Architecture

| Layer | Technology |
|-------|-----------|
| Frontend | React 18, TypeScript, Vite 5, Tailwind CSS, TanStack Query, Zustand |
| Backend | Java 21, Spring Boot 3.3, Gradle (Kotlin DSL) |
| Database | PostgreSQL 16, Flyway migrations |
| ORM | jOOQ (plain DSL, no code generation) |
| Auth | JWT (stateless) |
| AI | OpenAI API (gpt-4o-mini / gpt-4o) via backend only |
| Storage | Local filesystem (S3-compatible abstraction) |

## Quick Start

### Prerequisites

- Docker & Docker Compose (everything else runs inside containers)

### 1. Environment variables

```bash
cp .env.example .env
# Edit .env — set OPENAI_API_KEY and JOBOPS_JWT_SECRET
```

### 2. Start everything

```bash
docker compose up --build
```

| Service | URL |
|---------|-----|
| App (frontend) | http://localhost:3000 |
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |

The first build takes a few minutes (Gradle + npm). Subsequent `--build` runs are faster due to Docker layer caching.

### First login

Register at http://localhost:3000/register. On first registration, the system automatically seeds:
- 40+ target companies (Priority 1, 1.5, 2) based on Germany job-search strategy
- 10 saved LinkedIn/job-board searches with boolean queries

### Development (hot-reload)

For faster iteration, run only the database in Docker and start frontend/backend locally:

```bash
# DB only
docker compose up postgres -d

# Backend (separate terminal) — reads .env automatically via Spring Boot
cd backend && ./gradlew bootRun

# Frontend (separate terminal)
cd frontend && npm install && npm run dev   # http://localhost:5173
```

## Project Structure

```
jobops/
├── backend/                          # Spring Boot application
│   ├── src/main/java/com/sutulovai/jobops/
│   │   ├── config/                   # JWT, Security, AppConfig
│   │   ├── controller/               # REST endpoints
│   │   ├── dto/                      # Request/response DTOs
│   │   ├── exception/                # Custom exceptions
│   │   ├── repository/               # Port interfaces + jOOQ implementations
│   │   │   └── jooq/
│   │   ├── service/                  # Business logic
│   │   │   ├── ai/                   # OpenAI client
│   │   │   ├── action/               # NextActionEngine
│   │   │   ├── analysis/             # JobAnalysisService
│   │   │   └── message/              # MessageGenerationService
│   │   ├── storage/                  # File storage abstraction
│   │   └── util/
│   └── src/main/resources/
│       ├── db/migration/             # Flyway migrations (V1–V10)
│       └── prompts/                  # OpenAI prompt templates
├── frontend/                         # React application
│   └── src/
│       ├── app/                      # API client, auth store, router
│       ├── components/               # Shared UI components
│       │   ├── common/               # Badges, bars, buttons
│       │   └── layout/               # AppLayout, Sidebar
│       ├── features/                 # Feature pages
│       │   ├── analyze/              # JD analysis page
│       │   ├── analytics/            # Funnel analytics
│       │   ├── auth/                 # Login / Register
│       │   ├── companies/            # Company list + detail
│       │   ├── contacts/             # Contacts list
│       │   ├── dashboard/            # Today / main page
│       │   ├── messages/             # Outreach messages
│       │   ├── pipeline/             # Application Kanban
│       │   ├── profile/              # Profile + CV upload
│       │   ├── searches/             # Saved searches
│       │   ├── vacancies/            # Vacancy list + detail
│       │   └── weekly/               # Weekly review
│       ├── lib/                      # Utility functions
│       └── types/                    # TypeScript interfaces
└── docker-compose.yml
```

## Key Features

### AI-powered JD Analysis
Paste a job description → get: fit score (0–100), APPLY/MAYBE/SKIP recommendation, stack/domain/location/language/seniority breakdown, hard blockers, red flags, suggested salary strategy, suggested first message.

### Strategic Company Database
Pre-seeded with 40+ Germany tech companies ranked by priority tier (P1/P1.5/P2), with English-speaking likelihood, relocation support status, salary pitch ranges, and recommended outreach strategy.

### Next Action Engine
Scheduled engine calculates weekly targets and creates prioritized next actions:
- Week 1: 15–18 applications, 12 recruiter DMs, 8 manager DMs, 10 referral requests
- Automatic follow-up reminders (T+1, T+4, T+7, T+14, T+21)
- Funnel diagnostic actions when response rates fall below benchmarks

### Message Generation
AI-generated outreach messages for: LinkedIn recruiter DM, manager outreach, referral request, follow-up, post-interview thank you, salary answer, relocation answer, Why Germany/Munich, cover note. All messages use anti-desperate guardrails and focus on payments/fintech/distributed systems positioning.

### Weekly Review
On-demand diagnostic that calculates response rates by channel, detects blockers (salary/language/relocation), compares against funnel benchmarks, and generates AI-powered strategic recommendations.

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENAI_API_KEY` | OpenAI API key (required for AI features) | — |
| `JOBOPS_JWT_SECRET` | JWT signing secret (min 32 chars) | — |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/jobops` |
| `SPRING_DATASOURCE_USERNAME` | DB user | `jobops` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `jobops` |
| `JOBOPS_STORAGE_ROOT` | Local file storage path | `.jobops/storage` |

## Development Notes

- The app uses **jOOQ plain DSL** (no code generation) — all queries use `table("name")` / `field("name")` syntax
- Flyway migrations live in `backend/src/main/resources/db/migration/`
- OpenAI integration is **backend-only** — no API key is sent to the frontend
- CV files are stored locally under `JOBOPS_STORAGE_ROOT`
- All endpoints require JWT auth except `/api/auth/**`
