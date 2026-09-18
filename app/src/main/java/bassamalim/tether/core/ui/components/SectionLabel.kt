package bassamalim.tether.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import bassamalim.tether.core.ui.theme.InkFaint

/** The uppercase eyebrow over a group of rows, e.g. "SLIPPING". */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = InkFaint,
        modifier = modifier
    )
}
