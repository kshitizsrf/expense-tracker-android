# Hisab Kitab

Hisab Kitab is an offline-first expense and budget tracker for Android. Record income and expenses in seconds with a built-in calculator keypad, set a daily, weekly, monthly or custom budget, and see where your money goes with category breakdowns.

## Features

- **Fast entry**: add or edit transactions with a calculator keypad (`120 + 45 × 2`), a category picker, date, time and an optional note
- **Home dashboard**: monthly balance, income and expenses, budget progress with a "safe to spend per day" figure, and recent activity
- **History**: transactions grouped by day with daily totals, search across notes and categories, and income/expense filters
- **Statistics**: an animated donut chart and per-category breakdown for this month, last month, last 3 months, this year or any custom range
- **Categories**: 139 icons and 15 colors; create, edit and delete (deleting a category removes its transactions after confirmation)
- **Budgets**: daily, weekly (respecting your locale's first day of week), monthly, or a custom date range
- **Personalisation**: light, dark or system theme, 5 color palettes, Material You dynamic color (Android 12+), any currency
- **Daily reminder**: an optional notification at a time you choose, sent only if nothing was logged that day
- **CSV export**: transactions and categories, saved wherever you choose (no storage permission needed)

## Tech stack

| Area | Choice |
|---|---|
| Language | Kotlin 2.4 (AGP built-in Kotlin) |
| UI | Jetpack Compose, Material 3, edge-to-edge, predictive back, SplashScreen API |
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
├── build-logic/            # Convention plugins shared by all modules
│   └── convention/         #   hisabkitab.android.{application,library,compose,feature}, hisabkitab.hilt
├── core/
│   ├── model/              # Domain models (Transaction, Category, Budget, ...), no Android code
│   ├── common/             # Money formatting, calculator engine, date helpers, DI qualifiers
│   ├── database/           # Room database, entities, DAOs, default categories, v1 importer
│   ├── data/               # Repositories (single source of truth), DataStore preferences, CSV export
│   ├── designsystem/       # Material 3 theme, palettes, reusable components, shared strings
│   ├── ui/                 # Shared UI that knows domain models (transaction row, date labels)
│   ├── icons/              # 139 category icons + typed lookup table
│   └── notifications/      # Daily reminder worker, scheduler and notification
├── feature/
│   ├── home/               # Dashboard: month summary, budget progress, recent activity
│   ├── transactions/       # History, detail and the add/edit editor with calculator keypad
│   ├── stats/              # Donut chart and category breakdown
│   ├── categories/         # Category list and editor
│   ├── budget/             # Budget setup
│   └── settings/           # Appearance, currency, reminders, export
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
```

## Upgrading from v1

Version 1 kept data in `expense_tracker.db` with dates stored as text. On first launch, v2 imports every category and transaction into the new Room database (converting dates and amounts), then removes the old file. Budgets and theme choices from v1 are not carried over.

## Data and privacy

Everything stays on the device. There are no accounts, no network access and no analytics. Android backup includes the database and preferences.

## Known limitations

- The application ID is still `com.example.budget_planner`, so existing installs upgrade in place. Pick a unique ID before publishing to Google Play.
- Release signing is not configured.

## License

No license file is included yet. Add one before distributing the project publicly.
