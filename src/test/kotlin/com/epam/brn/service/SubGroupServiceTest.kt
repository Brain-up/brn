package com.epam.brn.service

import com.epam.brn.dto.response.SubGroupResponse
import com.epam.brn.dto.request.SubGroupChangeRequest
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockkClass
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class SubGroupServiceTest {

    @InjectMockKs
    private lateinit var subGroupService: SubGroupService

    @MockK
    private lateinit var seriesRepository: SeriesRepository

    @MockK
    private lateinit var subGroupRepository: SubGroupRepository

    @MockK
    private lateinit var exerciseRepository: ExerciseRepository

    @MockK
    private lateinit var urlConversionService: UrlConversionService

    @Test
    fun `findSubGroupsForSeries should return sorted data when there are subGroups for seriesId`() {
        // GIVEN
        val subGroupMockk = mockkClass(SubGroup::class, relaxed = true)
        val subGroupMockkWithPictures1 = mockkClass(SubGroup::class, relaxed = true)
        val subGroupMockkWithPictures2 = mockkClass(SubGroup::class, relaxed = true)
        val seriesId = 1L
        val pictureUrl = "url/code"
        val subGroupResponse = SubGroupResponse(seriesId, 1L, 5, "name", pictureUrl, "description", false)
        val subGroupResponseWithPictures1 = SubGroupResponse(seriesId, 2L, 1, "name", pictureUrl, "description", true)
        val subGroupResponseWithPictures2 = SubGroupResponse(seriesId, 2L, 2, "name", pictureUrl, "description", true)
        every { subGroupRepository.findBySeriesId(seriesId) } returns listOf(subGroupMockk, subGroupMockkWithPictures2, subGroupMockkWithPictures1)
        every { urlConversionService.makeUrlForSubGroupPicture("code") } returns pictureUrl
        every { subGroupMockk.toResponse(pictureUrl) } returns subGroupResponse
        every { subGroupMockkWithPictures1.toResponse(pictureUrl) } returns subGroupResponseWithPictures1
        every { subGroupMockkWithPictures2.toResponse(pictureUrl) } returns subGroupResponseWithPictures2
        every { subGroupMockk.code } returns "code"
        every { subGroupMockkWithPictures1.code } returns "code"
        every { subGroupMockkWithPictures2.code } returns "code"
        // WHEN
        val allGroups = subGroupService.findSubGroupsForSeries(seriesId)
        // THEN
        verify(exactly = 1) { subGroupRepository.findBySeriesId(seriesId) }
        allGroups shouldBe listOf(subGroupResponseWithPictures1, subGroupResponseWithPictures2, subGroupResponse)
    }

    @Test
    fun `deleteSubGroupById should delete subGroup without exercises`() {
        // GIVEN
        val subGroupId = 1L
        every { subGroupRepository.existsById(subGroupId) } returns true
        every { exerciseRepository.existsBySubGroupId(subGroupId) } returns false
        justRun { subGroupRepository.deleteById(subGroupId) }
        // WHEN
        subGroupService.deleteSubGroupById(subGroupId)
        // THEN
        verify(exactly = 1) { exerciseRepository.existsBySubGroupId(subGroupId) }
    }

    @Test
    fun `deleteSubGroupById should throw an exception with exercises in subGroup`() {
        // GIVEN
        val subGroupId = 1L
        every { subGroupRepository.existsById(subGroupId) } returns true
        every { exerciseRepository.existsBySubGroupId(subGroupId) } returns true
        justRun { subGroupRepository.deleteById(subGroupId) }
        // THEN
        shouldThrow<IllegalArgumentException> {
            subGroupService.deleteSubGroupById(subGroupId)
        }
    }

    @Test
    fun `deleteSubGroupById should throw an exception when subGroup is not found`() {
        // GIVEN
        val subGroupId = 1L
        every { subGroupRepository.existsById(subGroupId) } returns false
        // THEN
        shouldThrow<IllegalArgumentException> {
            subGroupService.deleteSubGroupById(subGroupId)
        }
    }

    @Test
    fun `findById should return subGroupDto when subGroup with id found`() {
        // GIVEN
        val subGroupMockk = mockkClass(SubGroup::class, relaxed = true)
        val subGroupId = 1L
        val subGroupResponse = SubGroupResponse(subGroupId, 1L, 5, "name", "url/code", "description", false)
        every { subGroupRepository.findById(subGroupId) } returns Optional.of(subGroupMockk)
        every { subGroupMockk.toResponse("url/code") } returns subGroupResponse
        every { urlConversionService.makeUrlForSubGroupPicture("code") } returns "url/code"
        every { subGroupMockk.code } returns "code"
        // WHEN
        val resultSubGroupDto = subGroupService.findById(subGroupId)
        // THEN
        verify(exactly = 1) { subGroupRepository.findById(subGroupId) }
        resultSubGroupDto shouldBe subGroupResponse
    }

    @Test
    fun `findById should trow exception when subGroup not found`() {
        // GIVEN
        val subGroupId = 1L
        every { subGroupRepository.findById(subGroupId) } returns Optional.empty()
        // THEN
        shouldThrow<EntityNotFoundException> { subGroupService.findById(subGroupId) }
    }

    @Test
    fun `should return subgroup dto with picture url`() {
        // GIVEN
        val code = "code"
        val pictureUrl = "url/code"
        val seriesMockk = mockkClass(Series::class)
        val seriesId = 1L
        val subGroup = spyk(SubGroup(1, "", code = code, 2, "", false, seriesMockk))
        val subGroupResponse = SubGroupResponse(2, 2, 2, "name", pictureUrl, "description", false)
        every { seriesMockk.id } returns seriesId
        every { subGroup.toResponse(pictureUrl) } returns subGroupResponse
        every { urlConversionService.makeUrlForSubGroupPicture(code) } returns pictureUrl
        // WHEN
        val resultSubGroupDto = subGroupService.toSubGroupResponse(subGroup)
        // THEN
        verify(exactly = 1) { subGroup.toResponse(pictureUrl) }
        resultSubGroupDto.pictureUrl shouldBe "url/code"
    }

    @Test
    fun `addSubGroupToSeries should add new subGroup for existing series`() {
        // GIVEN
        val seriesId = 1L
        val seriesMockk = mockkClass(Series::class, relaxed = true)
        val subGroupMockk = mockkClass(SubGroup::class, relaxed = true)
        val subGroupRequest = SubGroupRequest("Test name", 1, "code", "Test description")
        every { subGroupRepository.findByNameAndLevel(subGroupRequest.name, subGroupRequest.level!!) } returns null
        every { seriesRepository.findById(seriesId) } returns Optional.of(seriesMockk)
        every { subGroupRepository.save(subGroupRequest.toModel(seriesMockk)) } returns subGroupMockk
        every { subGroupMockk.code } returns "code"
        every { urlConversionService.makeUrlForSubGroupPicture("code") } returns "url/code"
        // WHEN
        subGroupService.addSubGroupToSeries(seriesId = seriesId, subGroupRequest = subGroupRequest)
        // THEN
        verify(exactly = 1) { subGroupRepository.save(subGroupRequest.toModel(seriesMockk)) }
    }

    @Test
    fun `addSubGroupToSeries should trow exception when subGroup is exists`() {
        // GIVEN
        val seriesId = 1L
        val subGroupMockk = mockkClass(SubGroup::class, relaxed = true)
        val subGroupRequest = SubGroupRequest("Test name", 1, "code", "Test description")
        every { subGroupRepository.findByNameAndLevel(subGroupRequest.name, subGroupRequest.level!!) } returns subGroupMockk
        every { subGroupMockk.code } returns "code"
        every { urlConversionService.makeUrlForSubGroupPicture("code") } returns "url/shortWords"
        // THEN
        shouldThrow<IllegalArgumentException> {
            subGroupService.addSubGroupToSeries(
                seriesId = seriesId,
                subGroupRequest = subGroupRequest
            )
        }
    }

    @Test
    fun `addSubGroupToSeries should trow exception when series does not exist`() {
        // GIVEN
        val seriesId = 1L
        val subGroupRequest = SubGroupRequest("Test name", 1, "shortWords", "Test description")
        every { subGroupRepository.findByNameAndLevel(subGroupRequest.name, subGroupRequest.level!!) } returns null
        every { seriesRepository.findById(seriesId) } returns Optional.empty()
        every { urlConversionService.makeUrlForSubGroupPicture("shortWords") } returns "url/shortWords"
        // THEN
        shouldThrow<EntityNotFoundException> {
            subGroupService.addSubGroupToSeries(
                seriesId = seriesId,
                subGroupRequest = subGroupRequest
            )
        }
    }

    @Test
    fun`updateSubGroupById should update existing subgroup`() {
        // GIVEN
        val subGroupId = 1L
        val subGroupChangeRequest = SubGroupChangeRequest(withPictures = true)
        val subGroupMockk = mockkClass(SubGroup::class, relaxed = true)
        val subGroupResponseMockk = mockkClass(SubGroupResponse::class, relaxed = true)
        every { subGroupRepository.findById(subGroupId) } returns Optional.of(subGroupMockk)
        every { subGroupRepository.save(subGroupMockk) } returns subGroupMockk
        every { subGroupMockk.code } returns "code"
        every { urlConversionService.makeUrlForSubGroupPicture("code") } returns "someUrl"
        every { subGroupMockk.toResponse("someUrl") } returns subGroupResponseMockk
        // THEN
        subGroupService.updateSubGroupById(subGroupId, subGroupChangeRequest) shouldBe subGroupResponseMockk
    }

    @Test
    fun`updateSubGroupById should throw exception when subgroup does not exist`() {
        // GIVEN
        val subGroupId = 1L
        every { subGroupRepository.findById(subGroupId) } returns Optional.empty()
        // THEN
        shouldThrow<EntityNotFoundException> {
            subGroupService.updateSubGroupById(subGroupId, SubGroupChangeRequest(withPictures = false))
        }
    }
}
