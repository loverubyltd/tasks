package org.tasks.billing

import android.app.Activity
import android.content.Context
import android.content.IntentSender
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient.PriceType
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.entity.OwnedPurchasesResult
import com.huawei.hms.iap.entity.PurchaseIntentResult
import com.todoroo.andlib.utility.AndroidUtilities
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tasks.BuildConfig
import org.tasks.R
import org.tasks.analytics.Firebase
import timber.log.Timber

class BillingClientImpl(
    @ApplicationContext val context: Context,
    val inventory: Inventory,
    val firebase: Firebase
) :
    BillingClient {

    private val iapClient = Iap.getIapClient(context)
    private var onPurchasesUpdated: OnPurchasesUpdated? = null
    private var purchaseLifecycleObserver: PurchaseLifecycleObserver? = null


    override fun queryPurchases() {
        queryPurchasesInternal(null)
    }

    private fun queryPurchasesInternal(continuationToken: String?) {
        IapRequestHelper.obtainOwnedPurchases(
            iapClient = iapClient,
            type = PriceType.IN_APP_SUBSCRIPTION,
            continuationToken = null,
            onSuccessListener = { result ->
                Timber.i("obtainOwnedPurchases, success")

                result?.let { onQueryPurchasesFinished(it) }
            },
            onFailureListener = { e ->
                Timber.e(
                    "obtainOwnedPurchases, type=%s, %s",
                    TYPE_SUBS,
                    e!!.message
                )
                if (e.statusCode == OrderStatusCode.ORDER_HWID_NOT_LOGIN) {
                    Toast.makeText(
                        context,
                        "Please sign in to the app with a HUAWEI ID.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "get Purchases fail, ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    /** Handle a result from querying of purchases and report an updated list to the listener  */
    private fun onQueryPurchasesFinished(result: OwnedPurchasesResult) {
        AndroidUtilities.assertMainThread()

        // Have we been disposed of in the meantime? If so, or bad result code, then quit
        if (iapClient == null || !result.status.isSuccess) {
            Timber.w(
                "Billing client was null or result code (%s) was bad - quitting",
                result.returnCode
            )
            return
        }
        Timber.d("Query inventory was successful.")

        // Update the UI and purchases inventory with new list of purchases
        if (TextUtils.isEmpty(result.continuationToken)) {
            inventory.clear()
        }

        val purchases = result.inAppPurchaseDataList.map { Purchase(it) }
        inventory.add(purchases)

        if (!TextUtils.isEmpty(result.continuationToken)) {
            queryPurchasesInternal(result.continuationToken)
        }
    }

    override fun initiatePurchaseFlow(
        _activity: Activity, sku: String, skuType: String, oldSku: String?
    ) {

        val activity = _activity as ComponentActivity
        purchaseLifecycleObserver = PurchaseLifecycleObserver(activity)

        IapRequestHelper.createPurchaseIntent(
            iapClient = iapClient,
            productId = sku,
            type = PriceType.IN_APP_SUBSCRIPTION,
            onSuccessListener =    fun(result: PurchaseIntentResult) {
                Timber.i("createPurchaseIntent, onSuccess")

                val status = result.status
                    ?: return Timber.e("status is null")
                 // you should pull up the page to complete the payment process.
                if (status.hasResolution()) {
                    try {
                        // status.startResolutionForResult(activity, REQ_CODE_BUY)
                        val pendingIntent = status.getPendingIntent()
                        if (pendingIntent != null) {
                            val request =
                                IntentSenderRequest.Builder(pendingIntent.intentSender).build()

                            purchaseLifecycleObserver!!.purchaseThing(request)
                        }

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
                    Timber.e(
                        exception,
                        "createPurchaseIntent, returnCode: %s",
                        exception.statusCode
                    )
                    // handle error scenarios
                } else {
                    Timber.e(exception, "Unexpected Error")
                }
            })
    }

    override fun addPurchaseCallback(onPurchasesUpdated: OnPurchasesUpdated) {
        this.onPurchasesUpdated = onPurchasesUpdated
    }

    override fun consume(sku: String) {
        check(BuildConfig.DEBUG)
        require(inventory.purchased(sku))

        IapRequestHelper.consumeOwnedPurchase(iapClient, inventory.getPurchase(sku).purchaseToken)
    }


    private class PurchaseLifecycleObserver(private val activity: ComponentActivity) :
        DefaultLifecycleObserver {

        private val registry = activity.activityResultRegistry

        init {
            activity.lifecycle.addObserver(this)
        }

        private lateinit var purchaseResultLauncher: ActivityResultLauncher<IntentSenderRequest>

        override fun onCreate(owner: LifecycleOwner) {
            purchaseResultLauncher = registry.register(
                "key",
                owner,
                ActivityResultContracts.StartIntentSenderForResult(), fun(result: ActivityResult) {
                    val data = result.data
                        ?: return Timber.e("data is null")
                    when (SubscriptionUtils.getPurchaseResult(activity, data)) {
                        OrderStatusCode.ORDER_STATE_SUCCESS -> {
                            Toast.makeText(activity, "ORDER_STATE_SUCCESS", Toast.LENGTH_SHORT)
                                .show()
                            // presenter.refreshSubscription()
                            return
                        }
                        OrderStatusCode.ORDER_STATE_CANCEL -> {
                            Toast.makeText(activity, R.string.cancel, Toast.LENGTH_SHORT).show()
                            return
                        }
                        else -> {
                            Toast.makeText(activity, " R.string.pay_fail", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            )
        }


        fun purchaseThing(request: IntentSenderRequest) {
            purchaseResultLauncher.launch(request)
        }

    }

    companion object {
        const val TYPE_SUBS: String = PriceType.IN_APP_SUBSCRIPTION.toString()

        fun PurchaseResponseToString(response: Int): String? {
            return when (response) {
                OrderStatusCode.ORDER_ACCOUNT_AREA_NOT_SUPPORTED -> "ACCOUNT_AREA_NOT_SUPPORTED"
                OrderStatusCode.ORDER_HIGH_RISK_OPERATIONS -> "HIGH_RISK_OPERATIONS"
                OrderStatusCode.ORDER_HWID_NOT_LOGIN -> "HWID_NOT_LOGIN"
                OrderStatusCode.ORDER_NOT_ACCEPT_AGREEMENT -> "NOT_ACCEPT_AGREEMENT"
                OrderStatusCode.ORDER_PRODUCT_CONSUMED -> "PRODUCT_CONSUMED"
                OrderStatusCode.ORDER_PRODUCT_NOT_OWNED -> "PRODUCT_NOT_OWNED"
                OrderStatusCode.ORDER_PRODUCT_OWNED -> "PRODUCT_OWNED"
                OrderStatusCode.ORDER_STATE_CALLS_FREQUENT -> "CALLS_FREQUENT"
                OrderStatusCode.ORDER_STATE_CANCEL -> "CANCEL"
                OrderStatusCode.ORDER_STATE_FAILED -> "FAILED"
                OrderStatusCode.ORDER_STATE_IAP_NOT_ACTIVATED -> "IAP_NOT_ACTIVATED"
                OrderStatusCode.ORDER_STATE_NET_ERROR -> "NET_ERROR"
                OrderStatusCode.ORDER_STATE_PARAM_ERROR -> "PARAM_ERROR"
                OrderStatusCode.ORDER_STATE_PMS_TYPE_NOT_MATCH -> "PMS_TYPE_NOT_MATCH"
                OrderStatusCode.ORDER_STATE_PRODUCT_COUNTRY_NOT_SUPPORTED -> "PRODUCT_COUNTRY_NOT_SUPPORTED"
                OrderStatusCode.ORDER_STATE_PRODUCT_INVALID -> "PRODUCT_INVALID"
                OrderStatusCode.ORDER_STATE_SUCCESS -> "SUCCESS"
                OrderStatusCode.ORDER_VR_UNINSTALL_ERROR -> "VR_UNINSTALL_ERROR"
                else -> "Unknown"
            }
        }
    }
}

