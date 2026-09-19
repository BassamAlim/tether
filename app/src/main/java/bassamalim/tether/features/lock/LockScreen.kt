package bassamalim.tether.features.lock

import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import bassamalim.tether.core.ui.theme.Accent
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.Action
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.Spacing
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.TetherType
import bassamalim.tether.R
import androidx.compose.ui.res.painterResource

/**
 * The app's own locked surface. Android draws the fingerprint sheet itself, so all this screen
 * owns is the branded empty state behind it and the Unlock button you get if you dismiss the
 * prompt. Nothing about your people is on screen before you're through it, not even a count.
 */
@Composable
fun LockScreen(viewModel: LockViewModel = hiltViewModel()) {
    val activity = LocalContext.current.findFragmentActivity()

    val unlock = {
        if (activity == null || !activity.canAuthenticate()) {
            // A phone with no screen lock at all must still open: you can never lock yourself out.
            viewModel.onUnlocked()
        }
        else {
            activity.promptForUnlock(onSuccess = viewModel::onUnlocked)
        }
    }

    // Backing out of the lock would be a way past it.
    BackHandler(enabled = true) {}

    LaunchedEffect(Unit) { unlock() }

    LockScreen(onUnlockClick = unlock)
}

@Composable
private fun LockScreen(onUnlockClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface0)
            .padding(horizontal = Spacing.screen)
            .padding(bottom = Spacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_tether_mark),
                contentDescription = null,
                tint = Accent,
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "Tether",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(top = Spacing.xl)
            )

            Text(
                text = "Locked",
                style = MaterialTheme.typography.bodySmall,
                color = InkFaint,
                modifier = Modifier.padding(top = Spacing.sm)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(color = Action)
                .clickable(onClick = onUnlockClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_fingerprint),
                contentDescription = null,
                tint = AccentInk,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = "Unlock",
                style = MaterialTheme.typography.labelLarge,
                color = AccentInk
            )
        }

        Text(
            text = "Uses your fingerprint, or your screen lock if you'd rather.",
            style = TetherType.Caption,
            color = InkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 14.dp)
        )
    }
}

private const val AUTHENTICATORS = BIOMETRIC_WEAK or DEVICE_CREDENTIAL

private fun FragmentActivity.canAuthenticate() =
    BiometricManager.from(this).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

private fun FragmentActivity.promptForUnlock(onSuccess: () -> Unit) {
    val prompt = BiometricPrompt(
        this,
        ContextCompat.getMainExecutor(this),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        }
    )

    prompt.authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Tether")
            // Device credential is the fallback, so no negative button is allowed here.
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()
    )
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
