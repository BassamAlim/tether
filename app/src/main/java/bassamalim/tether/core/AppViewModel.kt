package bassamalim.tether.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.data.repositories.PreferencesRepository
import bassamalim.tether.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Decides which door the app opens on: the lock if it's enabled, the one-screen explanation if
 * there's nobody in here yet, otherwise People.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    peopleRepository: PeopleRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    val startDestination: StateFlow<Screen?> = combine(
        preferencesRepository.observeLockEnabled(),
        peopleRepository.observeCount()
    ) { lockEnabled, peopleCount ->
        when {
            lockEnabled -> Screen.Lock
            peopleCount == 0 -> Screen.FirstRun
            else -> Screen.Main
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )
}
