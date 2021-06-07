package com.epam.brn.config.gson.serializer

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import org.springframework.stereotype.Component
import java.lang.reflect.Type
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class LocalDateTimeJsonSerializer : TypedJsonSerializer<LocalDateTime> {
    override fun getType(): Type {
        return LocalDateTime::class.java
    }

    override fun serialize(src: LocalDateTime?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        if (src == null) {
            return JsonPrimitive("")
        }
        return JsonPrimitive(src.format(DateTimeFormatter.ISO_DATE_TIME))
    }
}
