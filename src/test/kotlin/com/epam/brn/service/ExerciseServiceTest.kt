package com.epam.brn.service

import com.epam.brn.constant.ExerciseTypeEnum
import com.epam.brn.dto.ExerciseDto
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.nhaarman.mockito_kotlin.verify
import java.util.Optional
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyLong
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
    fun `should get exercises by user`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        val exerciseDtoMock = ExerciseDto(1, 1, "name", "descr", 1, ExerciseTypeEnum.SINGLE_WORDS)
        val exerciseId = 1L
        `when`(exerciseMock.toDto(true)).thenReturn(exerciseDtoMock)
        `when`(exerciseMock.id).thenReturn(exerciseId)
        `when`(studyHistoryRepository.getDoneExercisesIdList(anyLong())).thenReturn(listOf(exerciseId))
        `when`(exerciseRepository.findAll()).thenReturn(listOf(exerciseMock))
        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserId(exerciseId)
        // THEN
        assertEquals(actualResult, listOf(exerciseDtoMock))
        verify(exerciseRepository).findAll()
        verify(studyHistoryRepository).getDoneExercisesIdList(anyLong())
    }

    @Test
    fun `should get exercises by user and series`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        val exerciseDtoMock = ExerciseDto(1, 1, "name", "descr", 1, ExerciseTypeEnum.SINGLE_WORDS)
        val exerciseId = 1L
        val seriesId = 1L
        `when`(exerciseMock.toDto(true)).thenReturn(exerciseDtoMock)
        `when`(exerciseMock.id).thenReturn(exerciseId)
        `when`(studyHistoryRepository.getDoneExercisesIdList(anyLong(), anyLong())).thenReturn(listOf(exerciseId))
        `when`(exerciseRepository.findExercisesBySeriesId(seriesId)).thenReturn(listOf(exerciseMock))
        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserIdAndSeries(exerciseId, seriesId)
        // THEN
        assertEquals(actualResult, listOf(exerciseDtoMock))
        verify(exerciseRepository).findExercisesBySeriesId(seriesId)
        verify(studyHistoryRepository).getDoneExercisesIdList(anyLong(), anyLong())
    }

    @Test
    fun `should get exercise by id`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        val exerciseDtoMock = ExerciseDto(1, 1, "name", "descr", 1, ExerciseTypeEnum.SINGLE_WORDS)
        `when`(exerciseMock.toDto()).thenReturn(exerciseDtoMock)
        `when`(exerciseRepository.findById(anyLong())).thenReturn(Optional.of(exerciseMock))
        // WHEN
        val actualResult: ExerciseDto = exerciseService.findExerciseById(1L)
        // THEN
        assertEquals(actualResult, exerciseDtoMock)
        verify(exerciseRepository).findById(anyLong())
    }
}
