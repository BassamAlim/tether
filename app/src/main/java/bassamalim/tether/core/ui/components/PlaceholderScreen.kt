package bassamalim.tether.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0

/**
 * Stands in for a screen that is designed but not built yet. Delete each use as its screen
 * lands — if this composable still exists at v1, something went wrong.
 */
@Composable
fun PlaceholderScreen(title: String, designBoard: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
            .padding(Spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)

        Text(
            text = "Designed in \"$designBoard\" — not built yet.",
            style = MaterialTheme.typography.bodySmall,
            color = InkFaint
        )
    }
}
