package com.epam.brn.upload.csv.seriesWordsKoroleva

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

internal class SeriesWordsKorolevaRecordMappingIteratorProviderTest {
    private lateinit var inputStream: InputStream
    private val seriesWordsKorolevaRecordMappingIteratorProvider: SeriesWordsKorolevaRecordMappingIteratorProvider =
        SeriesWordsKorolevaRecordMappingIteratorProvider()

    @BeforeEach
    internal fun setUp() {
        val taskFile =
            MockMultipartFile(
                "series_words_en.csv",
                FileInputStream(
                    "src${File.separator}test${File.separator}resources${File.separator}inputData${File.separator}koroleva-record${File.separator}right_example.csv",
                ),
            )
        inputStream = taskFile.inputStream
    }

    @Test
    operator fun iterator() {
        val actualIterator: MappingIterator<SeriesWordsKorolevaRecord> =
            seriesWordsKorolevaRecordMappingIteratorProvider.iterator(inputStream)
        assertNotNull(actualIterator)
        val seriesWordsKorolevaRecords: List<SeriesWordsKorolevaRecord> = actualIterator.readAll()
        assertTrue(seriesWordsKorolevaRecords.isNotEmpty())
        assertEquals(3, seriesWordsKorolevaRecords[0].wordsColumns)
        assertEquals(1, seriesWordsKorolevaRecords[0].playWordsCount)
        assertEquals("быль", seriesWordsKorolevaRecords[0].words[2])
    }

    @Test
    fun isApplicable() {
        assertTrue(seriesWordsKorolevaRecordMappingIteratorProvider.isApplicable(SeriesWordsKorolevaRecord.FORMAT))
        assertFalse(seriesWordsKorolevaRecordMappingIteratorProvider.isApplicable("missingFormat"))
    }
}
