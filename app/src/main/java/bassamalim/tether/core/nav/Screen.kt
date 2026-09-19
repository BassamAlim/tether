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
        val thenCatchUp: Boolean = false,
        /** Likewise for a reminder, which is about one person rather than the list. 0 is none. */
        val thenPersonId: Long = 0
    ) : Screen

    @Serializable data class Person(val id: Long) : Screen

    @Serializable data object AddPerson : Screen

    /**
     * The log sheet, opened over a person. [interactionId] is 0 for a new catch-up and the
     * row's id when an existing one is being corrected — the same sentinel the entity uses for
     * "not inserted yet", since Room's ids start at 1.
     */
    @Serializable data class LogInteraction(
        val personId: Long,
        val interactionId: Long = 0
    ) : Screen

    /** The bell on Person detail: one reminder about one person, set or cleared. */
    @Serializable data class Reminder(val personId: Long) : Screen

    /** The picker that links this person to someone else in Tether. */
    @Serializable data class Connect(val personId: Long) : Screen

    @Serializable data object Search : Screen

    /**
     * The contacts picker, reached from New person: the other way in, for people the address
     * book already knows.
     */
    @Serializable data object ImportContacts : Screen

    /**
     * The walk through people just copied in from contacts, one at a time, asking for the parts
     * an address book has no idea about. [personIds] are the rows the import created.
     */
    @Serializable data class SetUpImported(val personIds: List<Long>) : Screen
}
