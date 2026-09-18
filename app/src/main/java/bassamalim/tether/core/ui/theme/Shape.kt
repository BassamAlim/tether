package bassamalim.tether.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** radius tokens. [Shapes.medium] is the workhorse: inputs, buttons, list rows, small cards. */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // radius-sm: small nested elements inside a row
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),      // radius-md
    large = RoundedCornerShape(20.dp),       // radius-lg: cards and bottom sheets
    extraLarge = RoundedCornerShape(28.dp)   // radius-xl: large sheets and modals
)

/** radius-full — avatars and pill-shaped chips. */
val Pill = RoundedCornerShape(percent = 50)
