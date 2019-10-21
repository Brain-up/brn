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
        val series = SeriesDto(1, "testName", "testDescr")
        val listSeries = listOf(series)
        Mockito.`when`(seriesService.findSeriesForGroup(groupId)).thenReturn(listOf(series))
        // WHEN
        val actualResult = seriesController.getSeries(groupId)
        // THEN
        Assertions.assertEquals(listSeries, actualResult)
        verify(seriesService).findSeriesForGroup(groupId)
    }
}