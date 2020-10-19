package org.tasks.location

import android.app.PendingIntent
import android.os.Looper
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineCallback
import com.mapbox.android.core.location.LocationEngineRequest
import com.mapbox.android.core.location.LocationEngineResult

// class LocationEngineDerived(locationEngineImpl: LocationEngineImpl<T>) : LocationEngine by
class LocationEngineDerived(locationEngineImpl: LocationEngine ) : LocationEngine by locationEngineImpl

// 
// 
// internal interface LocationEngineImpl<T : LocationEngineCallback<LocationEngineResult?>> : LocationEngine {
//     fun createListener(callback: LocationEngineCallback<LocationEngineResult?>?): T
// 
//     @Throws(SecurityException::class)
//     override fun getLastLocation(callback: LocationEngineCallback<LocationEngineResult?>)
// 
//     @Throws(SecurityException::class)
//     fun requestLocationUpdates(
//         request: LocationEngineRequest,
//         listener: T,
//         looper: Looper?
//     )
// 
//     @Throws(SecurityException::class)
//     override fun requestLocationUpdates(
//         request: LocationEngineRequest,
//         pendingIntent: PendingIntent
//     )
// 
//     override fun removeLocationUpdates(listener: T)
//     override fun removeLocationUpdates(pendingIntent: PendingIntent?)
// }
// 