package bassamalim.tether.features.firstRun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FirstRunViewModel @Inject constructor(
    peopleRepository: PeopleRepository,
    private val navigator: Navigator
) : ViewModel() {

    init {
        // The moment there's someone to show, this screen has nothing left to say.
        peopleRepository.observeCount()
            .filter { it > 0 }
            .onEach {
                navigator.navigate(Screen.Main()) {
                    popUpTo(Screen.FirstRun) { inclusive = true }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onAddPersonClick() = navigator.navigate(Screen.AddPerson)

    fun onImportClick() = navigator.navigate(Screen.ImportContacts)
}
