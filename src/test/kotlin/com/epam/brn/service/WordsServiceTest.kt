package com.epam.brn.service

import com.epam.brn.dto.AudioFileMetaData
import com.epam.brn.enums.Voice
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

    @Test
    fun `should return default man voice for locale`() {
        wordsService.getDefaultManVoiceForLocale("ru-ru") shouldBe Voice.FILIPP.name
        wordsService.getDefaultManVoiceForLocale("en-us") shouldBe Voice.JOHN.name
    }

    @Test
    fun `should return default woman voice for locale`() {
        wordsService.getDefaultWomanVoiceForLocale("ru-ru") shouldBe Voice.ALENA.name
        wordsService.getDefaultWomanVoiceForLocale("en-us") shouldBe Voice.JANE.name
    }

    @Test
    fun `should return voice names for locale`() {
        val voices = wordsService.getVoicesForLocale("ru-ru")

        voices shouldContain Voice.FILIPP.name
        voices shouldContain Voice.MARINA.name
    }

    @Test
    fun `should find voice for matching locale`() {
        wordsService.getVoiceForLocale("ru-ru", "FILIPP") shouldBe Voice.FILIPP
    }

    @Test
    fun `should return null for voice with wrong locale`() {
        wordsService.getVoiceForLocale("en-us", "FILIPP") shouldBe null
    }

    @Test
    fun `should return null for non-existent voice`() {
        wordsService.getVoiceForLocale("ru-ru", "NONEXISTENT") shouldBe null
    }

    @Test
    fun `should throw on setDefaultVoice for unsupported locale`() {
        val ex =
            assertThrows<IllegalArgumentException> {
                wordsService.setDefaultVoiceForLocale("xx-xx", "FILIPP")
            }
        ex.message shouldContain "xx-xx"
    }

    @Test
    fun `should throw on setDefaultVoice for invalid voice`() {
        val ex =
            assertThrows<IllegalArgumentException> {
                wordsService.setDefaultVoiceForLocale("ru-ru", "NONEXISTENT")
            }
        ex.message shouldContain "NONEXISTENT"
    }

    @Test
    fun `should build sub path for word`() {
        val meta = AudioFileMetaData(text = "hello", locale = "ru-ru", voice = "FILIPP", speedFloat = "1.0")

        wordsService.getSubPathForWord(meta) shouldBe "/audio/ru-ru/filipp/1.0"
    }

    @Test
    fun `should build sub file path for word`() {
        val meta = AudioFileMetaData(text = "hello", locale = "ru-ru", voice = "FILIPP", speedFloat = "1.0")

        val path = wordsService.getSubFilePathForWord(meta)
        path shouldEndWith ".ogg"
        path shouldContain "/audio/ru-ru/filipp/1.0/"
    }

    @Test
    fun `should throw on getDefaultVoiceForLocale for unsupported locale`() {
        assertThrows<IllegalArgumentException> {
            wordsService.getDefaultVoiceForLocale("xx-xx")
        }
    }

    @Test
    fun `should build local file path for word`() {
        val field = WordsService::class.java.getDeclaredField("localFolderForFiles")
        field.isAccessible = true
        field.set(wordsService, "/tmp/test-files")

        val meta = AudioFileMetaData(text = "hello", locale = "ru-ru", voice = "FILIPP", speedFloat = "1.0")

        val path = wordsService.getLocalFilePathForWord(meta)
        path shouldContain "/tmp/test-files"
        path shouldEndWith ".ogg"
    }
}
