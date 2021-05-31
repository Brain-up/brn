package com.epam.brn.service.date

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
internal class ISODateTimeToLocalDateConverterTest {

    @InjectMockKs
    private lateinit var converter: ISODateTimeToLocalDateConverter

    @Test
    fun `convert should convert ISO date time to LocalDate`() {
        // GIVEN
        val dateTime = LocalDateTime.now()
        val formattedDateTime = dateTime.format(DateTimeFormatter.ISO_DATE_TIME)

        // WHEN
        val result = converter.convert(formattedDateTime)

        // THEN
        assertEquals(dateTime.toLocalDate(), result)
    }

    @Test
    fun `convert should return null when source is empty`() {
        // GIVEN
        val format = ""

        // WHEN
        val result = converter.convert(format)

        // THEN
        assertNull(result)
    }

    @Test
    fun `convert should throw DateTimeParseException when can not parse the date`() {
        // GIVEN
        val formattedDateTime = "11.02-2020"

        // THEN
        assertThrows<DateTimeParseException> { converter.convert(formattedDateTime) }
    }
}
