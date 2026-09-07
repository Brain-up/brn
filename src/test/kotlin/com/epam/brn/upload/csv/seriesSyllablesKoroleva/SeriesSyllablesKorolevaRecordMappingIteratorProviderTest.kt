package com.epam.brn.upload.csv.seriesSyllablesKoroleva

import com.fasterxml.jackson.databind.MappingIterator
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

internal class SeriesSyllablesKorolevaRecordMappingIteratorProviderTest {
    private lateinit var inputStream: InputStream
    private val seriesSyllablesKorolevaProvider: SeriesSyllablesKorolevaRecordMappingIteratorProvider =
        SeriesSyllablesKorolevaRecordMappingIteratorProvider()

    @BeforeEach
    internal fun setUp() {
        val taskFile =
            MockMultipartFile(
                "series_syllables_en.csv",
                FileInputStream(
                    "src${File.separator}test${File.separator}resources${File.separator}inputData${File.separator}koroleva-record${File.separator}right_syllables_example.csv",
                ),
            )
        inputStream = taskFile.inputStream
    }

    @Test
    operator fun iterator() {
        val actualIterator: MappingIterator<SeriesSyllablesKorolevaRecord> =
            seriesSyllablesKorolevaProvider.iterator(inputStream)
        actualIterator.shouldNotBeNull()
        val seriesSyllablesKorolevaRecords: List<SeriesSyllablesKorolevaRecord> = actualIterator.readAll()
        seriesSyllablesKorolevaRecords.isNotEmpty() shouldBe true
        seriesSyllablesKorolevaRecords[0].wordsColumns shouldBe 3
        seriesSyllablesKorolevaRecords[0].words[2] shouldBe "быль"
    }

    @Test
    fun isApplicable() {
        seriesSyllablesKorolevaProvider.isApplicable(SeriesSyllablesKorolevaRecord.FORMAT) shouldBe true
        seriesSyllablesKorolevaProvider.isApplicable("missingFormat") shouldBe false
    }
}
