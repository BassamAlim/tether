# CLAUDE.md

Guidance for Claude Code working in this repository.

## What Tether is

A personal CRM for Android: the people you care about, how often you mean to reach out, and
whether you're slipping. It is offline-first by design — no account, no server, no sync. The
database is the archive, which is why Settings has to be honest about backups. The one network
request it makes is naming a pasted Google Maps link (see the "Where" rule); nothing about a
person or a catch-up ever leaves the phone.

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
- **History is yours to correct.** Tapping an entry on Person detail reopens the log sheet on
  that row (`Screen.LogInteraction(personId, interactionId)`, 0 meaning "a new one") and saving
  rewrites it in place, id and all, so a fixed typo or a wrong date doesn't become a second
  catch-up — and moving the date moves the clock with it. Deleting one is offered in two
  visible places, never as a gesture: the three-dots menu on the entry, and a Delete button at
  the bottom of the sheet when it's editing. Either way it goes through with no dialog and an
  undo bar instead. `InteractionsRepository.delete` announces the removed row on a `deletions`
  flow, which is how Person detail can raise the bar for a delete tapped on the sheet that was
  covering it, and undo re-inserts that row with its own id rather than logging a new one.
  **Every destructive write offers undo** — the one-tap log on Catch up, and this.
- **Import is a picker, not a sync.** `READ_CONTACTS` is requested when the button is tapped,
  people are copied once, and Tether never writes back to the address book. It is reached from
  **New person**, not from Settings: copying someone in is a way of adding a person, not a
  preference, and since most people worth keeping up with are already in your phone, the picker
  sits above the form and typing a name out by hand is the fallback. An address book knows how
  to reach someone, not what they are to you, so the import hands straight over to the set-up
  walk (`features/setUpImported`, `Screen.SetUpImported`): one imported person per screen, in
  People's order, asking for relationship, cadence, how you met, and where they work (already
  filled in when the address book said). It is a walk rather than one
  long list because a list gets answered once and rubber-stamped. The rows are already saved
  when it opens, so it never creates anything and Skip — or backing out — costs nothing; anyone
  you skip stays exactly as the import left them, untracked, to be opted in later from their
  own screen.
- **A restore merges, and never overwrites.** Importing a backup is not "become this file": the
  archive on the phone is the live one, and the file may be older, may be from a phone used
  since, or may be the one you imported ten minutes ago. So people are matched by name, a
  matched person keeps their own relationship, cadence and number unless the field is empty
  here, and details and catch-ups are added only where the file holds more copies of an entry
  than the database does (`missingCopies`, a multiset difference — two coffees on one day are
  two). Importing the same file twice therefore changes nothing the second time, and an import
  interrupted halfway can simply be run again. Nothing is ever deleted, which is the only
  reading of it that doesn't owe the user an undo bar. The cost is that two people who share a
  name can't be told apart: same-name rows are claimed one at a time in file order. Ids are
  deliberately not in the file, so names are all a restore has.

  The relationship vocabulary comes back **only as the file recorded it** (`relationshipTypes`),
  never harvested from the tags and labels the imported rows happen to carry. The list is a
  decision, not a summary of the data: a word can be deleted from it while the connections that
  already use it keep using it, and sweeping the incoming labels would quietly put those words
  back in the pills. A file older than format 4 has no list, so it changes the vocabulary not at
  all.
- **The message button opens WhatsApp**, not SMS: it hands WhatsApp the raw number first (so
  WhatsApp matches the contact itself), falls back to `wa.me` with the number in international
  form, and only then to SMS. `com.whatsapp` is declared in the manifest's `<queries>` or
  `setPackage` could never resolve on Android 11+.
- **The cadence always starts at "Never", and there is no setting for it.** New people arrive
  untracked and are opted in on New person itself, where the cadence picker sits next to the
  name; imported people are asked the same question one at a time, on the set-up walk that
  follows the picker, and stay untracked if they aren't answered for. There is deliberately no
  "cadence for new people" preference: a cadence is a decision
  about one person, and a stored default either nags you about everyone you wrote down or hides
  the decision on a screen you visit once.
- **One relationship vocabulary, and it's the user's.** How you know a person and how two
  people know each other are the same kind of fact, so they draw on one list rather than two:
  `RelationshipTypes.DEFAULTS` is where it starts (11 entries: the union of the old per-person
  tag enum and the old connection suggestions, less "Worked together" and "Studied together" —
  with one vocabulary for both ends, "Work" and "School"/"University" already say those from
  either side), and anything typed into either joins it
  (`relationship_types` table, `RelationshipTypesRepository`). The table holds only what was
  added, so the built-in list can grow in a later version without fighting stored rows. There is
  no `RelationshipTag` enum any more — `Person.tag` is the label itself, nullable, one per
  person, set on New person or by tapping the chip on Person detail. People's filter chips are
  drawn from the people, not fixed (`features/people/PeopleFilters.kt`): All, then Untracked
  when anyone's cadence is Never (the to-do list an import leaves behind), then every
  relationship in use, most people first, matched case-insensitively and each with its count.
  A label nobody carries has no chip, so none comes back empty. There is no Slipping chip on
  purpose: the list already leads with that section, and Catch up is a whole tab of it. This
  departs from the board's fixed All / Slipping / Close / Work. Row chips uppercase the
  label, and ellipsize, since the list is open-ended and "STUDIED TOGETHER" is long.
- **Connections are one undirected edge with one shared label.** Who knows who is a row in
  `connections`, stored with the smaller id in `personAId` and a unique index over the pair, so
  connecting A to B and B to A is the same row. The label is free text written to read the same
  from either end ("Siblings", "Work together at Careem") rather than one word per direction —
  a single sentence to keep true instead of two that drift apart. Removing a connection forgets
  only the link; deleting a person cascades theirs away. Connections link people already in
  Tether; there is no such thing as a connection to a name that isn't a person.
- **Connections are added in batches.** Connect ticks any number of people, then writes one
  shared label over the lot — that's how they come to mind ("these six are from university") —
  and any one of them can be tapped for its own line instead. The shared line is inherited, not
  copied: rewriting it moves everyone who hasn't been singled out, and "Use shared" hands
  someone back. The batch lands in one transaction, so a person is never left half-connected.
- **Person details are free-form label/value rows** (`PersonDetail`: "Met", "Kids"), not fixed
  columns — what's worth remembering differs per person. Interaction notes are
  separate, and search covers names, work, details, notes and where a catch-up happened.
- **Where they work is the one detail with its own columns** — `Person.workplace` and
  `Person.jobTitle`, each null when unsaid, read as one line by `workLabel` in
  `core/utils/Labels.kt` ("Designer at Careem", "Works at Careem", or just the role). Columns
  rather than a detail row because it is asked for by name in three places — New person, the
  set-up walk, and the address book's Organization entry, which is the one fact besides a number
  that contacts can actually answer — and because it is how you place someone, so it sits under
  the name on Person detail rather than in the Details card. Tapping that line opens a dialog
  with both fields (empty both and the line goes away: people leave jobs). It is the only
  person field besides name, relationship and cadence that can be changed after creation.
- **A catch-up records where it happened and who reached out** — `Interaction.location`, free
  text and blank when unasked, and `Interaction.initiatedBy` (`Initiator`: ME / THEM, null when
  unasked or when neither did — you ran into each other). The log sheet offers "Where" as a
  field and "Who reached out" as two pills that tap off again; Person detail's history joins
  them into one line under the title ("They reached out · Blue Tokai"). Both are optional: a
  catch-up with neither still resets the clock, and the one-tap log on Catch up stays one tap,
  so it records neither. "Where" may be a pasted Google Maps link, alone or after a name; it
  stays one free-text column and is read for showing by `core/utils/Places.kt` (`parsePlace`):
  words typed beside the link win, then the name inside the link (`/maps/place/…`, `?q=`), then
  "Dropped pin" for bare coordinates or "Google Maps" for a short link whose name couldn't be
  had. A short link carries no name, so when one is pasted alone the log sheet asks Google where
  it redirects (`MapsLinkDataSource`, behind `PlacesRepository` — only the `Location` header is
  read, with a non-browser User-Agent, since a browser is sent an `intent://` instead) and
  writes the name into the field in front of the link, where it can still be corrected. Save
  waits for a lookup in flight; typing over the field wins over a late answer; offline, it
  simply stays "Google Maps". Reopening an old entry with a bare link looks it up then. History
  shows the label underlined and opens the link; the sheet says under the field how it will
  read, and a multi-line Maps share pasted in keeps only its first line and the link.
- **The nudge is weekly**, not daily — a daily nudge becomes wallpaper within a fortnight. It
  names people rather than counting them (`core/nudge/NudgeCopy.kt`), offers "Tomorrow" so
  dismissing isn't the only way out, and opens the app on Catch up — through the lock if one is
  set, never around it. It is scheduled as self-rescheduling one-time work rather than periodic
  work, because a periodic job's flex window drifts off the hour the user chose.
- **A reminder is one moment about one person**, separate from the cadence: the cadence is the
  standing arrangement the weekly nudge speaks for, a reminder is the thing you thought of just
  now ("coffee with Maya, Saturday at seven"). The bell on Person detail opens
  `Screen.Reminder(personId)`, which asks for a catch-up type (optional, as on the log sheet)
  and a day and time, and Person detail shows what's set under a **Reminder** header. At most
  one per person — a unique index on `reminders.personId` says so — because a bell is either
  set or it isn't; setting a new one replaces it. **A reminder is spent when it arrives**: the
  worker posts it and deletes the row, so nothing quietly re-reminds you. It is stored as wall
  clock, not an instant, so seven in the evening survives a flight, and it is booked as one-time
  work named `reminder_<personId>`, re-synced whenever the app is opened
  (`ReminderScheduler.syncAll()`, from `Activity`) for the cases WorkManager can't survive on
  its own. Notifications live on their own channel, so you
  can keep the reminders you asked for and refuse the standing nudge. Tapping one opens the app
  on that person — pushed on top of the tabs, through the lock if one is set, never around it.
  Reminders are deliberately **not** in the backup: they're pending intentions, not the archive.
- **Circle is you in the middle and everyone around you** (`features/circle`, a tab between
  Catch up and Settings). Distance is the cadence, not a score: three rings that never overlap —
  in touch inside the dashed due ring (further out the more of the cadence has gone by),
  slipping outside it (a whole cadence overdue or more is the rim), untracked apart on the
  outside, dimmed, since they have no clock. It reads `DueState`, so it can't disagree with
  Catch up. Colour is the relationship: the eight most-carried relationships get a hue each, in
  legend order, and the rest share a neutral Other; someone with no relationship is a hollow
  dot. The hues (`RelationshipHues` in `Color.kt`) are the app's only categorical palette,
  validated in that order for colour-blind separation on `Surface0`, and deliberately hold no
  lime and no red — a relationship is neither an action nor an error. Colour is never the only
  cue: each dot has the person's full name written inside it (never initials — monograms don't
  identify anyone at a glance), the legend names each hue and taps to dim the rest, and tapping
  a dot opens a card naming the person. Spokes run from you to everyone; connections are faint
  lines between people. The layout (`CircleLayout.kt`) is pure and deterministic, in node
  diameters, so the same people always land in the same places.
- **Lock** is `BiometricPrompt` on cold start and after 60s in the background, with device
  credential as the fallback. It stops someone picking up an unlocked phone; it is not encryption
  at rest, and the UI should not imply otherwise. The rule lives in `core/lock/LockManager`
  (monotonic clock, so changing the device time can't skip the grace); `Activity` reports
  `onStart`/`onStop` to it and is a `FragmentActivity` because that's what `BiometricPrompt`
  attaches to. A phone with no screen lock at all opens straight through — you can never lock
  yourself out.

## Current state

Every designed screen is built, plus **Circle** (the graph tab): **People**, **New person**, **Catch up** (one-tap log and undo
bar), **Person detail**, **Log a catch-up**, **Search**, **Settings**, **First run**, **From
contacts** and **Locked**.

Adding someone starts on **New person**, which opens with "Pick from contacts" and the form
below it. Choosing contacts replaces the form rather than stacking over it (the picker is a
different route to the same place), the picker leaves the stack as the set-up walk opens (the
copying has happened; coming back to a still-ticked list would only invite importing the same
people twice), and the walk pops back to wherever the whole thing started. First run steps
aside to People from the screen rather than from its ViewModel, so that an import adding people
underneath an open picker doesn't pop the flow away mid-walk, and it renders nothing until the
count is in rather than flashing "Nobody here yet" at someone who just imported twenty people.

Connections are built: Person detail carries a **Connections** section under Details — tapping a
row walks to that person, the button on it edits or removes the link — and the **Connect** screen
(`features/connect`, `Screen.Connect`) ticks people, then labels them. Its two steps live on one
destination: Cancel/Next on the picker, Back/Save on the labels, with the system gesture wired to
the same Back so a long selection survives a swipe.

Relationship and cadence can both be changed from Person detail by tapping the chip or the
cadence line, and the name by tapping it or from "Edit name" in the three-dots menu (a dialog;
a blank name can't be saved). Renaming rewrites the one row, so history, connections and a
pending reminder follow it — but a restore matches people by name, so a backup made before a
rename brings the old name back as a second person. Where they work can be rewritten the same
way, by tapping the line under the name. Phone and details are still set-once at
creation, and New person collects no phone number at all, so the WhatsApp button only lights
up for people brought in from contacts. An edit flow for those is the obvious next gap; logged catch-ups can already be
edited by tapping them in the history and deleted from the menu on the entry or the sheet.

Per-person reminders are built: `features/reminder` behind the bell on Person detail, with
`core/reminder/` holding the channel, the scheduler, the worker and the copy — the same shape as
`core/nudge/`.

Missing: photos (`Person` has no photo column, so the New person screen's photo button is inert).

Backups go both ways: Settings' **Your data** card is Export a backup and Import a backup, one
above the other. The file is JSON at format 5 — connections as name pairs, the relationship
types you added by hand, and each person's workplace and role, so it reads without Tether. The format, the reader and the wording both
screens use live in **`core/backup/`** (`BackupFile.kt`, `BackupRestore.kt`, `BackupLabels.kt`),
not in `features/settings`, because First run imports too; `SettingsDomain.buildBackup()` still
writes the file, and writer and reader share one mapping (`Interaction.asBackup` /
`BackupInteraction.asRow`) so that what a restore compares is exactly what an export wrote.

**First run offers a restore as well**, quietly under its two buttons, and has to: Settings is
inside the tab shell, which the app only opens once somebody is in Tether, so on a reinstalled
phone First run is the only door. The confirm dialog is shared
(`core/ui/components/ImportDialog.kt`). It says what the file holds rather than only its name,
because a backup's name is a date and two of them look alike. On First run a restore that works
announces itself by filling the app — the screen steps aside to People the moment there are
people — so only a file that couldn't be used says anything out loud; Settings, which stays put,
reports the counts in a snackbar. Both pickers accept any mime type, because a file that came
back off a desktop or out of a chat app is routinely offered as `text/plain`, and an unreadable
one is caught by the reader a moment later. A file from a later format is refused rather than
half-read.

The weekly nudge is built: `App` supplies Hilt's `HiltWorkerFactory` to WorkManager (so the
manifest removes `WorkManagerInitializer`), `NudgeScheduler.sync()` runs whenever the app is
opened and whenever the nudge settings change, and `NudgeActionReceiver` handles "Tomorrow".

Both re-syncs hang off `Activity`, not `App.onCreate`, and have to: they enqueue with
`REPLACE`, and `App.onCreate` runs in *every* process start — including the one WorkManager
starts in order to run `NudgeWorker` or `ReminderWorker`. Re-booking from there cancelled the
very worker the process had woken up for, which is why notifications only ever arrived while
the app was already open.

The launcher icon comes from the design's own app-icon asset: a 108x108 tile with the mark at
`translate(18 18) scale(0.75)` — 44% of the tile. Android shows only the inner 72dp of the
108dp canvas and scales it up, so `ic_launcher_foreground` uses **0.5** off the 96x96 source to
land at that same 44% on screen; copying the asset's 0.75 renders the mark half again too
large. Background is surface-0 full bleed (the launcher mask supplies the corners), plus a
monochrome layer for themed icons. The same mark is the notification's small icon.

Circle has no board — it came after the design — so it borrows People's header, subtitle and sideways chip row, and the
selected-person card follows People's rows.

Neither Connect nor the set-up walk has a board — the design predates both — so Connect follows
New person's shape
(Cancel / title / lime action, then fields), its picker rows follow From contacts' ticked rows
(the tick itself is now shared as `core/ui/components/SelectionTick`), and the Connections rows
on Person detail follow People's. Naming a link is one control everywhere it happens —
`core/ui/components/RelationshipField`, the field plus the vocabulary — so the usual answers are
always a tap away and typing is never the only way in. It is the same control on New person, Set
up imported, Person detail's relationship and connection dialogs, and both of Connect's steps. The set-up walk borrows the same shape
(Skip / "2 of 5" / lime Next, then Person detail's monogram header over New person's pickers).

Five places the implementation reads differently from the boards, all deliberate: the log sheet
is a `ModalBottomSheet` on its own nav destination, so its scrim covers the app background
rather than the screen you came from (no M3 `bottomSheet` destination exists to fix this); the
contacts picker shows real phone numbers where the mock masks them; People's search field is
a button that opens Search rather than filtering the list inline, so there is one search in the
app rather than two; People's filter chips are the relationships in use plus Untracked, in a
row that scrolls sideways, rather than the board's fixed four (they still filter in place); and
Settings' nudge is one row rather than a switch over a "Nudge me on" row — "Weekly nudge" over
"Sundays at 10:00 AM" (or "Off"), the text opening the schedule and a switch beside it, since
both only ever described one thing. Picking a time while it's off turns it on.
