package org.tasks.location.support

import android.content.Context
import at.bitfire.cert4android.CustomCertManager
import at.bitfire.dav4jvm.BasicDigestAuthHandler
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.huawei.hmf.tasks.Task
import com.huawei.hms.site.api.model.Coordinate
import com.huawei.hms.site.api.model.Site
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.internal.tls.OkHostnameVerifier
import org.tasks.DebugNetworkInterceptor
import org.tasks.R
import org.tasks.caldav.MemoryCookieStore
import org.tasks.preferences.Preferences
import org.tasks.security.KeyStoreEncryption
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.net.ssl.SSLContext


class GeoCodeManager {
    private val interceptor: DebugNetworkInterceptor
    private val httpClient: OkHttpClient?
    private val httpUrl: HttpUrl?
    private val context: Context
    private val basicDigestAuthHandler: BasicDigestAuthHandler?
    private var foreground = false

    private val gson: Gson = GsonBuilder().registerTypeAdapter(
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
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
        httpClient = builder.build()
        httpUrl = url?.toHttpUrlOrNull()
    }


    val JSON = """{
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


    var reverseGeocodeResponse: ReverseGeocodeResponse? = null
    public fun reverseGeocode(lat: Double, lng: Double):  Site?  {
        var result = ""

        val location: Coordinate = Coordinate(lat, lng)

        var root = ReverseGeocodeRequest(location);
        val json = GsonBuilder().create().toJson(root)

        val url =
            "https://siteapi.cloud.huawei.com/mapApi/v1/siteService/reverseGeocode?key=" + android.net.Uri.encode(
                context.getString(R.string.huawei_key)
            )

        val request = Request.Builder()
            .get()
            .url(url)
            .build()

        if (httpClient != null) {
            httpClient.newCall(request).execute().use { response ->
                response.body

                val gson: Gson = GsonBuilder()
                    .registerTypeAdapter(
                        ReverseGeocodeResponse::class.java,
                        ReverseGeocodeResponseDeserializer()
                    )
                    .create()

                reverseGeocodeResponse = gson.fromJson(JSON, ReverseGeocodeResponse::class.java)
            }
        }

        return reverseGeocodeResponse?.sites?.firstOrNull()
    }
}