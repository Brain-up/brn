package com.epam.brn.service.load

import com.epam.brn.enums.AudiometryType
import com.epam.brn.model.Audiometry
import com.epam.brn.model.AudiometryTask
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
import org.springframework.test.util.ReflectionTestUtils

@ExtendWith(MockKExtension::class)
internal class AudiometryLoaderTest {
    @InjectMockKs
    lateinit var audiometryLoader: AudiometryLoader

    @MockK
    lateinit var audiometryTaskRepository: AudiometryTaskRepository

    @MockK
    lateinit var audiometryRepository: AudiometryRepository

    @Test
    fun `should load Initial Audiometrics with only creation`() {
        // GIVEN
        ReflectionTestUtils.setField(audiometryLoader, "createOrUpdate", false)
        ReflectionTestUtils.setField(audiometryLoader, "frequencyForDiagnostic", listOf(125, 500))
        every { audiometryRepository.saveAll(any<List<Audiometry>>()) } returns emptyList()
        val audiometryMockk = mockk<Audiometry>()
        every { audiometryRepository.findByAudiometryType(AudiometryType.SIGNALS.name) } returns listOf(audiometryMockk)
        every { audiometryTaskRepository.findByAudiometry(audiometryMockk) } returns emptyList()
        every { audiometryTaskRepository.saveAll(any<List<AudiometryTask>>()) } returns emptyList()
        // WHEN
        audiometryLoader.loadInitialAudiometricsWithTasks()
        // THEN
        verify(exactly = 1) { audiometryRepository.saveAll(any<List<Audiometry>>()) }
    }

    @Test
    fun `should load Initial Audiometrics with update on exist`() {
        // GIVEN
        ReflectionTestUtils.setField(audiometryLoader, "createOrUpdate", true)
        ReflectionTestUtils.setField(audiometryLoader, "frequencyForDiagnostic", listOf(125, 500))
        every { audiometryRepository.findByAudiometryTypeAndLocale(any(), any()) } returns null
        every { audiometryRepository.save(any()) } returns mockk()

        val audiometryMockk = mockk<Audiometry>()
        every { audiometryRepository.findByAudiometryType(AudiometryType.SIGNALS.name) } returns listOf(audiometryMockk)
        every { audiometryTaskRepository.findByAudiometry(audiometryMockk) } returns emptyList()
        every { audiometryTaskRepository.saveAll(any<List<AudiometryTask>>()) } returns emptyList()
        // WHEN
        audiometryLoader.loadInitialAudiometricsWithTasks()
        // THEN
        verify(exactly = 6) { audiometryRepository.save(any()) }
    }

    @Test
    fun `should load Initial Audiometrics with update on exist with tasks`() {
        // GIVEN
        ReflectionTestUtils.setField(audiometryLoader, "createOrUpdate", true)
        ReflectionTestUtils.setField(audiometryLoader, "frequencyForDiagnostic", listOf(125, 500))
        every { audiometryRepository.findByAudiometryTypeAndLocale(any(), any()) } returns null
        every { audiometryRepository.save(any()) } returns mockk()
        val audiometryMockk = mockk<Audiometry>()
        every { audiometryRepository.findByAudiometryType(AudiometryType.SIGNALS.name) } returns listOf(audiometryMockk)
        every { audiometryTaskRepository.findByAudiometry(audiometryMockk) } returns emptyList()
        every { audiometryTaskRepository.saveAll(any<List<AudiometryTask>>()) } returns emptyList()
        // WHEN
        audiometryLoader.loadInitialAudiometricsWithTasks()
        // THEN
        verify(exactly = 6) { audiometryRepository.save(any()) }
        verify(exactly = 1) { audiometryTaskRepository.saveAll(any<List<AudiometryTask>>()) }
    }
}
