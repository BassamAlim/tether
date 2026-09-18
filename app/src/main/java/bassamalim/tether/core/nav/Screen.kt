package bassamalim.tether.core.nav

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes (Navigation Compose 2.8+): destinations are serializable objects,
 * so arguments are real types rather than strings parsed out of a URL.
 */
sealed interface Screen {

    /** The tabbed shell: People, Catch up, Settings. */
    @Serializable data object Main : Screen

    @Serializable data object FirstRun : Screen

    @Serializable data object Lock : Screen

    @Serializable data class Person(val id: Long) : Screen

    @Serializable data object AddPerson : Screen

    /** The log sheet, opened over a person. */
    @Serializable data class LogInteraction(val personId: Long) : Screen

    @Serializable data object Search : Screen

    @Serializable data object ImportContacts : Screen
}
