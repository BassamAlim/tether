package bassamalim.tether.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The Tether palette, straight from the design system's tokens. There is one theme and it is
 * dark: no light variant, no dynamic color.
 */

/** App background: the base canvas behind every screen. */
val Surface0 = Color(0xFF0A0A0A)
/** List rows and cards at rest. */
val Surface100 = Color(0xFF141414)
/** Raised surfaces: search fields, text inputs, hovered or pressed rows. */
val Surface200 = Color(0xFF1E1E1E)
/** Default borders and dividers, including between stacked [Surface100] rows. */
val Surface300 = Color(0xFF2A2A2A)

/** Primary text and icons on any surface color. */
val Ink = Color(0xFFFAFAFA)
/** Secondary text: subtitles, section labels, secondary metadata. */
val InkMuted = Color(0xFFACACAC)
/** Tertiary text: timestamps, placeholders, disabled labels. */
val InkFaint = Color(0xFF888888)

/**
 * The one accent color in the system: primary actions, active states, and "needs attention"
 * signals. Use once per screen: it works because it's rare.
 */
val Accent = Color(0xFFD8FF3D)
/** Text and icons sitting on an accent-filled surface, e.g. the plus inside the FAB. */
val AccentInk = Color(0xFF0A0A0A)
/** Tinted background for accent chips, badges and selected rows, rgba(216,255,61,0.14). */
val AccentWash = Color(0x24D8FF3D)

/** Destructive actions and error states. */
val Danger = Color(0xFFFF6B6B)
/** Background for destructive confirmations and inline errors, rgba(255,107,107,0.14). */
val DangerWash = Color(0x24FF6B6B)

/** Scrim behind a bottom sheet or modal, rgba(0,0,0,0.6). */
val Overlay = Color(0x99000000)

/**
 * Semantic alias of [Accent] for things you TAP: primary buttons, the FAB, selected states.
 * Prefer this over [Accent] in code, so "the action color" can diverge from "the attention
 * color" later.
 */
val Action = Accent
/**
 * Semantic alias of [Accent] for things you should NOTICE: an overdue person, a due reminder,
 * a slipping badge. If a screen ever shows both and they compete, this is the one to change.
 */
val Attention = Accent
/** Focus outline on inputs and interactive elements when navigated by keyboard. */
val FocusRing = Accent

/**
 * Circle's relationship colours: the only categorical palette in the app, kept clear of the
 * accent (no yellow-green) and of [Danger] (no red), since a relationship is neither something to
 * tap nor something wrong. The order is deliberate — the first five are the furthest apart under
 * the common colour-blindnesses on [Surface0], and the last three fill the gaps left between them
 * — so assign them in order, never cycled, and fold a ninth relationship into [RelationshipOther]
 * rather than invent a hue. Every one is light enough to carry [Surface0] text inside a dot.
 * Colour is never the only cue: the legend names each one and the dots carry names.
 */
val RelationshipHues = listOf(
    Color(0xFF3987E5), // blue
    Color(0xFFD95926), // orange
    Color(0xFF199E70), // aqua
    Color(0xFF9085E9), // violet
    Color(0xFFD55181), // magenta
    Color(0xFFE6A02E), // amber
    Color(0xFF5EC4E8), // sky
    Color(0xFFB369D6)  // orchid
)
/** Every relationship past the eighth, shared. Neutral, so it doesn't read as a ninth hue. */
val RelationshipOther = InkFaint
