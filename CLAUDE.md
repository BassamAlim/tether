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
- **Icons are the design's own stroke glyphs**, transcribed from the boards into
  `res/drawable/ic_*.xml` and used with `painterResource`. There is no `material-icons`
  dependency — don't add one; port the glyph from the board instead.
- `TetherTheme` wraps everything in a `Surface(color = Surface0, contentColor = Ink)`. Without
  that, `Text` with no explicit colour falls back to Compose's default black, which is invisible
  here — it only looked fine on screens that happened to sit inside a `Scaffold`.
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
- **The nudge is weekly**, not daily — a daily nudge becomes wallpaper within a fortnight. It
  names people rather than counting them (`core/nudge/NudgeCopy.kt`), offers "Tomorrow" so
  dismissing isn't the only way out, and opens the app on Catch up — through the lock if one is
  set, never around it. It is scheduled as self-rescheduling one-time work rather than periodic
  work, because a periodic job's flex window drifts off the hour the user chose.
- **Lock** is `BiometricPrompt` on cold start and after 60s in the background, with device
  credential as the fallback. It stops someone picking up an unlocked phone; it is not encryption
  at rest, and the UI should not imply otherwise. The rule lives in `core/lock/LockManager`
  (monotonic clock, so changing the device time can't skip the grace); `Activity` reports
  `onStart`/`onStop` to it and is a `FragmentActivity` because that's what `BiometricPrompt`
  attaches to. A phone with no screen lock at all opens straight through — you can never lock
  yourself out.

## Current state

Every designed screen is built: **People**, **New person**, **Catch up** (one-tap log and undo
bar), **Person detail**, **Log a catch-up**, **Search**, **Settings**, **First run**, **From
contacts** and **Locked**.

Missing: per-person reminders (the bell on Person detail is deliberately disabled), and photos
(`Person` has no photo column, so the New person screen's photo button is inert). Backup export
writes JSON; there is no import of that file yet.

The weekly nudge is built: `App` supplies Hilt's `HiltWorkerFactory` to WorkManager (so the
manifest removes `WorkManagerInitializer`), `NudgeScheduler.sync()` runs on every launch and
whenever the nudge settings change, and `NudgeActionReceiver` handles "Tomorrow".

The launcher icon comes from the design's own app-icon asset: a 108x108 tile with the mark at
`translate(18 18) scale(0.75)` — 44% of the tile. Android shows only the inner 72dp of the
108dp canvas and scales it up, so `ic_launcher_foreground` uses **0.5** off the 96x96 source to
land at that same 44% on screen; copying the asset's 0.75 renders the mark half again too
large. Background is surface-0 full bleed (the launcher mask supplies the corners), plus a
monochrome layer for themed icons. The same mark is the notification's small icon.

Three places the implementation reads differently from the boards, all deliberate: the log sheet
is a `ModalBottomSheet` on its own nav destination, so its scrim covers the app background
rather than the screen you came from (no M3 `bottomSheet` destination exists to fix this); the
contacts picker shows real phone numbers where the mock masks them; and People's search field is
a button that opens Search rather than filtering the list inline, so there is one search in the
app rather than two. People's filter chips still filter in place.
