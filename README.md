# Transaction Ingestion & Rule-Based Categorisation Engine

Spring Boot 3 service that ingests payment transactions in bulk, categorises merchants using keyword rules (optional LLM fallback), and exposes list + analytics APIs.

## Stack

- Java 21
- Spring Boot 3.5.6
- Spring Data JPA
- Bean Validation
- MySQL 8
- Flyway
- Testcontainers
- Docker Compose

### Why Flyway

Schema is versioned SQL under `src/main/resources/db/migration/`. Flyway applies scripts on startup. Hibernate `ddl-auto` is `validate`, so tables come only from migrations.

## Data model

- `transactions` — raw payments
- `category_rules` — keyword → category recipes (`priority`, `is_active`)
- `merchant_categories` — one classification result per merchant name (`confidence_score`, `strategy_used`)

A merchant is uncategorised when it appears in `transactions` and has no row in `merchant_categories`.

Duplicate ingest rule: same `merchantName` + `amount` + `currency` + `transactionDate` + `source` is rejected.

## APIs

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/transactions/bulk` | Ingest JSON array. Returns accepted / rejected |
| POST | `/api/v1/rules` | Create or update a rule (upsert by keyword) |
| POST | `/api/v1/categorise/trigger` | Async classify uncategorised merchants (`202`) |
| GET | `/api/v1/transactions` | Paginated list. Filters: `category`, `source`, `from`, `to` |
| GET | `/api/v1/analytics/summary` | Spend by category, top 10 merchants, month-over-month |

## Categorisation

Strategy pattern:

1. `RuleBasedStrategy` — case-insensitive keyword contains-match, highest `priority` first. Confidence `1.0`, `strategy_used = RULE_BASED`.
2. `FallbackStrategy` — if no rule matches and `categorisation.fallback.enabled=true`, call `llm.api-url` and parse `{ category, sub_category, confidence }`.

Trigger runs on `categorizationExecutor` (`@Async`). Pool size is configurable.

## Run

Prerequisites: JDK 21, Docker Desktop.

```bash
docker compose up --build