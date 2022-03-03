package com.epam.brn.service

import org.assertj.core.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class WordAnalyzingServiceTest {
    private val wordAnalyzingService = WordAnalyzingService()

    @ParameterizedTest
    @ValueSource(strings = ["мышь", "кот", "смрад"])
    fun `should find Syllable 1 Count`(word: String) {
        Assertions.assertThat(wordAnalyzingService.findSyllableCount(word)).isEqualTo(1)
    }

    @ParameterizedTest
    @ValueSource(strings = ["мышка", "кошка", "муан", "портфель"])
    fun `should find Syllable 2 Count`(word: String) {
        Assertions.assertThat(wordAnalyzingService.findSyllableCount(word)).isEqualTo(2)
    }

    @ParameterizedTest
    @ValueSource(strings = ["машина", "королёв", "моошка"])
    fun `should find Syllable 3 Count`(word: String) {
        Assertions.assertThat(wordAnalyzingService.findSyllableCount(word)).isEqualTo(3)
    }
}
