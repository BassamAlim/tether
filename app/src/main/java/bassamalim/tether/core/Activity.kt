package bassamalim.tether.core

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.lock.LockManager
import bassamalim.tether.core.nav.Navigation
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import bassamalim.tether.core.nudge.Nudges
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.TetherTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** A [FragmentActivity] because that's what BiometricPrompt attaches to. */
@AndroidEntryPoint
class Activity : FragmentActivity() {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var lockManager: LockManager

    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // The app is dark-only, so the system bars are told so rather than asked.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )

        super.onCreate(savedInstanceState)

        setContent {
            TetherTheme {
                val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()

                // Nothing about your people is on screen before the start destination is known.
                Box(Modifier.fillMaxSize().background(Surface0)) {
                    startDestination?.let {
                        Navigation(navigator = navigator, startDestination = withNudgeTarget(it))
                    }
                }
            }
        }
    }

    /**
     * A nudge opens the app on Catch up — through the lock if there is one, never around it.
     */
    private fun withNudgeTarget(destination: Screen): Screen {
        val fromNudge = intent?.getBooleanExtra(Nudges.EXTRA_OPEN_CATCH_UP, false) == true
        if (!fromNudge) return destination

        return when (destination) {
            is Screen.Lock -> destination.copy(thenCatchUp = true)
            is Screen.Main -> Screen.Main(showCatchUp = true)
            else -> destination
        }
    }

    override fun onStart() {
        super.onStart()

        if (lockManager.shouldLockOnResume(SystemClock.elapsedRealtime())) {
            lockManager.onLocked()
            navigator.navigate(Screen.Lock(resumable = true)) { launchSingleTop = true }
        }
    }

    override fun onStop() {
        super.onStop()

        lockManager.onBackgrounded(SystemClock.elapsedRealtime())
    }

}
