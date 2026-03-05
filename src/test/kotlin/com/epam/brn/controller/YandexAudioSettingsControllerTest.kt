package com.epam.brn.controller

import com.epam.brn.dto.request.audio.AudioVoiceOverrideRequest
import com.epam.brn.enums.Voice
import com.epam.brn.service.WordsService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class YandexAudioSettingsControllerTest {
    @InjectMockKs
    lateinit var controller: YandexAudioSettingsController

    @MockK
    lateinit var wordsService: WordsService

    @Test
    fun `should return available voices with current default`() {
        every { wordsService.getDefaultVoiceForLocale("ru-ru") } returns Voice.FILIPP.name
        every { wordsService.getAvailableVoicesForLocale("ru-ru") } returns listOf(Voice.FILIPP, Voice.MARINA)

        val response = controller.getVoices("ru-ru")

        response.statusCode.value() shouldBe 200
        response.body?.data?.defaultVoice shouldBe Voice.FILIPP.name
        response.body
            ?.data
            ?.voices
            ?.map { it.name } shouldBe listOf(Voice.FILIPP.name, Voice.MARINA.name)
        response.body
            ?.data
            ?.voices
            ?.first { it.name == Voice.FILIPP.name }
            ?.isDefault shouldBe true
    }

    @Test
    fun `should update runtime default voice`() {
        val request = AudioVoiceOverrideRequest(locale = "ru-ru", voice = Voice.MARINA.apiValue)
        every { wordsService.setDefaultVoiceForLocale("ru-ru", Voice.MARINA.apiValue) } returns Voice.MARINA
        every { wordsService.getDefaultVoiceForLocale("ru-ru") } returns Voice.MARINA.name
        every { wordsService.getAvailableVoicesForLocale("ru-ru") } returns listOf(Voice.FILIPP, Voice.MARINA)

        val response = controller.setDefaultVoice(request)

        response.statusCode.value() shouldBe 200
        response.body?.data?.defaultVoice shouldBe Voice.MARINA.name
        verify(exactly = 1) { wordsService.setDefaultVoiceForLocale("ru-ru", Voice.MARINA.apiValue) }
    }
}
