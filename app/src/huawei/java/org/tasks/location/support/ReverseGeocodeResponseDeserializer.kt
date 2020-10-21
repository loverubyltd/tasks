package org.tasks.location.support

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.huawei.hms.site.api.model.AddressDetail
import com.huawei.hms.site.api.model.Poi
import com.huawei.hms.site.api.model.Site
import java.lang.reflect.Type
import kotlin.jvm.Throws

class ReverseGeocodeResponseDeserializer : JsonDeserializer<ReverseGeocodeResponse> {

    fun deserializeAddress(jsonElement: JsonElement): AddressDetail {
        val jsonObject = jsonElement.asJsonObject

        return AddressDetail().apply {
            country = jsonObject.get("country").asString
            countryCode = jsonObject.get("countryCode").asString
            subLocality = jsonObject.get("subLocality").asString
            postalCode = jsonObject.get("postalCode").asString
            locality = jsonObject.get("locality").asString
            adminArea = jsonObject.get("adminArea").asString
            subAdminArea = jsonObject.get("subAdminArea").asString
            thoroughfare = jsonObject.get("thoroughfare").asString
        }
    }

    fun deserializePoi(jsonElement: JsonElement): Poi {
        val jsonObject = jsonElement.asJsonObject

        return Poi().apply {
            hwPoiTypes = jsonObject.get("hwPoiTypes").asJsonArray.map {
                it.asString
            }.toTypedArray()
            poiTypes = jsonObject.get("poiTypes").asJsonArray.map {
                it.asString
            }.toTypedArray()
            rating = jsonObject.get("ratingg").asDouble
        }
    }

    fun deserializeSite(json: JsonElement): Site {
        val jsonObject = json.asJsonObject

        return Site().apply {
            formatAddress = jsonObject.get("formatAddress").asString
            address = deserializeAddress(jsonObject.get("address"))
            // viewPort = null
            name = jsonObject.get("name").asString
            poi = deserializePoi(jsonObject.get("poi"))

        }
    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ReverseGeocodeResponse? {
        val jsonObject = json?.asJsonObject
            ?: return null
        return ReverseGeocodeResponse(
            jsonObject.get("returnCode").asInt,
            jsonObject.get("sites").asJsonArray.map {
                deserializeSite(it)
            },
            jsonObject.get("returnDesc").asString
        )
    }

}