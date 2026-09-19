package bassamalim.tether.core.data.dataSources.room.entities

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * The whole point of the canonical pair: "Ahmed knows Sara" and "Sara knows Ahmed" have to be
 * the same row, or the unique index never fires and both pages grow a duplicate.
 */
class ConnectionTest {

    @Test
    fun `orders the pair by id whichever way round it is given`() {
        val forwards = Connection.between(3, 7)
        val backwards = Connection.between(7, 3)

        assertEquals(3, forwards.personAId)
        assertEquals(7, forwards.personBId)
        assertEquals(forwards, backwards)
    }

    @Test
    fun `keeps the label`() {
        assertEquals("Siblings", Connection.between(7, 3, "Siblings").label)
    }

    @Test
    fun `a bare link needs no label`() {
        assertEquals("", Connection.between(1, 2).label)
    }
}
