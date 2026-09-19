package bassamalim.tether.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import bassamalim.tether.R
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.Surface300

/**
 * The square tick on a row you can select: filled lime when it's on, an empty outline when it
 * isn't. The one place a list of rows may spend the accent.
 */
@Composable
fun SelectionTick(isSelected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(26.dp)
            .then(
                if (isSelected) Modifier.background(
                    color = Accent,
                    shape = MaterialTheme.shapes.extraSmall
                )
                else Modifier.border(
                    width = 2.dp,
                    color = Surface300,
                    shape = MaterialTheme.shapes.extraSmall
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                tint = AccentInk,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
