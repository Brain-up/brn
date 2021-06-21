package com.epam.brn.controller

import com.epam.brn.dto.SeriesDto
import com.epam.brn.model.ExerciseType
import com.epam.brn.service.SeriesService
import com.epam.brn.upload.CsvUploadService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SeriesControllerTest {

    @InjectMockKs
    lateinit var seriesController: SeriesController

    @MockK
    lateinit var seriesService: SeriesService

    @MockK
    lateinit var csvUploadService: CsvUploadService

    @Test
    fun `should get series for group`() {
        // GIVEN
        val groupId: Long = 1
        val series1 = SeriesDto(1, 1, ExerciseType.SINGLE_SIMPLE_WORDS, "testName1", 1)
        val series2 = SeriesDto(1, 2, ExerciseType.SINGLE_SIMPLE_WORDS, "testName2", 2)
        val listSeries = listOf(series1, series2)
        every { seriesService.findSeriesForGroup(groupId) } returns listSeries

        // WHEN
        val actualResult = seriesController.getSeriesForGroup(groupId)

        // THEN
        verify(exactly = 1) { seriesService.findSeriesForGroup(groupId) }
        assertTrue(actualResult.body.toString().contains("testName1"))
        assertTrue(actualResult.body.toString().contains("testName2"))
    }

    @Test
    fun `should get series for id`() {
        // GIVEN
        val seriesId: Long = 1
        val series = SeriesDto(1, seriesId, ExerciseType.SINGLE_SIMPLE_WORDS, "testName", 1)
        every { seriesService.findSeriesDtoForId(seriesId) } returns series

        // WHEN
        val actualResult = seriesController.getSeriesForId(1)

        // THEN
        verify(exactly = 1) { seriesService.findSeriesDtoForId(seriesId) }
        assertTrue(actualResult.body.toString().contains("testName"))
    }
}
