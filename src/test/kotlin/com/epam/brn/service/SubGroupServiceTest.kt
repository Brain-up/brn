package com.epam.brn.service

import com.epam.brn.dto.SubGroupDto
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.Series
import com.epam.brn.model.SubGroup
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SubGroupRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockkClass
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class SubGroupServiceTest {

    @InjectMockKs
    private lateinit var subGroupService: SubGroupService

    @MockK
    private lateinit var subGroupRepository: SubGroupRepository

    @MockK
    private lateinit var exerciseRepository: ExerciseRepository

    @MockK
    private lateinit var subGroup: SubGroup

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
        allGroups shouldBe listOf(subGroupDto)
    }

    @Test
    fun `deleteById should delete subGroup without exercises`() {
        // GIVEN
        val subGroupId = 1L
        mockkStatic("com.epam.brn.service.SubGroupServiceKt")
        every { exerciseRepository.findExercisesBySubGroupId(subGroupId) } returns emptyList()
        justRun { subGroupRepository.deleteById(subGroupId) }

        // WHEN
        subGroupService.deleteSubGroupById(subGroupId)

        // THEN
        verify(exactly = 1) { exerciseRepository.findExercisesBySubGroupId(subGroupId) }
    }

    @Test
    fun `deleteById should throw an exception with exercises in subGroup`() {
        // GIVEN
        val subGroupId = 1L
        val exercise = Exercise(
            id = 1L,
            name = "nameOfExercise",
            template = "",
            level = 1,
            noiseLevel = 1,
            noiseUrl = "noiseUrl",
            active = true,
            subGroup = subGroup
        )
        val listExercises = listOf(exercise)
        every { exerciseRepository.findExercisesBySubGroupId(subGroupId) } returns listExercises

        // THEN
        shouldThrow<IllegalArgumentException> {
            subGroupService.deleteSubGroupById(subGroupId)
        }
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
        verify(exactly = 1) { subGroupRepository.findById(subGroupId) }
        group shouldBe subGroupDto
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
        verify(exactly = 1) { subGroup.toDto() }
        resultDto.pictureUrl shouldBe "template url"
    }
}
