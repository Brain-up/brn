package com.epam.brn.controller

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.service.ExerciseGroupsService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class GroupControllerTest {

    @InjectMockKs
    lateinit var groupController: GroupController

    @MockK
    lateinit var exerciseGroupsService: ExerciseGroupsService

    @Test
    fun `should get all groups`() {
        // GIVEN
        val group = ExerciseGroupDto(1, "en", "name", "desc")
        val listGroups = listOf(group)
        every { exerciseGroupsService.findByLocale("") } returns listGroups

        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: List<ExerciseGroupDto> =
            groupController.getGroups("").body?.data as List<ExerciseGroupDto>

        // THEN
        verify(exactly = 1) { exerciseGroupsService.findByLocale("") }
        assertTrue(actualResultData.contains(group))
    }

    @Test
    fun `should get group by id`() {
        // GIVEN
        val groupId = 1L
        val group = ExerciseGroupDto(1, "en", "name", "desc")
        every { exerciseGroupsService.findGroupDtoById(groupId) } returns group

        // WHEN
        val actualResultData: ExerciseGroupDto = groupController.getGroupById(groupId).body?.data as ExerciseGroupDto

        // THEN
        verify(exactly = 1) { exerciseGroupsService.findGroupDtoById(groupId) }
        assertEquals(actualResultData, group)
    }
}
