package bassamalim.tether.core.enums

/** How a catch-up happened. Null on a one-tap log, where the type isn't worth asking for. */
enum class InteractionType {
    CALL,
    MESSAGE,
    MEETUP,
    OTHER
}
