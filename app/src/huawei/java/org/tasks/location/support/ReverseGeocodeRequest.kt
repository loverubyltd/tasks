package org.tasks.location.support

import com.huawei.hms.site.api.model.Coordinate

data class ReverseGeocodeRequest @JvmOverloads constructor(
    val location: Coordinate,
    val language: String? = null,
    val politicalView : String? = null, //  a two-letter country/region code complying with ISO 3166-1 alpha-2.
    val returnPoi: Boolean? = true
    )