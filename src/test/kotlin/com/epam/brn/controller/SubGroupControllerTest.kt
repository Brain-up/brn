package com.epam.brn.controller

import com.epam.brn.dto.SubGroupDto
import com.epam.brn.service.SubGroupService
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class SubGroupControllerTest {

    @InjectMocks
    private lateinit var subGroupController: SubGroupController

    @Mock
    private lateinit var subGroupService: SubGroupService

    @Mock
    private lateinit var subGroupDto: SubGroupDto

    @Test
    fun `getAllGroups should return data when there are subGroups for seriesId`() {
        // GIVEN
        val seriesId = 1L
        `when`(subGroupService.findSubGroupsForSeries(seriesId)).thenReturn(listOf(subGroupDto))

        // WHEN
        val allGroups = subGroupController.getAllGroups(seriesId)

        // THEN
        verify(subGroupService, times(1)).findSubGroupsForSeries(seriesId)
        assertEquals(HttpStatus.SC_OK, allGroups.statusCode.value())
    }

    @Test
    fun getSeriesForId() {
        // GIVEN
        val subGroupId = 1L
        `when`(subGroupService.findById(subGroupId)).thenReturn(subGroupDto)

        // WHEN
        val seriesForId = subGroupController.getSeriesForId(subGroupId)

        // THEN
        verify(subGroupService, times(1)).findById(subGroupId)
        assertEquals(HttpStatus.SC_OK, seriesForId.statusCode.value())
    }
}
