package com.epam.brn.service

import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Series
import com.epam.brn.repo.SeriesRepository
import com.nhaarman.mockito_kotlin.verify
import java.io.IOException
import java.util.Optional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
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
        verify(seriesRepository).findByExerciseGroupLike(groupId)
    }

    @Test
    fun `should get series for id`() {
        // GIVEN
        val seriesId: Long = 1
        val series = mock(Series::class.java)
        `when`(seriesRepository.findById(seriesId)).thenReturn(Optional.of(series))
        // WHEN
        seriesService.findSeriesDtoForId(seriesId)
        // THEN
        verify(seriesRepository).findById(seriesId)
    }

    @Test
    fun `should not get series for id`() {
        // GIVEN
        val seriesId: Long = 1
        `when`(seriesRepository.findById(seriesId)).thenReturn(Optional.empty())
        // WHEN
        assertThrows(EntityNotFoundException::class.java) { seriesService.findSeriesDtoForId(seriesId) }
        // THEN
        verify(seriesRepository).findById(seriesId)
    }

    @Test
    fun `should get series file format`() {
        // GIVEN
        val seriesId: Long = 1
        // WHEN
        val file: String = seriesService.getSeriesUploadFileFormat(seriesId)
        // THEN
        val numLines = file.split("\n").size
        kotlin.test.assertEquals(seriesService.dataFormatNumLines, numLines)
    }

    @Test
    fun `should throw exception for missing series file`() {
        // GIVEN
        val nonExistingSeriesId: Long = Long.MAX_VALUE
        // WHEN
        val executable: () -> Unit = { seriesService.getSeriesUploadFileFormat(nonExistingSeriesId) }
        val expectedType: Class<IOException> = IOException::class.java
        val message = "should throw IO exception"
        assertThrows(
            expectedType, executable, message
        )
        // THEN
    }
}
