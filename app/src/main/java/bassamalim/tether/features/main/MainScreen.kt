package bassamalim.tether.features.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import bassamalim.tether.core.ui.theme.AccentInk
import bassamalim.tether.core.ui.theme.AccentWash
import bassamalim.tether.core.ui.theme.Action
import bassamalim.tether.core.ui.theme.Ink
import bassamalim.tether.core.ui.theme.InkFaint
import bassamalim.tether.core.ui.theme.Surface0
import bassamalim.tether.features.catchUp.CatchUpScreen
import bassamalim.tether.features.people.PeopleScreen
import bassamalim.tether.features.settings.SettingsScreen

/**
 * The tabbed shell. Tabs are local state rather than nav destinations: switching them is not a
 * place you should be able to go "back" to.
 */
@Composable
fun MainScreen(viewModel: MainViewModel = hiltViewModel()) {
    var selected by rememberSaveable { mutableStateOf(MainTab.PEOPLE) }

    Scaffold(
        containerColor = Surface0,
        bottomBar = {
            NavigationBar(containerColor = Surface0) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == selected,
                        onClick = { selected = tab },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Ink,
                            selectedTextColor = Ink,
                            unselectedIconColor = InkFaint,
                            unselectedTextColor = InkFaint,
                            indicatorColor = AccentWash
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selected == MainTab.PEOPLE) {
                FloatingActionButton(
                    onClick = viewModel::onAddPersonClick,
                    containerColor = Action,
                    contentColor = AccentInk,
                    shape = MaterialTheme.shapes.large
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = "Add person")
                }
            }
        }
    ) { innerPadding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selected) {
                MainTab.PEOPLE -> PeopleScreen()
                MainTab.CATCH_UP -> CatchUpScreen()
                MainTab.SETTINGS -> SettingsScreen()
            }
        }
    }
}

enum class MainTab(val label: String, val icon: ImageVector) {
    PEOPLE("People", Icons.Rounded.Person),
    CATCH_UP("Catch up", Icons.Rounded.Notifications),
    SETTINGS("Settings", Icons.Rounded.Settings)
}
