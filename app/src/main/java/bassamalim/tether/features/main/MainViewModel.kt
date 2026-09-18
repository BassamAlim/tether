package bassamalim.tether.features.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import bassamalim.tether.core.nav.Navigator
import androidx.navigation.toRoute
import bassamalim.tether.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val navigator: Navigator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    /** The weekly nudge opens the app here. */
    val initialTab =
        if (savedStateHandle.toRoute<Screen.Main>().showCatchUp) MainTab.CATCH_UP
        else MainTab.PEOPLE

    fun onAddPersonClick() = navigator.navigate(Screen.AddPerson)
}
