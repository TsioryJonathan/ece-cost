# Milestones

Owner: **J** = Jonathan (backend), **C** = Claude (frontend). Tick when done + tests green.
Budget: 2h total.

## M0 — Groundwork (C, ~10 min)

- [x] git repo + .gitignore
- [x] `docs/openapi.yaml` (API contract)
- [x] `docs/design.md`, `docs/MILESTONES.md`
- [x] `IA.md` started, `CLAUDE.md` (AI context file kept in repo)

## M1 — Database (J, ~15 min)

- [x] Datasource config (`application.properties`)
- [x] Flyway `V1__schema.sql` (3 tables + CHECK constraints)
- [x] Flyway `V2__seed.sql` (3–5 work items, 5–8 resources, some lines)

## M2 — Price calculation (J, ~15 min)

- [x] Pure calculator: line cost, direct cost, sale price (K = 1.35), rounding
- [x] Unit tests: multi-line sum, empty work item = 0, rounding, K applied on exact value

## M3 — API (J, ~30 min)

- [x] `GET /api/work-items`, `GET /api/resources`
- [x] `POST /api/work-items/{id}/resources` (400 / 404 / 409)
- [x] `PATCH /api/resources/{id}` (400 / 404)
- [x] ProblemDetail errors
- [x] MockMvc tests (happy path + validation errors)

## M4 — Frontend scaffold (C, ~15 min)

- [x] Vite + React + TS in `frontend/`
- [x] `npm run generate:api` → types from `docs/openapi.yaml`, typed client with `openapi-fetch`
- [x] Vite proxy `/api` → `:8080` (`npm run dev:mock` → mock on `:8081`), Vitest setup
- [x] Stateful mock API (`npm run mock`, no dependency)

## M5 — Screen (C, ~35 min)

- [x] Work items with direct cost, sale price, lines table, cost split by resource type
- [x] Add resource form (resource select + quantity > 0, comma or dot decimals)
- [x] Resource prices list with inline edit (Enter saves, Escape cancels)
- [x] Loading / error states (ProblemDetail `detail` shown), notice after a price change

## M6 — Frontend tests (C, ~10 min)

- [x] List renders amounts
- [x] Quantity ≤ 0 is refused before calling the API
- [x] Price change refreshes work items
- [x] Add resource shows recomputed prices; 409 detail shown; negative price refused
- [x] Validation unit tests

## M7 — Integration (J + C, ~10 min)

- [ ] Run front against real backend, check the 3 features end to end

## M8 — Delivery (J + C, ~10 min)

- [ ] README (FR): run, done / not done, assumptions, 5-line business view
- [ ] IA.md (FR) finalized
- [ ] J reviews frontend code
- [ ] Push, send link — **before noon (Madagascar time), then no more commits**
