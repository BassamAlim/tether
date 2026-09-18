package bassamalim.tether.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.Sizes
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface200

/**
 * The undo bar. Every one-tap write offers one, because the tap is easy to make by accident and
 * the data is irreplaceable.
 */
@Composable
fun UndoSnackbar(data: SnackbarData, actionLabel: String) {
    Row(
        modifier = Modifier
            .padding(horizontal = Spacing.screen, vertical = Spacing.md)
            .fillMaxWidth()
            .background(color = Surface200, shape = MaterialTheme.shapes.medium)
            .padding(start = Spacing.lg, end = Spacing.sm, top = Spacing.sm, bottom = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            text = data.visuals.message,
            style = MaterialTheme.typography.bodySmall,
            color = Ink,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .height(Sizes.field)
                .clickable { data.performAction() }
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelSmall,
                color = Accent
            )
        }
    }
}
