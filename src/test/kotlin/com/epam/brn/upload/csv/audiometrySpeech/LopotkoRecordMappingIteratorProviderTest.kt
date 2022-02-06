package com.epam.brn.upload.csv.audiometrySpeech

import com.epam.brn.upload.csv.seriesWords.LopotkoRecord
import com.epam.brn.upload.csv.seriesWords.LopotkoRecordMappingIteratorProvider
import com.fasterxml.jackson.databind.MappingIterator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class LopotkoRecordMappingIteratorProviderTest {

    private lateinit var inputStream: InputStream
    private val lopotkoRecordMappingIteratorProvider: LopotkoRecordMappingIteratorProvider =
        LopotkoRecordMappingIteratorProvider()

    @BeforeEach
    fun setUp() {
        inputStream = BufferedInputStream(
            FileInputStream("src/test/resources/inputData.lopotko-record/right_example.csv")
        )
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    operator fun iterator() {
        val actualIterator: MappingIterator<LopotkoRecord> = lopotkoRecordMappingIteratorProvider.iterator(inputStream)
        assertNotNull(actualIterator)
        val lopotkoRecords: List<LopotkoRecord> = actualIterator.readAll()
        assertTrue(lopotkoRecords.isNotEmpty())
        assertEquals(1, lopotkoRecords[0].order)
        assertEquals("быль", lopotkoRecords[0].words[2])
    }

    @Test
    fun isApplicable() {
        assertTrue(lopotkoRecordMappingIteratorProvider.isApplicable(LopotkoRecord.FORMAT))
        assertFalse(lopotkoRecordMappingIteratorProvider.isApplicable("missingFormat"))
    }
}
