package bassamalim.tether.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Tether is dark-only by design: no light scheme, and no dynamic color — the lime accent is the
 * app's single recognizable signal and Material You would take it away.
 */
private val TetherColorScheme = darkColorScheme(
    primary = Action,
    onPrimary = AccentInk,
    primaryContainer = AccentWash,
    onPrimaryContainer = Accent,
    secondary = InkMuted,
    onSecondary = Surface0,
    secondaryContainer = Surface200,
    onSecondaryContainer = Ink,
    tertiary = Accent,
    onTertiary = AccentInk,
    background = Surface0,
    onBackground = Ink,
    surface = Surface0,
    onSurface = Ink,
    surfaceVariant = Surface200,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = Surface0,
    surfaceContainerLow = Surface100,
    surfaceContainer = Surface100,
    surfaceContainerHigh = Surface200,
    surfaceContainerHighest = Surface200,
    inverseSurface = Ink,
    inverseOnSurface = Surface0,
    outline = Surface300,
    outlineVariant = Surface300,
    error = Danger,
    onError = Surface0,
    errorContainer = DangerWash,
    onErrorContainer = Danger,
    scrim = Overlay
)

@Composable
fun TetherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TetherColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
