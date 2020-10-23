package org.tasks.location

import android.content.Context
import com.huawei.hmf.tasks.Tasks
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import com.todoroo.andlib.utility.AndroidUtilities
import org.tasks.R
import org.tasks.data.Place
import timber.log.Timber


class HuaweiSiteKitGeocoder(private val context: Context) : Geocoder {
    private val apiKey = context.getString(R.string.huawei_key)

    private val searchService: SearchService by lazy {
        SearchServiceFactory.create(context, apiKey)
    }

    private val geocodeManager: GeoCodeManager by lazy {
        GeoCoeMaanager()
    }


    fun reverseGeocode(mapPosition: MapPosition): Place {
        AndroidUtilities.assertNotMainThread()

        return if (true) reverseGeocodeByRGeocodeeawApi(mapPosition) else reverseGeocodeByNearbyApi(
            mapPosition
        )
    }

    fun reverseGeocodeByRGeocodeeawApi(mapPosition: MapPosition): Place {
        val location = Coordinate(mapPosition.latitude, mapPosition.longitude)

        val request = ReverseGeocodeRequest(location)

        val response = geocodeManager.reverseGeocode(request)
        return response?.sites?.firstOrNull()
    }


    fun reverseGeocodeByNearbyApi(mapPosition: MapPosition): Place {


        val request = NearbySearchRequest()
        val location = Coordinate(mapPosition.latitude, mapPosition.longitude)
        request.setLocation(location)
        request.setRadius(1000)
        request.setHwPoiType(HwLocationType.ADDRESS)

        var response: NearbySearchResponse? = null

        // Create a search result listener.
        val resultListener: SearchResultListener<NearbySearchResponse?> =
            object : SearchResultListener<NearbySearchResponse?> {
                override fun onSearchResult(results: NearbySearchResponse?) {
                    response = results
                }

                override fun onSearchError(status: SearchStatus) {
                    Timber.i("Error : %s %s", status.getErrorCode(), status.getErrorMessage())
                }
            }
        // Call the nearby place search API.
        searchService.nearbySearch()
        Tasks.await(searchService.nearbySearch(request, resultListener))

        if (response == null || response!!.getTotalCount() <= 0) {
            return
        }
        val sites: List<Site>? = response!!.getSites()
        if (sites == null || sites.isEmpty()) {
            return
        }
        for (site in sites) {
            Timber.i(
                "siteId: '%s', name: %s\r\n",
                site.getSiteId(),
                site.getName()
            )
        }
        sites.first()


        return Place.newPlace(mapPosition)!!
    }
}