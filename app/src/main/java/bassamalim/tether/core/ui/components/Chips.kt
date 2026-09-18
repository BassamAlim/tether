package bassamalim.tether.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentWash
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Pill
import bassamalim.tether.core.ui.theme.Sizes
import bassamalim.tether.core.ui.theme.Surface200

/** The relationship label on a person's row, e.g. "CLOSE". */
@Composable
fun TagChip(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = InkMuted,
        modifier = modifier
            .background(color = Surface200, shape = Pill)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** A filter pill. Selected is the accent wash — the one place a list may spend the accent. */
@Composable
fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(Sizes.field)
            .background(color = if (selected) AccentWash else Surface200, shape = Pill)
            .clickable(onClick = onClick)
            .padding(PaddingValues(horizontal = 16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Accent else InkMuted
        )
    }
}
