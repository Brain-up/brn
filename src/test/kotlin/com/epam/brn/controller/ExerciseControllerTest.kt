package com.epam.brn.controller

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.NoiseDto
import com.epam.brn.service.ExerciseService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class ExerciseControllerTest {
    @InjectMockKs
    lateinit var exerciseController: ExerciseController

    @MockK
    lateinit var exerciseService: ExerciseService

    @Test
    fun `should get exercises for user and series`() {
        // GIVEN
        val subGroupId: Long = 2
        val exercise = ExerciseDto(subGroupId, 1, "name", 1, NoiseDto(0, ""))
        val listExercises = listOf(exercise)
        every { exerciseService.findExercisesBySubGroupForCurrentUser(subGroupId) } returns listExercises

        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: List<ExerciseDto> =
            exerciseController.getExercisesBySubGroup(subGroupId).body?.data as List<ExerciseDto>

        // THEN
        verify { exerciseService.findExercisesBySubGroupForCurrentUser(subGroupId) }
        assertTrue(actualResultData.contains(exercise))
    }

    @Test
    fun `should get exercise by id`() {
        // GIVEN
        val exerciseID: Long = 1
        val exercise = ExerciseDto(2, 1, "exe", 1, NoiseDto(0, ""))
        every { exerciseService.findExerciseById(exerciseID) } returns exercise

        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: ExerciseDto = exerciseController.getExercisesByID(exerciseID).body?.data as ExerciseDto

        // THEN
        verify { exerciseService.findExerciseById(exerciseID) }
        assertEquals(actualResultData, exercise)
    }
}
