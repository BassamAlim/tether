package bassamalim.tether.core.data.repositories

import bassamalim.tether.core.data.dataSources.maps.MapsLinkDataSource
import javax.inject.Inject
import javax.inject.Singleton

/** Names for pasted Maps links. The only thing in Tether that goes online. */
@Singleton
class PlacesRepository @Inject constructor(
    private val mapsLinkDataSource: MapsLinkDataSource
) {

    suspend fun placeName(url: String): String? = mapsLinkDataSource.placeName(url)
}
