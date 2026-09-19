package bassamalim.tether.core.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacesTest {

    @Test
    fun `plain words are the place and go nowhere`() {
        assertEquals(Place("Blue Tokai", url = null, isNamed = true), parsePlace(" Blue Tokai "))
    }

    @Test
    fun `a blank where is no place at all`() {
        assertNull(parsePlace(""))
        assertNull(parsePlace("   "))
    }

    @Test
    fun `a place link names itself`() {
        val url = "https://www.google.com/maps/place/Blue+Tokai+Coffee+Roasters/@12.97,77.64,17z/data=!3m1"
        val place = parsePlace(url)!!

        assertEquals("Blue Tokai Coffee Roasters", place.label)
        assertEquals(url, place.url)
        assertTrue(place.isNamed)
    }

    @Test
    fun `escaped names are decoded`() {
        val place = parsePlace("https://www.google.com/maps/place/Caf%C3%A9+de+Flore/@48.85,2.33,17z")!!
        assertEquals("Café de Flore", place.label)
    }

    @Test
    fun `a search or a query names the place`() {
        assertEquals(
            "Blue Tokai",
            parsePlace("https://www.google.com/maps/search/?api=1&query=Blue%20Tokai")!!.label
        )
        assertEquals("Blue Tokai", parsePlace("https://maps.google.com/?q=Blue+Tokai")!!.label)
    }

    @Test
    fun `words typed around a link beat what the link says`() {
        val place = parsePlace("Maya's place - https://maps.app.goo.gl/AbCdEf123")!!

        assertEquals("Maya's place", place.label)
        assertEquals("https://maps.app.goo.gl/AbCdEf123", place.url)
        assertTrue(place.isNamed)
    }

    @Test
    fun `a short link on its own says only that it's a map`() {
        val place = parsePlace("https://maps.app.goo.gl/AbCdEf123?g_st=ic")!!

        assertEquals("Google Maps", place.label)
        assertFalse(place.isNamed)
    }

    @Test
    fun `coordinates alone are a dropped pin`() {
        assertEquals("Dropped pin", parsePlace("https://maps.google.com/?q=24.7136,46.6753")!!.label)
        assertEquals(
            "Dropped pin",
            parsePlace("https://www.google.com/maps/place/24%C2%B042'49.0%22N+46%C2%B040'31.1%22E/@24.71,46.67,17z")!!.label
        )
        assertEquals("Dropped pin", parsePlace("https://www.google.com/maps/@24.7136,46.6753,15z")!!.label)
    }

    @Test
    fun `other links are called by their site`() {
        val place = parsePlace("https://www.openstreetmap.org/#map=18/24.71/46.67")!!

        assertEquals("openstreetmap.org", place.label)
        assertFalse(place.isNamed)
    }

    @Test
    fun `punctuation after a link is not part of it`() {
        val place = parsePlace("Dinner at hers (https://maps.app.goo.gl/AbC).")!!

        assertEquals("https://maps.app.goo.gl/AbC", place.url)
        assertEquals("Dinner at hers", place.label)
    }

    @Test
    fun `a malformed escape doesn't throw`() {
        assertEquals("Google Maps", parsePlace("https://www.google.com/maps/place/%E0%A4%A")!!.label)
    }

    @Test
    fun `a pasted maps share keeps the name and the link`() {
        val shared = "Blue Tokai Coffee Roasters\n" +
                "12th Main Rd, Indiranagar, Bengaluru\n" +
                "https://maps.app.goo.gl/AbCdEf123"

        assertEquals(
            "Blue Tokai Coffee Roasters https://maps.app.goo.gl/AbCdEf123",
            tidyPlaceInput(shared)
        )
    }

    @Test
    fun `typing is left alone`() {
        assertEquals("Blue Tokai ", tidyPlaceInput("Blue Tokai "))
        assertEquals("Blue Tokai Indiranagar", tidyPlaceInput("Blue Tokai\nIndiranagar"))
    }

    @Test
    fun `only a bare unnamed maps link is worth looking up`() {
        assertEquals(
            "https://maps.app.goo.gl/AbC",
            linkToLookUp("https://maps.app.goo.gl/AbC")
        )
        assertNull(linkToLookUp("Blue Tokai https://maps.app.goo.gl/AbC"))
        assertNull(linkToLookUp("https://www.google.com/maps/place/Blue+Tokai/@12.9,77.6,17z"))
        assertNull(linkToLookUp("https://maps.google.com/?q=24.7136,46.6753"))
        assertNull(linkToLookUp("https://www.openstreetmap.org/#map=18/24.71/46.67"))
        assertNull(linkToLookUp("Blue Tokai"))
    }

    @Test
    fun `a short link's redirect names the place`() {
        // As Google answered a plain request for a real short link.
        val location = "https://www.google.com/maps/place/47+Federal+St,+Salem,+MA+01970,+USA/" +
                "data=!4m2!3m1!1s0x89e3147a9244c291:0x6d82b322f79f1d70?utm_source=mstt_1&entry=gps"

        assertEquals("47 Federal St, Salem, MA 01970, USA", placeNameFromRedirect(location))
    }

    @Test
    fun `a browser's intent redirect names the place through its fallback`() {
        // As Google answered the same link asked for with a phone browser's user agent.
        val location = "intent://maps.app.goo.gl/zrRSuHGd1gea3k896#Intent;" +
                "package=com.google.android.gms;scheme=https;S.browser_fallback_url=" +
                "https://www.google.com/maps/place/47%2BFederal%2BSt,%2BSalem,%2BMA%2B01970,%2BUSA/" +
                "data%3D!4m2!3m1!1s0x89e3147a9244c291:0x6d82b322f79f1d70%3Futm_source%3Dmstt_1;end;"

        assertEquals("47 Federal St, Salem, MA 01970, USA", placeNameFromRedirect(location))
    }

    @Test
    fun `a consent detour names the place through continue`() {
        val location = "https://consent.google.com/ml?continue=" +
                "https%3A%2F%2Fwww.google.com%2Fmaps%2Fplace%2FBlue%2BTokai%2F%4012.9%2C77.6%2C17z&gl=DE"

        assertEquals("Blue Tokai", placeNameFromRedirect(location))
    }

    @Test
    fun `a redirect that names nowhere says so`() {
        assertNull(placeNameFromRedirect("https://www.google.com/maps/@24.7136,46.6753,15z"))
    }
}
