package org.tasks.location.support

import com.huawei.hms.site.api.model.Site
import org.tasks.data.Place

fun Site.toPlace(): Place = Place.newPlace().also { place ->
    place.name = name
    place.address = formatAddress
    place.phone = poi.phone
    place.url = poi.websiteUrl
    place.latitude = location.lat
    place.longitude = location.lng
}