package com.epam.brn.controller

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.service.ExerciseService
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class ExercisesControllerTest {
    @InjectMocks
    lateinit var exerciseController: ExerciseController
    @Mock
    lateinit var exerciseService: ExerciseService

    @Test
    fun `should get exercises for group`() {
        // GIVEN
        val userID: Long = 1
        val exercise = ExerciseDto(1, "exe", "desc")
        val listExercises = listOf(exercise)
        Mockito.`when`(exerciseService.findAvailableExercises(userID)).thenReturn(listExercises)
        // WHEN
        val actualResult = exerciseController.getAvailableExercises(userID)
        // THEN
        Assertions.assertTrue(actualResult.contains(exercise))
        verify(exerciseService).findAvailableExercises(userID)
    }
}