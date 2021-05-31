package com.epam.brn.service.date

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.jvm.Throws

@Component
class ISODateTimeToLocalDateConverter : Converter<String, LocalDate> {
    @Throws(DateTimeParseException::class)
    override fun convert(source: String): LocalDate? {
        if (source.isEmpty())
            return null
        val localDateTime = LocalDateTime.parse(source, DateTimeFormatter.ISO_DATE_TIME)
        return localDateTime.toLocalDate()
    }
}
