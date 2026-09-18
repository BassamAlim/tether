package bassamalim.tether.core.nav

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes (Navigation Compose 2.8+): destinations are serializable objects,
 * so arguments are real types rather than strings parsed out of a URL.
 */
sealed interface Screen {

    /**
     * The tabbed shell: People, Catch up, Settings. [showCatchUp] opens on Catch up, which is
     * where the weekly nudge sends you.
     */
    @Serializable data class Main(val showCatchUp: Boolean = false) : Screen

    @Serializable data object FirstRun : Screen

    /**
     * [resumable] is true when the lock was raised over a running session, so unlocking returns
     * you where you were; false on a cold start, where it opens the app instead.
     */
    @Serializable data class Lock(
        val resumable: Boolean = false,
        /** Carried through the lock, so a nudge still lands on Catch up after you unlock. */
        val thenCatchUp: Boolean = false
    ) : Screen

    @Serializable data class Person(val id: Long) : Screen

    @Serializable data object AddPerson : Screen

    /** The log sheet, opened over a person. */
    @Serializable data class LogInteraction(val personId: Long) : Screen

    @Serializable data object Search : Screen

    @Serializable data object ImportContacts : Screen
}
