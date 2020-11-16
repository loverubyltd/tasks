package org.tasks.location

import android.content.Context
import android.location.Address
import com.todoroo.andlib.utility.AndroidUtilities
import org.tasks.data.Place
import org.tasks.data.Place.Companion.newPlace
import java.io.IOException

class AndroidGeocoder(private val context: Context) : Geocoder {
    @Throws(IOException::class)
    override fun reverseGeocode(mapPosition: MapPosition): Place? {
        AndroidUtilities.assertNotMainThread()

        val geocoder = android.location.Geocoder(context)
        val addresses = geocoder.getFromLocation(mapPosition.latitude, mapPosition.longitude, 1)
        val place = newPlace(mapPosition)!!
        val address: Address = addresses.firstOrNull()
            ?: return place
        if (address.maxAddressLineIndex >= 0) {
            place.name = address.getAddressLine(0)
            place.address = buildString {
                append(place.name)
                for (i in 1..address.maxAddressLineIndex) {
                    append(", ").append(address.getAddressLine(i))
                }
            }
        }
        if (address.hasLatitude() && address.hasLongitude()) {
            place.latitude = address.latitude
            place.longitude = address.longitude
        }
        place.phone = address.phone
        place.url = address.url
        return place
    }
}