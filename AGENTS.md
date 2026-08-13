# AGENTS.md

Mononote for Android — minimalist single-note app (one active note at a time). Kotlin + Jetpack Compose, single `:app` module, package `com.mononote.app`. Offline-only.

## Build & verify

- `./gradlew :app:assembleDebug` — build; APK at `app/build/outputs/apk/debug/app-debug.apk` (~2 min, builds clean).
- Lint: `./gradlew :app:ktlintCheck` (auto-fix with `:app:ktlintFormat`) and `./gradlew :app:detekt`. Config lives in `.editorconfig` and `config/detekt/detekt.yml`.
- Common commands are aliased in the `justfile` (`just check`, `just build`, `just test`, `just lint`, `just format`, `just detekt`, `just hooks`, ...).
- Unit tests (JUnit 5): `./gradlew :app:testDebugUnitTest`. Instrumented Compose UI tests (JUnit 4): `./gradlew :app:assembleDebugAndroidTest` (compiles; running needs an emulator/device).
- CI (`.github/workflows/ci.yml`) runs ktlintCheck + detekt + testDebugUnitTest + assembleDebug on every PR. Lefthook git hooks (`.lefthook.yml`) run ktlint/detekt on pre-commit — install with `npm install` (or `npx lefthook install`). Renovate (`.github/workflows/renovate-automerge.yml`) opens dep PRs; patch bumps auto-merge behind the `renovate/automerge` label.
- Gradle wrapper 8.14.3; `org.gradle.configuration-cache=true` and build cache are on.

## Toolchain — do NOT bump blindly

Versions are pinned in `gradle/libs.versions.toml` (single source of truth): AGP 8.11.0, Kotlin 2.1.20, KSP 2.1.20-2.0.1, Compose BOM 2025.09.00, Room 2.8.4, Glance 1.1.1, compileSdk/targetSdk 36, minSdk 26.

- **AGP 9.x is deliberately avoided.** It enables built-in Kotlin and breaks the classic `org.jetbrains.kotlin.android` plugin setup. Upgrading is a migration, not a version bump.
- The Compose compiler plugin version must track the Kotlin version (both 2.1.20), and KSP must match Kotlin.
- **Lint-tool versions are a locked set:** ktlint 1.5.0 + detekt 1.23.8 + compose-rules 0.4.23. compose-rules 0.4.23 is the last that supports BOTH ktlint 1.5.x AND detekt 1.23.x; newer compose-rules need ktlint 1.8/detekt 2.0-alpha. Do not bump one in isolation — the ktlint version is pinned in the `ktlint {}` block as well as the catalog.
- Env: `local.properties` pins `sdk.dir=/home/ali/Android/Sdk` (platforms 35/36 installed). Gradle targets `jvmTarget 17`; `JAVA_HOME` is JDK 17 while `java` on PATH is 21 — do not assume which.
- minSdk 26 ⇒ launcher icon is a vector adaptive icon only (`mipmap-anydpi-v26/`, `drawable/ic_launcher_*`); never add PNG mipmaps.

## Product rules (from the build spec — do not violate)

- Exactly one active note (`archivedAt == null`) at any time; invariant enforced in the repository.
- Autosave is debounced (~500ms). The repository's save must write Room AND the widget DataStore snapshot in **one suspend function** — never two fire-and-forget calls (avoids stale widget text).
- Process death mid-typing: `SavedStateHandle` retains the in-progress draft (key `editor_draft`; a restored non-blank draft wins over the older persisted text on relaunch), and `onPause`/`onStop` flush an immediate, non-debounced save.
- The "Done" pill shows only while the text field is focused and ONLY dismisses the keyboard — it never saves, archives, or deletes. Otherwise the editor shows a go-live action bar (archive the current note, go live, delete, left to right). The overflow menu holds exactly two items: Archived Notes and Settings; Settings is a placeholder entry for a later phase (dark/light mode, font, widget & live-notification settings, feedback, about).
- Archive = set `archivedAt` (reversible, no confirmation). Delete = hard row delete (permanent; confirm dialog first). Overflow menu hides both when the active note is blank.
- Restore: empty active note → swap restored note in; non-empty → confirm, archive current first, then restore.
- Go live: a persistent live notification of the note (the iOS Live Activity equivalent), now a v1 feature. It is a `specialUse` foreground service (`LiveNoteService`) that observes the active note and keeps the notification in sync. Requires explicit `POST_NOTIFICATIONS` + `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_SPECIAL_USE` manifest permissions and a runtime notification-permission request (API 33+) before starting.
- No network permission in the manifest (acceptance requirement). Transitive `ACCESS_NETWORK_STATE`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE` come from WorkManager/Glance — expected, do not remove.

## Current state & architecture

- Phases 1–4 done (scaffold, data layer, editor screen, archive screen). Phase 5 (Glance widget), Phase 6 (polish) pending.
- Dev tooling wired (Phase 1.5): ktlint + detekt + compose-rules, Lefthook hooks, GitHub Actions CI, Renovate, Timber, LeakCanary (debug), JUnit 5 + Turbine + MockK unit tests, Compose UI test scaffold. Unit tests live in `src/test` (JUnit 5), instrumented in `src/androidTest` (JUnit 4).
- `data/` holds the Note entity, DAO, Room DB, `SettingsDataStore` (widget snapshot), and `NotesRepository` (single-note invariant).
- `notification/` holds the go-live feature: `LiveNoteService` (specialUse foreground service), `LiveNoteNotifications`, and `LiveNoteController` (app-scoped `isLive` state).
- `ui/editor/` holds the real editor screen + ViewModel. `ui/archive/` holds the archive screen + ViewModel (archived list with per-row restore/delete, both confirmation-gated).
- Design tokens: `ui/theme/Color.kt` defines `MononoteColors` + `LocalMononoteColors` (CompositionLocal). Use these, not stock Material 3 defaults. Secondary text is `#9A9A9E` in BOTH light and dark. `MononoteTheme` (Theme.kt) follows the system theme.
- Routes: `editor` (start) and `archive` in `navigation/MononoteNavHost.kt`; the editor's overflow menu navigates to `archive` (the go-live bar archives the current note instead).

## Git

- Origin: `https://github.com/Alimedhat000/Mononote-android.git`, branch `main`. Commit and push only when explicitly asked.
