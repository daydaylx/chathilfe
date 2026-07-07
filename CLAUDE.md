@AGENTS.md

## Claude Code

This file exists because Claude Code reads `CLAUDE.md` as the project memory entrypoint.

Use `AGENTS.md` as the shared source for all coding agents. The rules below are Claude-specific overlays and must not contradict `AGENTS.md`.

## Claude Sonnet 5 defaults

- Use `high` effort for normal implementation work.
- Use `xhigh` effort for architecture, Android lifecycle, permissions, security, privacy, multi-file refactors, and debugging hard failures.
- Use `medium` only for small documentation or narrow cleanup tasks.
- Use `low` only for trivial, clearly scoped edits.
- Do not use manual extended thinking budgets with Claude Sonnet 5.
- Do not set non-default `temperature`, `top_p`, or `top_k` for Claude Sonnet 5.
- Keep enough `max_tokens` headroom for tool use and final summaries on long tasks.

## Project workflow

- Follow `docs/IMPLEMENTATION_PLAN.md` by phase.
- Follow `docs/DEVICE_TEST_POLICY.md`: real device testing is deferred to Phase 8.
- Before editing, inspect the relevant source-of-truth document.
- Keep changes focused and small enough to review.
- Do not broaden MVP scope without updating `docs/DECISIONS.md`.
- Do not claim builds, tests, lint, or device behavior unless actually validated.
- Mark untested device behavior as risk until Phase 8.

## Output style

Use the completion format from `AGENTS.md` after implementation work.

For long tasks, provide short progress updates only when useful. Do not flood the user with low-level operational details.