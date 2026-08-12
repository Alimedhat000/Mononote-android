# Mononote

A minimalist notes app for Android with **exactly one active note at a time**.

Inspired by [Mononote](https://www.digitalminimalist.com/blog/introducing-mononote) by The Digital Minimalist. Write down anything you want your future self to remember — a to-do, a reminder, a motivational note — and keep it visible. When you're done, archive it or delete it, and a fresh blank note is ready.

## Features

- **One note at a time** — no lists, no folders, no clutter
- **Autosaves as you type** — no save button, just a subtle saving indicator
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

## Project structure

```
app/src/main/java/com/mononote/app/
├── MainActivity.kt            # Single-activity Compose host
├── MononoteApp.kt             # Application class
├── data/                      # Note entity, DAO, Room DB, repository (in progress)
├── navigation/
│   └── MononoteNavHost.kt     # editor → archive routes
├── ui/
│   ├── editor/                # Active-note editor screen (in progress)
│   ├── archive/               # Archived notes screen (in progress)
│   └── theme/                 # MononoteColors, typography, theme
└── widget/                    # Glance home-screen widget (in progress)
```

## Roadmap

- [x] Phase 1 — Scaffold: Gradle, manifest, theme tokens, navigation shell
- [ ] Phase 2 — Data layer: Room entity/DAO/DB, repository with single-note invariant
- [ ] Phase 3 — Editor screen: autosave, status indicator, overflow menu, archive/delete
- [ ] Phase 4 — Archive screen: list, restore, permanent delete
- [ ] Phase 5 — Home screen widget (Glance)
- [ ] Phase 6 — Polish: edge cases, final QA

## License

Private project — source is published for reference.
