package org.tasks.location.support

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.tasks.BuildConfig
import org.tasks.HuaweiDebugNetworkInterceptor
import org.tasks.preferences.Preferences
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HuaweiSiteKitClient @Inject constructor(
    private val interceptor: HuaweiDebugNetworkInterceptor,
    private val preferences: Preferences
) {

    private val gson: Gson = GsonBuilder().create()

    internal val apiClient = Retrofit.Builder()
        .client(createHttpClient())
        .baseUrl(Companion.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    internal val geocodingService =
        apiClient.create(HuaweiSiteKitGeocodingService::class.java)

    fun reverseGeocode(body: ReverseGeocodeRequest, apiKey: String): Call<ReverseGeocodeResponse> =
        geocodingService.reverseGeocode(body, apiKey)

    private fun createHttpClient(): OkHttpClient {
        val builder = OkHttpClient().newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)

        if (preferences.isFlipperEnabled) {
            interceptor.apply(builder)
        } else if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor { message ->
                Timber.tag(Companion.TAG).d(message)
            }.apply { level = HttpLoggingInterceptor.Level.BODY }
            builder.addNetworkInterceptor(loggingInterceptor)
        }

        return builder.build()
    }


    interface HuaweiSiteKitGeocodingService {
        @POST("reverseGeocode")
        fun reverseGeocode(
            @Body req: ReverseGeocodeRequest,
            @Query("key") apiKey: String
        ): Call<ReverseGeocodeResponse>
    }

    companion object {
        private const val BASE_URL = "https://siteapi.cloud.huawei.com/mapApi/v1/siteService/"
        private const val TAG = "HuaweiSiteKitClient"
    }
}
