package com.epam.brn.service

import com.epam.brn.dto.SubGroupDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.SubGroup
import com.epam.brn.repo.SubGroupRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional

internal class SubGroupServiceTest {

    @InjectMockKs
    private lateinit var subGroupService: SubGroupService

    @MockK
    private lateinit var subGroupRepository: SubGroupRepository

    @MockK
    private lateinit var subGroup: SubGroup

    @BeforeEach
    fun init() {
        MockKAnnotations.init(this)
    }

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
        verify { subGroupRepository.findBySeriesId(seriesId) }
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
}
