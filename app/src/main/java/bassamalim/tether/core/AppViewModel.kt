package bassamalim.tether.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.core.data.repositories.PeopleRepository
import bassamalim.tether.core.data.repositories.PreferencesRepository
import bassamalim.tether.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Decides which door the app opens on: the lock if it's enabled, the one-screen explanation if
 * there's nobody in here yet, otherwise People.
 *
 * Decided once, from the first values — the graph's start destination must not move under the
 * user because they added their first person.
 */
@HiltViewModel
class AppViewModel @Inject constructor(
    peopleRepository: PeopleRepository,
    preferencesRepository: PreferencesRepository
) : ViewModel() {

    val startDestination: StateFlow<Screen?> = flow {
        val lockEnabled = preferencesRepository.observeLockEnabled().first()
        val peopleCount = peopleRepository.observeCount().first()

        emit(
            when {
                lockEnabled -> Screen.Lock(resumable = false)
                peopleCount == 0 -> Screen.FirstRun
                else -> Screen.Main()
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null
    )
}
