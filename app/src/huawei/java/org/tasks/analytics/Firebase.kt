package org.tasks.analytics


import android.content.Context
import android.content.res.XmlResourceParser
import android.os.Bundle
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
        // crashReporter?.recordException(t)
    }

    fun reportIabResult(response: Int, sku: String?) {
        analytics?.onEvent(
            HAEventType.COMPLETEPURCHASE, bundleOf(
                HAParamType.PRODUCTID to sku,
                HAEventType.CREATEORDER to BillingClientImpl.BillingResponseToString(response)
            )
        )
    }

    fun updateRemoteConfig() {
        remoteConfig?.fetch()?.addOnSuccessListener {
            Timber.d(it.toString())
        }
    }

    fun logEvent(@StringRes event: Int, vararg p: Pair<Int, Any>) {
        analytics?.onEvent(context.getString(event), Bundle().apply {
            p.forEach {
                val key = context.getString(it.first)
                when (it.second::class) {
                    String::class -> putString(key, it.second as String)
                    Boolean::class -> putBoolean(key, it.second as Boolean)
                    else -> Timber.e("Unhandled param: $it")
                }
            }
        })
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
        val defaultsMap: MutableMap<String?, Any?> = HashMap()
        val context = AGConnectInstance.getInstance().context
        try {
            val resources = context.resources ?: return defaultsMap
            val xmlParser = resources.getXml(resId)
            var curTag: String? = null
            var key: String? = null
            var value: String? = null
            var eventType = xmlParser.eventType
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG) {
                    curTag = xmlParser.name
                } else if (eventType == XmlResourceParser.END_TAG) {
                    if (xmlParser.name == "entry") {
                        if (key != null && value != null) {
                            defaultsMap[key] = value
                        }
                        key = null
                        value = null
                    }
                    curTag = null
                } else if (eventType == XmlResourceParser.TEXT) {
                    if (curTag != null) {
                        if ("key" == curTag) {
                            key = xmlParser.text
                        } else if ("value" == curTag) {
                            value = xmlParser.text
                        }
                    }
                }
                eventType = xmlParser.next()
            }
        } catch (ex: XmlPullParserException) {
            Timber.e(ex, "parse default values xml failed")
        } catch (ex: IOException) {
            Timber.e(ex, "parse default values xml failed")
        }
        return defaultsMap
    }
}