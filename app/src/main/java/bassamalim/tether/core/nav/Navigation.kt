package bassamalim.tether.core.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import bassamalim.tether.core.ui.inFromBottom
import bassamalim.tether.core.ui.inFromTop
import bassamalim.tether.core.ui.outToBottom
import bassamalim.tether.core.ui.outToTop
import bassamalim.tether.features.addPerson.AddPersonScreen
import bassamalim.tether.features.connect.ConnectScreen
import bassamalim.tether.features.firstRun.FirstRunScreen
import bassamalim.tether.features.importContacts.ImportContactsScreen
import bassamalim.tether.features.lock.LockScreen
import bassamalim.tether.features.logInteraction.LogInteractionScreen
import bassamalim.tether.features.main.MainScreen
import bassamalim.tether.features.person.PersonScreen
import bassamalim.tether.features.search.SearchScreen

@Composable
fun Navigation(navigator: Navigator, startDestination: Screen = Screen.Main()) {
    val navController = rememberNavController()

    LaunchedEffect(navController) {
        navigator.events.collect { command ->
            when (command) {
                is NavCommand.To -> navController.navigate(command.destination, command.options)
                is NavCommand.Back -> navController.popBackStack()
                is NavCommand.BackTo ->
                    navController.popBackStack(command.destination, command.inclusive)
            }
        }
    }

    NavGraph(navController = navController, startDestination = startDestination)
}

@Composable
fun NavGraph(navController: NavHostController, startDestination: Screen) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = inFromBottom,
        exitTransition = outToBottom,
        popEnterTransition = inFromTop,
        popExitTransition = outToTop
    ) {
        composable<Screen.Lock> { LockScreen() }

        composable<Screen.FirstRun> { FirstRunScreen() }

        composable<Screen.Main> { MainScreen() }

        composable<Screen.Person> { PersonScreen() }

        composable<Screen.AddPerson> { AddPersonScreen() }

        composable<Screen.LogInteraction> { LogInteractionScreen() }

        composable<Screen.Connect> { ConnectScreen() }

        composable<Screen.Search> { SearchScreen() }

        composable<Screen.ImportContacts> { ImportContactsScreen() }
    }
}
