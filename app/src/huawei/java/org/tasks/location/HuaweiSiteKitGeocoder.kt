package org.tasks.location

import android.content.Context
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.huawei.hms.site.api.model.Coordinate
import com.todoroo.andlib.utility.AndroidUtilities
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tasks.BuildConfig
import org.tasks.R
import org.tasks.data.Place
import org.tasks.locale.Locale
import org.tasks.location.support.HuaweiSiteKitClient
import org.tasks.location.support.ReverseGeocodeRequest
import org.tasks.location.support.toPlace
import timber.log.Timber
import javax.inject.Inject

class HuaweiSiteKitGeocoder @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val client: HuaweiSiteKitClient,
    private val locale: Locale
) : Geocoder {
    private val apiKey = context.getString(R.string.huawei_key)

    override fun reverseGeocode(mapPosition: MapPosition): Place? {
        AndroidUtilities.assertNotMainThread()

        val request = ReverseGeocodeRequest(
            location = Coordinate(mapPosition.latitude, mapPosition.longitude),
            language = locale.locale.toLanguageTag(),
            politicalView = locale.locale.country
        )
        val response = client.reverseGeocode(request, apiKey).execute()

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
                GsonBuilder().setPrettyPrinting().create().toJson(JsonParser().parse(json))
            } else json
        }
    }

}
