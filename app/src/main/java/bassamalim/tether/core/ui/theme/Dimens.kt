package bassamalim.tether.core.ui.theme

import androidx.compose.ui.unit.dp

/** The spacing scale. Anything that isn't on it is a mistake worth noticing. */
object Spacing {
    /** Hairline gaps, e.g. an icon to its label. */
    val xxs = 2.dp
    /** Tight internal padding, e.g. inside a chip. */
    val xs = 4.dp
    /** Gap between an avatar and its text block. */
    val sm = 8.dp
    /** Gap between list rows; chip horizontal padding. */
    val md = 12.dp
    /** Standard card and row padding. */
    val lg = 16.dp
    /** Screen horizontal margin. */
    val screen = 20.dp
    /** Spacing between grouped sections on a screen. */
    val xl = 24.dp
    /** Spacing above a major section; empty-state padding. */
    val xxl = 32.dp
    /** Top-of-screen spacing; large empty-state padding. */
    val xxxl = 48.dp
}

/** Fixed sizes the design pins down. */
object Sizes {
    val avatar = 44.dp
    val fab = 56.dp
    val bottomBar = 80.dp
    val field = 44.dp
    val border = 1.dp
}
