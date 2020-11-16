package org.tasks.location

interface LocationProvider {
    fun interface SuccessCallback {
        fun onSuccess(result: LocationResult)
    }

    fun interface FailureCallback {
        fun onFailure(exception: Exception)
    }

    fun getLastLocation(onSuccess: SuccessCallback, onFailure: FailureCallback?)
}