package bassamalim.tether.features.addPerson

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.enums.CadencePreset
import bassamalim.tether.core.ui.components.FilterPill
import bassamalim.tether.core.ui.components.LabeledTextField
import bassamalim.tether.core.ui.components.RelationshipField
import bassamalim.tether.core.ui.components.SectionLabel
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Pill
import bassamalim.tether.core.ui.theme.Sizes
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface100
import bassamalim.tether.core.ui.theme.Surface200
import bassamalim.tether.core.ui.theme.Surface300
import bassamalim.tether.core.ui.theme.TetherType
import bassamalim.tether.R
import androidx.compose.ui.res.painterResource

@Composable
fun AddPersonScreen(viewModel: AddPersonViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AddPersonScreen(
        state = state,
        onFromContactsClick = viewModel::onFromContactsClick,
        onNameChange = viewModel::onNameChange,
        onTagChange = viewModel::onTagChange,
        onTagSelect = viewModel::onTagSelect,
        onCadenceSelect = viewModel::onCadenceSelect,
        onHowYouMetChange = viewModel::onHowYouMetChange,
        onWorkplaceChange = viewModel::onWorkplaceChange,
        onJobTitleChange = viewModel::onJobTitleChange,
        onCancel = viewModel::onCancel,
        onSave = viewModel::onSave
    )
}

@Composable
private fun AddPersonScreen(
    state: AddPersonUiState,
    onFromContactsClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onTagChange: (String) -> Unit,
    onTagSelect: (String) -> Unit,
    onCadenceSelect: (CadencePreset) -> Unit,
    onHowYouMetChange: (String) -> Unit,
    onWorkplaceChange: (String) -> Unit,
    onJobTitleChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.xxl)
    ) {
        TopBar(canSave = state.canSave, onCancel = onCancel, onSave = onSave)

        // Most people you want to keep up with are already in your phone, so the shortcut comes
        // before the form: typing out a name and number the address book already has is the
        // fallback, not the first offer.
        FromContactsButton(
            onClick = onFromContactsClick,
            modifier = Modifier.padding(top = Spacing.sm, start = Spacing.screen, end = Spacing.screen)
        )

        Text(
            text = "or write down someone your contacts don't have",
            style = TetherType.Caption,
            color = InkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 14.dp, start = Spacing.screen, end = Spacing.screen)
                .fillMaxWidth()
        )

        PhotoButton(modifier = Modifier.align(Alignment.CenterHorizontally))

        LabeledTextField(
            label = "Name",
            value = state.name,
            onValueChange = onNameChange,
            placeholder = "Their name",
            modifier = Modifier.padding(top = 26.dp, start = Spacing.screen, end = Spacing.screen)
        )

        RelationshipField(
            label = "Relationship",
            value = state.tag,
            options = state.relationshipOptions,
            onValueChange = onTagChange,
            onOptionClick = onTagSelect,
            modifier = Modifier.padding(top = 22.dp, start = Spacing.screen, end = Spacing.screen)
        )

        Column(Modifier.padding(top = 22.dp, start = Spacing.screen, end = Spacing.screen)) {
            SectionLabel(text = "Reach out every")

            ChipRow(modifier = Modifier.padding(top = 10.dp)) {
                CadencePreset.entries.forEach { preset ->
                    FilterPill(
                        label = preset.label,
                        selected = preset == state.cadence,
                        onClick = { onCadenceSelect(preset) }
                    )
                }
            }

            Text(
                text = "This is the only field that makes Tether a CRM: it's what the Catch up " +
                        "screen counts from.",
                style = TetherType.Caption,
                color = InkFaint,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        LabeledTextField(
            label = "How you met",
            value = state.howYouMet,
            onValueChange = onHowYouMetChange,
            placeholder = "Where, when, through whom",
            modifier = Modifier.padding(top = 22.dp, start = Spacing.screen, end = Spacing.screen)
        )

        LabeledTextField(
            label = "Works at",
            value = state.workplace,
            onValueChange = onWorkplaceChange,
            placeholder = "Company, school, hospital",
            modifier = Modifier.padding(top = 22.dp, start = Spacing.screen, end = Spacing.screen)
        )

        LabeledTextField(
            label = "Role",
            value = state.jobTitle,
            onValueChange = onJobTitleChange,
            placeholder = "What they do there",
            modifier = Modifier.padding(top = 22.dp, start = Spacing.screen, end = Spacing.screen)
        )
    }
}

@Composable
private fun TopBar(canSave: Boolean, onCancel: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.screen, start = Spacing.sm, end = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Cancel",
            style = MaterialTheme.typography.labelMedium,
            color = InkMuted,
            modifier = Modifier
                .clickable(onClick = onCancel)
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
        )

        Text(
            text = "New person",
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "Save",
            style = MaterialTheme.typography.labelLarge,
            // Dimmed rather than hidden: you can see what you're one field away from.
            color = if (canSave) Accent else InkFaint,
            modifier = Modifier
                .clickable(enabled = canSave, onClick = onSave)
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
        )
    }
}

@Composable
private fun FromContactsButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(color = Surface200)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_people),
            contentDescription = null,
            tint = InkMuted,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = "Pick from contacts",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f)
        )

        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = InkFaint,
            modifier = Modifier.size(18.dp)
        )
    }
}

// TODO: photos aren't stored yet; wire this to a picker when Person gets a photo.
@Composable
private fun PhotoButton(modifier: Modifier = Modifier) {
    // The board draws this outline dashed, which Compose has no Border for.
    val dashes = remember { PathEffect.dashPathEffect(floatArrayOf(14f, 10f)) }

    Box(
        modifier = modifier
            .padding(top = Spacing.md)
            .size(80.dp)
            .background(color = Surface100, shape = Pill)
            .drawBehind {
                val stroke = Sizes.border.toPx()

                drawCircle(
                    color = Surface300,
                    radius = (size.minDimension - stroke) / 2,
                    style = Stroke(width = stroke, pathEffect = dashes)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = "Add a photo",
            tint = InkFaint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun ChipRow(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        content()
    }
}
