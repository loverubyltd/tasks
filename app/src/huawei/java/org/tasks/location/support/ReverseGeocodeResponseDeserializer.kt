package org.tasks.location.support

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.huawei.hms.site.api.model.Site
import java.lang.reflect.Type

class ReverseGeocodeResponseDeserializer : JsonDeserializer<ReverseGeocodeResponse> {
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
