package com.epam.brn.service

import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class YandexSpeechKitServiceTest {

    val yandexSpeechKitService = YandexSpeechKitService()

    @ParameterizedTest
    @ValueSource(strings = ["ru-ru", "en-us", "tr-tr"])
    fun `should success pass locale validation`(locale: String) {
        yandexSpeechKitService.validateLocale(locale)
    }

    @ParameterizedTest
    @ValueSource(strings = ["ruru", "en-en", "tr"])
    fun `should failed on locale validation`(locale: String) {
        assertThrows<IllegalArgumentException> { yandexSpeechKitService.validateLocale(locale) }
    }
}
