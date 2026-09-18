package bassamalim.tether.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentWash
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Pill
import bassamalim.tether.core.ui.theme.Sizes
import bassamalim.tether.core.ui.theme.Surface200

/** A monogram. Photos are designed but not stored yet, so every avatar is initials for now. */
@Composable
fun Avatar(
    initials: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false
) {
    Box(
        modifier = modifier
            .size(Sizes.avatar)
            .background(color = if (highlighted) AccentWash else Surface200, shape = Pill),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight(700),
            color = if (highlighted) Accent else InkMuted
        )
    }
}
