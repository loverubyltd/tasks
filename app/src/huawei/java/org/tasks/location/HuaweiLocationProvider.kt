package org.tasks.location


import android.annotation.SuppressLint
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes
import com.huawei.hmf.tasks.Task
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*
import org.tasks.R
import timber.log.Timber


class HuaweiLocationProvider(val context: Context) : LocationProvider {
    private val settingsClient: SettingsClient
        get() = LocationServices.getSettingsClient(context)

    private val fusedLocationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(context)

    private var locationUpdatesRequested = false

    private val locationRequest = LocationRequest().apply {
        interval = LOCATION_UPDATE_INTERVAL
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    private val locationCallback by lazy {
        object : LocationCallback() {
            override fun onLocationResult(locationResult: com.huawei.hms.location.LocationResult?) {
                if (locationResult != null) {
                    val locations = locationResult.locations
                    for (location in locations) {
                        Timber.d(
                            "onLocationResult location[Longitude,Latitude,Accuracy]: %s,%s,%s",
                            location.longitude,
                            location.latitude,
                            location.accuracy
                        )
                    }
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability?) {
                if (locationAvailability != null) {
                    val flag = locationAvailability.isLocationAvailable
                    Timber.d("onLocationAvailability isLocationAvailable: %b", flag)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun getLastLocation(
        onSuccess: LocationProvider.SuccessCallback,
        onFailure: LocationProvider.FailureCallback?
    ) {
//        if (!locationUpdatesRequested) {
//            checkLocationSettings()
//                .onSuccessTask { requestLocationUpdates() }
//                .addOnSuccessListener {
//                    locationUpdatesRequested = true
//
//                    onSuccess.onSuccess(LocationResult.create(fusedLocationProviderClient.lastLocation) )
//                }
//                .addOnFailureListener { e -> onFailure?.onFailure(e) }
//        } else {
//            //fusedLocationProviderClient.lastLocation
//        }

        checkLocationSettings()
            .onSuccessTask {
                fusedLocationProviderClient.lastLocation
                    .addOnSuccessListener { onSuccess.onSuccess(LocationResult.create(it)) }
                    .addOnFailureListener { e -> onFailure?.onFailure(e) }
            }
    }

    fun checkLocationSettings(): Task<LocationSettingsResponse> {
        val locationSettingsRequest = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .build()

        return settingsClient.checkLocationSettings(locationSettingsRequest)
            .addOnSuccessListener {
                Timber.d("checkLocationSettings success")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "checkLocationSettings failure")
                handleLocationSettingsFailure(e as ApiException)
            }
    }

    fun requestLocationUpdates(): Task<Void>? =
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
            .addOnSuccessListener {
                Timber.d("requestLocationUpdatesWithCallback onSuccess")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "requestLocationUpdatesWithCallback onFailure: %s", e.message)
            }

    fun removeLocationUpdates(): Task<Void>? =
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            .addOnSuccessListener {
                Timber.d("removeLocationUpdatesWithCallback onSuccess")
            }
            .addOnFailureListener { e ->
                Timber.e(e, "removeLocationUpdatesWithCallback onFailure: %s", e.message)
            }

    private fun handleLocationSettingsFailure(apiException: ApiException) {
        when (apiException.statusCode) {
            LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                try {

                    val rae = apiException as ResolvableApiException
                    //                rae.startResolutionForResult(
                    //                    this@HuaweiLocationProvider,
                    //                    0
                    //                )
                } catch (sie: SendIntentException) {
                    Timber.e(sie, "PendingIntent unable to execute request.")
                }
            }
            in OTHER_KNOWN_LOCATION_SETTINGS_STATUS_CODES -> {
                Timber.e(
                    "checkLocationSettings failed with status: %s code: %0d",
                    LocationSettingsStatusCodes.getStatusCodeString(apiException.statusCode),
                    apiException.statusCode
                )
                handleOtherLocationSettingsFailure(apiException)
            }
            else -> {
                Timber.e(
                    "checkLocationSettings failed with an unknown, or unexpected status code: %0d",
                    apiException.statusCode
                )
            }
        }
    }


    private fun handleOtherLocationSettingsFailure(apiException: ApiException) {
        @StringRes val messageResId: Int = when (apiException.statusCode) {
            LocationSettingsStatusCodes.API_NOT_CONNECTED -> R.string.location_settings_error_api_not_connected
            LocationSettingsStatusCodes.CANCELED -> R.string.location_settings_error_canceled
            LocationSettingsStatusCodes.DEVELOPER_ERROR -> R.string.location_settings_error_developer_error
            LocationSettingsStatusCodes.ERROR -> R.string.location_settings_error_error
            LocationSettingsStatusCodes.INTERNAL_ERROR -> R.string.location_settings_error_internal_error
            LocationSettingsStatusCodes.INTERRUPTED -> R.string.location_settings_error_interrupted
            LocationSettingsStatusCodes.INVALID_ACCOUNT -> R.string.location_settings_error_invalid_account
            LocationSettingsStatusCodes.NETWORK_ERROR -> R.string.location_settings_error_network_error
            LocationSettingsStatusCodes.SIGN_IN_REQUIRED -> R.string.location_settings_error_sign_in_required
            LocationSettingsStatusCodes.TIMEOUT -> R.string.location_settings_error_timeout
            else -> -1
        }

        // FIXME: use a snackbar?
        Toast.makeText(context, messageResId, Toast.LENGTH_LONG).show()
    }


    companion object {
        const val LOCATION_UPDATE_INTERVAL: Long = 10000L
        val OTHER_KNOWN_LOCATION_SETTINGS_STATUS_CODES = setOf(
            LocationSettingsStatusCodes.API_NOT_CONNECTED,
            LocationSettingsStatusCodes.CANCELED,
            LocationSettingsStatusCodes.DEVELOPER_ERROR,
            LocationSettingsStatusCodes.ERROR,
            LocationSettingsStatusCodes.INTERNAL_ERROR,
            LocationSettingsStatusCodes.INTERRUPTED,
            LocationSettingsStatusCodes.INVALID_ACCOUNT,
            LocationSettingsStatusCodes.NETWORK_ERROR,
            LocationSettingsStatusCodes.SIGN_IN_REQUIRED,
            LocationSettingsStatusCodes.TIMEOUT
        )
    }
}
