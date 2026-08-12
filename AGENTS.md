# AGENTS.md

Mononote for Android — minimalist single-note app (one active note at a time). Kotlin + Jetpack Compose, single `:app` module, package `com.mononote.app`. Offline-only.

## Build & verify

- `./gradlew :app:assembleDebug` — build; APK at `app/build/outputs/apk/debug/app-debug.apk` (~2 min, builds clean).
- No tests or lint configured yet (no test source set). If you add them, record the commands here.
- Gradle wrapper 8.14.3; `org.gradle.configuration-cache=true` is on.

## Toolchain — do NOT bump blindly

Versions are pinned in `gradle/libs.versions.toml` (single source of truth): AGP 8.11.0, Kotlin 2.1.20, KSP 2.1.20-2.0.1, Compose BOM 2025.09.00, Room 2.8.4, Glance 1.1.1, compileSdk/targetSdk 36, minSdk 26.

- **AGP 9.x is deliberately avoided.** It enables built-in Kotlin and breaks the classic `org.jetbrains.kotlin.android` plugin setup. Upgrading is a migration, not a version bump.
- The Compose compiler plugin version must track the Kotlin version (both 2.1.20), and KSP must match Kotlin.
- Env: `local.properties` pins `sdk.dir=/home/ali/Android/Sdk` (platforms 35/36 installed). Gradle targets `jvmTarget 17`; `JAVA_HOME` is JDK 17 while `java` on PATH is 21 — do not assume which.
- minSdk 26 ⇒ launcher icon is a vector adaptive icon only (`mipmap-anydpi-v26/`, `drawable/ic_launcher_*`); never add PNG mipmaps.

## Product rules (from the build spec — do not violate)

- Exactly one active note (`archivedAt == null`) at any time; invariant enforced in the repository.
- Autosave is debounced (~500ms). The repository's save must write Room AND the widget DataStore snapshot in **one suspend function** — never two fire-and-forget calls (avoids stale widget text).
- Process death mid-typing: retain in-progress text via `rememberSaveable`/`SavedStateHandle`; flush an immediate, non-debounced save in `onPause`/`onStop`.
- The "Done" button ONLY dismisses the keyboard — it never saves, archives, or deletes.
- Archive = set `archivedAt` (reversible, no confirmation). Delete = hard row delete (permanent; confirm dialog first). Overflow menu hides both when the active note is blank.
- Restore: empty active note → swap restored note in; non-empty → confirm, archive current first, then restore.
- Persistent notification (the iOS Live Activity equivalent) is explicitly cut from v1 — future phase only.
- No network permission in the manifest (acceptance requirement). Transitive `ACCESS_NETWORK_STATE`, `WAKE_LOCK`, `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE` come from WorkManager/Glance — expected, do not remove.

## Current state & architecture

- Phase 1 (scaffold) done. Phases 2–6 pending: data layer (Room/DataStore/repository), editor screen, archive screen, Glance widget, polish.
- `data/` (Note entity, DAO, Room DB, repository) and `widget/` do NOT exist yet — their deps are already in the version catalog.
- `ui/editor/EditorScreen.kt` and `ui/archive/ArchiveScreen.kt` are placeholder stubs awaiting Phases 3/4.
- Design tokens: `ui/theme/Color.kt` defines `MononoteColors` + `LocalMononoteColors` (CompositionLocal). Use these, not stock Material 3 defaults. Secondary text is `#9A9A9E` in BOTH light and dark. `MononoteTheme` (Theme.kt) follows the system theme.
- Routes: `editor` (start) and `archive` in `navigation/MononoteNavHost.kt`.

## Git

- Origin: `https://github.com/Alimedhat000/Mononote-android.git`, branch `main`. Commit and push only when explicitly asked.
