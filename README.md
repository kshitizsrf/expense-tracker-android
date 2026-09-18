# Hisab Kitab

Hisab Kitab is an Android expense and budget planner for recording daily transactions, organizing spending by category, and understanding income and expenses through charts and summaries.

## Features

- Add income and expense transactions with amount, category, date, and time
- View monthly income, expenses, balance, budget, and recent transactions
- Filter and sort transactions by date
- Visualize income and expenses with date-range charts
- Create, edit, delete, and select custom income and expense categories
- Choose from multiple category icons supplied by the icon library module
- Set budgets for daily, weekly, monthly, or custom periods
- Configure app themes from the settings screen
- Export data and receive notification reminders
- Use the built-in calculator while entering transaction amounts

## Tech Stack

- Java
- Android SDK 34
- Minimum Android version: Android 7.0 (API 24)
- Gradle 8.2
- Android Gradle Plugin 8.2.0
- AndroidX AppCompat, ConstraintLayout, Preference, and WorkManager
- Material Components
- MPAndroidChart
- SQLite for local data storage
- Rhino Android for calculator expressions

## Project Structure

```text
.
├── app/
│   ├── src/main/java/com/example/budget_planner/
│   │   ├── MainActivity.java
│   │   ├── DBHelper.java
│   │   ├── Home_Fragment.java
│   │   ├── Chart_Fragment.java
│   │   ├── Transaction_Fragment.java
│   │   ├── Category_Settings.java
│   │   └── ...
│   └── src/main/res/
└── app/iconlibrary/
    └── src/main/res/drawable/
```

The `app` module contains the application UI, activities, fragments, local database, notifications, and resources. The `iconlibrary` module contains the category icon resources used by the app.

## Requirements

- Android Studio with Android SDK 34 installed
- JDK 8 or a compatible Android Studio Gradle JDK
- A physical Android device or emulator running API 24 or newer

## Getting Started

1. Clone the repository:

   ```bash
   git clone <repository-url>
   cd expense-tracker-android
   ```

2. Open the project in Android Studio.

3. Allow Gradle to sync and install any requested Android SDK components.

4. Select the `app` run configuration and launch it on an emulator or connected device.

### Build From the Command Line

On Windows:

```powershell
\.\gradlew.bat assembleDebug
```

On macOS or Linux:

```bash
./gradlew assembleDebug
```

The debug APK is generated under `app/build/outputs/apk/debug/`.

## Testing

Run local unit tests with:

```bash
./gradlew test
```

Run instrumented tests on a connected device or emulator with:

```bash
./gradlew connectedAndroidTest
```

The repository currently includes basic template tests. Database behavior, transaction calculations, date filtering, budgets, notifications, exports, and the main UI flows still need dedicated coverage.

## Permissions

The app declares the Android 13+ notification permission so it can deliver reminder and export notifications. The permission may need to be granted at runtime on supported Android versions.

## Data and Privacy

Transaction and category data is stored locally in the app's SQLite database. No backend service or account system is currently configured in the project.

## Known Limitations

- Database upgrades currently require a migration strategy before the schema changes in a released app.
- Date values are represented as formatted text in several parts of the application, which can affect sorting and filtering across locales.
- The project contains some legacy or unused helper classes and duplicated theme-related logic.
- Release signing and a production distribution workflow are not included yet.

## License

No license file is currently included. Add a license before distributing the project publicly.