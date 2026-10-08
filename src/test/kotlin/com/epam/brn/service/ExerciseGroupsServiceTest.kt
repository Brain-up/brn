package com.epam.brn.service

import com.epam.brn.dto.ExerciseGroupDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class ExerciseGroupsServiceTest {
    @InjectMockKs
    lateinit var exerciseGroupsService: ExerciseGroupsService

    @MockK
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Test
    fun `findByLocale should return groups for a non-empty locale without embedded series`() {
        // GIVEN
        val locale = "ru-ru"
        val group = mockk<ExerciseGroup>()
        val groupDto = mockk<ExerciseGroupDto>()
        every { exerciseGroupRepository.findByLocale(locale) } returns listOf(group)
        every { group.toDtoWithoutSeries() } returns groupDto

        // WHEN
        val result = exerciseGroupsService.findByLocale(locale)

        // THEN
        verify(exactly = 1) { exerciseGroupRepository.findByLocale(locale) }
        verify(exactly = 1) { group.toDtoWithoutSeries() }
        result shouldBe listOf(groupDto)
    }

    @Test
    fun `findByLocale should return all groups without embedded series when locale is empty`() {
        // GIVEN
        val group = mockk<ExerciseGroup>()
        val groupDto = mockk<ExerciseGroupDto>()
        every { exerciseGroupRepository.findAll() } returns listOf(group)
        every { group.toDtoWithoutSeries() } returns groupDto

        // WHEN
        val result = exerciseGroupsService.findByLocale("")

        // THEN
        verify(exactly = 1) { exerciseGroupRepository.findAll() }
        verify(exactly = 1) { group.toDtoWithoutSeries() }
        result shouldBe listOf(groupDto)
    }

    @Test
    fun `findGroupDtoById should return the group with its series when found`() {
        // GIVEN
        val groupId = 1L
        val group = mockk<ExerciseGroup>()
        val groupDto = mockk<ExerciseGroupDto>()
        every { exerciseGroupRepository.findById(groupId) } returns Optional.of(group)
        every { group.toDto() } returns groupDto

        // WHEN
        val result = exerciseGroupsService.findGroupDtoById(groupId)

        // THEN
        verify(exactly = 1) { exerciseGroupRepository.findById(groupId) }
        result shouldBe groupDto
    }

    @Test
    fun `findGroupDtoById should throw EntityNotFoundException when group is not found`() {
        // GIVEN
        val groupId = 1L
        every { exerciseGroupRepository.findById(groupId) } returns Optional.empty()

        // THEN
        shouldThrow<EntityNotFoundException> { exerciseGroupsService.findGroupDtoById(groupId) }
    }
}
