package org.tasks.location

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.huawei.hms.location.Geofence
import com.huawei.hms.location.GeofenceRequest
import com.huawei.hms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tasks.data.LocationDao
import org.tasks.data.MergedGeofence
import org.tasks.data.Place
import org.tasks.preferences.PermissionChecker
import timber.log.Timber
import javax.inject.Inject

class GeofenceApi @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val permissionChecker: PermissionChecker,
    private val locationDao: LocationDao
) {

    suspend fun registerAll() = locationDao.getPlacesWithGeofences().forEach { update(it) }

    suspend fun update(taskId: Long) = update(locationDao.getPlaceForTask(taskId))

    suspend fun update(place: String) = update(locationDao.getPlace(place))

    @SuppressLint("MissingPermission")
    suspend fun update(place: Place?) {
        if (place == null || !permissionChecker.canAccessBackgroundLocation()) {
            return
        }
        val client = LocationServices.getGeofenceService(context)
        val geofence = locationDao.getGeofencesByPlace(place.uid!!)
        if (geofence != null) {
            Timber.d("Adding geofence for %s", geofence)
            client.createGeofenceList(
                GeofenceRequest.Builder().createGeofence(toHuaweiGeofence(geofence)).build(),
                PendingIntent.getBroadcast(
                    context,
                    0,
                    Intent(context, GeofenceTransitionsIntentService.Broadcast::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        } else {
            Timber.d("Removing geofence for %s", place)
            client.deleteGeofenceList(listOf(place.id.toString()))
        }
    }

    private fun toHuaweiGeofence(geofence: MergedGeofence): Geofence {
        var transitionTypes = 0
        if (geofence.arrival) {
            transitionTypes = transitionTypes or GeofenceRequest.ENTER_INIT_CONVERSION
        }
        if (geofence.departure) {
            transitionTypes = transitionTypes or GeofenceRequest.EXIT_INIT_CONVERSION
        }
        return Geofence.Builder()
            .setRoundArea(geofence.latitude, geofence.longitude, geofence.radius.toFloat())
            .setUniqueId(geofence.uid)
            .setConversions(transitionTypes)
            .setValidContinueTime(Geofence.GEOFENCE_NEVER_EXPIRE)
            .build()
    }
}