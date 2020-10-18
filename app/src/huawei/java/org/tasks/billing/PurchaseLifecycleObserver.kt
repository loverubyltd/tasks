package org.tasks.billing


import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.entity.OrderStatusCode
import org.tasks.R
import timber.log.Timber


class PurchaseLifecycleObserver(
    private val activity: ComponentActivity,
    private val registry: ActivityResultRegistry = activity.activityResultRegistry
) : DefaultLifecycleObserver {

    init {
        activity.lifecycle.addObserver(this)
    }

    private lateinit var purchaseResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(owner: LifecycleOwner) {
        purchaseResultLauncher = registry.register(
            "key",
            owner,
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            // Handle the returned Uri
            if (result.data == null) {
                Timber.e("data is null")
                return@register
            }

            val purchaseResultInfo =
                Iap.getIapClient(activity).parsePurchaseResultInfoFromIntent(result.data)
            when (purchaseResultInfo.returnCode) {
                OrderStatusCode.ORDER_STATE_CANCEL -> {
                    Toast.makeText(activity, R.string.cancel, Toast.LENGTH_SHORT).show()
                }
                OrderStatusCode.ORDER_STATE_FAILED,
                OrderStatusCode.ORDER_PRODUCT_OWNED -> {
                    // yofo
                }
                OrderStatusCode.ORDER_STATE_SUCCESS -> {
                    // pay success.
                    val inAppPurchaseData = purchaseResultInfo.inAppPurchaseData
                    val inAppPurchaseDataSignature = purchaseResultInfo.inAppDataSignature


                }
                else -> {
                    // ignore
                }
            }
        }
    }

//    override fun onActivityResult(  requestCode:Int,  resultCode:Int,  data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == BillingClientImpl.REQ_CODE_BUY) {
//            if (resultCode == Activity.RESULT_OK) {
//                when(SubscriptionUtils.getPurchaseResult(this, data)) {
//                    OrderStatusCode.ORDER_STATE_SUCCESS  -> {
//                        Toast.makeText(this, R.string.pay_success, Toast.LENGTH_SHORT).show()
//                        presenter.refreshSubscription()
//                        return
//                    }
//                    OrderStatusCode.ORDER_STATE_CANCEL   -> {
//                        Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show()
//                        return
//                    }
//                    else -> {
//                        Toast.makeText(this, R.string.pay_fail, Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }
//            else {
//                Timber.i(  "cancel subscribe")
//
//                Toast.makeText(this, R.string.cancel, Toast.LENGTH_SHORT).show()
//            }
//        }
//    }


    fun purchaseThing(request: IntentSenderRequest) {
        purchaseResultLauncher.launch(request)
    }

}