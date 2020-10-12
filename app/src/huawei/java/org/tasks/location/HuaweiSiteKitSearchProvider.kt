package org.tasks.location

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.annotation.DrawableRes
import com.huawei.hms.site.api.SearchResultListener
import com.huawei.hms.site.api.SearchService
import com.huawei.hms.site.api.SearchServiceFactory
import com.huawei.hms.site.api.model.*
import org.tasks.Callback
import org.tasks.R
import org.tasks.data.Place
import org.tasks.data.Place.Companion.newPlace


class HuaweiSiteKitSearchProvider(private val activity: Activity) : PlaceSearchProvider {
    private var searchService: SearchService? = null

    override fun restoreState(savedInstanceState: Bundle?) {
    }

    override fun saveState(outState: Bundle?) {
    }

    @DrawableRes
    override fun getAttributionRes(dark: Boolean): Int = R.drawable.huawei_logo

    override fun search(
        query: String,
        bias: MapPosition?,
        onSuccess: Callback<List<PlaceSearchResult>>,
        onError: Callback<String>
    ) {
        val apiKey = activity.getString(R.string.huawei_key)
        if (searchService == null) {
            searchService = SearchServiceFactory.create(
                activity,
                apiKey
            )
        }

        val request = QueryAutocompleteRequest().apply {
            setQuery(query)
            if (bias != null) {
                location = Coordinate(bias.latitude, bias.longitude)
            }
        }

        searchService!!.queryAutocomplete(
            request,
            object : SearchResultListener<QueryAutocompleteResponse> {
                override fun onSearchResult(response: QueryAutocompleteResponse?) {
                    response?.let {
                        onSuccess.call(toSearchResults(it.sites))
                    }
                }

                override fun onSearchError(status: SearchStatus) {
                    onError.call(status.getErrorMessage())
                }
            })
    }

    override fun fetch(
        placeSearchResult: PlaceSearchResult,
        onSuccess: Callback<Place>,
        onError: Callback<String>
    ) {
        onSuccess.call(placeSearchResult.place)
    }

    private fun toSearchResults(sites: Array<Site>): List<PlaceSearchResult> {
        return sites.map {
            PlaceSearchResult(
                it.siteId,
                it.name,
                it.formatAddress,
                toPlace(it)
            )
        }
    }

    private fun toPlace(site: Site): Place = newPlace().apply {
        name = site.name
        address = site.formatAddress
        phone = site.poi.phone
        url = site.poi.websiteUrl
        val location = site.location
        latitude = location.lat
        longitude = location.lng
    }

    companion object {
    }
}