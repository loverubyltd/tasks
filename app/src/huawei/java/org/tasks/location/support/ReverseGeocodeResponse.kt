package org.tasks.location.support

import com.huawei.hms.site.api.model.Site

data class ReverseGeocodeResponse(
    val returnCode: Int,
    val sites: List<Site>,
    val returnDesc: String
)