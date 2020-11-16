package org.tasks.location

import android.content.Context
import android.os.Bundle
import com.mapbox.api.geocoding.v5.MapboxGeocoding
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.api.geocoding.v5.models.GeocodingResponse
import com.mapbox.geojson.Point
import org.tasks.Callback
import org.tasks.R
import org.tasks.data.Place
import org.tasks.data.Place.Companion.newPlace
import retrofit2.Call
import retrofit2.Response

class MapboxSearchProvider(private val context: Context) : PlaceSearchProvider {
    private var builder: MapboxGeocoding.Builder? = null

    override fun restoreState(savedInstanceState: Bundle) {}
    override fun saveState(outState: Bundle) {}
    override fun getAttributionRes(dark: Boolean): Int {
        return R.drawable.mapbox_logo_icon
    }

    override fun search(
        query: String,
        bias: MapPosition?,
        onSuccess: Callback<List<PlaceSearchResult>>,
        onError: Callback<String>
    ) {
        if (builder == null) {
            val token = context.getString(R.string.mapbox_key)
            builder = MapboxGeocoding.builder()
                .autocomplete(true)
                .accessToken(token)
                .apply {
                    if (bias != null) {
                        proximity(Point.fromLngLat(bias.longitude, bias.latitude))
                    }
                }
        }
        builder!!
            .query(query)
            .build()
            .enqueueCall(
                object : retrofit2.Callback<GeocodingResponse> {
                    override fun onResponse(
                        call: Call<GeocodingResponse>, response: Response<GeocodingResponse>
                    ) {
                        if (response.isSuccessful) {
                            val results = response.body()?.features()?.map { toSearchResult(it) }
                                ?: emptyList()
                            onSuccess.call(results)
                        }
                    }

                    override fun onFailure(call: Call<GeocodingResponse>, t: Throwable) {
                        onError.call(t.message)
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

    private fun toSearchResult(feature: CarmenFeature): PlaceSearchResult {
        val place = newPlace(feature)
        return PlaceSearchResult(feature.id()!!, place.name!!, place.displayAddress!!, place)
    }
}