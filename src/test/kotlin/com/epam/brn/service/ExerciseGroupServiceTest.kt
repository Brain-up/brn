package com.epam.brn.service

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
internal class ExerciseGroupServiceTest {
    @MockK
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @InjectMockKs
    lateinit var exerciseGroupsService: ExerciseGroupsService

    @Test
    fun `should get all groups`() {
        // GIVEN
        val exerciseGroupMock: ExerciseGroup = mockk(relaxed = true)
        val exerciseGroupDtoMock = ExerciseGroupDto(id = 1, locale = "en", name = "name", description = "descr")

        every { exerciseGroupMock.toDto() } returns (exerciseGroupDtoMock)
        every { exerciseGroupRepository.findAll() } returns (listOf(exerciseGroupMock))
        // WHEN
        val actualResult: List<ExerciseGroupDto> = exerciseGroupsService.findAllGroups()
        // THEN
        assertTrue(actualResult.contains(exerciseGroupDtoMock))
    }

    @Test
    fun `should get group by id`() {
        // GIVEN
        val groupId = 1L
        val exerciseGroupMock: ExerciseGroup = mockk(relaxed = true)
        val exerciseGroupDtoMock = ExerciseGroupDto(id = 1, locale = "en", name = "name", description = "descr")
        every { exerciseGroupMock.toDto() } returns (exerciseGroupDtoMock)
        every { exerciseGroupRepository.findById(ofType(Long::class)) } returns (Optional.of(exerciseGroupMock))
        // WHEN
        val actualResult: ExerciseGroupDto = exerciseGroupsService.findGroupDtoById(groupId)
        // THEN
        assertEquals(actualResult, exerciseGroupDtoMock)
    }

    @Test
    fun `should get group by code if it's exists`() {
        // GIVEN
        val groupCode = "CODE"
        val exerciseGroupMock =
            ExerciseGroup(id = 1, code = groupCode, locale = "en", name = "name", description = "descr")
        every { exerciseGroupRepository.findByCode(groupCode) } returns (Optional.of(exerciseGroupMock))
        // WHEN
        val actualResult: ExerciseGroup = exerciseGroupsService.findGroupByCode(groupCode)
        // THEN
        assertEquals(actualResult, exerciseGroupMock)
    }

    @Test
    fun `should throw error when find group by not exist code`() {
        // GIVEN
        val groupCode = "NOT_EXISTS_CODE"
        every { exerciseGroupRepository.findByCode(groupCode) } returns (Optional.empty())
        // WHEN
        assertFailsWith<EntityNotFoundException> {
            exerciseGroupsService.findGroupByCode(groupCode)
        }
    }
}
