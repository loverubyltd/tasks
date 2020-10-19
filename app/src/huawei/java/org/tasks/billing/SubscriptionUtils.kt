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
import android.content.Intent
import com.huawei.hms.iap.Iap
import com.huawei.hms.iap.entity.InAppPurchaseData
import com.huawei.hms.iap.entity.OrderStatusCode
import com.huawei.hms.iap.entity.OwnedPurchasesResult
import org.json.JSONException
import timber.log.Timber

/**
 * Utility methods for Subscription functionality.
 *
 * Based on HUAWEI example code.
 *
 * @see https://github.com/huaweicodelabs/IAP/blob/master/app/src/main/java/com/huawei/iapdemo/subscription/SubscriptionUtils.java#L8
 */
object SubscriptionUtils {
    private const val TAG = "SubscriptionUtils"

    /**
     * Decide whether to offer subscription service
     *
     * @param result the OwnedPurchasesResult from IapClient.obtainOwnedPurchases
     * @param productId subscription product id
     * @return decision result
     */
    fun shouldOfferService(result: OwnedPurchasesResult?, productId: String): Boolean {
        if (null == result) {
            TImber.e("OwnedPurchasesResult is null")
            return false
        }
        val inAppPurchaseDataList = result.inAppPurchaseDataList
        for (data in inAppPurchaseDataList) {
            try {
                val inAppPurchaseData = InAppPurchaseData(data)
                if (productId == inAppPurchaseData.productId) {
                    val index = inAppPurchaseDataList.indexOf(data)
                    val signature = result.inAppSignature[index]
                    val credible: Boolean =
                        Security.verify(data, signature, Security.getPublicKey())
                    return if (credible) {
                        inAppPurchaseData.isSubValid
                    } else {
                        Timber.e("check the data signature fail")
                        false
                    }
                }
            } catch (e: JSONException) {
                Timber.e(e, "parse InAppPurchaseData JSONException")
                return false
            }
        }
        return false
    }

    /**
     * Parse PurchaseResult data from intent
     *
     * @param activity Activity
     * @param data the intent from onActivityResult
     * @return result status
     */
    fun getPurchaseResult(activity: Activity?, data: Intent?): Int {
        val purchaseResultInfo = Iap.getIapClient(activity).parsePurchaseResultInfoFromIntent(data)
        if (null == purchaseResultInfo) {
            Timber.e("PurchaseResultInfo is null")
            return OrderStatusCode.ORDER_STATE_FAILED
        }
        val returnCode = purchaseResultInfo.returnCode
        val errMsg = purchaseResultInfo.errMsg
        return when (returnCode) {
            OrderStatusCode.ORDER_PRODUCT_OWNED -> {
                Timber.w("you have owned this product")
                OrderStatusCode.ORDER_PRODUCT_OWNED
            }
            OrderStatusCode.ORDER_STATE_SUCCESS -> {
                val credible: Boolean = Security.verifyPurchase(
                    context
                    purchaseResultInfo.inAppPurchaseData,
                    purchaseResultInfo.inAppDataSignature
                )

                verifySignature(purchase)

                purchaseResultInfo.inAppDataSignature

                if (credible) {
                    try {
                        val inAppPurchaseData =
                            InAppPurchaseData(purchaseResultInfo.inAppPurchaseData)
                        if (inAppPurchaseData.isSubValid) {
                            return OrderStatusCode.ORDER_STATE_SUCCESS
                        }
                    } catch (e: JSONException) {
                        Timber.e(e, "parse InAppPurchaseData JSONException")
                        return OrderStatusCode.ORDER_STATE_FAILED
                    }
                } else {
                    Timber.e("check the data signature fail")
                }
                OrderStatusCode.ORDER_STATE_FAILED
            }
            else -> {
                Timber.e("returnCode: $returnCode , errMsg: $errMsg")
                returnCode
            }
        }
    }
}
