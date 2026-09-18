package bassamalim.tether.core

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import bassamalim.tether.core.nav.Navigation
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.core.ui.theme.TetherTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Activity : ComponentActivity() {

    @Inject lateinit var navigator: Navigator

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
                        Navigation(navigator = navigator, startDestination = it)
                    }
                }
            }
        }
    }

}
