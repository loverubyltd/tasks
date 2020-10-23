package org.tasks.location.support

import android.content.Context
import android.net.Uri
import at.bitfire.cert4android.CustomCertManager
import at.bitfire.dav4jvm.BasicDigestAuthHandler
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.huawei.hms.site.api.model.Coordinate
import com.huawei.hms.site.api.model.Site
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.HttpUrl.Companion.toString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.tls.OkHostnameVerifier
import okio.IOException
import org.json.JSONException
import org.tasks.DebugNetworkInterceptor
import org.tasks.R
import org.tasks.caldav.MemoryCookieStore
import org.tasks.preferences.Preferences
import org.tasks.security.KeyStoreEncryption
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.SSLContext

object Retrofititty {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}


class GeoCodeManager {
    private val interceptor: DebugNetworkInterceptor
    private val httpClient: OkHttpClient?
    private val httpUrl: HttpUrl?
    private val context: Context
    private val basicDigestAuthHandler: BasicDigestAuthHandler?
    private var foreground = false


    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(
            ReverseGeocodeResponse::class.java,
            ReverseGeocodeResponseDeserializer()
        )
        .create()

    @Inject
    internal constructor(
        @ApplicationContext context: Context,
        encryption: KeyStoreEncryption,
        preferences: Preferences,
        interceptor: DebugNetworkInterceptor
    ) {
        this.context = context
        this.interceptor = interceptor
        httpClient = null
        httpUrl = null
        basicDigestAuthHandler = null
    }

    private constructor(
        context: Context,
        interceptor: DebugNetworkInterceptor,
        url: String?,
        username: String,
        password: String,
        foreground: Boolean
    ) {
        this.context = context
        this.interceptor = interceptor
        val customCertManager = CustomCertManager(context)
        customCertManager.appInForeground = foreground
        val hostnameVerifier = customCertManager.hostnameVerifier(OkHostnameVerifier)
        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(customCertManager), null)
        basicDigestAuthHandler = BasicDigestAuthHandler(null, username, password)

        val builder = OkHttpClient()
            .newBuilder()
            .addNetworkInterceptor(basicDigestAuthHandler)
            .authenticator(basicDigestAuthHandler)
            .cookieJar(MemoryCookieStore())
            .followRedirects(false)
            .followSslRedirects(true)
            .sslSocketFactory(sslContext.socketFactory, customCertManager)
            .hostnameVerifier(hostnameVerifier)
            .connectTimeout(10000, TimeUnit.SECONDS)
            .readTimeout(10000, TimeUnit.MILLISECONDS)
        httpClient = builder.build()
        httpUrl = url?.toHttpUrlOrNull()
    }


    val JsSON = """{
   "returnCode":"0",
   "sites":[
      {
         "formatAddress":"Arapsuyu, 600. Sokak, 07070, Konyaaltı, Antalya, Türkiye",
         "address":{
            "country":"Türkiye",
            "countryCode":"TR",
            "subLocality":"Arapsuyu",
            "postalCode":"07070",
            "locality":"Konyaaltı",
            "adminArea":"Antalya",
            "subAdminArea":"Konyaaltı",
            "thoroughfare":"600. Sokak"
         },
         "viewport":{
            "southwest":{
               "lng":30.656440070240773,
               "lat":36.875268166794406
            },
            "northeast":{
               "lng":30.659619929759227,
               "lat":36.87781183320559
            }
         },
         "name":"Miniature Culture Park",
         "siteId":"6EBE1448D8235A19B9BBBCB3D3432D07",
         "location":{
            "lng":30.65803,
            "lat":36.87654
         },
         "poi":{
            "hwPoiTypes":[
               "IMPORTANT_TOURIST_ATTRACTION"
            ],
            "poiTypes":[
               "TOURIST_ATTRACTION"
            ],
            "rating":0.0,
            "internationalPhone":"",
            "openingHours":{

            },
            "hwPoiTranslatedTypes":[
               "Important Tourist Attraction"
            ]
         },
         "matchedLanguage":"tr"
      }
   ],
   "returnDesc":"OK"
}"""


    interface PlaceAutoCompleteAPI {

        @GET("api/place/autocomplete/json?types=address&key=YOUR-KEY")
        fun loadPredictions(@Query("input") address: String?): Call<ReverseGeocodeResponse?>?
    }

    var reverseGeocodeResponse: ReverseGeocodeResponse? = null

    fun reverseGeocode(lat: Double, lng: Double): Site? {
        var result = ""


        val url = REVERSE_GEOCODE_URL.format(getKey())
        val json = gson.toJson(ReverseGeocodeRequest(Coordinate(lat, lng)))
        val request = Request.Builder()
            .post(json.toRequestBody(JSON))
            .url(url)
            .build()

        if (httpClient != null) {
            try {
                httpClient.newCall(request).execute().use { response ->
                    reverseGeocodeResponse =
                        gson.fromJson(response.body,toString(), ReverseGeocodeResponse::class )
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        return reverseGeocodeResponse
    }

    private fun getKey(): String? {
        val key = context.getString(R.string.huawei_key)
        return Uri.encode(key)
    }

    companion object {
        const val REVERSE_GEOCODE_URL =
            "https://siteapi.cloud.huawei.com/mapApi/v1/siteService/reverseGeocode?key=%s"
        private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

    }
}