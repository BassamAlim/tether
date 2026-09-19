package bassamalim.tether.core.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The vocabulary is defaults plus whatever you've typed, merged on read, so the rule that
 * decides "is this one already in the list" is what keeps a second "Siblings" out of it.
 */
class RelationshipTypesTest {

    @Test
    fun `recognises a default whatever the case`() {
        assertTrue(RelationshipTypes.isDefault("Siblings"))
        assertTrue(RelationshipTypes.isDefault("siblings"))
        assertTrue(RelationshipTypes.isDefault("NEIGHBOURS"))
    }

    @Test
    fun `does not claim one you invented`() {
        assertFalse(RelationshipTypes.isDefault("Gym"))
        // Dropped from the defaults: "Work" and "School" say it from either side.
        assertFalse(RelationshipTypes.isDefault("Worked together"))
        assertFalse(RelationshipTypes.isDefault("Studied together"))
        assertFalse(RelationshipTypes.isDefault("Sibling"))
        assertFalse(RelationshipTypes.isDefault(""))
    }

    /** A duplicate would show twice, since the merge only filters customs against defaults. */
    @Test
    fun `the defaults are distinct`() {
        val lowercased = RelationshipTypes.DEFAULTS.map { it.lowercase() }

        assertEquals(lowercased.size, lowercased.distinct().size)
    }

    /** Both halves of the union survived: a person's relationship, and a pair's. */
    @Test
    fun `covers both of the lists it replaced`() {
        assertTrue(RelationshipTypes.isDefault("Close friend"))
        assertTrue(RelationshipTypes.isDefault("Work"))
        assertTrue(RelationshipTypes.isDefault("Siblings"))
        assertTrue(RelationshipTypes.isDefault("Neighbours"))
    }
}
