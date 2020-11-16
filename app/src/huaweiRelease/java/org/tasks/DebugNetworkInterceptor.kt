package org.tasks

import com.huawei.cloud.base.http.HttpRequest
import com.huawei.cloud.base.http.HttpResponse
import okhttp3.OkHttpClient
import javax.inject.Inject

class DebugNetworkInterceptor @Inject constructor() {
    fun add(builder: OkHttpClient.Builder?) {}
    fun <T> execute(httpRequest: HttpRequest?, responseClass: Class<T>?): T? = null
    fun <T> report(httpResponse: HttpResponse?, responseClass: Class<T>?, start: Long, finish: Long): T? = null
}