package org.tasks.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import org.tasks.BuildConfig
import androidx.annotation.StringRes
import com.huawei.agconnect.crash.AGConnectCrash
import com.huawei.agconnect.remoteconfig.AGConnectConfig
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.huawei.hms.analytics.HiAnalyticsTools
import dagger.hilt.android.qualifiers.ApplicationContext
import org.tasks.R
import org.tasks.preferences.Preferences
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Firebase @Inject constructor(
        @param:ApplicationContext val context: Context,
        preferences: Preferences
) {

    private var agConnectCrash: AGConnectCrash? = null
    private var hiAnalytics: HiAnalyticsInstance? = null
    private var agConnectConfig: AGConnectConfig? = null

    fun reportException(t: Throwable) {
        Timber.e(t)
        agConnectCrash?.setCustomKey("reportingMethod", "Firebase#reportException")
        agConnectCrash?.log(Log.WARN, "Reporting ${t.javaClass}")
        throw t
    }

    fun updateRemoteConfig() {
        agConnectConfig?.fetch()?.addOnSuccessListener {
            Timber.d(it.toString())
        }
    }

    fun logEvent(@StringRes event: Int, vararg p: Pair<Int, Any>) {
        hiAnalytics?.onEvent(context.getString(event), Bundle().apply {
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

    fun noChurn(): Boolean = agConnectConfig?.getValueAsBoolean("no_churn") ?: false

    init {
        if (preferences.isTrackingEnabled) {
            HiAnalyticsTools.enableLog()
            hiAnalytics = HiAnalytics.getInstance(context).apply {
                setAnalyticsEnabled(true)
            }
            agConnectCrash = AGConnectCrash.getInstance().apply {
                enableCrashCollection(true)
            }
            agConnectConfig = AGConnectConfig.getInstance().apply {
                setDeveloperMode(BuildConfig.DEBUG)
                applyDefault(R.xml.remote_config_defaults)
            }
        }
    }
}
