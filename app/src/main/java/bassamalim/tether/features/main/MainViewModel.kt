package bassamalim.tether.features.main

import androidx.lifecycle.ViewModel
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.nav.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val navigator: Navigator
) : ViewModel() {

    fun onAddPersonClick() = navigator.navigate(Screen.AddPerson)
}
