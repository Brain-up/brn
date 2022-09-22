package com.epam.brn.controller

import com.epam.brn.dto.request.SubGroupChangeRequest
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.dto.response.SubGroupResponse
import com.epam.brn.service.SubGroupService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockkClass
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

    @Test
    fun `addSubGroupToSeries should return http status 204`() {
        // GIVEN
        val seriesId = 1L
        val subGroupRequest = SubGroupRequest("Test name", 1, "shortWords", "Test description")
        val subGroupResponse = mockkClass(SubGroupResponse::class, relaxed = true)

        every { subGroupService.addSubGroupToSeries(subGroupRequest, seriesId) } returns subGroupResponse

        // WHEN
        val createdSubGroup = subGroupController.addSubGroupToSeries(seriesId, subGroupRequest)

        // THEN
        createdSubGroup.statusCodeValue shouldBe HttpStatus.SC_CREATED
    }

    @Test
    fun `updateSubGroupById should update subGroup by subGroupId`() {
        // GIVEN
        val subGroupId = 1L
        val subGroupChangeRequest = SubGroupChangeRequest(withPictures = true)
        val updatedSubGroup = mockkClass(SubGroupResponse::class, relaxed = true)
        every { subGroupService.updateSubGroupById(subGroupId, subGroupChangeRequest) } returns updatedSubGroup

        // WHEN
        val actual = subGroupController.updateSubGroupById(subGroupId, subGroupChangeRequest)

        // THEN
        actual.statusCode.value() shouldBe HttpStatus.SC_OK
        actual.body!!.data shouldBe updatedSubGroup
    }
}
