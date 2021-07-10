package com.epam.brn.service

import com.epam.brn.dto.AudiometryDto
import com.epam.brn.enums.AudiometryType
import com.epam.brn.enums.Locale
import com.epam.brn.model.Audiometry
import com.epam.brn.model.AudiometryTask
import com.epam.brn.repo.AudiometryHistoryRepository
import com.epam.brn.repo.AudiometryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
internal class AudiometryServiceTest {

    @InjectMockKs
    lateinit var audiometryService: AudiometryService

    @MockK
    lateinit var audiometryRepository: AudiometryRepository

    @MockK
    lateinit var audiometryTaskRepository: AudiometryTaskRepository

    @MockK
    lateinit var audiometryHistoryRepository: AudiometryHistoryRepository

    @MockK
    lateinit var userAccountService: UserAccountService

    @Test
    fun `should get audiometrics without tasks`() {
        // GIVEN
        val audiometryMock = mockk<Audiometry>()
        val audiometryDtoMock = mockk<AudiometryDto>()
        every { audiometryRepository.findByLocale(Locale.RU.locale) } returns listOf(audiometryMock)
        every { audiometryMock.toDtoWithoutTasks() } returns audiometryDtoMock

        // WHEN
        val audiometrics = audiometryService.getAudiometrics(Locale.RU.locale)

        // THEN
        verify(exactly = 1) { audiometryRepository.findByLocale(Locale.RU.locale) }
        verify(exactly = 1) { audiometryMock.toDtoWithoutTasks() }
        assertEquals(1, audiometrics.size)
        assertTrue(audiometrics.contains(audiometryDtoMock))
    }

    @Test
    fun `should get audiometry with tasks`() {
        // GIVEN
        val audiometry = Audiometry(1, Locale.RU.locale, "audiometry test", AudiometryType.SIGNALS.name)
        val audiometryTask = AudiometryTask(1, audiometry = audiometry, frequencies = "[100, 200, 300]")
        every { audiometryRepository.findById(1L) } returns Optional.of(audiometry)
        every { audiometryTaskRepository.findByAudiometry(audiometry) } returns listOf(audiometryTask)

        // WHEN
        val audiometryDto = audiometryService.getAudiometry(1L)

        // THEN
        verify(exactly = 1) { audiometryRepository.findById(1L) }
        verify(exactly = 1) { audiometryTaskRepository.findByAudiometry(audiometry) }
        assertEquals(1, audiometryDto.id)
        assertEquals(1, (audiometryDto.audiometryTasks as List<*>).size)
    }
}
