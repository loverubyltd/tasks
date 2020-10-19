package org.tasks.location


import android.app.PendingIntent
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.annotation.VisibleForTesting
import com.huawei.hmf.tasks.OnFailureListener
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.location.*
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult


class HuaweiLocationEngineImpl(private val fusedLocationProviderClient: FusedLocationProviderClient) :
    LocationEngine  {

    constructor(context: Context) : this(LocationServices.getFusedLocationProviderClient(context))

    override fun createListener(callback: LocationEngineCallback<LocationEngineResult > ): LocationCallback =
        HuaweiLocationEngineCallbackTransport(callback)

    @Throws(SecurityException::class)
    override fun getLastLocation(callback: LocationEngineCallback<LocationEngineResult>) {
        val transport = HuaweiLastLocationEngineCallbackTransport(callback)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(transport)
            .addOnFailureListener(transport)
    }

    @Throws(SecurityException::class)

    override fun requestLocationUpdates(
        request: LocationEngineRequest,
        callback: LocationEngineCallback<LocationEngineResult>,
        looper: Looper?
    ) {
        fusedLocationProviderClient.requestLocationUpdates(
            toHMSLocationRequest(request),
            HuaweiLocationEngineCallbackTransport(callback),
            looper
        )
    }



    @Throws(SecurityException::class)
    override fun requestLocationUpdates(
        request: LocationEngineRequest,
        pendingIntent: PendingIntent
    ) {
        fusedLocationProviderClient.requestLocationUpdates(
            toHMSLocationRequest(request),
            pendingIntent
        )
    }

    override fun removeLocationUpdates(callback: LocationEngineCallback<LocationEngineResult>) =
        removeLocationUpdates(HuaweiLocationEngineCallbackTransport  (callback) )

      fun removeLocationUpdates(listener: LocationCallback) {
        fusedLocationProviderClient.removeLocationUpdates(listener)
    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent?) {
        if (pendingIntent != null) {
            fusedLocationProviderClient.removeLocationUpdates(pendingIntent)
        }
    }

    private fun toHMSLocationRequest(request: LocationEngineRequest): LocationRequest? =
        LocationRequest().also {
            it.interval = request.interval
            it.fastestInterval = request.fastestInterval
            it.smallestDisplacement = request.displacement
            it.maxWaitTime = request.maxWaitTime
            it.priority = toHMSLocationPriority(request.priority)
        }

    private fun toHMSLocationPriority(enginePriority: Int): Int = when (enginePriority) {
        LocationEngineRequest.PRIORITY_HIGH_ACCURACY -> LocationRequest.PRIORITY_HIGH_ACCURACY
        LocationEngineRequest.PRIORITY_BALANCED_POWER_ACCURACY -> LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        LocationEngineRequest.PRIORITY_LOW_POWER -> LocationRequest.PRIORITY_LOW_POWER
        LocationEngineRequest.PRIORITY_NO_POWER -> LocationRequest.PRIORITY_NO_POWER
        else -> LocationRequest.PRIORITY_NO_POWER
    }

    private class HuaweiLocationEngineCallbackTransport(private val callback: LocationEngineCallback<LocationEngineResult>) :
        LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val locations: List<Location> = locationResult.locations
            if (locations.isNotEmpty()) {
                callback.onSuccess(LocationEngineResult.create(locations))
            } else {
                callback.onFailure(Exception("Unavailable location"))
            }
        }
    }

    @VisibleForTesting
    internal class HuaweiLastLocationEngineCallbackTransport(private val callback: LocationEngineCallback<LocationEngineResult>) :
        OnSuccessListener<Location?>, OnFailureListener {
        override fun onSuccess(location: Location?) {
            callback.onSuccess(
                if (location != null) LocationEngineResult.create(location) else LocationEngineResult.create(listOf())
            )
        }

        override fun onFailure(e: Exception) = callback.onFailure(e)
    }


}

