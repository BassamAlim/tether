package bassamalim.tether.core.data.dataSources.maps

import bassamalim.tether.core.di.IoDispatcher
import bassamalim.tether.core.utils.placeNameFromRedirect
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Asks Google where a short Maps link points, to learn the place's name. Only the redirect is
 * read — never a page, and nothing about the person or the catch-up goes with it — so this is
 * one small request for a link that came from Google in the first place.
 */
@Singleton
class MapsLinkDataSource @Inject constructor(
    @param:IoDispatcher private val dispatcher: CoroutineDispatcher
) {

    /** Null when offline, too slow, or the link leads nowhere with a name. */
    suspend fun placeName(url: String): String? = withContext(dispatcher) {
        var next = url
        repeat(MAX_HOPS) {
            val location = redirectOf(next) ?: return@withContext null
            placeNameFromRedirect(location)?.let { return@withContext it }
            // Only an ordinary web address is worth another hop; an intent:// has nothing more.
            if (!location.startsWith("http")) return@withContext null
            next = location
        }
        null
    }

    private fun redirectOf(url: String): String? {
        val connection = try {
            URL(url).openConnection() as HttpURLConnection
        } catch (_: Exception) {
            return null
        }

        return try {
            connection.instanceFollowRedirects = false
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            // Not a browser's: a browser is sent an intent:// for the Maps app rather than the
            // plain link with the name in it.
            connection.setRequestProperty("User-Agent", "Tether")
            if (connection.responseCode in 300..399) connection.getHeaderField("Location") else null
        } catch (_: IOException) {
            null
        } finally {
            connection.disconnect()
        }
    }
}

private const val MAX_HOPS = 3
private const val TIMEOUT_MS = 5_000
