package com.epam.brn.service

import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class WordsServiceTest {

    @InjectMockKs
    lateinit var wordsService: WordsService

    @Test
    fun `should create file with words`() {
        // GIVEN
//        val words = hashMapOf("girl" to "girlHex", "boy" to "boyHex", "man" to "manHex")
//        val fileName = "testWordsFile.txt"
//        // WHEN
//        val fileResult = wordsService.createTxtFilesWithExerciseWordsMap(words, fileName)
//        // THAN
//        assertTrue(fileResult.exists())
//        val expected = "man=manHex${System.lineSeparator()}girl=girlHex${System.lineSeparator()}boy=boyHex${System.lineSeparator()}"
//        assertEquals(expected, fileResult.readText())
//        fileResult.deleteOnExit()
    }
}
