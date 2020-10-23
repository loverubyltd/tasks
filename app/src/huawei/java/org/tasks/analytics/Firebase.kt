package org.tasks.analytics


import android.content.Context
import android.content.res.XmlResourceParser
import android.util.Log
import androidx.annotation.StringRes
import androidx.annotation.XmlRes
import androidx.core.os.bundleOf
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.crash.AGConnectCrash
import com.huawei.agconnect.remoteconfig.AGConnectConfig
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.huawei.hms.analytics.HiAnalyticsTools
import com.huawei.hms.analytics.type.HAEventType
import com.huawei.hms.analytics.type.HAParamType
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tasks.BuildConfig
import org.tasks.R
import org.tasks.billing.BillingClientImpl
import org.tasks.preferences.Preferences
import org.xmlpull.v1.XmlPullParserException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton


@Suppress("SameParameterValue")
@Singleton
class Firebase @Inject constructor(
    @param:ApplicationContext val context: Context,
    preferences: Preferences
) {

    private var crashReporter: AGConnectCrash? = null
    private var analytics: HiAnalyticsInstance? = null
    private var remoteConfig: AGConnectConfig? = null

    fun reportException(t: Throwable) {
        Timber.e(t)
        crashReporter?.setCustomKey("reportingMethod", "Firebase#reportException")
        crashReporter?.log(
            Log.WARN,
            "Reporting Exception: ${t.javaClass}"
        )
        throw t
    }

    fun reportIabResult(response: Int, sku: String?) {
        analytics?.onEvent(
            HAEventType.COMPLETEPURCHASE, bundleOf(
                HAParamType.PRODUCTID to sku,
                HAEventType.CREATEORDER to BillingClientImpl.PurchaseResponseToString(response)
            )
        )
    }

    fun updateRemoteConfig() {
        remoteConfig?.fetch()?.addOnSuccessListener {
            Timber.d(it.toString())
        }
    }

    fun logEvent(@StringRes event: Int, vararg p: Pair<Int, Any>) {
        val keyValuePairs: Array<Pair<String, Any>> = p.map { (keyResId, value) ->
            Pair(context.getString(keyResId), value)
        }.toTypedArray()

        p.mapTo([], )

        analytics?.onEvent(context.getString(event), bundleOf(*keyValuePairs))
    }

    fun noChurn(): Boolean = remoteConfig?.getValueAsBoolean("no_churn") ?: false

    init {
        if (preferences.isTrackingEnabled) {
            HiAnalyticsTools.enableLog()

            analytics = HiAnalytics.getInstance(context).apply {
                setAnalyticsEnabled(true)
            }

            crashReporter = AGConnectCrash.getInstance().apply {
                enableCrashCollection(true)
            }

            remoteConfig = AGConnectConfig.getInstance().apply {
                setDeveloperMode(BuildConfig.DEBUG)
                applyDefault(readXml2Map(R.xml.remote_config_defaults))
            }
        }
    }

    private fun readXml2Map(@XmlRes resId: Int): Map<String?, Any?>? {
        val defaultsMap: MutableMap<String?, Any?> = hashMapOf()
        val context = AGConnectInstance.getInstance().context
        try {
            val resources = context.resources ?: return defaultsMap
            val xmlParser = resources.getXml(resId)
            var curTag: String? = null
            var key: String? = null
            var value: String? = null
            var eventType = xmlParser.eventType
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                when (eventType) {
                    XmlResourceParser.START_TAG -> {
                        curTag = xmlParser.name
                    }
                    XmlResourceParser.END_TAG -> {
                        if (xmlParser.name == "entry") {
                            if (key != null && value != null) {
                                defaultsMap[key] = value
                            }
                            key = null
                            value = null
                        }
                        curTag = null
                    }
                    XmlResourceParser.TEXT -> {
                        if (curTag != null) {
                            when (curTag) {
                                "key" -> key = xmlParser.text
                                "value" -> value = xmlParser.text
                            }
                        }
                    }
                }
                eventType = xmlParser.next()
            }
        } catch (ex: XmlPullParserException) {
            Timber.e(ex, "Parsing  default values xml failed")
        } catch (ex: IOException) {
            Timber.e(ex, "parse default values xml failed")
        }
        return defaultsMap
    }
}