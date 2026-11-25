# Design — Price breakdown ("Sous-détail de prix")

## Intent
Timed (2h) case study for ECE Tech. Show a small, working, tested feature from DB to screen that we can
fully explain. Small scope that works > full scope we can't explain.

## Goals
1. List 3–5 seeded work items with direct cost and sale price.
2. Add a resource (from 5–8 seeded) + quantity per unit to a work item.
3. Change a resource unit price; every work item using it follows.

## Non-goals
Auth, permissions, Docker, CI, price history, deleting/editing lines, creating work items or resources.

## Business rules
- Quantity required, strictly positive.
- Direct cost = Σ (quantity × unit price). Sale price = direct cost × K, K = 1.35 (app-wide constant).
- Prices are never entered by hand nor stored: computed on read. Amounts in ariary.

## Decisions (assumptions go to the README in French)
- Schema: `resource`, `work_item`, `work_item_resource (surrogate id PK, UNIQUE work_item_id + resource_id, quantity > 0)`.
  DB CHECK constraints mirror the rules.
- Adding a resource already on the work item → 409, not summed.
- Unit price ≥ 0 (0 allowed, negative refused). NUMERIC(14,2); quantity NUMERIC(12,4).
- Exact BigDecimal computation; `lineCost`, `directCost`, `salePrice` each rounded to whole ariary
  (HALF_UP) from their exact value. A work item without lines costs 0.
- K is a config constant, not a table.
- Price calculation is a pure Java class (no Spring/DB) → plain unit tests.

## Approaches considered
- **A (picked):** compute in a pure Java calculator, load data with JPA. Testable without a DB, logic in one place.
- B: SQL view with `SUM(qty*price)`. Compact, but testing the core rule needs a DB and logic splits SQL/Java.

## Stack
- Backend (user): Spring Boot 4.2, Java 21, JPA, Flyway (schema + seed), Validation, ProblemDetail errors.
  Tests: JUnit (calculator), MockMvc (controller).
- Frontend (Claude): Vite + React + TypeScript, plain CSS. Client generated from `docs/openapi.yaml`
  with `openapi-typescript` (types only) + `openapi-fetch` (~6 kB typed fetch wrapper).
  Vite dev proxy `/api → http://localhost:8080` (no CORS config needed on the backend).
  Tests: Vitest + Testing Library + jsdom.
- Contract: `docs/openapi.yaml` is the single source of truth; any change goes there first.

## Conventions
- English everywhere except README and IA.md (French, per the brief).
- Conventional Commits, subject only, authored by the user.
- Tests for every feature.

## Left to the builder
Exact seed data, component split on the frontend, Java package layout.
