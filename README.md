# Mononote

A minimalist notes app for Android with **exactly one active note at a time**.

Inspired by [Mononote](https://www.digitalminimalist.com/blog/introducing-mononote) by The Digital Minimalist. Write down anything you want your future self to remember — a to-do, a reminder, a motivational note — and keep it visible. When you're done, archive it or delete it, and a fresh blank note is ready.

## Features

- **One note at a time** — no lists, no folders, no clutter
- **Autosaves as you type** — no save button, just a subtle saving indicator
- **Done pill while editing** — dismisses the keyboard; a go-live action bar appears when you're done: view archived notes, go live, delete
- **Go live** — keep your current note as a persistent live notification that stays in sync as you type
- **Archive** (recoverable) or **delete** (permanent) when you're done
- **Restore** any archived note whenever you need it
- **Home screen widget** showing your active note
- **Light & dark mode** following the system theme
- **100% offline** — your notes never leave the device

## Tech stack

| Layer        | Technology                                       |
|--------------|--------------------------------------------------|
| Language     | Kotlin 2.1.20                                    |
| UI           | Jetpack Compose (Material 3), Navigation Compose |
| Persistence  | Room                                             |
| Preferences  | DataStore (widget snapshot, settings)            |
| Widget       | Jetpack Glance                                   |
| Architecture | MVVM (ViewModel + StateFlow)                     |

## Development tooling

- **Linting:** ktlint + detekt, both with [compose-rules](https://mrmans0n.github.io/compose-rules) for Compose-specific checks
- **Git hooks:** Lefthook (`npm install` to install) — runs lint on pre-commit
- **CI:** GitHub Actions — ktlint, detekt, unit tests, and build on every PR
- **Dependencies:** Renovate keeps `gradle/libs.versions.toml` up to date; patch bumps auto-merge behind the `renovate/automerge` label
- **Debugging:** Timber for logging, LeakCanary for memory leaks (debug builds only)
- **Testing:** JUnit 5 + MockK + Turbine (unit), Compose UI tests (instrumented)

## Requirements

- JDK 17+
- Android SDK 36 (platform + build-tools)
- Gradle 8.14.3 (use the bundled wrapper — no global install needed)

## Build & run

```bash
./gradlew :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Install it on a connected device or emulator:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Roadmap

- [x] Phase 1 — Scaffold: Gradle, manifest, theme tokens, navigation shell
- [x] Phase 2 — Data layer: Room entity/DAO/DB, repository with single-note invariant
- [x] Phase 3 — Editor screen: autosave, status ring, overflow menu, archive/delete, go-live live notification
- [ ] Phase 4 — Archive screen: list, restore, permanent delete
- [ ] Phase 5 — Home screen widget (Glance)
- [ ] Phase 6 — Polish: edge cases, final QA

## License

Private project — source is published for reference.
