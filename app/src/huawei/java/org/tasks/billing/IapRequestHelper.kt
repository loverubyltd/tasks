/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.tasks.billing

import android.app.Activity
import android.content.IntentSender.SendIntentException
import android.text.TextUtils
import com.huawei.hmf.tasks.Task
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.IapApiException
import com.huawei.hms.iap.IapClient
import com.huawei.hms.iap.entity.*
import com.huawei.hms.support.api.client.Status
import timber.log.Timber


/**
 * The tool class of IAP interface.
 *
 * @see
 */
@Suppress("unused")
object IapRequestHelper {

    /**
     * Create a PurchaseIntentReq object.
     * @param type In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param productId ID of the in-app product to be paid.
     * The in-app product ID is the product ID you set during in-app product configuration in AppGallery Connect.
     * @return PurchaseIntentReq
     */
    private fun createPurchaseIntentReq(type: Int, productId: String): PurchaseIntentReq =
        PurchaseIntentReq().also {
            it.priceType = type
            it.productId = productId
            it.developerPayload = "testPurchase"
        }

    /**
     * Creates a ConsumeOwnedPurchaseReq object.
     *
     * @param purchaseToken which is generated by the Huawei payment server during product payment and returned to the app through InAppPurchaseData.
     * The app transfers this parameter for the Huawei payment server to update the order status and then deliver the in-app product.
     * @return ConsumeOwnedPurchaseReq
     */
    private fun createConsumeOwnedPurchaseReq(purchaseToken: String): ConsumeOwnedPurchaseReq =
        ConsumeOwnedPurchaseReq().also {
            it.purchaseToken = purchaseToken
            it.developerChallenge = "testConsume"
        }

    /**
     * Creates a OwnedPurchasesReq object.
     *
     * @param type type In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param continuationToken A data location flag which returns from obtainOwnedPurchases api or obtainOwnedPurchaseRecord api.
     * @return OwnedPurchasesReq
     */
    private fun createOwnedPurchasesReq(type: Int, continuationToken: String?): OwnedPurchasesReq =
        OwnedPurchasesReq().also {
            it.priceType = type
            it.continuationToken = continuationToken
        }

    /**
     * Creates a ProductInfoReq object.
     *
     * @param type In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param productIds ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @return ProductInfoReq
     */
    private fun createProductInfoReq(type: Int, productIds: List<String>): ProductInfoReq =
        ProductInfoReq().also {
            it.priceType = type
            it.productIds = productIds
        }

    /**
     * Checks whether the country or region of the logged in HUAWEI ID is included in the countries
     * or regions supported by HUAWEI IAP.
     *
     * @param iapClient IapClient instance to call the isEnvReady API.
     * @param onSuccessListener callback on success
     * @param onFailureListener callback on failure
     */
    @JvmStatic
    fun isEnvReady(
        iapClient: IapClient,
        onSuccessListener: IapOnSuccessListener<IsEnvReadyResult>,
        onFailureListener: IapOnFailureListener
    ) {
        Timber.i("call isEnvReady")
        val task: Task<IsEnvReadyResult> = iapClient.isEnvReady
        task.addOnSuccessListener { result ->
            Timber.i("isEnvReady, success")
            onSuccessListener.onSuccess(result)
        }.addOnFailureListener { e ->
            Timber.e("isEnvReady, fail")
            if (e is IapApiException) {
                onFailureListener.onFail(e)
            } else {
                // external error
                Timber.e(e)
            }
        }
    }

    /**
     * Obtain in-app product details configured in AppGallery Connect.
     * @param iapClient IapClient instance to call the obtainProductInfo API.
     * @param productIds ID list of products to be queried. Each product ID must exist and be unique in the current app.
     * @param type In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param onSuccessListener callback on success
     * @param onFailureListener callback on failure
     */
    @JvmStatic
    fun obtainProductInfo(
        iapClient: IapClient,
        productIds: List<String>,
        type: Int,
        onSuccessListener: IapOnSuccessListener<ProductInfoResult>,
        onFailureListener: IapOnFailureListener
    ) {
        Timber.i("call obtainProductInfo")
        val task: Task<ProductInfoResult> =
            iapClient.obtainProductInfo(createProductInfoReq(type, productIds))
        task.addOnSuccessListener { result ->
            Timber.i("obtainProductInfo, success")
            onSuccessListener.onSuccess(result)
        }.addOnFailureListener { e ->
            Timber.e("obtainProductInfo, fail")
            if (e is IapApiException) {
                onFailureListener.onFail(e)
            } else {
                // external error
                Timber.e(e)
            }
        }
    }

    /**
     * create orders for in-app products in the PMS
     * @param iapClient IapClient instance to call the createPurchaseIntent API.
     * @param productId ID of the in-app product to be paid.
     * The in-app product ID is the product ID you set during in-app product configuration in AppGallery Connect.
     * @param type  In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param onSuccessListener callback on success
     * @param onFailureListener callback on failure
     */
    @JvmStatic
    fun createPurchaseIntent(
        iapClient: IapClient,
        productId: String,
        type: Int,
        onSuccessListener: IapOnSuccessListener<PurchaseIntentResult>,
        onFailureListener: IapOnFailureListener
    ) {
        Timber.i("call createPurchaseIntent")
        val task: Task<PurchaseIntentResult> = iapClient.createPurchaseIntent(
            createPurchaseIntentReq(type, productId)
        )
        task.addOnSuccessListener { result ->
            Timber.i("createPurchaseIntent, success")
            onSuccessListener.onSuccess(result)
        }.addOnFailureListener { e ->
            Timber.e("createPurchaseIntent, fail")
            if (e is IapApiException) {
                onFailureListener.onFail(e)
            } else {
                // external error
                Timber.e(e)
            }
        }
    }

    /**
     * to start an activity.
     *
     * @param activity the activity to launch a new page.
     * @param status This parameter contains the pendingIntent object of the payment page.
     * @param reqCode Result code.
     */
    @JvmStatic
    fun startResolutionForResult(activity: Activity?, status: Status?, reqCode: Int) {
        if (status == null) {
            Timber.e("status is null")
            return
        }
        if (status.hasResolution()) {
            try {
                status.startResolutionForResult(activity, reqCode)
            } catch (exp: SendIntentException) {
                Timber.e(exp)
            }
        } else {
            Timber.e("intent is null")
        }
    }

    /**
     * query information about all subscribed in-app products, including consumables,
     * non-consumables, and auto-renewable subscriptions.
     *
     * If consumables are returned, the system needs to deliver them and calls the consumeOwnedPurchase API to consume the products.
     * If non-consumables are returned, the in-app products do not need to be consumed.
     * If subscriptions are returned, all existing subscription relationships of the user under the app are returned.
     *
     * @param iapClient IapClient instance to call the obtainOwnedPurchases API.
     * @param type In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription
     * @param onSuccessListener callback on success
     * @param onFailureListener callback on failure
     */
    @JvmStatic
    fun obtainOwnedPurchases(
        iapClient: IapClient,
        type: Int,
        continuationToken: String?,
        onSuccessListener: IapOnSuccessListener<OwnedPurchasesResult?>,
        onFailureListener: IapOnFailureListener
    ) {
        Timber.i("call obtainOwnedPurchases")
        val task: Task<OwnedPurchasesResult> = iapClient.obtainOwnedPurchases(
            createOwnedPurchasesReq(type, continuationToken)
        )
        task.addOnSuccessListener { result ->
            Timber.i("obtainOwnedPurchases, success")
            onSuccessListener.onSuccess(result)
        }.addOnFailureListener { e ->
            Timber.e("obtainOwnedPurchases, fail")
            if (e is IapApiException) {
                onFailureListener.onFail(e)
            } else {
                // external error
                Timber.e(e)
            }
        }
    }

    /**
     * obtain the historical consumption information about a consumable in-app product or all
     * subscription receipts of a subscription.
     *
     * @param iapClient IapClient instance to call the obtainOwnedPurchaseRecord API.
     * @param priceType In-app product type.
     * The value contains: 0: consumable 1: non-consumable 2 auto-renewable subscription.
     * @param continuationToken Data locating flag for supporting query in pagination mode.
     * @param onSuccessListener callback on success
     * @param onFailureListener callback on failure
     */
    @JvmStatic
    fun obtainOwnedPurchaseRecord(
        iapClient: IapClient,
        priceType: Int,
        continuationToken: String?,
        onSuccessListener: IapOnSuccessListener<OwnedPurchasesResult>,
        onFailureListener: IapOnFailureListener
    ) {
        Timber.i("call obtainOwnedPurchaseRecord")
        val task: Task<OwnedPurchasesResult> = iapClient.obtainOwnedPurchaseRecord(
            createOwnedPurchasesReq(priceType, continuationToken)
        )
        task.addOnSuccessListener { result ->
            Timber.i("obtainOwnedPurchaseRecord, success")
            onSuccessListener.onSuccess(result)
        }.addOnFailureListener { e ->
            Timber.e("obtainOwnedPurchaseRecord, fail")
            if (e is IapApiException) {
                onFailureListener.onFail(e)
            } else {
                // external error
                Timber.e(e)
            }
        }
    }

    /**
     * Consume all the unconsumed purchases with priceType 0.
     *
     * @param iapClient IapClient instance to call the consumeOwnedPurchase API.
     * @param purchaseToken which is generated by the Huawei payment server during product payment and returned to the app through InAppPurchaseData.
     */
    @JvmStatic
    fun consumeOwnedPurchase(iapClient: IapClient, purchaseToken: String) {
        Timber.i("call consumeOwnedPurchase")
        val task: Task<ConsumeOwnedPurchaseResult> = iapClient.consumeOwnedPurchase(
            createConsumeOwnedPurchaseReq(purchaseToken)
        )
        task.addOnSuccessListener { // Consume success.
            Timber.i("consumeOwnedPurchase success")
        }.addOnFailureListener { e ->
            if (e is IapApiException) {
                val returnCode = e.statusCode
                Timber.e(
                    "consumeOwnedPurchase fail, IapApiException returnCode: %s", returnCode
                )
            } else {
                // Other external errors
                Timber.e(e)
            }
        }
    }

    /**
     * Link to subscription manager page
     *
     * @param activity activity
     * @param productId the productId of the subscription product
     */
    @JvmStatic
    fun showSubscription(activity: Activity?, productId: String?) {
        val req = StartIapActivityReq()
        if (TextUtils.isEmpty(productId)) {
            req.type = StartIapActivityReq.TYPE_SUBSCRIBE_MANAGER_ACTIVITY
        } else {
            req.type = StartIapActivityReq.TYPE_SUBSCRIBE_EDIT_ACTIVITY
            req.subscribeProductId = productId
        }
        val iapClient = Iap.getIapClient(activity)
        val task: Task<StartIapActivityResult> = iapClient.startIapActivity(req)
        task.addOnSuccessListener { result ->
            result?.startActivity(activity)
        }.addOnFailureListener { e -> IapExceptionHandler.handle(activity, e) }
    }
}