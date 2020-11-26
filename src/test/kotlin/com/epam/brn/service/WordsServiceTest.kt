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
        val words = hashSetOf<String>("girl", "boy", "man")
        val fileName = "testWordsFile.txt"
        // WHEN
        val file = wordsService.createTxtFileWithExerciseWords(words, fileName)
        // THAN
        assertTrue(file.exists())
        assertEquals("girl,boy,man", file.readText())
        file.deleteOnExit()
    }
}
