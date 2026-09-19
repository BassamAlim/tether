package bassamalim.tether.features.importContacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bassamalim.tether.core.models.DeviceContact
import bassamalim.tether.core.nav.Navigator
import bassamalim.tether.core.utils.cadenceValueLabel
import bassamalim.tether.core.utils.initials
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportContactsViewModel @Inject constructor(
    private val domain: ImportContactsDomain,
    private val navigator: Navigator
) : ViewModel() {

    /** The whole address book, held once and filtered in memory as you type. */
    private var contacts: List<DeviceContact> = emptyList()
    private var selectedIds: Set<Long> = emptySet()

    private val _uiState = MutableStateFlow(ImportContactsUiState())
    val uiState: StateFlow<ImportContactsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val days = domain.defaultCadenceDays()

            _uiState.update {
                it.copy(
                    cadenceLabel = cadenceValueLabel(days).lowercase(),
                    hasDefaultCadence = days != null
                )
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(hasPermission = granted, isPermissionDenied = !granted) }

        if (granted) load()
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
        refresh()
    }

    fun onToggle(id: Long) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
        refresh()
    }

    fun onCancel() = navigator.popBackStack()

    fun onImport() {
        if (!_uiState.value.canImport) return

        _uiState.update { it.copy(isImporting = true) }

        viewModelScope.launch {
            domain.import(contacts.filter { it.id in selectedIds })
            navigator.popBackStack()
        }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            contacts = domain.readContacts()
            _uiState.update { it.copy(isLoading = false, totalCount = contacts.size) }
            refresh()
        }
    }

    private fun refresh() {
        val query = _uiState.value.query

        val rows = contacts
            .filter { it.name.contains(query, ignoreCase = true) }
            .map { contact ->
                ContactRow(
                    id = contact.id,
                    name = contact.name,
                    initials = initials(contact.name),
                    subtitle = contact.phone ?: contact.email.orEmpty(),
                    isSelected = contact.id in selectedIds
                )
            }

        _uiState.update { it.copy(contacts = rows, selectedCount = selectedIds.size) }
    }
}
