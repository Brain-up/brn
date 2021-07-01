package com.epam.brn.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
internal class YandexSpeechKitServiceTest {

    @InjectMockKs
    lateinit var yandexSpeechKitService: YandexSpeechKitService

    @MockK
    lateinit var wordsService: WordsService

    @MockK
    lateinit var timeService: TimeService

    @ParameterizedTest
    @ValueSource(strings = ["ru-ru", "en-us", "tr-tr"])
    fun `should success pass locale validation without Exceptions`(locale: String) {
        every { timeService.now() } returns LocalDateTime.now()
        // WHENv
        yandexSpeechKitService.validateLocale(locale)
    }

    @ParameterizedTest
    @ValueSource(strings = ["ruru", "en-en", "tr"])
    fun `should failed on locale validation`(locale: String) {
        // WHEN
        assertThrows<IllegalArgumentException> { yandexSpeechKitService.validateLocale(locale) }
    }
}
