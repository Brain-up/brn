package com.epam.brn.service.utility

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateSerializer : StdSerializer<LocalDate>(LocalDate::class.java) {

    @Throws(IOException::class)
    override fun serialize(
        value: LocalDate,
        generator: JsonGenerator,
        provider: SerializerProvider?
    ) {
        generator.writeString(value.format(DateTimeFormatter.ISO_LOCAL_DATE))
    }
}
