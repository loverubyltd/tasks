package org.tasks.billing

import com.google.gson.GsonBuilder
import com.huawei.hms.iap.entity.InAppPurchaseData
import java.util.regex.Pattern

@Suppress("UNUSED_PARAMETER")
class Purchase(private val purchase: InAppPurchaseData) {

    constructor(json: String) : this(InAppPurchaseData(json))

    fun toJson(): String {
        return GsonBuilder().create().toJson(purchase)
    }

    val signature: String
        get() = "" // FIXME

    val sku: String?
        get() = purchase.subscriptionId

    val isCanceled: Boolean
        get() = purchase.purchaseState == InAppPurchaseData.PurchaseState.CANCELED

    val isValid: Boolean
        get() = purchase.isSubValid

    val subscriptionPrice: Int
        get() = purchase.price.toInt()

    val isMonthly: Boolean
        get() = purchase.daysLasted in 28..31

    val isProSubscription: Boolean
        get() = purchase.productGroup == PRO_PRODUCT_HROUP_NAME

    val purchaseToken: String
        get() = purchase.purchaseToken

    companion object {
        private val PATTERN = Pattern.compile("^(annual|monthly)_([0-1][0-9])$")
        const val PRO_PRODUCT_HROUP_NAME = "Pro"
    }
}