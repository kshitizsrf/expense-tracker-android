# Hisab Kitab

Hisab Kitab is an offline-first expense and budget tracker for Android. Record income and expenses in seconds with a built-in calculator keypad, set a daily, weekly, monthly or custom budget, and see where your money goes with category breakdowns.

## Features

- **Aurora Glass design**: each theme is a living, slowly drifting gradient backdrop with frosted-glass cards. Animations switch off when system animations are disabled.
- **The original themes, reimagined**: Skyline, Dawn, Horizon, Pinkwalk and Rosewood, each with light and dark moods, plus Wallpaper (Material You) on Android 12+
- **Floating navigation, four styles**: Liquid (stretchy pill), Bubble (the icon rises in a glowing orb), Glow (a neon underline) and Expand (the tab widens to show its label), all with a raised **+** button. Pick one in Settings → Motion & style.
- **Home**: greeting, animated balance ticker (swipe to change month), daily-spending sparkline, budget ring with "safe to spend today", and quick-insight tiles
- **Fast entry**: calculator keypad (`120 + 45 × 2`), category strip, date, time and note
- **Activity**: day-grouped glass cards, search, filters, swipe-to-delete with undo
- **Insights, five ways**: interactive donut, category ranking, trend chart you can scrub, calendar heatmap (month view or year-at-a-glance), and a 6-month cash-flow chart, plus a comparison with the previous period. Charts switch with a Fade, Slide, 3D Flip or Zoom animation of your choice.
- **Spotlight**: tap a figure (balance, income, expenses, insight tiles, totals) to see it full screen over a blurred background, with only a few related details
- **Categories**: tiles with this month's spend; 139 icons and 15 colors; live preview in the editor
- **Budgets**: daily, weekly, monthly or custom
- **28 languages**: English, Hindi and 11 other Indian languages (Bengali, Marathi, Telugu, Tamil, Gujarati, Kannada, Malayalam, Punjabi, Odia, Assamese, Urdu), Nepali, and major world languages (Spanish, French, German, Portuguese, Italian, Russian, Arabic, Turkish, Indonesian, Vietnamese, Thai, Chinese, Japanese, Korean). Pick one in a searchable, flag-labelled list during onboarding or in Settings, where "System default" is also offered. Default category names follow the chosen language until you rename them.
- **App lock**: require a fingerprint, face or screen lock to open the app. The lock engages 30 seconds after leaving the app, and the screen is hidden from recents and screenshots while it is enabled.
- **Onboarding**: language (Hindi preselected), welcome, pick your theme (the app re-themes live), currency, reminder and app lock
- **Daily reminder** sent only when nothing was logged that day, and **CSV export** through the system file picker

## Tech stack

| Area | Choice |
|---|---|
| Language | Kotlin 2.4 (AGP built-in Kotlin) |
| UI | Jetpack Compose, Material 3, edge-to-edge, predictive back, SplashScreen API, Plus Jakarta Sans (variable font) |
| Architecture | MVVM with unidirectional data flow (`StateFlow` UI state and event callbacks) |
| Navigation | Navigation 3, with type-safe `@Serializable` keys |
| DI | Hilt (with assisted injection for screen arguments) |
| Persistence | Room (money stored as integer minor units, time as epoch millis, cascading foreign keys) |
| Preferences | Preferences DataStore |
| Background work | WorkManager + Hilt workers |
| Async | Kotlin Coroutines and Flow |
| Build | Gradle 9.8, AGP 9.4, version catalog, KSP, configuration cache |
| Tests | JUnit, kotlinx-coroutines-test, fakes; Room DAO instrumented tests |

- `minSdk` 26 (Android 8.0), `targetSdk`/`compileSdk` 37

## Project structure

The app is split into Gradle modules. Features never depend on each other; `:app` wires them together through navigation.

```text
.
├── app/                    # Application, MainActivity, Navigation 3 graph, bottom-bar scaffold
├── baselineprofile/        # Baseline Profile generator and startup benchmark
├── build-logic/            # Convention plugins shared by all modules
│   └── convention/         #   hisabkitab.android.{application,library,compose,feature}, hisabkitab.hilt
├── core/
│   ├── model/              # Domain models (Transaction, Category, Budget, ...), no Android code
│   ├── common/             # Money formatting, calculator engine, date helpers, DI qualifiers
│   ├── database/           # Room database, entities, DAOs, default categories
│   ├── data/               # Repositories (single source of truth), DataStore preferences, CSV export
│   ├── designsystem/       # Material 3 theme, palettes, reusable components, shared strings
│   ├── ui/                 # Shared UI that knows domain models (transaction row, date labels)
│   ├── icons/              # 139 category icons + typed lookup table
│   ├── notifications/      # Daily reminder worker, scheduler and notification
│   └── security/           # App lock: lock state, biometric prompt, lock screen
├── feature/
│   ├── onboarding/         # First-run flow: language, theme, currency, reminder, app lock
│   ├── home/               # Dashboard: month summary, budget progress, recent activity
│   ├── transactions/       # History, detail and the add/edit editor with calculator keypad
│   ├── stats/              # Insights: five chart types, period picker, comparisons
│   ├── categories/         # Category list and editor
│   ├── budget/             # Budget setup
│   └── settings/           # Appearance, motion, language, currency, reminders, privacy, export
└── gradle/libs.versions.toml  # Version catalog
```

Dependency direction: `app → feature:* → core:ui → core:designsystem`, and `feature:* → core:data → core:database → core:model`.

Each feature follows the same pattern: a `…ViewModel` exposes one immutable `…UiState` as a `StateFlow`. A stateful `…Screen` collects it with `collectAsStateWithLifecycle()` and passes it to a stateless composable that can be previewed.

## Getting started

1. Open the project in the latest stable Android Studio.
2. Let Gradle sync; install Android SDK 37 if prompted.
3. Run the `app` configuration on a device or emulator running Android 8.0 or newer.

From the command line:

```bash
./gradlew assembleDebug          # build
./gradlew testDebugUnitTest      # unit tests
./gradlew connectedDebugAndroidTest  # Room DAO tests on a device/emulator
./gradlew :app:generateReleaseBaselineProfile  # regenerate the Baseline Profile (device or emulator, API 28+)
```

## Data and privacy

Everything stays on the device. There are no accounts, no network access and no analytics. Android backup includes the database and preferences. A privacy policy you can host for the Play Store listing is in [docs/privacy-policy.md](docs/privacy-policy.md).

## Releasing to Google Play

- Application ID: `com.hisabkitab.app`, version `1.0.0` (versionCode `1`). Increase `versionCode` for every upload.

1. Create an upload key once. Keep the file and passwords safe and out of git (`*.jks` and `keystore.properties` are already ignored):

   ```bash
   keytool -genkeypair -v -keystore upload-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
   ```

2. Create `keystore.properties` in the project root:

   ```properties
   storeFile=upload-keystore.jks
   storePassword=...
   keyAlias=upload
   keyPassword=...
   ```

3. Build the bundle, then upload `app/build/outputs/bundle/release/app-release.aab` to the Play Console. Without `keystore.properties`, the release build is left unsigned.

   ```bash
   ./gradlew bundleRelease
   ```

4. Enroll in Play App Signing, host `docs/privacy-policy.md` at a public URL, and fill in the Data safety form ("no data collected or shared").

## License

The bundled Plus Jakarta Sans font is licensed under the SIL Open Font License 1.1 (`core/designsystem/FONT_LICENSE_OFL.txt`).


No license file is included yet. Add one before distributing the project publicly.
