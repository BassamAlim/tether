package bassamalim.tether.core.enums

/**
 * Who reached out. The asymmetry is the point: a friendship where you're always the one calling
 * reads the same as a healthy one until the history says otherwise.
 *
 * Null when it wasn't asked for (the one-tap log on Catch up) or when it doesn't divide that
 * way — you ran into each other. The sheet saves happily without it.
 *
 * [label] is what the log sheet offers; [historyLabel] is how a history row says it.
 */
enum class Initiator(val label: String, val historyLabel: String) {
    ME("I did", "You reached out"),
    THEM("They did", "They reached out")
}
