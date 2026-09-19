package bassamalim.tether.core.enums

/**
 * How a catch-up happened, as the log sheet offers it.
 *
 * Null when it wasn't asked for (the one-tap log on Catch up) or when none of these fit: the
 * sheet saves happily without a type, which is why there is no "Other". Ordered by kind:
 * remote, then over a drink, then over a meal, then side by side.
 */
enum class InteractionType(val label: String) {
    CALL("Call"),
    TEXT("Text"),
    COFFEE("Coffee"),
    TEA("Tea"),
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    WALK("Walk"),
    RUN("Run")
}
