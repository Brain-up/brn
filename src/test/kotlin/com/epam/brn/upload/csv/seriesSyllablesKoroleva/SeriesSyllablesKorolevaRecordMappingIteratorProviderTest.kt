package com.epam.brn.upload.csv.seriesSyllablesKoroleva

import com.fasterxml.jackson.databind.MappingIterator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class SeriesSyllablesKorolevaRecordMappingIteratorProviderTest {

    private lateinit var inputStream: InputStream
    private val seriesSyllablesKorolevaProvider: SeriesSyllablesKorolevaRecordMappingIteratorProvider =
        SeriesSyllablesKorolevaRecordMappingIteratorProvider()

    @BeforeEach
    internal fun setUp() {
        val taskFile = MockMultipartFile(
            "series_syllables_en.csv",
            FileInputStream("src${File.separator}test${File.separator}resources${File.separator}inputData${File.separator}koroleva-record${File.separator}right_syllables_example.csv")
        )
        inputStream = taskFile.inputStream
    }

    @Test
    operator fun iterator() {
        val actualIterator: MappingIterator<SeriesSyllablesKorolevaRecord> =
            seriesSyllablesKorolevaProvider.iterator(inputStream)
        assertNotNull(actualIterator)
        val seriesSyllablesKorolevaRecords: List<SeriesSyllablesKorolevaRecord> = actualIterator.readAll()
        assertTrue(seriesSyllablesKorolevaRecords.isNotEmpty())
        assertEquals(3, seriesSyllablesKorolevaRecords[0].wordsColumns)
        assertEquals("быль", seriesSyllablesKorolevaRecords[0].words[2])
    }

    @Test
    fun isApplicable() {
        assertTrue(seriesSyllablesKorolevaProvider.isApplicable(SeriesSyllablesKorolevaRecord.FORMAT))
        assertFalse(seriesSyllablesKorolevaProvider.isApplicable("missingFormat"))
    }
}
