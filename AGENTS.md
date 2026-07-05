# AGENTS.md

## Purpose

This is the primary instruction file for coding agents working on ChatHilfe.

Keep this file short. Detailed rules live in the linked project documents. When in doubt, follow the most restrictive rule regarding privacy, permissions, automation, and scope.

---

## Project summary

ChatHilfe is a private Android MVP.

Goal: show a small floating AI reply helper over WhatsApp. The app helps the user formulate, rewrite, or answer messages. The user always copies, pastes, and sends manually.

---

## Read order

Before implementation work, read in this order:

1. `README.md`
2. `AGENTS.md`
3. `Konzept.md`
4. `docs/ARCHITECTURE.md`
5. `docs/ANDROID_CONSTRAINTS.md`
6. `docs/IMPLEMENTATION_PLAN.md`
7. task-relevant docs from the source-of-truth table below

Do not load every document unless needed for the current task.

---

## Source of truth

| Topic | File |
|---|---|
| Product goal and MVP scope | `Konzept.md` |
| Agent rules and stop conditions | `AGENTS.md` |
| External implementation prompt | `Arbeitsauftrag.md` |
| Technical architecture | `docs/ARCHITECTURE.md` |
| Android 15/16, overlay, services, permissions | `docs/ANDROID_CONSTRAINTS.md` |
| Implementation phases | `docs/IMPLEMENTATION_PLAN.md` |
| Testing | `docs/TEST_PLAN.md` |
| Privacy and security | `docs/PRIVACY_SECURITY.md` |
| AI prompts and response parsing | `docs/PROMPTS.md` |
| UI/UX behavior | `docs/UI_UX_SPEC.md` |

If documents conflict, use this priority:

1. `AGENTS.md`
2. `docs/PRIVACY_SECURITY.md`
3. `docs/ANDROID_CONSTRAINTS.md`
4. `Konzept.md`
5. task-specific docs

---

## Hard scope boundaries

Do not build or add:

- automatic WhatsApp chat reading
- full conversation reading
- automatic insertion into WhatsApp
- automatic sending
- Accessibility Service
- Notification Listener
- screen scraping
- contact access
- SMS access
- background clipboard monitoring
- clipboard history
- message history storage
- account system
- cloud sync
- analytics/tracking
- Play Store release work unless explicitly requested
- multi-app support unless explicitly requested
- large Clean Architecture rewrite
- unnecessary abstraction layers

If a requested implementation appears to require any forbidden item, stop and explain the tradeoff before coding.

---

## Technical defaults

| Area | Default |
|---|---|
| Platform | Android |
| Language | Kotlin |
| Main app UI | Jetpack Compose |
| Overlay | Android `WindowManager` |
| Overlay type | `TYPE_APPLICATION_OVERLAY` |
| WhatsApp detection | `UsageStatsManager.queryEvents()` |
| Settings | DataStore |
| AI provider | one provider only for MVP |
| Distribution | private APK |
| Primary test device | Samsung Galaxy S25 / Android 15 or 16 |

---

## Build and test commands

Before changing commands, inspect the actual Gradle project.

Expected commands after Android setup exists:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

Do not claim successful builds, tests, or device behavior unless actually validated.

---

## Required validation before completion

For every implementation task, validate as much as practical:

- project builds, if build files exist
- relevant tests pass, if tests exist
- lint is checked, if practical
- no forbidden permissions were added
- no Accessibility Service was added
- no WhatsApp automation was added
- no background clipboard monitoring was added
- no API keys, user text, clipboard text, or generated replies are logged
- overlay lifecycle avoids duplicate views
- missing permissions show clear UI

For device-only behavior, report what still needs manual testing.

---

## Dependency rules

Before adding a production dependency:

1. Check whether Android/Kotlin standard APIs are enough.
2. Explain why the dependency is needed.
3. Prefer stable, common libraries.
4. Avoid large frameworks for small tasks.
5. Never add analytics, tracking, ads, or crash-reporting SDKs unless explicitly requested.

Ask for approval before adding new production dependencies.

---

## Git rules

Before making changes:

- inspect current branch and repo status when possible
- avoid overwriting user changes
- keep changes focused

After making changes:

- summarize changed files
- summarize validation
- list what was not validated
- list remaining risks

Do not commit or push unless explicitly asked.

Exception: when explicitly asked to add or update repository documentation, committing those documentation changes is allowed.

---

## Completion format

Use this format at the end of implementation tasks:

```text
Summary:
- ...

Files changed:
- ...

Validation:
- ...

Not validated:
- ...

Risks:
- ...

Next sensible step:
- ...
```

---

## Stop conditions

Stop and ask for explicit approval before:

- adding Accessibility
- adding auto-send
- adding auto-insert into WhatsApp
- reading WhatsApp chats automatically
- adding contact access
- adding notification scraping
- adding screen scraping
- adding analytics/tracking
- adding server-side storage
- changing the app into a keyboard
- expanding to other messengers
- introducing a large architecture rewrite
- changing targetSdk strategy
- adding paid APIs or subscriptions
