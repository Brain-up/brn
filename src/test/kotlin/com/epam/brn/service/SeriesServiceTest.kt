package com.epam.brn.service

import com.epam.brn.dto.SeriesDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.model.Series
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class SeriesServiceTest {

    @InjectMockKs
    lateinit var seriesService: SeriesService

    @MockK
    lateinit var seriesRepository: SeriesRepository

    @Test
    fun `should get series for group`() {
        // GIVEN
        val groupId: Long = 1
        val series = mockk<Series>()
        val seriesDto = mockk<SeriesDto>()
        val listSeries = listOf(series)
        val expectedResult = listOf(seriesDto)
        every { seriesRepository.findByExerciseGroupLike(groupId) } returns listSeries
        every { series.toDto() } returns seriesDto

        // WHEN
        val actualResult = seriesService.findSeriesForGroup(groupId)

        // THEN
        verify(exactly = 1) { seriesRepository.findByExerciseGroupLike(groupId) }
        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `should get series for id`() {
        // GIVEN
        val seriesId: Long = 1
        val series = mockk<Series>()
        val seriesDto = mockk<SeriesDto>()
        every { seriesRepository.findById(seriesId) } returns Optional.of(series)
        every { series.toDto() } returns seriesDto

        // WHEN
        seriesService.findSeriesDtoForId(seriesId)

        // THEN
        verify(exactly = 1) { seriesRepository.findById(seriesId) }
    }

    @Test
    fun `should not get series for id`() {
        // GIVEN
        val seriesId: Long = 1
        every { seriesRepository.findById(seriesId) } returns Optional.empty()

        // WHEN
        assertThrows(EntityNotFoundException::class.java) { seriesService.findSeriesDtoForId(seriesId) }

        // THEN
        verify(exactly = 1) { seriesRepository.findById(seriesId) }
    }
}
