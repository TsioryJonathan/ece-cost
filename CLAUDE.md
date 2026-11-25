# Project rules (context file given to the AI)

- Brief: `Use case — Chiffrage.pdf` (ECE Tech case study, 2h max). Design: `docs/design.md`. Tracking: `docs/MILESTONES.md`.
- API contract: `docs/openapi.yaml` is the single source of truth. Change it first, then code.
- Split: backend (Spring Boot) by Jonathan, frontend (React + TS) by Claude. Jonathan reviews and makes every final decision.
- English everywhere (code, UI, commits) except README.md and IA.md, which are in French (required by the brief).
- Commits: Conventional Commits, English, subject line only, no body, authored by Jonathan.
- Always add tests. No overkill: minimal frameworks and libraries.
- Keep `IA.md` up to date: main prompts copied verbatim, tools, method, a rejected/corrected AI proposal.
