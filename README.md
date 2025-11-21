# UniTask

UniTask is a Compose-first Android productivity app that helps students track assignments, manage subjects, set alarms, and stay focused via ambient sensors and gentle notifications. The project is organized with manual dependency wiring (via `AppModule`), Jetpack Compose screens, a Kotlin MVVM layer for UI state, and supporting Android services like sensors, notifications, and DataStore-backed settings.

## Architecture overview

- **Presentation (Compose + MVVM)**: Each screen exposes a `Route` composable that wires view models, handles navigation callbacks, and feeds data into stateless screen composables. `DashboardRoute`, `AddTaskRoute`, `SubjectsRoute`, and `AlarmSettingsScreen` host `Scaffold` layouts and call down into shared UI components (`TaskCard`, `RewardsBar`, `FocusSensorBanner`, etc.).
- **ViewModel layer**: View models live under `presentation.viewmodel`. `DashboardViewModel` aggregates tasks, urgent tasks, and notification/reward counts from use cases. `AddTaskViewModel` manages task form state (editing vs creating). `SubjectsViewModel` handles CRUD for subjects; `AlarmViewModel` orchestrates alarm creation/cancellation.
- **Domain/use cases**: Located in `domain.usecase`, each use case wraps a repository to keep business rules isolated. Examples: `GetAllTasksUseCase`, `CompleteTaskUseCase`, `AddSubjectUseCase`, `ScheduleAlarmUseCase`.
- **Repositories & data**: `data.repository` houses in-memory repositories for subjects/tasks along with sample data generators. Shared preference-based repositories (`SharedPrefsNotificationRepository`, `SharedPrefsRewardRepository`) store notifications/rewards. Focus alert settings use a `DataStore` repository under `settings`.
- **Notifications & alarms**: Notification channels and alarm scheduling rely on `NotificationHelper`, `AlarmScheduler`, and wrappers (`AlarmManagerWrapper`). `FocusSensorManager` drives light and proximity tracking, pushing notifications and UI banners via `FocusSensorState`.
- **Sensors & focus workflow**: `FocusSensorManager` registers the light and proximity sensors, exposes a `StateFlow`, and posts reminders when darkness or device proximity suggests high focus potential. The app overlays a composable banner and ships notifications through `NotificationHelper`. Focus alerts honor a `FocusSensorSettingsRepository` toggle backed by `DataStore` and can be enabled/disabled from the dashboard settings dialog.
- **Navigation**: `presentation.navigation.NavGraph` stitches together routes with Jetpack Compose Navigation (`NavHost`, `composable`). `UniTaskApp` hosts the graph, the focus banner, and wires theme toggling, sensor state, and `FocusSensorSettingsRepository` so that settings persistence flows throughout the UI.
- **Dependency wiring**: `AppModule` lazily instantiates repositories, use cases, and exposes `ViewModelProvider.Factory` instances. `MainActivity` configures `AppModule`, permissions, notification channels, instantiates `FocusSensorManager`, and embeds `UniTaskApp` inside `UniTaskTheme`.

## File-by-file explanation

### Root Gradle files (`build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`)

- Standard Android Gradle configuration targeting Compose, Kotlin, and required dependencies (Compose BOM, KotlinStdLib); `gradle/libs.versions.toml` tracks versions.

### `MainActivity.kt`

- Sets up runtime permissions (notifications, exact alarms), configures `AppModule`, initializes `NotificationHelper` and `FocusSensorManager`, and starts/stops sensor listeners tied to the activity lifecycle.
- Hosts `UniTaskApp` inside `UniTaskTheme`, passing theme state, `FocusSensorManager`, and `FocusSensorSettingsRepository` for persistence.

### `AppModule.kt`

- Manual dependency container that wires repositories, use cases, `AlarmScheduler`, and reward/notification storage.
- Exposes view model factories for dashboard, add-task, alarm, subjects, and rewards screens.

### `presentation/navigation/NavGraph.kt`

- Defines `UniTaskDestination` sealed objects for each navigation route (`Dashboard`, `AddTask`, `Subjects`, `AlarmSettings`).
- `UniTaskApp` composes the `UniTaskNavHost`, shows the `FocusSensorBanner` when alerts are enabled, and listens to repository state to toggle sensors.
- `UniTaskNavHost` wires Compose destinations: Dashboard (with focus settings and task-to-edit navigation), AddTask (accepts optional `taskId` for editing), Subjects, and AlarmSettings (passes task id if available).

### `presentation/ui/screens/DashboardScreen.kt`

- `DashboardRoute` collects UI state from `DashboardViewModel`, focuses on error handling via snackbar, and reads focus alert preference from `FocusSensorSettingsRepository`.
- `DashboardScreen` renders the top app bar (theme toggle, settings icon), floating action button, urgent tasks section, reward bar, and scrolling list of tasks. It also displays the `FocusSensorSettingsDialog` when requested.
- `UrgentTasksSection` and `TaskCard` components show task details, allow influence on alarms, completion, and navigation to edit the selected task.

### `presentation/ui/screens/AddTaskScreen.kt` & `AddTaskRoute` (renamed in navigation)

- `AddTaskRoute` wires `AddTaskViewModel`, handles UI events (success vs error), and exposes `onAlarmSettingsClick`.
- `AddTaskScreen` renders the form with text input, subject selector, date/time pickers, alarm configuration link, and action buttons; it behaves differently if editing an existing task.

### `presentation/ui/screens/AlarmSettingsScreen.kt`

- Provides UI for alarm scheduling (enable toggles, repeat selectors). Not touched recently but integral to alarm workflow.

### `presentation/ui/screens/SubjectsScreen.kt`

- Manages subject CRUD (list, dialogs for add/edit/delete) via `SubjectsViewModel` and shared components (subject items, color pickers, confirmations).

### `presentation/ui/components/TaskCard.kt`

- Displays task metadata (subject badge, due date, alarm summary, completion button).
- Exposes callbacks for completing tasks, opening alarm settings, and now editing via `onTaskClick` with `Modifier.clickable`.

### `presentation/ui/components/FocusSensorBanner.kt` & `FocusSensorSettingsDialog.kt`

- Banner reacts to current `FocusSensorState`, displaying dark/proximity alerts. It now auto-dismisses after 5 seconds using `LaunchedEffect` and coroutine delay while respecting focus toggle state.
- Dialog exposes a switch linked to `FocusSensorSettingsRepository`, letting users enable/disable focus alerts and providing contextual text.

### `sensors/FocusSensorManager.kt`

- Bridges Android sensors with Compose UI: registers `SensorEventListener` for light and proximity sensors, calculates alert conditions (`isDark`, `isUserPresent`), and posts notifications via `NotificationHelper`.
- Added state to respect `setAlertsEnabled` toggle, pausing sensors and resetting state when the user turns focus mode off.

### `notifications/` & `data/` packages

- `NotificationHelper` defines channels and helper functions for reminder notifications.
- `AlarmScheduler` + `AlarmManagerWrapper` send pending intents for scheduled alarms.
- Repositories under `data.repository` store sample tasks/subjects and provide in-memory access for the view models; notification/reward repos persist to `SharedPreferences`.

### `settings/FocusSensorSettingsRepository.kt`

- Exposes a `DataStore` preference (`focusAlertsEnabled`) that the dashboard reads/writes whenever the user toggles the focus alert switch.

### Resources (`res/values/strings.xml`, drawables, themes)

- Strings support Spanish translations for tasks, focus alerts, rewards, notifications, and settings.
- Themes and colors define `UniTaskTheme` (Material3) supporting light/dark modes toggled from the dashboard app bar.

## Usage flow summary

1. When the app starts, `MainActivity` sets up notifications, sensors, and UI theme.
2. The dashboard loads tasks via `DashboardViewModel`, surfaces urgent tasks, and renders the focus banner/dialog controls.
3. Users can toggle theme, manage subjects, add/edit tasks, configure alarms, or enable/disable focus alerts.
4. Focus alerts listen to ambient light and proximity; when triggered, they display a timed banner, send a notification, and can be disabled entirely via settings (persisted with DataStore).
5. Navigation allows task editing by clicking on task cards (the `onTaskClick` callback navigates to `AddTask` with an optional `taskId`).

This architecture keeps UI, business logic, and system integrations decoupled while leveraging Compose for responsive layouts and flows. With manual dependency injection via `AppModule`, all view models share consistent repositories and use cases. Let me know if you’d like generated diagrams or more granular explanations for any module.
