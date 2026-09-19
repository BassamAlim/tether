package bassamalim.tether.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bassamalim.tether.core.ui.theme.Spacing

/**
 * Naming a relationship: the field and the vocabulary, together. The same control asks wherever
 * one is named — a person's own, a whole batch of connections, or one link in it — so the usual
 * answers are always a tap away and typing is never the only way in.
 *
 * [options] comes from the repository rather than a constant, because anything typed here joins
 * the list. Tapping the one already chosen clears it, the way every other chip in the app does.
 */
@Composable
fun RelationshipField(
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    onOptionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Family, from school, neighbours…"
) {
    Column(modifier) {
        LabeledTextField(
            label = label,
            value = value,
            onValueChange = onValueChange,
            placeholder = placeholder
        )

        FlowRow(
            modifier = Modifier
                .padding(top = 10.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            options.forEach { option ->
                FilterPill(
                    label = option,
                    selected = value.equals(option, ignoreCase = true),
                    onClick = { onOptionClick(option) }
                )
            }
        }
    }
}
