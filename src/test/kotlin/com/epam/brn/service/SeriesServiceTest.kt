package com.epam.brn.service

import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class SeriesServiceTest {
    @InjectMocks
    lateinit var seriesService: SeriesService
    @Mock
    lateinit var seriesRepository: SeriesRepository

    @Test
    fun `should get series for group`() {
        // GIVEN
        val groupId: Long = 1
        val series = mock(Series::class.java)
        val listSeries = listOf(series)
        `when`(seriesRepository.findByExerciseGroupLike(groupId)).thenReturn(listOf(series))
        // WHEN
        val actualResult = seriesService.findSeriesForGroup(groupId)
        // THEN
        val expectedResult = listSeries.map { seriesEntry -> seriesEntry.toDto() }
        assertEquals(expectedResult, actualResult)
    }
}