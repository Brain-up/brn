package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class ExerciseServiceTest {
    @InjectMocks
    lateinit var exerciseService: ExerciseService
    @Mock
    lateinit var exerciseRepository: ExerciseRepository
    @Mock
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Test
    fun `should get available exercises by user`() {
        // GIVEN
        val userID = 1L
        val studyHistoryMock: StudyHistory = mock(StudyHistory::class.java)
        val exerciseMock: Exercise = mock(Exercise::class.java)
        `when`(studyHistoryMock.exercise).thenReturn(exerciseMock)
        `when`(exerciseMock.toDto()).thenReturn(mock(ExerciseDto::class.java))
        `when`(studyHistoryRepository.findByUserAccount_Id(userID)).thenReturn(listOf(studyHistoryMock))
        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findAvailableExercises(userID)
        // THEN
        assertTrue(actualResult.contains(exerciseMock.toDto()))
    }
}