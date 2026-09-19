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
import androidx.compose.ui.draw.clip
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

/**
 * A read-only badge for a fact on a row, such as a cadence beside a relationship chip. Same
 * shape as [TagChip] but sentence case, because it's a frequency rather than a category.
 */
@Composable
fun InfoChip(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        // ink-muted, matching TagChip. The design had this as ink-faint, but that was plain text
        // set deliberately quieter than the chip beside it; as a badge it just looked wrong.
        color = InkMuted,
        modifier = modifier
            .clip(Pill)
            .background(color = Surface200)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

/** A filter pill. Selected is the accent wash, the one place a list may spend the accent. */
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
            .clip(Pill)
            .background(color = if (selected) AccentWash else Surface200)
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
