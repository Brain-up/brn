package com.epam.brn.controller

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.service.ExerciseService
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
internal class ExerciseControllerTest {
    @InjectMocks
    lateinit var exerciseController: ExerciseController
    @Mock
    lateinit var exerciseService: ExerciseService

    @Test
    fun `should get done exercises for user`() {
        // GIVEN
        val userID: Long = 1
        val exercise = ExerciseDto(1, "name", "desc", 1)
        val listExercises = listOf(exercise)
        Mockito.`when`(exerciseService.findDoneExercisesByUserId(userID)).thenReturn(listExercises)
        // WHEN
        val actualResultData: List<ExerciseDto> =
            exerciseController.getExercisesByUserID(userID, null).body?.data as List<ExerciseDto>
        // THEN
        assertTrue(actualResultData.contains(exercise))
        verify(exerciseService).findDoneExercisesByUserId(userID)
    }

    @Test
    fun `should get exercise by id`() {
        // GIVEN
        val exerciseID: Long = 1
        val exercise = ExerciseDto(1, "exe", "desc")
        Mockito.`when`(exerciseService.findExerciseById(exerciseID)).thenReturn(exercise)
        // WHEN
        val actualResultData: ExerciseDto =
            exerciseController.getExercisesByID(exerciseID).body?.data as ExerciseDto
        // THEN
        assertEquals(actualResultData, exercise)
        verify(exerciseService).findExerciseById(exerciseID)
    }
}