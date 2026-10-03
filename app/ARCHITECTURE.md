# Requiem: Clean Architecture + MVVM

The Android `:app` module has three explicit layers and one composition root. The screen UI is entirely Jetpack Compose, including authentication, profile editing, language selection, navigation and the system translation overlay.

```text
com.application.requiemproject/
  domain/
    model/          Platform-independent values and translation results
    repository/     Storage contracts
    usecase/        Account validation, settings, search and translation rules
  data/
    api/            Retrofit API and transport DTOs
    local/          Room, session storage and converters
    repository/     Implementations of domain contracts, OCR adapter
    translator/     MyMemory adapter
    platform/       Android capture and notification infrastructure
  presentation/
    account/        Authentication and profile UI + AccountViewModel
    home/           Translation UI + HomeViewModel
    help/           Searchable help UI + HelpViewModel
    navigation/     Destinations and saved navigation state
    components/     Shared Compose elements and original vector artwork
    theme/          Color and typography tokens
    overlay/        Compose overlay and its lifecycle host
    preview/        Android Studio previews
    MainActivity    Activity results and Android permission boundary
  di/
    AppContainer    Dependency construction and ViewModel factory
```

Domain has no Android, Room, Retrofit or presentation dependencies. Repositories implement domain interfaces; ViewModels depend on domain use cases. Composables receive immutable state and callbacks and never access a database, preferences or a service. State flows are collected with `collectAsStateWithLifecycle`. Only the Android entry points wire platform operations to the application container.

The existing OCR/capture pipeline remains an Android infrastructure adapter. Its text coordinates now use a platform-independent `TextBounds`; the adapter converts framework coordinates at the boundary. The translation operation lives in `TranslateBlocksUseCase`. Room tables, preference keys and existing local accounts remain compatible.

Navigation, help search, filters and language-picker selection use `SavedStateHandle`. Translation settings are persisted through `SettingsRepository`. Account credentials remain transient ViewModel state; passwords are not put in saved-instance state. The existing local account storage mechanism is retained; this UI migration does not introduce remote authentication.

## Visual system

- Ink `#0D0D10`, paper `#F4F0E8`, red `#F32040`, gold `#EAC977`.
- Slanted panels, italic display headlines, numbered sections, halftone background and an original lightning insignia drawn in Compose.
- Animated navigation, selected states, button presses, expanding answers and a slow insignia motion. Compose respects the system animation duration scale.
- Scrollable screens and sheets, safe drawing insets, keyboard handling, semantic tabs/radio choices, labelled icon actions, and previews for compact screens and larger text.
- Persona 5 Royal is the visual reference; no extracted game assets, logos or fonts are bundled.

`res/layout` and `res/menu` are removed, along with Fragments, adapters, AppCompat and ViewBinding. Remaining XML files are Android platform resources: manifest, launcher/notification assets, application label, launch theme and service/backup configuration. Compose does not replace these Android resource contracts.

## Checks

From the repository root, with JDK 17 and Android SDK 36:

```powershell
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Unit tests exercise domain rules, ViewModel state and dependency boundaries. `ComposeNavigationTest` exercises the guest flow, Activity recreation, registration, profile editing, logout and returning login on a clean app installation. `ComposeOverlayTest` checks that the Compose window can attach, render, dispose and reattach. Capture requires notification, overlay and MediaProjection consent; Accessibility mode additionally requires enabling the accessibility service. The UI displays cancellation and denied-permission feedback instead of claiming that capture is active.

After upgrading an existing installation, re-enable the accessibility service in Android settings: its component moved into `data.platform.service`. No new Android permissions are added.

UI tests copy review screenshots into `/sdcard/Download/requiem-ui-review` before Gradle removes the test application. Retrieve them with `adb pull /sdcard/Download/requiem-ui-review`. Android Studio previews are in `presentation/preview/ScreenPreviews.kt`.
