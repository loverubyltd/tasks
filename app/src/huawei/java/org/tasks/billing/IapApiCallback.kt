package org.tasks.billing

import com.huawei.hms.iap.IapApiException

fun interface IapOnSuccessListener<T> {
    /**
     * The request is successful.
     * @param result The result of a successful response.
     */
    fun onSuccess(result: T)
}

fun interface IapOnFailureListener {
    /**
     * Callback fail.
     * @param e An Exception from IAP SDK.
     */
    fun onFail(e: IapApiException)
}