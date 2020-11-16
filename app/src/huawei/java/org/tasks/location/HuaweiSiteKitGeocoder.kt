package org.tasks.location

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.huawei.hms.site.api.model.Coordinate
import com.todoroo.andlib.utility.AndroidUtilities
import org.tasks.BuildConfig
import org.tasks.R
import org.tasks.data.Place
import org.tasks.location.support.HuaweiSiteKitClient
import org.tasks.location.support.HuaweiSiteKitGeocodingService
import org.tasks.location.support.ReverseGeocodeRequest
import org.tasks.location.support.toPlace
import timber.log.Timber


class HuaweiSiteKitGeocoder(private val context: Context) : Geocoder {
    private val apiKey = context.getString(R.string.huawei_key)

    override fun reverseGeocode(mapPosition: MapPosition): Place? {
        AndroidUtilities.assertNotMainThread()

        val location = Coordinate(mapPosition.latitude, mapPosition.longitude)
        val request = ReverseGeocodeRequest(location)

        val service: HuaweiSiteKitGeocodingService =
            HuaweiSiteKitClient.geocodingService()
        val response = service.reverseGeocode(request, apiKey).execute()

        val body = response.body()
        if (response.isSuccessful && body != null) {
            // Timber.d(prettyPrint(body))
            return body.sites.first().toPlace()
        } else {
            Timber.e(response.errorBody()!!.string())
        }
        return Place.newPlace(mapPosition)
    }

    companion object {
        private fun prettyPrint(json: String): String {
            return if (BuildConfig.DEBUG) {
                GsonBuilder().setPrettyPrinting().create()
                    .toJson(JsonParser().parse(json))
            } else json
        }
    }

}
