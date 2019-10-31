package com.epam.brn.controller

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.service.ExerciseGroupsService
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class GroupControllerTest {
    @Mock
    lateinit var exerciseGroupsService: ExerciseGroupsService
    @InjectMocks
    lateinit var groupController: GroupController

    @Test
    fun `should get all groups`() {
        // GIVEN
        val group = ExerciseGroupDto(1, "name", "desc")
        val listGroups = listOf(group)
        Mockito.`when`(exerciseGroupsService.findAllGroups()).thenReturn(listGroups)
        // WHEN
        val actualResultData: List<ExerciseGroupDto> =
            groupController.getAllGroups().data as List<ExerciseGroupDto>
        // THEN
        assertTrue(actualResultData.contains(group))
        verify(exerciseGroupsService).findAllGroups()
    }

    @Test
    fun `should get group by id`() {
        // GIVEN
        val groupId = 1L
        val group = ExerciseGroupDto(1, "name", "desc")
        Mockito.`when`(exerciseGroupsService.findGroupById(groupId)).thenReturn(group)
        // WHEN
        val actualResultData: ExerciseGroupDto =
            groupController.getGroupById(groupId).data as ExerciseGroupDto
        // THEN
        assertEquals(actualResultData, group)
        verify(exerciseGroupsService).findGroupById(groupId)
    }
}