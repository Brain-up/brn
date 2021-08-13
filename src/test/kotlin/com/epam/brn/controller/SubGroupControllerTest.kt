package com.epam.brn.controller

import com.epam.brn.dto.SubGroupResponse
import com.epam.brn.service.SubGroupService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class SubGroupControllerTest {

    @InjectMockKs
    private lateinit var subGroupController: SubGroupController

    @MockK
    private lateinit var subGroupService: SubGroupService

    @MockK
    private lateinit var subGroupResponse: SubGroupResponse

    @Test
    fun `getAllGroups should return data when there are subGroups for seriesId`() {
        // GIVEN
        val seriesId = 1L
        every { subGroupService.findSubGroupsForSeries(seriesId) } returns listOf(subGroupResponse)

        // WHEN
        val allGroups = subGroupController.getAllGroups(seriesId)

        // THEN
        verify(exactly = 1) { subGroupService.findSubGroupsForSeries(seriesId) }
        allGroups.statusCode.value() shouldBe HttpStatus.SC_OK
        allGroups.body!!.data shouldBe listOf(subGroupResponse)
    }

    @Test
    fun `getSeriesForId should return series by subGroupId`() {
        // GIVEN
        val subGroupId = 1L
        every { subGroupService.findById(subGroupId) } returns subGroupResponse

        // WHEN
        val seriesForId = subGroupController.getSeriesForId(subGroupId)

        // THEN
        verify(exactly = 1) { subGroupService.findById(subGroupId) }
        seriesForId.statusCode.value() shouldBe HttpStatus.SC_OK
        seriesForId.body!!.data shouldBe subGroupResponse
    }
    @Test
    fun `deleteSubGroupById should delete subGroup by subGroupId`() {
        // GIVEN
        val subGroupId = 1L
        justRun { subGroupService.deleteSubGroupById(subGroupId) }

        // WHEN
        val deleteSubGroupForId = subGroupController.deleteSubGroupById(subGroupId)

        // THEN
        verify(exactly = 1) { subGroupService.deleteSubGroupById(subGroupId) }
        deleteSubGroupForId.statusCode.value() shouldBe HttpStatus.SC_OK
        deleteSubGroupForId.body!!.data shouldBe Unit
    }
}
