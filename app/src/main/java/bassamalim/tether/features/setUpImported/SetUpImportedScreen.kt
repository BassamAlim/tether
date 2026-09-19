package bassamalim.tether.features.setUpImported

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface200
import bassamalim.tether.core.ui.theme.TetherType

/**
 * The half of an import an address book can't do: who these people are to you, and how often
 * you mean to be in touch. One person per screen, in the order People lists them, because the
 * answers are per person and a grid of pickers would get answered once and rubber-stamped.
 *
 * They are already saved by the time this opens, so leaving early costs nothing but the
 * cadence — which is the one thing that has to be a decision rather than a default.
 */
@Composable
fun SetUpImportedScreen(viewModel: SetUpImportedViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SetUpImportedScreen(
        state = state,
        onTagChange = viewModel::onTagChange,
        onTagSelect = viewModel::onTagSelect,
        onCadenceSelect = viewModel::onCadenceSelect,
        onHowYouMetChange = viewModel::onHowYouMetChange,
        onSkip = viewModel::onSkip,
        onSave = viewModel::onSave
    )
}

@Composable
private fun SetUpImportedScreen(
    state: SetUpImportedUiState,
    onTagChange: (String) -> Unit,
    onTagSelect: (String) -> Unit,
    onCadenceSelect: (CadencePreset) -> Unit,
    onHowYouMetChange: (String) -> Unit,
    onSkip: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
            .verticalScroll(rememberScrollState())
            .padding(bottom = Spacing.xxl)
    ) {
        TopBar(
            progress = state.progress,
            actionLabel = state.actionLabel,
            canSave = state.canSave,
            onSkip = onSkip,
            onSave = onSave
        )

        if (state.isLoading) return@Column

        Identity(state = state)

        RelationshipField(
            label = "Relationship",
            value = state.tag,
            options = state.relationshipOptions,
            onValueChange = onTagChange,
            onOptionClick = onTagSelect,
            modifier = Modifier.padding(
                top = Spacing.xl,
                start = Spacing.screen,
                end = Spacing.screen
            )
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
                text = "Leave it at Never and they stay in People without ever turning up on " +
                        "Catch up.",
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

        Text(
            text = "Skip and they're still saved — you can set this from their page any time.",
            style = TetherType.Caption,
            color = InkFaint,
            modifier = Modifier.padding(top = Spacing.xl, start = Spacing.screen, end = Spacing.screen)
        )
    }
}

@Composable
private fun TopBar(
    progress: String,
    actionLabel: String,
    canSave: Boolean,
    onSkip: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.screen, start = Spacing.sm, end = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Skip sits where Cancel does on New person, and means the same thing here: move on
        // without answering. There's nothing to cancel — the person is already saved.
        Text(
            text = "Skip",
            style = MaterialTheme.typography.labelMedium,
            color = InkMuted,
            modifier = Modifier
                .clickable(onClick = onSkip)
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
        )

        Text(
            text = progress,
            style = MaterialTheme.typography.labelLarge,
            color = InkMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelLarge,
            color = if (canSave) Accent else InkFaint,
            modifier = Modifier
                .clickable(enabled = canSave, onClick = onSave)
                .padding(horizontal = Spacing.md, vertical = Spacing.md)
        )
    }
}

/** Name and number are read-only: they came from the address book, unedited by design. */
@Composable
private fun Identity(state: SetUpImportedUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spacing.lg, start = Spacing.screen, end = Spacing.screen),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(color = Surface200, shape = Pill),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.initials,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight(700),
                color = InkMuted
            )
        }

        Text(
            text = state.name,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Spacing.md)
        )

        if (state.phone.isNotEmpty()) {
            Text(
                text = state.phone,
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint,
                modifier = Modifier.padding(top = Spacing.xs)
            )
        }
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
