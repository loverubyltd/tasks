package org.tasks.location

import android.annotation.SuppressLint
import android.content.Context
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.location.LocationEngineResult
import java.lang.Exception

class MapboxBestLocationProvider(context: Context) : LocationProvider {
    private val locationEngine =  LocationEngineProvider.getBestLocationEngine(context)

    @SuppressLint("MissingPermission")
    override fun getLastLocation(
        onSuccess: LocationProvider.SuccessCallback,
        onFailure: LocationProvider.FailureCallback?
    ) {
        locationEngine.getLastLocation(object : LocationEngineCallback<LocationEngineResult> {
            override fun onSuccess(result: LocationEngineResult?) {
                onSuccess.onSuccess(LocationResult.create(result?.locations))
            }

            override fun onFailure(exception: Exception) {
                onFailure?.onFailure(exception)
            }
        })
    }
}