package com.epam.brn.controller

import com.epam.brn.csv.CsvUploadService
import com.epam.brn.dto.SeriesDto
import com.epam.brn.service.SeriesService
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
        val series1 = SeriesDto(1, 1, "testName1", "testDescr1")
        val series2 = SeriesDto(1, 2, "testName2", "testDescr2")
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
        val series = SeriesDto(1, seriesId, "testName", "testDescr")
        Mockito.`when`(seriesService.findSeriesDtoForId(seriesId)).thenReturn(series)
        // WHEN
        val actualResult = seriesController.getSeriesForId(1)
        // THEN
        verify(seriesService).findSeriesDtoForId(seriesId)
        assertTrue(actualResult.body.toString().contains("testName"))
    }
}
