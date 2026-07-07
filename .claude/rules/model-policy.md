# Model policy rules

- Follow `docs/AGENT_MODEL_POLICY.md` for Claude Sonnet 5 and GLM-5.2 work.
- For Claude Sonnet 5, use high effort for normal implementation and xhigh for architecture, security, privacy, permissions, and hard debugging.
- Do not use non-default temperature, top_p, or top_k with Claude Sonnet 5.
- For GLM-5.2, prefer max effort for long or risky coding work.
- Do not turn model guidance into app-level model routing.