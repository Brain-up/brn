package com.epam.brn.upload.csv.audiometrySpeech

import com.fasterxml.jackson.databind.MappingIterator
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

internal class LopotkoRecordMappingIteratorProviderTest {
    private lateinit var inputStream: InputStream
    private val lopotkoRecordMappingIteratorProvider: LopotkoRecordMappingIteratorProvider =
        LopotkoRecordMappingIteratorProvider()

    @BeforeEach
    fun setUp() {
        val taskFile =
            MockMultipartFile(
                "series_words_en.csv",
                FileInputStream(
                    "src${File.separator}test${File.separator}resources${File.separator}inputData${File.separator}lopotko-record${File.separator}right_example.csv",
                ),
            )
        inputStream = taskFile.inputStream
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    operator fun iterator() {
        val actualIterator: MappingIterator<LopotkoRecord> = lopotkoRecordMappingIteratorProvider.iterator(inputStream)
        actualIterator.shouldNotBeNull()
        val lopotkoRecords: List<LopotkoRecord> = actualIterator.readAll()
        lopotkoRecords.isNotEmpty() shouldBe true
        lopotkoRecords[0].order shouldBe 1
        lopotkoRecords[0].words[2] shouldBe "быль"
    }

    @Test
    fun isApplicable() {
        lopotkoRecordMappingIteratorProvider.isApplicable(LopotkoRecord.FORMAT) shouldBe true
        lopotkoRecordMappingIteratorProvider.isApplicable("missingFormat") shouldBe false
    }
}
