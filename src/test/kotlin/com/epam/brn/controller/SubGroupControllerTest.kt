package com.epam.brn.controller

import com.epam.brn.dto.SubGroupDto
import com.epam.brn.service.SubGroupService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SubGroupControllerTest {

    @InjectMockKs
    private lateinit var subGroupController: SubGroupController

    @MockK
    private lateinit var subGroupService: SubGroupService

    @MockK
    private lateinit var subGroupDto: SubGroupDto

    @Test
    fun `getAllGroups should return data when there are subGroups for seriesId`() {
        // GIVEN
        val seriesId = 1L
        every { subGroupService.findSubGroupsForSeries(seriesId) } returns listOf(subGroupDto)

        // WHEN
        val allGroups = subGroupController.getAllGroups(seriesId)

        // THEN
        verify(exactly = 1) { subGroupService.findSubGroupsForSeries(seriesId) }
        assertEquals(HttpStatus.SC_OK, allGroups.statusCode.value())
        assertEquals(listOf(subGroupDto), allGroups.body!!.data)
    }

    @Test
    fun `getSeriesForId should return series by subGroupId`() {
        // GIVEN
        val subGroupId = 1L
        every { subGroupService.findById(subGroupId) } returns subGroupDto

        // WHEN
        val seriesForId = subGroupController.getSeriesForId(subGroupId)

        // THEN
        verify(exactly = 1) { subGroupService.findById(subGroupId) }
        assertEquals(HttpStatus.SC_OK, seriesForId.statusCode.value())
        assertEquals(subGroupDto, seriesForId.body!!.data)
    }
}
