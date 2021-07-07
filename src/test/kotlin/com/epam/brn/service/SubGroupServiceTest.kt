package com.epam.brn.service

import com.epam.brn.dto.SubGroupDto
import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import java.lang.IllegalArgumentException
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class SubGroupServiceTest {

    @InjectMockKs
    private lateinit var subGroupService: SubGroupService

    @MockK
    private lateinit var subGroupRepository: SubGroupRepository

    @MockK
    private lateinit var subGroup: SubGroup

    @MockK
    private lateinit var seriesRepository: SeriesRepository

    @Test
    fun `findSubGroupsForSeries should return data when there are subGroups for seriesId`() {
        // GIVEN
        val seriesId = 1L
        val pictureTheme = "pictureTheme"
        ReflectionTestUtils.setField(subGroupService, "pictureTheme", pictureTheme)
        val subGroupDto = SubGroupDto(seriesId, 1L, 5, "name", "pictureURL", "description")
        mockkStatic("com.epam.brn.service.SubGroupServiceKt")
        every { subGroupRepository.findBySeriesId(seriesId) } returns listOf(subGroup)
        every { subGroup.toDto(pictureTheme) } returns subGroupDto

        // WHEN
        val allGroups = subGroupService.findSubGroupsForSeries(seriesId)

        // THEN
        verify(exactly = 1) { subGroupRepository.findBySeriesId(seriesId) }
        assertEquals(listOf(subGroupDto), allGroups)
    }

    @Test
    fun `findById should return group when subGroup with id found`() {
        // GIVEN
        val subGroupId = 1L
        val pictureTheme = "pictureTheme"
        ReflectionTestUtils.setField(subGroupService, "pictureTheme", pictureTheme)
        val subGroupDto = SubGroupDto(subGroupId, 1L, 5, "name", "pictureURL", "description")
        mockkStatic("com.epam.brn.service.SubGroupServiceKt")
        every { subGroupRepository.findById(subGroupId) } returns Optional.of(subGroup)
        every { subGroup.toDto(pictureTheme) } returns subGroupDto

        // WHEN
        val group = subGroupService.findById(subGroupId)

        // THEN
        assertEquals(subGroupDto, group)
    }

    @Test
    fun `findById should trow exception when subGroup not found`() {
        // GIVEN
        val subGroupId = 1L
        every { subGroupRepository.findById(subGroupId) } returns Optional.empty()
        // THEN
        assertThrows<EntityNotFoundException> { subGroupService.findById(subGroupId) }
    }

    @Test
    fun `should return dto for url`() {
        // GIVEN
        val pictureUrl = "url"
        val pictureUrlTemplate = "template %s"
        val seriesMockk = mockkClass(Series::class)
        val seriesId = 1L
        val subGroup = spyk(SubGroup(1, "", pictureUrl, 2, "", seriesMockk))
        val subGroupDto = SubGroupDto(2, 2, 2, "name", pictureUrl, "description")
        every { seriesMockk.id } returns seriesId
        every { subGroup.toDto() } returns subGroupDto

        // WHEN
        val resultDto = subGroup.toDto(pictureUrlTemplate)

        // THEN
        assertEquals("template url", resultDto.pictureUrl)
        verify(exactly = 1) { subGroup.toDto() }
    }

    @Test
    fun `addSubGroupToSeries should add new subGroup for existing series`() {
        // GIVEN
        val seriesId = 1L
        val seriesMockk = mockkClass(Series::class, relaxed = true)
        val subGroup = mockkClass(SubGroup::class, relaxed = true)
        val subGroupRequest = SubGroupRequest(seriesId, 1, "Test name", "shortWords", "Test description")
        every { subGroupRepository.findByNameAndLevel(subGroupRequest.name, subGroupRequest.level) } returns null
        every { seriesRepository.findById(seriesId) } returns Optional.of(seriesMockk)
        every { subGroupRepository.save(subGroupRequest.toModel(seriesMockk)) } returns subGroup

        // WHEN
        subGroupService.addSubGroupToSeries(seriesId = seriesId, subGroupRequest = subGroupRequest)

        // THEN
        verify(exactly = 1) { subGroupRepository.save(subGroupRequest.toModel(seriesMockk)) }
    }

    @Test
    fun `addSubGroupToSeries should trow exception when subGroup is exists`() {
        // GIVEN
        val seriesId = 1L
        val subGroup = mockkClass(SubGroup::class, relaxed = true)
        val subGroupRequest = SubGroupRequest(seriesId, 1, "Test name", "shortWords", "Test description")
        every { subGroupRepository.findByNameAndLevel(subGroupRequest.name, subGroupRequest.level) } returns subGroup

        // THEN
        assertThrows<IllegalArgumentException> { subGroupService.addSubGroupToSeries(seriesId = seriesId, subGroupRequest = subGroupRequest) }
    }

    @Test
    fun `addSubGroupToSeries should trow exception when series does not exist`() {
        // GIVEN
        val seriesId = 1L
        val subGroupRequest = SubGroupRequest(seriesId, 1, "Test name", "shortWords", "Test description")
        every { subGroupRepository.findByNameAndLevel(subGroupRequest.name, subGroupRequest.level) } returns null
        every { seriesRepository.findById(seriesId) } returns Optional.empty()

        // THEN
        assertThrows<EntityNotFoundException> { subGroupService.addSubGroupToSeries(seriesId = seriesId, subGroupRequest = subGroupRequest) }
    }
}
