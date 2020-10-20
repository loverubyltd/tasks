package org.tasks.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.huawei.hms.location.Geofence
import com.huawei.hms.location.GeofenceData
import com.todoroo.andlib.utility.DateUtilities
import com.todoroo.astrid.reminders.ReminderService
import dagger.hilt.android.AndroidEntryPoint
import org.tasks.Notifier
import org.tasks.data.LocationDao
import org.tasks.data.Place
import org.tasks.injection.InjectingJobIntentService
import org.tasks.notifications.Notification
import org.tasks.time.DateTimeUtils
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceTransitionsIntentService : InjectingJobIntentService() {
    @Inject
    lateinit var locationDao: LocationDao

    @Inject
    lateinit var notifier: Notifier

    override suspend fun doWork(intent: Intent) {
        val geofencingEvent = GeofenceData.getDataFromIntent(intent)
        if (geofencingEvent.isFailure) {
            Timber.e("geofence error code %s", geofencingEvent.errorCode)
            return
        }
        val conversion = geofencingEvent.conversion
        val triggeringGeofences = geofencingEvent.convertingGeofenceList
        Timber.i("Received geofence transition: %s, %s", conversion, triggeringGeofences)

        if (conversion == Geofence.ENTER_GEOFENCE_CONVERSION || conversion == Geofence.EXIT_GEOFENCE_CONVERSION) {
            triggeringGeofences.forEach {
                triggerNotification(it, conversion == Geofence.ENTER_GEOFENCE_CONVERSION)
            }
        } else {
            Timber.w("invalid geofence transition type: %s", conversion)
        }
    }

    private suspend fun triggerNotification(triggeringGeofence: Geofence, arrival: Boolean) {
        val requestId = triggeringGeofence.uniqueId
        try {
            val place = locationDao.getPlace(requestId)
            if (place == null) {
                Timber.e("Can't find place for requestId %s", requestId)
                return
            }
            val geofences = if (arrival) {
                locationDao.getArrivalGeofences(place.uid!!, DateUtilities.now())
            } else {
                locationDao.getDepartureGeofences(place.uid!!, DateUtilities.now())
            }
            geofences
                .map { toNotification(place, it, arrival) }
                .let { notifier.triggerNotifications(it) }
        } catch (e: Exception) {
            Timber.e(e, "Error triggering geofence %s: %s", requestId, e.message)
        }
    }

    private fun toNotification(
        place: Place,
        geofence: org.tasks.data.Geofence,
        arrival: Boolean
    ): Notification = Notification().apply {
        taskId = geofence.task
        type =
            if (arrival) ReminderService.TYPE_GEOFENCE_ENTER else ReminderService.TYPE_GEOFENCE_EXIT
        timestamp = DateTimeUtils.currentTimeMillis()
        location = place.id
    }

    class Broadcast : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            enqueueWork(
                context,
                GeofenceTransitionsIntentService::class.java,
                JOB_ID_GEOFENCE_TRANSITION,
                intent
            )
        }
    }
}