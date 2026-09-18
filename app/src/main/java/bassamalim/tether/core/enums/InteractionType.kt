package bassamalim.tether.core.enums

/**
 * How a catch-up happened, as the log sheet offers it. Null on a one-tap log from Catch up,
 * where the type isn't worth asking for.
 */
enum class InteractionType(val label: String) {
    CALL("Call"),
    TEXT("Text"),
    COFFEE("Coffee"),
    MET_UP("Met up"),
    OTHER("Other")
}
