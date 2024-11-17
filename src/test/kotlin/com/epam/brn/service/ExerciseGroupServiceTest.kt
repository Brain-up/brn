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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional
import kotlin.test.assertFailsWith
import org.amshove.kluent.`should be`

@ExtendWith(MockKExtension::class)
internal class ExerciseGroupServiceTest {
    @MockK
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @InjectMockKs
    lateinit var exerciseGroupsService: ExerciseGroupsService

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
    fun `should get group by locale if it's exists`() {
        // GIVEN
        val locale = "ru-ru"
        val exerciseGroupMock =
            ExerciseGroup(id = 1, code = "groupCode", locale = "ru-ru", name = "name", description = "descr")
        every { exerciseGroupRepository.findByLocale(locale) } returns listOf(exerciseGroupMock)
        // WHEN
        val actualResult: List<ExerciseGroupDto> = exerciseGroupsService.findByLocale(locale)
        // THEN
        actualResult.isNotEmpty()
        actualResult.first().id `should be` 1
        actualResult.first().locale `should be` "ru-ru"
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
