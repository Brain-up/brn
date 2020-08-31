package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.NoiseDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseType
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyLong
import org.mockito.Mockito.anyString
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

    @Mock
    lateinit var userAccountService: UserAccountService

    @Test
    fun `should get exercises by user`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        val exerciseDtoMock = ExerciseDto(2, 1, "name", "descr", 1, NoiseDto(0, ""), ExerciseType.WORDS_SEQUENCES)
        val exerciseId = 1L
        `when`(exerciseMock.toDto(true)).thenReturn(exerciseDtoMock)
        `when`(exerciseMock.id).thenReturn(exerciseId)
        `when`(studyHistoryRepository.getDoneExercisesIdList(anyLong())).thenReturn(listOf(exerciseId))
        `when`(exerciseRepository.findAll()).thenReturn(listOf(exerciseMock))
        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserId(22L)
        // THEN
        assertEquals(actualResult, listOf(exerciseDtoMock))
        verify(exerciseRepository).findAll()
        verify(studyHistoryRepository).getDoneExercisesIdList(anyLong())
    }

    @Test
    fun `should get exercises by user and series`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        val exerciseDtoMock = ExerciseDto(2, 1, "name", "descr", 1, NoiseDto(0, ""), ExerciseType.WORDS_SEQUENCES)
        val exerciseId = 1L
        val seriesId = 2L
        val userId = 3L
        `when`(exerciseMock.toDto(true)).thenReturn(exerciseDtoMock)
        `when`(exerciseMock.id).thenReturn(exerciseId)
        `when`(studyHistoryRepository.getDoneExercisesIdList(seriesId, userId)).thenReturn(listOf(exerciseId))
        `when`(exerciseRepository.findExercisesBySeriesId(seriesId)).thenReturn(listOf(exerciseMock))
        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserIdAndSeries(userId, seriesId)
        // THEN
        assertEquals(actualResult, listOf(exerciseDtoMock))
        verify(exerciseRepository).findExercisesBySeriesId(seriesId)
        verify(studyHistoryRepository).getDoneExercisesIdList(anyLong(), anyLong())
    }

    @Test
    fun `should get exercise by id`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        val exerciseDtoMock = ExerciseDto(2, 1, "name", "descr", 1, NoiseDto(0, ""), ExerciseType.WORDS_SEQUENCES)
        `when`(exerciseMock.toDto()).thenReturn(exerciseDtoMock)
        `when`(exerciseRepository.findById(anyLong())).thenReturn(Optional.of(exerciseMock))
        // WHEN
        val actualResult: ExerciseDto = exerciseService.findExerciseById(1L)
        // THEN
        assertEquals(actualResult, exerciseDtoMock)
        verify(exerciseRepository).findById(anyLong())
    }

    @Test
    fun `should get exercise by name and level`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        `when`(exerciseRepository.findExerciseByNameAndLevel("name", 1)).thenReturn(Optional.of(exerciseMock))
        // WHEN
        val actualResult: Exercise = exerciseService.findExerciseByNameAndLevel("name", 1)
        // THEN
        assertEquals(actualResult, exerciseMock)
        verify(exerciseRepository).findExerciseByNameAndLevel(anyString(), anyInt())
    }
}
