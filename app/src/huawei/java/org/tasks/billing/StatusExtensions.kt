package org.tasks.billing

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import com.huawei.hms.support.api.client.Status

object FIeldHelper {
    inline fun <K, reified T> getFieldValue(javaClazz: Class<K>, fieldName: String): T? {
        val field = javaClass.getDeclaredField(fieldName).also { it.isAccessible = true }
        return field.get(javaClass) as? T?
    }
}


fun Status.getPendingIntent(): PendingIntent? {
    return FIeldHelper.getFieldValue(javaClass, "pendingIntent")
}

fun Status.getIntent(): Intent? {
    return FIeldHelper.getFieldValue(javaClass, "intent")
}


@Throws(IntentSender.SendIntentException::class)
fun Status.startResolutionForResult2(activity: Activity, resultCode: Int) {
    if (hasResolution()) {
        val pendingIntent = getPendingIntent()
        if (pendingIntent != null) {
            activity.startIntentSenderForResult(
                pendingIntent.intentSender,
                resultCode,
                null as Intent?,
                0,
                0,
                0
            )
        } else {
            activity.startActivityForResult(getIntent(), resultCode)
        }
    }
}

//@Throws(IntentSender.SendIntentException::class)
//fun Status.startResolutionForResult23(activity: Activity, resultCode: Int) {
//    if (hasResolution()) {
//        // val pendingIntentProperty = this::pendingIntent
//        val pendingIntentField = javaClass.getDeclaredField("pendingIntent").also {
//            it.isAccessible = true
//        }
//        val intentField = javaClass.getDeclaredField("pendingIntent").also {
//            it.isAccessible = true
//        }
//        val pendingIntent = pendingIntentField.get(javaClass) as? PendingIntent
//        val intent = intentField.get(javaClass) as? Intent
//
//        if (pendingIntent != null) {
//
//           val request =  IntentSenderRequest.Builder(pendingIntent.intentSender)
//               .build()
//
//
//
//             (activity as PurchaseActivity).observer.purchaseThing(request)
//
//
//        } else {
//            activity.startActivityForResult(intent, resultCode)
//        }
//    }
//}