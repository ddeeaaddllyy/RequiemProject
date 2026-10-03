# Repository Guidelines

## Project Structure & Module Organization

This directory is the `:app` module of the RequiemProject Android repository. Run Gradle commands from the repository root (`..`). Application code lives in `src/main/java/com/application/requiemproject/`: `ui/` contains activities and fragments, `viewmodel/` holds UI state, `data/` contains API, Room, and repository code, and `services/`, `managers/`, and `translator/` support screen capture and translation. XML layouts, strings, icons, and other assets live in `src/main/res/`. Unit tests belong in `src/test/`; device tests belong in `src/androidTest/`.

## Build, Test, and Development Commands

Use JDK 17 and an Android SDK with API 36 installed. From the repository root, run:

- `.\gradlew.bat :app:assembleDebug` — build the debug APK.
- `.\gradlew.bat :app:installDebug` — install it on a connected device or emulator.
- `.\gradlew.bat :app:testDebugUnitTest` — run local JVM tests.
- `.\gradlew.bat :app:connectedDebugAndroidTest` — run instrumented tests on a connected device or emulator.
- `.\gradlew.bat :app:lintDebug` — run Android lint checks.

Open the repository root in Android Studio for Gradle sync and interactive debugging.

## Coding Style & Naming Conventions

Follow the existing Kotlin and Java style: four-space indentation, `PascalCase` classes, `camelCase` functions and properties, and lowercase package names. Keep Android resource names in `snake_case` (for example, `fragment_home.xml` and `ic_search_24.xml`). Place new code beside related features and keep UI, data access, and background services in their existing packages. No separate formatter is configured; use Android Studio formatting and review lint output.

## Testing Guidelines

Use JUnit for local tests in `src/test/` and AndroidX JUnit with Espresso for UI or device tests in `src/androidTest/`. Name tests after the class or behavior under test, ending in `Test` (for example, `TranslationRepositoryTest`). Add focused tests for changed logic; test permission, overlay, and screen-capture flows on a device when affected. No coverage threshold is configured.

## Commit & Pull Request Guidelines

Recent commits use short, descriptive subjects such as “Update the UI” and “Add new function to app”; no prefix convention is enforced. Prefer an imperative subject that names the change. In pull requests, describe the behavior changed, link relevant issues, list tests run, and include screenshots for UI changes. Call out any new permissions or configuration needs.

## Security & Configuration

Keep API keys and machine-specific SDK settings in the repository root's ignored `local.properties`, as described in `README.md`. Do not commit credentials or generated build output.
