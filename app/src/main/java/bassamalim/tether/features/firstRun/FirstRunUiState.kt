package bassamalim.tether.features.firstRun

import bassamalim.tether.core.backup.ImportPreview

data class FirstRunUiState(
    /** Until the count is in, this screen has no idea whether it has anything to say. */
    val isLoading: Boolean = true,
    /** Whether anyone has been added yet, which is the only thing this screen waits for. */
    val hasPeople: Boolean = false,
    /** The picked backup, waiting on a yes. Null when no import has been offered. */
    val pendingImport: ImportPreview? = null,
    val isImporting: Boolean = false,
    /**
     * Why the last import didn't happen. There's no snackbar on this screen — and a restore
     * that works answers for itself by filling the app — so the only thing left to say out loud
     * is that a file couldn't be used.
     */
    val importProblem: String? = null
)
