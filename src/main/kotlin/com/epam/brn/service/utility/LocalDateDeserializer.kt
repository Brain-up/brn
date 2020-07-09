package com.epam.brn.service.utility

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.exc.InvalidFormatException
import java.io.IOException
import java.time.LocalDate

class LocalDateDeserializer : StdDeserializer<LocalDate>(LocalDate::class.java) {

    @Throws(IOException::class)
    override fun deserialize(parser: JsonParser, context: DeserializationContext?): LocalDate? {
        val date: String = parser.text
        throw InvalidFormatException.from(
            parser,
            LocalDateDeserializer::class.java,
            "Expected a field containing a list"
        )
        return LocalDate.parse(parser.readValueAs(String::class.java))
        // try {
        // LocalDate.parse(parser.readValueAs(String::class.java))
        // } catch (e: InvalidFormatException) {

        // )
        // }
    }
}
