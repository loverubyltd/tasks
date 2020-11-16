package org.tasks.location.support

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.huawei.hms.site.api.model.Site
import java.io.IOException
import java.lang.reflect.Type

//
//class ReverseGeocodeResponseAdapter : TypeAdapter<ReverseGeocodeResponse?>() {
//    @Throws(IOException::class)
//    override fun read(reader: JsonReader): ReverseGeocodeResponse? {
//        reader.beginObject()
//        var returnCode: Int? = null
//        var returnDesc: String? = null
//        val sites: MutableList<Site> = mutableListOf()
//
//        var fieldName: String? = null
//
//        while (reader.hasNext()) {
//            var token = reader.peek()
//            if (token == JsonToken.NAME) fieldName = reader.nextName()
//
//            if (fieldName == "returnCode") {
//                token = reader.peek()
//                returnCode = reader.nextInt()
//            }
//
//            if (fieldName == "returnDesc") {
//                token = reader.peek();
//                returnDesc = reader.nextString()
//            }
//
//            if (fieldName == "sites") {
//                token = reader.peek()
//                reader.beginArray()
//                val adapter =TypeAdapter.. (Site::class.java)
//                while (reader.hasNext()) {
//
//                }
//                reader.endArray()
//            }
//
//        }
//
//        reader.endObject()
//        checkNotNull(returnCode)
//        checkNotNull(returnDesc)
//        return ReverseGeocodeResponse(returnCode, returnDesc, sites)
//    }
//
//    companion object {
//        val typeToken = object : TypeToken< ReverseGeocodeResponse>() {}
//    }
//
//}


class ReverseGeocodeResponseDeserializer : JsonDeserializer<ReverseGeocodeResponse> {

//    fun deserializeAddress(jsonElement: JsonElement): AddressDetail {
//        val jsonObject = jsonElement.asJsonObject
//
//        return AddressDetail().apply {
//            countryCode = jsonObject.get("countryCode")?.asString
//            country = jsonObject.get("country")?.asString
//            adminArea = jsonObject.get("adminArea")?.asString
//            subAdminArea = jsonObject.get("subAdminArea")?.asString
//            tertiaryAdminArea = jsonObject.get("tertiaryAdminArea")?.asString
//            locality = jsonObject.get("locality")?.asString
//            subLocality = jsonObject.get("subLocality")?.asString
//            streetNumber = jsonObject.get("streetNumber")?.asString
//            thoroughfare = jsonObject.get("thoroughfare")?.asString
//            postalCode = jsonObject.get("postalCode")?.asString
//        }
//    }
//
//    fun deserializePoi(jsonElement: JsonElement): Poi {
//        val jsonObject = jsonElement.asJsonObject
//
//        return Poi().apply {
//            poiTypes = jsonObject.getAsJsonArray("poiTypes")
//                .map { it.asString }.toTypedArray()
//            hwPoiTypes = jsonObject.getAsJsonArray("hwPoiTypes").asJsonArray
//                .map { it.asString }.toTypedArray()
//            // optional parameters
//            phone = jsonObject.get("phone")?.asString
//            internationalPhone = jsonObject.get("internationalPhone")?.asString
//            jsonObject.get("rating")?.let { rating = it.asDouble }
//            websiteUrl = jsonObject.get("websiteUrl")?.asString
//            openingHours =  context.
//            photoUrls = jsonObject.getAsJsonArray("photoUrls")
//                .map { it.asString }.toTypedArray()
//            priceLevel = jsonObject.get("priceLevel")?.asString
//            businessStatus = jsonObject.get("businessStatus").asString
//            // childrenNodes = jsonObject.get("childrenNodes")
//        }
//    }
//
//    fun deserializeSite(json: JsonElement): Site {
//        val jsonObject = json.asJsonObject
//
//        return Site().apply {
//            formatAddress = jsonObject.get("formatAddress").asString
//            address = deserializeAddress(jsonObject.get("address"))
//            // viewPort = null
//            name = jsonObject.get("name").asString
//            poi = deserializePoi(jsonObject.get("poi"))
//
//        }
//    }

    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): ReverseGeocodeResponse? {
        val jsonObject = json?.asJsonObject
            ?: return null

        return ReverseGeocodeResponse(
            jsonObject.get("returnCode").asInt,
            jsonObject.get("returnDesc").asString,
            jsonObject.getAsJsonArray("sites").map {
                context.deserialize(it, Site::class.java)
            }
        )
    }

}