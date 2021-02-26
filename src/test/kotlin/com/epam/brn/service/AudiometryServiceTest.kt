package com.epam.brn.service

import com.epam.brn.dto.AudiometryDto
import com.epam.brn.dto.AudiometrySignalsTaskDto
import com.epam.brn.enums.AudiometryType
import com.epam.brn.enums.Locale
import com.epam.brn.model.Audiometry
import com.epam.brn.model.AudiometryTask
import com.epam.brn.repo.AudiometryHistoryRepository
import com.epam.brn.repo.AudiometryRepository
import com.epam.brn.repo.AudiometryTaskRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
internal class AudiometryServiceTest {
    @Mock
    lateinit var audiometryRepository: AudiometryRepository

    @Mock
    lateinit var audiometryTaskRepository: AudiometryTaskRepository

    @Mock
    lateinit var audiometryHistoryRepository: AudiometryHistoryRepository

    @Mock
    lateinit var userAccountService: UserAccountService

    @InjectMocks
    lateinit var audiometryService: AudiometryService

    @Test
    fun `should get audiometrics without tasks`() {
        // GIVEN
        val audiometryMock = mock(Audiometry::class.java)
        val audiometryDtoMock = mock(AudiometryDto::class.java)
        `when`(audiometryRepository.findByLocale(Locale.RU.locale)).thenReturn(listOf(audiometryMock))
        `when`(audiometryMock.toDtoWithoutTasks()).thenReturn(audiometryDtoMock)
        // WHEN
        val audiometrics = audiometryService.getAudiometrics(Locale.RU.locale)
        // THEN
        assertEquals(1, audiometrics.size)
        assertTrue(audiometrics.contains(audiometryDtoMock))
    }

    @Test
    fun `should get audiometry with tasks`() {
        // GIVEN
        val audiometry = Audiometry(1, Locale.RU.locale, "audiometry test", AudiometryType.SIGNALS.name)
        val audiometryTask = AudiometryTask(1, audiometry = audiometry, frequencies = "[100, 200, 300]")
        `when`(audiometryRepository.findById(1L)).thenReturn(Optional.of(audiometry))
        `when`(audiometryTaskRepository.findByAudiometry(audiometry)).thenReturn(listOf(audiometryTask))
        // WHEN
        val audiometryDto = audiometryService.getAudiometry(1L)
        // THEN
        assertEquals(1, audiometryDto.id)
        assertEquals(1, (audiometryDto.audiometryTasks as List<AudiometrySignalsTaskDto>).size)
    }
}
