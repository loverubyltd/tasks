package org.tasks.location.support

import com.google.gson.annotations.JsonAdapter
import com.huawei.hms.site.api.model.Site

@JsonAdapter(ReverseGeocodeResponseDeserializer::class)
data class ReverseGeocodeResponse(
    val returnCode: Int,
    val returnDesc: String,
    val sites: List<Site>
)