package com.epam.brn.service

import com.epam.brn.dto.SubGroupDto
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
    fun `findSubGroupsForSeries should return data when there are subGroups for seriesId`() {
        // GIVEN
        val subGroupMockk = mockkClass(SubGroup::class, relaxed = true)
        val seriesId = 1L
        val pictureUrl = "url/code"
        val subGroupDto = SubGroupDto(seriesId, 1L, 5, "name", pictureUrl, "description")
        every { subGroupRepository.findBySeriesId(seriesId) } returns listOf(subGroupMockk)
        every { urlConversionService.makeUrlForSubGroupPicture("code") } returns pictureUrl
        every { subGroupMockk.toDto(pictureUrl) } returns subGroupDto
        every { subGroupMockk.code } returns "code"
        // WHEN
        val allGroups = subGroupService.findSubGroupsForSeries(seriesId)

        // THEN
        verify(exactly = 1) { subGroupRepository.findBySeriesId(seriesId) }
        allGroups shouldBe listOf(subGroupDto)
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
        val subGroupDto = SubGroupDto(subGroupId, 1L, 5, "name", "url/code", "description")
        every { subGroupRepository.findById(subGroupId) } returns Optional.of(subGroupMockk)
        every { subGroupMockk.toDto("url/code") } returns subGroupDto
        every { urlConversionService.makeUrlForSubGroupPicture("code") } returns "url/code"
        every { subGroupMockk.code } returns "code"
        // WHEN
        val resultSubGroupDto = subGroupService.findById(subGroupId)

        // THEN
        verify(exactly = 1) { subGroupRepository.findById(subGroupId) }
        resultSubGroupDto shouldBe subGroupDto
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
        val subGroup = spyk(SubGroup(1, "", code = code, 2, "", seriesMockk))
        val subGroupDto = SubGroupDto(2, 2, 2, "name", pictureUrl, "description")
        every { seriesMockk.id } returns seriesId
        every { subGroup.toDto(pictureUrl) } returns subGroupDto
        every { urlConversionService.makeUrlForSubGroupPicture(code) } returns pictureUrl
        // WHEN
        val resultSubGroupDto = subGroupService.toSubGroupDto(subGroup)

        // THEN
        verify(exactly = 1) { subGroup.toDto(pictureUrl) }
        resultSubGroupDto.pictureUrl shouldBe "url/code"
    }

    @Test
    fun `addSubGroupToSeries should add new subGroup for existing series`() {
        // GIVEN
        val seriesId = 1L
        val seriesMockk = mockkClass(Series::class, relaxed = true)
        val subGroupMockk = mockkClass(SubGroup::class, relaxed = true)
        val subGroupRequest = SubGroupRequest("Test name", 1, "code", "Test description")
        every { subGroupRepository.findByNameAndLevel(subGroupRequest.name, subGroupRequest.level) } returns null
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
        every { subGroupRepository.findByNameAndLevel(subGroupRequest.name, subGroupRequest.level) } returns subGroupMockk
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
        every { subGroupRepository.findByNameAndLevel(subGroupRequest.name, subGroupRequest.level) } returns null
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
}
