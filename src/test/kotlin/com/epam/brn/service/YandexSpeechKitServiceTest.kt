package com.epam.brn.service

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class YandexSpeechKitServiceTest {

    @InjectMocks
    lateinit var yandexSpeechKitService: YandexSpeechKitService

    @Mock
    lateinit var wordsService: WordsService

    @ParameterizedTest
    @ValueSource(strings = ["ru-ru", "en-us", "tr-tr"])
    fun `should success pass locale validation without Exceptions`(locale: String) {
        yandexSpeechKitService.validateLocale(locale)
    }

    @ParameterizedTest
    @ValueSource(strings = ["ruru", "en-en", "tr"])
    fun `should failed on locale validation`(locale: String) {
        assertThrows<IllegalArgumentException> { yandexSpeechKitService.validateLocale(locale) }
    }
}
