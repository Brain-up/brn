package com.epam.brn.controller

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.service.ExerciseGroupsService
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class GroupControllerTest {
    @Mock
    lateinit var exerciseGroupsService: ExerciseGroupsService

    @InjectMocks
    lateinit var groupController: GroupController

    @Test
    fun `should get all groups`() {
        // GIVEN
        val group = ExerciseGroupDto(1, "en", "name", "desc")
        val listGroups = listOf(group)
        Mockito.`when`(exerciseGroupsService.findByLocale("")).thenReturn(listGroups)
        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: List<ExerciseGroupDto> =
            groupController.getGroups("").body?.data as List<ExerciseGroupDto>
        // THEN
        assertTrue(actualResultData.contains(group))
        verify(exerciseGroupsService).findByLocale("")
    }

    @Test
    fun `should get group by id`() {
        // GIVEN
        val groupId = 1L
        val group = ExerciseGroupDto(1, "en", "name", "desc")
        Mockito.`when`(exerciseGroupsService.findGroupDtoById(groupId)).thenReturn(group)

        // WHEN
        val actualResultData: ExerciseGroupDto =
            groupController.getGroupById(groupId).body?.data as ExerciseGroupDto
        // THEN
        assertEquals(actualResultData, group)
        verify(exerciseGroupsService).findGroupDtoById(groupId)
    }
}
