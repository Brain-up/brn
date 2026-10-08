package com.epam.brn.upload.csv.seriesWordsKoroleva

import com.fasterxml.jackson.databind.MappingIterator
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

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
        actualIterator.shouldNotBeNull()
        val seriesWordsKorolevaRecords: List<SeriesWordsKorolevaRecord> = actualIterator.readAll()
        seriesWordsKorolevaRecords.isNotEmpty() shouldBe true
        seriesWordsKorolevaRecords[0].wordsColumns shouldBe 3
        seriesWordsKorolevaRecords[0].playWordsCount shouldBe 1
        seriesWordsKorolevaRecords[0].words[2] shouldBe "быль"
    }

    @Test
    fun isApplicable() {
        seriesWordsKorolevaRecordMappingIteratorProvider.isApplicable(SeriesWordsKorolevaRecord.FORMAT) shouldBe true
        seriesWordsKorolevaRecordMappingIteratorProvider.isApplicable("missingFormat") shouldBe false
    }
}
