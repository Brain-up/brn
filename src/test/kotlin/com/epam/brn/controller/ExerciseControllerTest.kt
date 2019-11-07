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
    fun `should get exercises for user`() {
        // GIVEN
        val userID: Long = 1
        val exercise = ExerciseDto(1, "name", "desc", 1)
        val listExercises = listOf(exercise)
        Mockito.`when`(exerciseService.findExercisesByUserId(userID)).thenReturn(listExercises)
        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: List<ExerciseDto> =
            exerciseController.getExercises(userID, null).body?.data as List<ExerciseDto>
        // THEN
        assertTrue(actualResultData.contains(exercise))
        verify(exerciseService).findExercisesByUserId(userID)
    }

    @Test
    fun `should get exercises for user and series`() {
        // GIVEN
        val userId: Long = 1
        val seriesId: Long = 1
        val exercise = ExerciseDto(1, "name", "desc", 1)
        val listExercises = listOf(exercise)
        Mockito.`when`(exerciseService.findExercisesByUserIdAndSeries(userId, seriesId)).thenReturn(listExercises)
        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: List<ExerciseDto> =
            exerciseController.getExercises(userId, seriesId).body?.data as List<ExerciseDto>
        // THEN
        assertTrue(actualResultData.contains(exercise))
        verify(exerciseService).findExercisesByUserIdAndSeries(userId, seriesId)
    }

    @Test
    fun `should get exercise by id`() {
        // GIVEN
        val exerciseID: Long = 1
        val exercise = ExerciseDto(1, "exe", "desc")
        Mockito.`when`(exerciseService.findExerciseById(exerciseID)).thenReturn(exercise)
        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: List<ExerciseDto> =
            exerciseController.getExercisesByID(exerciseID).body?.data as List<ExerciseDto>
        // THEN
        assertEquals(actualResultData[0], exercise)
        verify(exerciseService).findExerciseById(exerciseID)
    }
}