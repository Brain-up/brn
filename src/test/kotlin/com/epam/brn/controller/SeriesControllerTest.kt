package com.epam.brn.controller

import com.epam.brn.dto.SeriesDto
import com.epam.brn.service.SeriesService
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions
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

    @Test
    fun `should get series for group`() {
        // GIVEN
        val groupId: Long = 1
        val series1 = SeriesDto(1, "testName", "testDescr")
        val series2 = SeriesDto(2, "testName", "testDescr")
        val listSeries = listOf(series1, series2)
        Mockito.`when`(seriesService.findSeriesForGroup(groupId)).thenReturn(listSeries)
        // WHEN
        val actualResult = seriesController.getSeriesForGroup(groupId)
        // THEN
        Assertions.assertEquals(listSeries, actualResult)
        verify(seriesService).findSeriesForGroup(groupId)
    }

    @Test
    fun `should get series for id`() {
        // GIVEN
        val seriesId: Long = 1
        val series = SeriesDto(seriesId, "testName", "testDescr")
        Mockito.`when`(seriesService.findSeriesForId(seriesId)).thenReturn(series)
        // WHEN
        val actualResult = seriesController.getSeriesForId(1, seriesId)
        // THEN
        Assertions.assertEquals(series, actualResult)
        verify(seriesService).findSeriesForId(seriesId)
    }
}