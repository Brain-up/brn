package com.epam.brn.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class WordsServiceTest {

    @InjectMocks
    lateinit var wordsService: WordsService

    @Test
    fun `should create file with words`() {
        // GIVEN
        val words = hashMapOf("girl" to "girlHex", "boy" to "boyHex", "man" to "manHex")
        val fileName = "testWordsFile.txt"
        // WHEN
        val fileResult = wordsService.createTxtFileWithExerciseWordsMap(words, fileName)
        // THAN
        assertTrue(fileResult.exists())
        val expected = "man=manHex${System.lineSeparator()}girl=girlHex${System.lineSeparator()}boy=boyHex${System.lineSeparator()}"
        assertEquals(expected, fileResult.readText())
        fileResult.deleteOnExit()
    }
}
