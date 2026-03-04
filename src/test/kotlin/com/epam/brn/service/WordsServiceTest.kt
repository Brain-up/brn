package com.epam.brn.service

import com.epam.brn.enums.Voice
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class WordsServiceTest {
    @InjectMockKs
    lateinit var wordsService: WordsService

    @Test
    fun `should return first male voice by default`() {
        wordsService.getDefaultVoiceForLocale("ru-ru") shouldBe Voice.FILIPP.name
        wordsService.getDefaultVoiceForLocale("en-us") shouldBe Voice.JOHN.name
    }

    @Test
    fun `should allow overriding default voice at runtime`() {
        wordsService.setDefaultVoiceForLocale("ru-ru", Voice.MARINA.name)

        wordsService.getDefaultVoiceForLocale("ru-ru") shouldBe Voice.MARINA.name
    }

    @Test
    fun `should expose voices directly from enum`() {
        val voices = wordsService.getAvailableVoicesForLocale("ru-ru")

        voices.first() shouldBe Voice.FILIPP
        voices shouldContain Voice.MARINA
    }
}
