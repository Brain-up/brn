package com.epam.brn.config.gson.serializer

import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@ExtendWith(MockKExtension::class)
internal class LocalDateTimeJsonSerializerTest {

    @InjectMockKs
    private lateinit var serializer: LocalDateTimeJsonSerializer

    @MockK
    private lateinit var context: JsonSerializationContext

    @Test
    fun `getType should return LocalDateTime type`() {
        // GIVEN
        val expectedType = LocalDateTime::class.java

        // WHEN
        val type = serializer.getType()

        // THEN
        assertEquals(expectedType, type)
    }

    @Test
    fun `serialize should return empty string when source date is null`() {
        // GIVEN
        val sourceDate = null
        val expectedSerializeValue = JsonPrimitive("")

        // WHEN
        val serializeResult = serializer.serialize(sourceDate, LocalDateTime::class.java, context)

        // THEN
        assertEquals(expectedSerializeValue, serializeResult)
    }

    @Test
    fun `serialize should return serialized date`() {
        // GIVEN
        val sourceDate = LocalDateTime.now()
        val expectedSerializeValue = JsonPrimitive(sourceDate.format(DateTimeFormatter.ISO_DATE_TIME))

        // WHEN
        val serializeResult = serializer.serialize(sourceDate, LocalDateTime::class.java, context)

        // THEN
        assertEquals(expectedSerializeValue, serializeResult)
    }
}
