package com.epam.brn.controller

import com.epam.brn.dto.SeriesDto
import com.epam.brn.enums.ExerciseType
import com.epam.brn.service.SeriesService
import com.epam.brn.upload.CsvUploadService
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class SeriesControllerTest {

    @InjectMocks
    lateinit var seriesController: SeriesController

    @Mock
    lateinit var seriesService: SeriesService

    @Mock
    lateinit var csvUploadService: CsvUploadService

    @Test
    fun `should get series for group`() {
        // GIVEN
        val groupId: Long = 1
        val series1 = SeriesDto(1, 1, ExerciseType.SINGLE_SIMPLE_WORDS, "testName1", 1)
        val series2 = SeriesDto(1, 2, ExerciseType.SINGLE_SIMPLE_WORDS, "testName2", 2)
        val listSeries = listOf(series1, series2)
        Mockito.`when`(seriesService.findSeriesForGroup(groupId)).thenReturn(listSeries)
        // WHEN
        val actualResult = seriesController.getSeriesForGroup(groupId)
        // THEN
        verify(seriesService).findSeriesForGroup(groupId)
        assertTrue(actualResult.body.toString().contains("testName1"))
        assertTrue(actualResult.body.toString().contains("testName2"))
    }

    @Test
    fun `should get series for id`() {
        // GIVEN
        val seriesId: Long = 1
        val series = SeriesDto(1, seriesId, ExerciseType.SINGLE_SIMPLE_WORDS, "testName", 1)
        Mockito.`when`(seriesService.findSeriesDtoForId(seriesId)).thenReturn(series)
        // WHEN
        val actualResult = seriesController.getSeriesForId(1)
        // THEN
        verify(seriesService).findSeriesDtoForId(seriesId)
        assertTrue(actualResult.body.toString().contains("testName"))
    }
}
