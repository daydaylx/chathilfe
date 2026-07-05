# AGENTS.md

## Purpose

This file defines the working rules for AI coding agents contributing to this Android MVP.

The project is a private Android APK that provides a floating AI reply helper over WhatsApp. The app must stay small, privacy-safe, and technically realistic.

Agents must follow this file before making changes.

---

## Project summary

Build a native Android app that shows a small floating button when WhatsApp is in the foreground.

When the user taps the button, a compact reply panel opens. The user can:

- choose a mode:
  - Reply
  - Compose
  - Rewrite
- optionally use a copied message from the clipboard
- describe what they want to say
- choose a tone
- generate 3 AI-written suggestions
- copy one suggestion manually

The user manually pastes and sends the message in WhatsApp.

---

## Hard scope boundaries

Do not build or add:

- no automatic reading of WhatsApp chats
- no automatic reading of full conversations
- no automatic message insertion into WhatsApp
- no automatic sending
- no Accessibility Service
- no contact access
- no SMS access
- no notification scraping
- no screen scraping
- no background clipboard monitoring
- no clipboard history
- no message history storage
- no account system
- no cloud sync
- no analytics/tracking
- no Play Store release work unless explicitly requested
- no multi-app support unless explicitly requested
- no large Clean Architecture rewrite
- no unnecessary abstraction layers

If a requested implementation appears to require any of the above, stop and explain the tradeoff before coding.

---

## Primary target

- Platform: Android
- Language: Kotlin
- Main app/settings UI: Jetpack Compose
- Overlay: Android WindowManager
- Overlay window type: TYPE_APPLICATION_OVERLAY
- Foreground app detection: UsageStatsManager.queryEvents()
- Local settings: DataStore
- AI provider: start with one provider only, preferably OpenRouter or OpenAI
- Distribution: private APK
- Primary test device: Samsung Galaxy S25 / modern Android 15 or Android 16 device

---

## Android implementation rules

### Overlay

Use only:

- WindowManager
- TYPE_APPLICATION_OVERLAY
- Settings.canDrawOverlays()
- Settings.ACTION_MANAGE_OVERLAY_PERMISSION

Do not use deprecated system window types such as:

- TYPE_PHONE
- TYPE_SYSTEM_ALERT
- TYPE_SYSTEM_OVERLAY
- TYPE_TOAST for overlay behavior

Centralize all addView, removeView, and updateViewLayout calls in one overlay controller.

The overlay controller must prevent duplicate attached views.

The overlay must be removed cleanly when the service/app stops.

---

### Foreground app detection

Use UsageStatsManager.queryEvents() to determine the current foreground package.

Required behavior:

- show the floating button only when package is com.whatsapp
- hide the floating button outside WhatsApp
- optionally prepare for com.whatsapp.w4b later, but do not implement unless requested
- handle missing usage-access permission clearly
- keep polling conservative and battery-aware

Suggested starting interval:

- 1000 ms
- reduce to 500 ms only if visibility feels too delayed

Do not use Accessibility APIs for foreground app detection.

---

### Service behavior

Do not start a hidden long-running service without user action.

Preferred flow:

1. User opens MainActivity.
2. User grants required permissions.
3. User explicitly enables the overlay.
4. The app starts the overlay runtime from visible user action.
5. The overlay runtime shows or hides the bubble based on WhatsApp foreground state.

If a Foreground Service is used:

- declare the required permission
- declare the correct foregroundServiceType for targetSdk 34+
- call startForeground() promptly
- provide a clear persistent notification
- do not run network requests continuously inside the service

Avoid designs that depend on starting a foreground service from the background.

---

### Clipboard

Clipboard access must be user-initiated.

Allowed:

- read clipboard when the user opens the reply panel
- show a preview of detected copied text
- use copied text only after the user confirms
- copy generated suggestions when the user taps Copy

Forbidden:

- background clipboard monitoring
- silently reading clipboard while the panel is closed
- storing clipboard contents
- logging clipboard contents

---

### Permissions

Allowed permissions:

- INTERNET
- SYSTEM_ALERT_WINDOW
- PACKAGE_USAGE_STATS
- POST_NOTIFICATIONS only if a foreground notification is required
- FOREGROUND_SERVICE only if a foreground service is actually used

Forbidden permissions unless explicitly approved:

- READ_CONTACTS
- READ_SMS
- SEND_SMS
- RECORD_AUDIO
- CAMERA
- ACCESS_FINE_LOCATION
- ACCESS_COARSE_LOCATION
- READ_EXTERNAL_STORAGE
- READ_MEDIA_IMAGES
- READ_MEDIA_VIDEO
- BIND_ACCESSIBILITY_SERVICE

Do not add permissions speculatively.

---

## Architecture

Keep the architecture small and feature-oriented.

Preferred structure:

```text
app/
├── MainActivity.kt
├── settings/
│   ├── SettingsScreen.kt
│   ├── SettingsStore.kt
│   └── PermissionStatus.kt
├── overlay/
│   ├── OverlayService.kt
│   ├── OverlayController.kt
│   ├── FloatingBubbleView.kt
│   ├── ReplyPanelView.kt
│   └── OverlayPositionStore.kt
├── detection/
│   └── ForegroundAppDetector.kt
├── clipboard/
│   └── ClipboardHelper.kt
├── ai/
│   ├── AiClient.kt
│   ├── PromptBuilder.kt
│   ├── AiResponseParser.kt
│   └── AiConfig.kt
└── model/
    ├── ReplyMode.kt
    ├── ToneOption.kt
    ├── ReplyRequest.kt
    └── ReplySuggestion.kt
```

Do not add repository/use-case/interactor/facade layers unless a concrete need appears.

Prefer direct, readable Kotlin over over-engineered abstractions.

---

## UI rules

The UI must be practical, compact, and non-invasive.

### Floating button

Required:

- small
- draggable
- edge-positioned
- visually unobtrusive
- does not block normal WhatsApp typing
- tap opens the reply panel
- drag does not accidentally open the panel
- position is saved locally
- visible only while WhatsApp is foreground

### Reply panel

Required fields:

- mode selector:
  - Reply
  - Compose
  - Rewrite
- optional clipboard preview
- button to use clipboard text
- user intent input
- tone selector
- generate button
- loading state
- error state
- exactly 3 generated suggestions
- copy button per suggestion
- close button

Do not build a full-screen chat app.

Do not build a complex navigation system.

---

## AI behavior

The AI must not infer the task freely. The app decides the mode and builds a mode-specific prompt.

Supported modes:

- Reply: answer a copied message
- Compose: write a new message from rough intent
- Rewrite: improve an existing text

The AI request may include only:

- selected mode
- confirmed copied text, if used
- user intent
- selected tone
- desired language
- requested count of 3 suggestions

Do not send:

- contacts
- full chat histories
- unconfirmed clipboard contents
- device identifiers
- analytics data

---

## Prompt rules

Prompts must produce directly copyable chat messages.

General output rules:

- natural German by default
- no explanations
- no analysis
- no markdown tables
- no long essays
- no therapy-style artificial wording
- no exaggerated politeness
- exactly 3 variants
- each variant should stand alone

The parser must be tolerant. If the model returns imperfect numbering, extract reasonable suggestions instead of crashing.

---

## Data storage

Allowed local storage:

- API key
- overlay enabled/disabled
- preferred tone
- last selected mode
- floating button position

Forbidden local storage:

- copied WhatsApp messages
- generated replies
- user intent text
- contacts
- chat history
- clipboard history

Never log:

- API keys
- copied text
- generated replies
- user intent text

---

## Build and test commands

Before changing commands, inspect the actual Gradle project.

Expected commands may include:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

If the project uses a different module name, adapt commands after inspecting the repo.

Do not invent passing tests. If a command cannot be run, report why.

---

## Required validation before completion

For every implementation task, validate as much as possible:

- project builds
- affected unit tests pass, if present
- lint is checked, if practical
- no forbidden permissions were added
- no Accessibility Service was added
- no WhatsApp automation was added
- no clipboard monitoring was added
- no sensitive values are logged
- overlay lifecycle avoids duplicate views
- missing permissions show clear UI

For device-only behavior, provide a manual test checklist.

---

## Manual test checklist

Use this checklist for APK/device validation:

- install APK
- open app
- grant overlay permission
- grant usage access
- save API key
- enable overlay
- open WhatsApp
- floating button appears
- leave WhatsApp
- floating button disappears
- return to WhatsApp
- button appears again
- drag button
- button position persists
- tap button
- reply panel opens
- copy a WhatsApp message manually
- open panel
- clipboard preview appears
- confirm clipboard use
- generate suggestions in Reply mode
- generate suggestions in Compose mode
- generate suggestions in Rewrite mode
- copy a suggestion
- paste manually into WhatsApp
- lock/unlock device
- verify overlay does not duplicate
- disable internet and confirm clean error
- remove API key and confirm clean error
- disable overlay and confirm button disappears

---

## Documentation rules

When behavior changes, update relevant docs:

- README.md for setup/install/user-facing behavior
- docs/ARCHITECTURE.md for structural decisions
- docs/ANDROID_CONSTRAINTS.md for Android-specific constraints
- docs/TEST_PLAN.md for test changes
- docs/PRIVACY_SECURITY.md for privacy/security changes
- docs/PROMPTS.md for prompt changes

Keep AGENTS.md concise. Do not duplicate entire documents here if a targeted doc exists.

---

## Coding style

General:

- prefer simple Kotlin
- prefer explicit names
- keep functions small enough to understand
- avoid clever abstractions
- avoid premature generalization
- avoid global mutable state where possible
- handle errors explicitly
- keep UI state predictable

Do not perform broad formatting-only changes unless requested.

Do not refactor unrelated code.

---

## Dependency rules

Before adding a dependency:

1. Check if Android/Kotlin standard APIs are enough.
2. Explain why the dependency is needed.
3. Prefer stable, common libraries.
4. Do not add large frameworks for small tasks.
5. Do not add analytics, tracking, or ad SDKs.

Ask for approval before adding new production dependencies.

---

## Security rules

- API keys must be stored locally only.
- API keys must never be committed.
- API keys must never be printed in logs.
- User text must not be logged.
- Clipboard content must not be logged.
- Generated replies must not be logged.
- Do not add telemetry.
- Do not add crash reporting SDKs unless explicitly requested.

---

## Git rules

Before making changes:

- inspect current branch
- inspect repo status
- avoid overwriting user changes
- keep changes focused

After making changes:

- summarize changed files
- summarize behavior change
- list commands run
- list commands not run and why
- list remaining risks

Do not commit unless explicitly asked.

Do not push unless explicitly asked.

Exception: when explicitly asked to add or update this file in the repository, committing this AGENTS.md file is allowed.

---

## Completion format

At the end of every task, report:

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

Be honest. Do not claim success for untested Android behavior.

---

## Decision defaults

If uncertain, prefer:

- private APK over Play Store work
- manual user action over automation
- UsageStats over Accessibility
- simple overlay over full app UI
- explicit mode selection over AI guessing
- local-only settings over server-side storage
- small implementation over general framework
- documented limitation over fragile workaround

---

## Stop conditions

Stop and ask for explicit approval before:

- adding Accessibility
- adding auto-send
- adding auto-insert into WhatsApp
- reading WhatsApp chats automatically
- adding contact access
- adding analytics/tracking
- adding server-side storage
- changing the app into a keyboard
- expanding to other messengers
- introducing a large architecture rewrite
- changing targetSdk strategy
- adding paid APIs or subscriptions
