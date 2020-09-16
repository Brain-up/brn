package com.epam.brn.controller

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.NoiseDto
import com.epam.brn.model.ExerciseType
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
    fun `should get exercises for user and series`() {
        // GIVEN
        val seriesId: Long = 2
        val exercise =
            ExerciseDto(seriesId, 1, "name", "pictureUrl", "desc", 1, NoiseDto(0, ""), ExerciseType.WORDS_SEQUENCES)
        val listExercises = listOf(exercise)
        Mockito.`when`(exerciseService.findExercisesBySeriesForCurrentUser(seriesId)).thenReturn(listExercises)
        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: List<ExerciseDto> =
            exerciseController.getExercises(seriesId).body?.data as List<ExerciseDto>
        // THEN
        assertTrue(actualResultData.contains(exercise))
        verify(exerciseService).findExercisesBySeriesForCurrentUser(seriesId)
    }

    @Test
    fun `should get exercise by id`() {
        // GIVEN
        val exerciseID: Long = 1
        val exercise = ExerciseDto(2, 1, "exe", "pictureUrl", "desc", 1, NoiseDto(0, ""), ExerciseType.WORDS_SEQUENCES)
        Mockito.`when`(exerciseService.findExerciseById(exerciseID)).thenReturn(exercise)
        // WHEN
        @Suppress("UNCHECKED_CAST")
        val actualResultData: ExerciseDto =
            exerciseController.getExercisesByID(exerciseID).body?.data as ExerciseDto
        // THEN
        assertEquals(actualResultData, exercise)
        verify(exerciseService).findExerciseById(exerciseID)
    }
}
