package bassamalim.tether.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import bassamalim.tether.R

@OptIn(ExperimentalTextApi::class)
private fun jakarta(weight: Int) = Font(
    resId = R.font.plus_jakarta_sans,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight))
)

/** Plus Jakarta Sans, bundled as a variable font and instanced at the weights the system uses. */
val PlusJakartaSans = FontFamily(
    jakarta(400),
    jakarta(500),
    jakarta(600),
    jakarta(700),
    jakarta(800)
)

/**
 * The design system's text styles mapped onto the Material 3 slots the components read.
 * Named styles that have no sensible Material slot live in [TetherType].
 */
val Typography = Typography(
    // display-lg: the Tether wordmark; big empty-state numbers.
    displaySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight(800),
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).em
    ),
    // display-md: screen titles, e.g. "People", "Reminders".
    headlineMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight(800),
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.01).em
    ),
    // title: contact names in a list; card and section headers.
    titleMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight(700),
        fontSize = 17.sp,
        lineHeight = 22.sp
    ),
    // body: notes, contact details, descriptions.
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight(500),
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    // body-secondary: the second line under a title; helper text under a field.
    bodySmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight(500),
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // button: the label inside a primary or secondary button.
    labelLarge = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight(700),
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    // chip: filter chips, segmented controls, selectable pills.
    labelMedium = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight(600),
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    // label: uppercase eyebrows and relationship chips, e.g. "CLOSE FRIEND".
    labelSmall = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight(700),
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.04.em
    )
)

/** Design-system styles with no Material slot of their own. */
object TetherType {

    /** caption: small captions and inline hints. */
    val Caption = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight(600),
        fontSize = 12.sp,
        lineHeight = 16.sp
    )

    /** timestamp: right-aligned metadata in a list row, e.g. "3d ago". */
    val Timestamp = TextStyle(
        fontFamily = PlusJakartaSans,
        fontWeight = FontWeight(600),
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
}
