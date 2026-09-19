package bassamalim.tether.features.lock

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.lock.LockManager
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockViewModel @Inject constructor(
    private val peopleRepository: PeopleRepository,
    private val lockManager: LockManager,
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Screen.Lock>()

    fun onUnlocked() {
        lockManager.onUnlocked()

        if (route.resumable) {
            navigator.popBackStack()
            return
        }

        viewModelScope.launch {
            val destination =
                if (peopleRepository.observeCount().first() == 0) Screen.FirstRun
                else Screen.Main(showCatchUp = route.thenCatchUp)

            navigator.navigate(destination) {
                popUpTo(Screen.Lock(resumable = false)) { inclusive = true }
            }

            // A reminder is about one person, so it lands on them — pushed after the tabs, so
            // back still goes to People rather than out of the app.
            if (route.thenPersonId != 0L) navigator.navigate(Screen.Person(route.thenPersonId))
        }
    }
}
