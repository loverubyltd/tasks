package org.tasks.billing

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.text.TextUtils
import android.widget.Toast
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient.PriceType
import com.huawei.hms.iap.entity.*
import org.json.JSONException
import org.tasks.analytics.Firebase
import timber.log.Timber


@Suppress("UNUSED_PARAMETER")
class BillingClientImpl(val context: Context?, inventory: Inventory?, firebase: Firebase?) :
    BillingClient {

    private val inventory: Inventory? = null
    private val firebase: Firebase? = null
    private var onPurchasesUpdated: OnPurchasesUpdated? = null

    override fun queryPurchases() {
        queryPurchasesInternal(null)
    }

    private fun queryPurchasesInternal(continuationToken: String?) {
        val iapClient = Iap.getIapClient(context);
        IapRequestHelper.obtainOwnedPurchases(
            iapClient = iapClient,
            type = PriceType.IN_APP_SUBSCRIPTION,
            continuationToken = null,
            onSuccessListener = { result ->
                Timber.i("obtainOwnedPurchases, success")
                // TODO sheckHiddenLevelPurchaseState(result)
                if (result != null && !TextUtils.isEmpty(result.continuationToken)) {
                    queryPurchasesInternal(result.continuationToken)
                }
            },
            onFailureListener = { e ->
                Timber.e(
                    "obtainOwnedPurchases, type=%s, %s",
                    TYPE_SUBS,
                    e!!.message
                )
                Toast.makeText(
                    context,
                    "get Purchases fail, ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            })


//                if (exception.statusCode == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
//                    Toast.makeText(
//                        context,
//                        "Please sign in to the app with a HUAWEI ID.",
//                        Toast.LENGTH_SHORT
//                    ).show()

    }

    override fun initiatePurchaseFlow(
        activity: Activity, sku: String, skuType: String, oldSku: String?
    ) {
        val iapClient = Iap.getIapClient(activity)
        IapRequestHelper.createPurchaseIntent(
            iapClient = iapClient,
            productId = sku,
            type = skuType.toInt(),
            onSuccessListener = { result ->
                Timber.i("createPurchaseIntent, onSuccess")
//                if (result == null) {
//                    Timber.e("result is null")
//                    return@OnSuccessListener
//                }
                 val status = result.status
//                if (status == null) {
//                    Timber.e("status is null")
//                    return@OnSuccessListener
//                }

                // you should pull up the page to complete the payment process.
                if (status.hasResolution()) {
                    try {
                        status.startResolutionForResult(activity, REQ_CODE_BUY)
                    } catch (ex: IntentSender.SendIntentException) {
                        Timber.e(ex)
                    }
                } else {
                    Timber.e("intent is null")
                }
            },
            onFailureListener = { exception ->
                Toast.makeText(activity, exception.message, Toast.LENGTH_SHORT).show()
                if (exception is IapApiException) {
                    Timber.e(exception, "createPurchaseIntent, returnCode: %s", exception.statusCode)
                    // handle error scenarios
                } else {
                    Timber.e(exception, "Unexpected Error")
                }
            }
        )
    }

    override fun addPurchaseCallback(onPurchasesUpdated: OnPurchasesUpdated) {
        this.onPurchasesUpdated = onPurchasesUpdated
    }

    override fun consume(sku: String) {
        val iapClient = Iap.getIapClient(context)
        val req = ConsumeOwnedPurchaseReq()
        // Parse purchaseToken from InAppPurchaseData in JSON format.
        try {
            val inAppPurchaseData = InAppPurchaseData("""{"product_id": "$sku"}""")
            req.purchaseToken = inAppPurchaseData.purchaseToken
        } catch (e: JSONException) {
            Timber.e("createConsumeOwnedPurchaseReq JSONExeption")
        }

        val task = iapClient.consumeOwnedPurchase(req)
        task.addOnSuccessListener { // Consume success
            Timber.i("consumeOwnedPurchase success")
            Toast.makeText(
                context,
                "Pay success, and the product has been delivered",
                Toast.LENGTH_SHORT
            ).show()
        }
        task.addOnFailureListener { exception ->
            Timber.e(exception)
            Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            if (exception is IapApiException) {
                Timber.e(
                    exception,
                    "consumeOwnedPurchase fail,returnCode: %s",
                    exception.statusCode
                )
            } else {
                // Other external errors
            }
        }
    }

    companion object {
        const val TYPE_SUBS: String = PriceType.IN_APP_SUBSCRIPTION.toString()
        const val REQ_CODE_BUY = 4002

        fun BillingResponseToString(response: Int): String? {
            return when (response) {
//            BillingResponse.FEATURE_NOT_SUPPORTED -> "FEATURE_NOT_SUPPORTED"
//            BillingResponse.SERVICE_DISCONNECTED -> "SERVICE_DISCONNECTED"
//            BillingResponse.OK -> "OK"
//            BillingResponse.USER_CANCELED -> "USER_CANCELED"
//            BillingResponse.SERVICE_UNAVAILABLE -> "SERVICE_UNAVAILABLE"
//            BillingResponse.BILLING_UNAVAILABLE -> "BILLING_UNAVAILABLE"
//            BillingResponse.ITEM_UNAVAILABLE -> "ITEM_UNAVAILABLE"
//            BillingResponse.DEVELOPER_ERROR -> "DEVELOPER_ERROR"
//            BillingResponse.ERROR -> "ERROR"
//            BillingResponse.ITEM_ALREADY_OWNED -> "ITEM_ALREADY_OWNED"
//            BillingResponse.ITEM_NOT_OWNED -> "ITEM_NOT_OWNED"
                else -> "Unknown"
            }
        }
    }
}