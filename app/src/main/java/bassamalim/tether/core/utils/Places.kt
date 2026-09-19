package bassamalim.tether.core.utils

import java.net.URLDecoder

/**
 * A catch-up's "Where", read for showing: what to call it and, when a link was pasted, where
 * tapping it goes.
 *
 * The column stays free text — "Blue Tokai", a Google Maps link, or both ("Blue Tokai
 * https://maps.app.goo.gl/…") — so this is a reading of what was typed, not a second field.
 */
data class Place(
    /** "Blue Tokai", or a stand-in when a link carries no name: "Dropped pin", "Google Maps". */
    val label: String,
    /** The link as pasted, or null for plain words. */
    val url: String?,
    /** True when [label] came from the text or the link rather than being a stand-in. */
    val isNamed: Boolean
)

/** Null for a blank "Where", which most one-tap logs are. */
fun parsePlace(text: String): Place? {
    val trimmed = text.trim()
    if (trimmed.isEmpty()) return null

    val url = URL.find(trimmed)?.value?.trimEnd(*TRAILING_PUNCTUATION)
        ?: return Place(label = trimmed, url = null, isNamed = true)

    // Words typed around the link are the name: nothing a link says beats what you called it.
    val words = trimmed
        .replace(url, " ")
        .replace(EMPTY_BRACKETS, " ")
        .replace(WHITESPACE, " ")
        .trim { it.isWhitespace() || it in SEPARATORS }
    if (words.isNotEmpty()) return Place(label = words, url = url, isNamed = true)

    nameFromMapsUrl(url)?.let { return Place(label = it, url = url, isNamed = true) }

    val label = when {
        isCoordinatesOnly(url) -> "Dropped pin"
        isGoogleMaps(url) -> "Google Maps"
        else -> host(url).removePrefix("www.").ifEmpty { url }
    }
    return Place(label = label, url = url, isNamed = false)
}

/**
 * Tidies a paste into the single-line field. Sharing a place out of Google Maps hands over a
 * block — the name, often an address, then the link — and a single-line field would show only
 * its first line and hide the link. Keeping the name and the link keeps what "Where" is for.
 */
fun tidyPlaceInput(text: String): String {
    if ('\n' !in text && '\r' !in text) return text

    val lines = text.lines().map { it.trim() }.filter { it.isNotEmpty() }
    val url = URL.find(text)?.value

    return if (url == null) {
        lines.joinToString(" ")
    } else {
        val name = lines.firstOrNull { URL.find(it) == null }
        listOfNotNull(name, url).joinToString(" ")
    }
}

/**
 * The link worth asking Google about: a Maps link standing alone and carrying no name — a short
 * `maps.app.goo.gl` link, most often. Null when there's nothing a lookup could add.
 */
fun linkToLookUp(text: String): String? {
    val place = parsePlace(text) ?: return null
    val url = place.url ?: return null
    return url.takeIf { !place.isNamed && isGoogleMaps(it) && !isCoordinatesOnly(it) }
}

/**
 * Where a redirect points, read for a name. A short link answers a plain request with the full
 * `/maps/place/<name>/…` link; a browser gets an Android `intent://` instead, with the same link
 * as its fallback, and outside the US a hop may detour through a consent page that carries it
 * as `continue`. Null when the hop names nowhere, so the caller can follow it further.
 */
fun placeNameFromRedirect(location: String): String? {
    val candidates = listOfNotNull(
        location,
        INTENT_FALLBACK.find(location)?.groupValues?.get(1)?.let(::decode),
        splitUrl(location).second.split('&')
            .firstOrNull { it.startsWith("continue=") }
            ?.substringAfter('=')
            ?.let(::decode)
    )
    return candidates.firstNotNullOfOrNull { nameFromMapsUrl(it) }
}

/** A place's name from the link itself: /maps/place/<name>/, /maps/search/<name>/, ?q=<name>. */
private fun nameFromMapsUrl(url: String): String? {
    if (!isGoogleMaps(url)) return null

    val (path, query) = splitUrl(url)
    val segments = path.split('/').filter { it.isNotEmpty() }

    val fromPath = segments.zipWithNext()
        .firstOrNull { (key, _) -> key == "place" || key == "search" }
        ?.second

    val fromQuery = query.split('&')
        .map { it.substringBefore('=') to it.substringAfter('=', "") }
        .firstOrNull { (key, _) -> key == "q" || key == "query" }
        ?.second

    return listOfNotNull(fromPath, fromQuery)
        .mapNotNull { decode(it)?.trim() }
        .firstOrNull { it.isNotEmpty() && !it.startsWith("@") && !isCoordinates(it) }
}

/** A dropped pin: the link names somewhere by its coordinates and nothing else. */
private fun isCoordinatesOnly(url: String): Boolean {
    if (!isGoogleMaps(url)) return false
    val decoded = decode(url) ?: url
    return COORDINATES_IN_URL.containsMatchIn(decoded) || '°' in decoded
}

private fun isGoogleMaps(url: String): Boolean {
    val host = host(url)
    val path = splitUrl(url).first
    return host == "maps.app.goo.gl" ||
            host.startsWith("maps.google.") ||
            (host == "goo.gl" && path.startsWith("/maps")) ||
            (GOOGLE_HOST.matches(host) && path.startsWith("/maps"))
}

private fun host(url: String): String = url
    .substringAfter("://")
    .takeWhile { it != '/' && it != '?' && it != '#' }
    .substringAfterLast('@')
    .substringBefore(':')
    .lowercase()

/** The path and the query, without the scheme, the host or the fragment. */
private fun splitUrl(url: String): Pair<String, String> {
    val rest = url.substringAfter("://").substringBefore('#')
    val afterHost = rest.dropWhile { it != '/' && it != '?' }
    return afterHost.substringBefore('?') to afterHost.substringAfter('?', "")
}

/** "24.71,46.67", or the degrees a dropped pin is named with: 24°42'49.0"N 46°40'31.1"E. */
private fun isCoordinates(text: String) = COORDINATES.matches(text) || '°' in text

/** Null for a malformed escape, which a hand-edited link can easily have. */
private fun decode(text: String): String? =
    runCatching { URLDecoder.decode(text, Charsets.UTF_8) }.getOrNull()

private val INTENT_FALLBACK = Regex("""S\.browser_fallback_url=([^;]+)""")
private val URL = Regex("""https?://\S+""", RegexOption.IGNORE_CASE)
private val GOOGLE_HOST = Regex("""(www\.)?google\.[a-z.]+""")
private val COORDINATES = Regex("""-?\d{1,3}(\.\d+)?,\s*-?\d{1,3}(\.\d+)?""")
private val COORDINATES_IN_URL = Regex("""[@=/]-?\d{1,3}\.\d+,\s*-?\d{1,3}\.\d+""")
private val TRAILING_PUNCTUATION = charArrayOf('.', ',', ')', ';', '!', '?')
private val SEPARATORS = setOf(',', '.', '-', '–', '—', '·', ':', '|')
private val EMPTY_BRACKETS = Regex("""\(\s*\)""")
private val WHITESPACE = Regex("""\s+""")
