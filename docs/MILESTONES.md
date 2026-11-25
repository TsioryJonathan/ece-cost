# Milestones

Owner: **J** = Jonathan (backend), **C** = Claude (frontend). Tick when done + tests green.
Budget: 2h total.

## M0 — Groundwork (C, ~10 min)
- [x] git repo + .gitignore
- [x] `docs/openapi.yaml` (API contract)
- [x] `docs/design.md`, `docs/MILESTONES.md`
- [x] `IA.md` started, `CLAUDE.md` (AI context file kept in repo)

## M1 — Database (J, ~15 min)
- [ ] Datasource config (`application.properties`)
- [ ] Flyway `V1__schema.sql` (3 tables + CHECK constraints)
- [ ] Flyway `V2__seed.sql` (3–5 work items, 5–8 resources, some lines)

## M2 — Price calculation (J, ~15 min)
- [ ] Pure calculator: line cost, direct cost, sale price (K = 1.35), rounding
- [ ] Unit tests: multi-line sum, empty work item = 0, rounding, K applied on exact value

## M3 — API (J, ~30 min)
- [ ] `GET /api/work-items`, `GET /api/resources`
- [ ] `POST /api/work-items/{id}/resources` (400 / 404 / 409)
- [ ] `PATCH /api/resources/{id}` (400 / 404)
- [ ] ProblemDetail errors
- [ ] MockMvc tests (happy path + validation errors)

## M4 — Frontend scaffold (C, ~15 min)
- [ ] Vite + React + TS in `frontend/`
- [ ] `npm run generate:api` → types from `docs/openapi.yaml`, typed client with `openapi-fetch`
- [ ] Vite proxy `/api` → `:8080`, Vitest setup

## M5 — Screen (C, ~35 min)
- [ ] Work items table (direct cost, sale price, expandable lines)
- [ ] Add resource form (resource select + quantity > 0)
- [ ] Resource prices list with inline edit
- [ ] Loading / error states (ProblemDetail `detail` shown)

## M6 — Frontend tests (C, ~10 min)
- [ ] List renders amounts
- [ ] Quantity ≤ 0 is refused before calling the API
- [ ] Price change refreshes work items

## M7 — Integration (J + C, ~10 min)
- [ ] Run front against real backend, check the 3 features end to end

## M8 — Delivery (J + C, ~10 min)
- [ ] README (FR): run, done / not done, assumptions, 5-line business view
- [ ] IA.md (FR) finalized
- [ ] J reviews frontend code
- [ ] Push, send link — **before noon (Madagascar time), then no more commits**
