package bassamalim.tether.features.firstRun

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.Action
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.InkMuted
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.Surface200
import bassamalim.tether.core.ui.theme.TetherType
import bassamalim.tether.R
import androidx.compose.ui.res.painterResource

/**
 * The only screen that has to explain anything: one line on what Tether does, then the two ways
 * in.
 */
@Composable
fun FirstRunScreen(viewModel: FirstRunViewModel = hiltViewModel()) {
    FirstRunScreen(
        onAddPersonClick = viewModel::onAddPersonClick,
        onImportClick = viewModel::onImportClick
    )
}

@Composable
private fun FirstRunScreen(onAddPersonClick: () -> Unit, onImportClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
            .padding(horizontal = Spacing.screen)
            .padding(bottom = Spacing.xxxl)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_tether_mark),
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(76.dp)
            )

            Text(
                text = "Nobody here yet",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = Spacing.xxl)
            )

            Text(
                text = "Add the people you don't want to drift away from, and say how often " +
                        "you'd like to be in touch. Tether keeps count and tells you who's " +
                        "slipping.",
                style = MaterialTheme.typography.bodyMedium,
                color = InkMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = Spacing.md)
            )
        }

        PrimaryButton(label = "Add your first person", onClick = onAddPersonClick)

        SecondaryButton(
            label = "Pick from contacts",
            onClick = onImportClick,
            modifier = Modifier.padding(top = 10.dp)
        )

        Text(
            text = "Contacts are read on this phone and never leave it.",
            style = TetherType.Caption,
            color = InkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 14.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(label = label, onClick = onClick, background = Action, content = AccentInk, modifier = modifier)
}

@Composable
private fun SecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(label = label, onClick = onClick, background = Surface200, content = Ink, modifier = modifier)
}

@Composable
private fun Button(
    label: String,
    onClick: () -> Unit,
    background: Color,
    content: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(color = background, shape = MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = content)
    }
}
