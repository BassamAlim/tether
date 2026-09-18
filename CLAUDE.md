# CLAUDE.md

Guidance for Claude Code working in this repository.

## What Tether is

A personal CRM for Android: the people you care about, how often you mean to reach out, and
whether you're slipping. It is offline-first by design — no account, no server, no sync. The
database is the archive, which is why Settings has to be honest about backups.

The UI is designed already. Screens and tokens live in the "Tether — app design" artifact
(`https://claude.ai/artifact/CA4CURchwDrWwR48eSftee`): boards for People, Catch up, Person
detail, Log a catch-up, New person, First run, Search, Settings, Just logged, The weekly nudge,
Locked, From contacts. **Build screens from those boards rather than inventing layouts.**

## Stack

- Kotlin, Jetpack Compose, Material 3 — AGP 9 with built-in Kotlin support (do **not** apply
  `org.jetbrains.kotlin.android`; AGP 9 rejects it)
- Hilt for DI, KSP for annotation processing (never KAPT)
- Room for storage, DataStore Preferences for settings
- Navigation Compose with **type-safe routes** (`@Serializable` destinations in `core/nav/Screen.kt`)
- WorkManager for the weekly nudge, AndroidX Biometric for the lock
- Version catalog: `gradle/libs.versions.toml`. Add dependencies there, never inline.

## Commands

```bash
./gradlew :app:assembleDebug        # build
./gradlew :app:testDebugUnitTest    # unit tests
./gradlew :app:installDebug         # install on a connected device
```

## Architecture

MVVM over a single data layer. Dependencies point one way: UI → ViewModel → Domain → Repository
→ DAO/DataStore. Nothing below the ViewModel knows about Compose; nothing above the repository
knows about Room.

```
core/
  data/dataSources/room/   entities, daos, AppDatabase, Converters, relations
  data/repositories/       the only way into storage; @Singleton + @Inject constructor
  di/                      Hilt modules for things Hilt can't construct itself
  domain/                  app-wide rules (e.g. DueState — the cadence maths)
  enums/                   shared enums
  models/                  domain models that aren't rows (e.g. TrackedPerson)
  nav/                     Screen (routes), Navigator, Navigation (the graph)
  ui/theme/                design tokens: Color, Type, Shape, Dimens, Theme
  ui/components/           shared composables
  utils/                   small pure helpers (labels, formatting)
features/<feature>/
  <Feature>Screen.kt       composables only; stateless inner composable + a hoisted wrapper
  <Feature>ViewModel.kt    @HiltViewModel; exposes one StateFlow<UiState>; no Android types
  <Feature>UiState.kt      immutable state + already-formatted UI models
  <Feature>Domain.kt       the feature's business logic, injected into the ViewModel
```

Rules that matter:

- **Repositories are `@Singleton class X @Inject constructor(...)`** — no module needed. Only
  things Hilt cannot construct (Room, DataStore, Clock, Resources) get a `@Provides`.
- **ViewModels expose exactly one `StateFlow<UiState>`**, built with `combine(...).stateIn(...)`
  and `SharingStarted.WhileSubscribed(5_000)`. Screens read it with
  `collectAsStateWithLifecycle()`.
- **No date maths in composables.** The ViewModel emits formatted labels ("7w ago", "every 2
  weeks"); helpers live in `core/utils/Labels.kt`.
- **`java.time.Clock` is injected**, never `LocalDate.now()` in logic, so "today" is fixable in
  tests.
- **Navigation goes through `Navigator`** (injected into ViewModels), which emits commands on a
  channel. Nothing outside `core/nav` touches a `NavController`. Add a destination by adding a
  `@Serializable` entry to `Screen` and a `composable<Screen.X>` to the graph.
- Room queries return `Flow`; writes are `suspend`. Bump the DB version and write a migration —
  schemas are exported to `app/schemas`.

## Design system

Tokens live in `core/ui/theme/` and are the only source of colour, type, spacing and radius.
Don't hardcode hex values or `.dp` literals that aren't on the scale in `Dimens.kt`.

- **Dark only.** No light scheme, no dynamic colour. `TetherTheme` takes no parameters.
- **One accent per screen.** Lime `#D8FF3D` is for the thing you tap (`Action`) or the thing you
  should notice (`Attention`). If a screen spends it twice, one of them is wrong.
- Type is Plus Jakarta Sans, bundled as a variable font at `res/font/plus_jakarta_sans.ttf` and
  mapped onto Material slots in `Type.kt`. Styles with no Material slot live in `TetherType`.
- Material 3 components with Tether's colours substituted — list rows are `ListItem`, filter
  pills are chips, the log sheet is `ModalBottomSheet`, the lime circle is a
  `FloatingActionButton`, the bottom bar is `NavigationBar` (80dp), undo is a `Snackbar`.

## Product rules

These are decided; don't re-litigate them in code.

- **Overdue**: someone is due when (today − last interaction) ≥ their cadence, and overdue by the
  excess. It is a single distance, never an accumulating debt. Logging resets from the
  interaction's own date, not from when Save was tapped. Someone with no interactions counts from
  the day they were added. Cadence "Never" (null `cadenceDays`) keeps a person out of Catch up
  and the slipping section, but still in People and search. Implemented in `core/domain/DueState.kt`.
- **History is append-only.** You log, you don't curate; deletion exists only to undo a one-tap
  log, and every one-tap write offers undo.
- **Import is a picker, not a sync.** `READ_CONTACTS` is requested when the button is tapped,
  people are copied once, and Tether never writes back to the address book.
- **Person details are free-form label/value rows** (`PersonDetail`: "Met", "Works at",
  "Kids"), not fixed columns — what's worth remembering differs per person. Interaction notes are
  separate, and search covers names, details and notes.
- **The nudge is weekly**, not daily — a daily nudge becomes wallpaper within a fortnight.
- **Lock** is `BiometricPrompt` on cold start and after 60s in the background, with device
  credential as the fallback. It stops someone picking up an unlocked phone; it is not encryption
  at rest, and the UI should not imply otherwise.

## Current state

Real: the data layer, DI, navigation, theme, **People** and **New person**. Every other screen is
a `PlaceholderScreen` naming its design board — replace them one at a time, following the feature
file layout above.

`observeLockEnabled()` currently defaults to **false** so the app doesn't start on the Lock
placeholder. Flip it back to `true` when the lock is built (there's a TODO on it).

Not built yet, in the order they're worth doing: Catch up (one-tap log + undo), Person detail and
the log sheet, Search, Settings, Import, First run, the weekly nudge (WorkManager), the lock
(BiometricPrompt). Photos are designed but not stored: `Person` has no photo column and the New
person screen's photo button is inert.
