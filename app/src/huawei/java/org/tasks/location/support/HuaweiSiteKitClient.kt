package org.tasks.location.support

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.huawei.hms.site.api.model.Site
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import timber.log.Timber


interface HuaweiSiteKitGeocodingService {
    @POST("reverseGeocode")
    fun reverseGeocode(
        @Body req: ReverseGeocodeRequest,
        @Query("key") apiKey: String
    ): Call<ReverseGeocodeResponse>
}

object HuaweiSiteKitClient {
    private const val TAG = "HuaweiSiteKitClient"
    private const val BASE_URL =
        "https://siteapi.cloud.huawei.com/mapApi/v1/siteService/"
    private val JSON: MediaType =
        "application/json; charset=utf-8".toMediaType()

    fun apiClient(): Retrofit {
        val gson: Gson = GsonBuilder()
//        .registerTypeAdapter(
//            ReverseGeocodeResponse::class.java,
//            ReverseGeocodeResponseDeserializer()
//        )
            .create()

        val interceptor = HttpLoggingInterceptor { message ->
            Timber.tag(TAG).d(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder().addInterceptor(interceptor).build()

        return Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun geocodingService() =
        apiClient().create(HuaweiSiteKitGeocodingService::class.java)


}
