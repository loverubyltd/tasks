package org.tasks.billing

import android.app.Activity
import android.widget.Toast
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.entity.OrderStatusCode
import org.tasks.billing.IapRequestHelper.startResolutionForResult
import timber.log.Timber

/**
 * Handles the exception returned from the iap api.
 *
 * @since 2019/12/9
 */
object IapExceptionHandler {
    /**s
     * The exception is solved.
     */
    private const val SOLVED = 0

    /**
     * Handles the exception returned from the iap api.
     * @param activity The Activity to call the iap api.
     * @param e The exception returned from the iap api.
     * @return int
     */
    fun handle(activity: Activity?, e: Exception): Int {
        return if (e is IapApiException) {
            Timber.i("returnCode: %s", e.statusCode)
            when (e.statusCode) {
                OrderStatusCode.ORDER_STATE_CANCEL -> {
                    Toast.makeText(activity, "Order has been canceled!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_STATE_PARAM_ERROR -> {
                    Toast.makeText(activity, "Order state param error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_STATE_NET_ERROR -> {
                    Toast.makeText(activity, "Order state net error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_VR_UNINSTALL_ERROR -> {
                    Toast.makeText(activity, "Order vr uninstall error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_HWID_NOT_LOGIN -> {
                    startResolutionForResult(
                        activity,
                        e.status,
                        Constants.REQ_CODE_LOGIN
                    )
                    SOLVED
                }
                OrderStatusCode.ORDER_PRODUCT_OWNED -> {
                    Toast.makeText(activity, "Product already owned error!", Toast.LENGTH_SHORT)
                        .show()
                    OrderStatusCode.ORDER_PRODUCT_OWNED
                }
                OrderStatusCode.ORDER_PRODUCT_NOT_OWNED -> {
                    Toast.makeText(activity, "Product not owned error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_PRODUCT_CONSUMED -> {
                    Toast.makeText(activity, "Product consumed error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED -> {
                    Toast.makeText(
                        activity,
                        "Order account area not supported error!",
                        Toast.LENGTH_SHORT
                    ).show()
                    SOLVED
                }
                OrderStatusCode.ORDER_NOT_ACCEPT_AGREEMENT -> {
                    Toast.makeText(
                        activity,
                        "User does not agree the agreement",
                        Toast.LENGTH_SHORT
                    ).show()
                    SOLVED
                }
                else -> {
                    // handle other error scenarios
                    Toast.makeText(activity, "Order unknown error!", Toast.LENGTH_SHORT).show()
                    SOLVED
                }
            }
        } else {
            Toast.makeText(activity, "external error", Toast.LENGTH_SHORT).show()
            Timber.e(e)
            SOLVED
        }
    }
}
