package org.tasks.location

import org.tasks.data.Place
import java.io.IOException

interface Geocoder {
    @Throws(IOException::class)
    fun reverseGeocode(mapPosition: MapPosition): Place?
}