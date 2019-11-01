package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
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
internal class ExerciseServiceTest {
    @InjectMocks
    lateinit var exerciseService: ExerciseService
    @Mock
    lateinit var exerciseRepository: ExerciseRepository
    @Mock
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Test
    fun `should get done exercises by user`() {
        // GIVEN
        val studyHistoryMock: StudyHistory = mock(StudyHistory::class.java)
        val exerciseMock: Exercise = mock(Exercise::class.java)
        `when`(studyHistoryMock.exercise).thenReturn(exerciseMock)
        `when`(studyHistoryRepository.findByUserAccountId(anyLong())).thenReturn(listOf(studyHistoryMock))
        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findDoneExercises(1L)
        // THEN
        assertTrue(actualResult.contains(exerciseMock.toDtoWithoutTasks()))
    }

    @Test
    fun `should get exercise by id`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        val exerciseDtoMock = ExerciseDto()
        `when`(exerciseMock.toDtoWithoutTasks()).thenReturn(exerciseDtoMock)
        `when`(exerciseRepository.findById(anyLong())).thenReturn(Optional.of(exerciseMock))
        // WHEN
        val actualResult: ExerciseDto = exerciseService.findExerciseById(1L)
        // THEN
        assertEquals(actualResult, exerciseDtoMock)
    }
}