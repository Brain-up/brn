package com.epam.brn.service

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Optional

@ExtendWith(MockitoExtension::class)
internal class ExerciseGroupServiceTest {
    @Mock
    lateinit var exerciseGroupRepository: ExerciseGroupRepository
    @InjectMocks
    lateinit var exerciseGroupsService: ExerciseGroupsService

    @Test
    fun `should get all groups`() {
        // GIVEN
        val exerciseGroupMock: ExerciseGroup = mock(ExerciseGroup::class.java)
        val exerciseGroupDtoMock = ExerciseGroupDto()
        `when`(exerciseGroupMock.toDto()).thenReturn(exerciseGroupDtoMock)
        `when`(exerciseGroupRepository.findAll()).thenReturn(listOf(exerciseGroupMock))
        // WHEN
        val actualResult: List<ExerciseGroupDto> = exerciseGroupsService.findAllGroups()
        // THEN
        assertTrue(actualResult.contains(exerciseGroupDtoMock))
    }

    @Test
    fun `should get group by id`() {
        // GIVEN
        val groupId = 1L
        val exerciseGroupMock: ExerciseGroup = mock(ExerciseGroup::class.java)
        val exerciseGroupDtoMock = ExerciseGroupDto()
        `when`(exerciseGroupMock.toDto()).thenReturn(exerciseGroupDtoMock)
        `when`(exerciseGroupRepository.findById(anyLong())).thenReturn(Optional.of(exerciseGroupMock))
        // WHEN
        val actualResult: ExerciseGroupDto = exerciseGroupsService.findGroupById(groupId)
        // THEN
        assertEquals(actualResult, exerciseGroupDtoMock)
    }
}