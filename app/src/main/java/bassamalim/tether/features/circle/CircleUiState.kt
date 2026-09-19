package bassamalim.tether.features.circle

data class CircleUiState(
    val isLoading: Boolean = true,
    /** "12 people · 3 slipping", as on People. */
    val subtitle: String = "",
    val nodes: List<CircleNode> = emptyList(),
    val links: List<CircleLink> = emptyList(),
    val legend: List<HueGroup> = emptyList(),
    /** The legend entry tapped, which dims everyone else. Null shows everyone alike. */
    val focus: HueSlot? = null,
    val selected: SelectedPerson? = null,
    /** The dashed ring, and how far out the drawing reaches, in node diameters. */
    val dueRadius: Float = 0f,
    val extent: Float = 0f
) {
    val isEmpty get() = !isLoading && nodes.isEmpty()
}

/** One dot, already placed: [x] and [y] are in node diameters, with you at the origin. */
data class CircleNode(
    val id: Long,
    val x: Float,
    val y: Float,
    /** Written inside the dot: names, not monograms, are what you recognise people by. */
    val name: String,
    val slot: HueSlot,
    val isUntracked: Boolean
)

/** A connection between two people, as line ends, so the screen needn't look anyone up. */
data class CircleLink(
    val aId: Long,
    val bId: Long,
    val ax: Float,
    val ay: Float,
    val bx: Float,
    val by: Float
)

/** The card under the drawing for the dot you tapped. */
data class SelectedPerson(
    val id: Long,
    val name: String,
    /** "Family · every 2 weeks". */
    val summary: String,
    /** "Last talked 7 weeks ago, 5 weeks overdue". */
    val status: String,
    val slot: HueSlot
)
