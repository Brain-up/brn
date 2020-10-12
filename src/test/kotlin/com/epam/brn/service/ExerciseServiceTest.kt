package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.NoiseDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
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
import org.springframework.test.util.ReflectionTestUtils
import java.util.Optional
import kotlin.test.assertTrue

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

    @Mock
    lateinit var urlConversionService: UrlConversionService

    @Test
    fun `should get exercises by user`() {
        // GIVEN
        val exerciseMock: Exercise = mock(Exercise::class.java)
        val exerciseDtoMock = ExerciseDto(2, 1, "name", "pictureUrl", "descr", 1, NoiseDto(0, ""), ExerciseType.WORDS_SEQUENCES)
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
        val exerciseDtoMock = ExerciseDto(2, 1, "name", "pictureUrl", "descr", 1, NoiseDto(0, ""), ExerciseType.WORDS_SEQUENCES)
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
        val exerciseDtoMock = ExerciseDto(2, 1, "name", "pictureUrl", "descr", 1, NoiseDto(0, ""), ExerciseType.WORDS_SEQUENCES)
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

    @Test
    fun `should return unavailableExercises`() {
        // GIVEN
        val ex1 = Exercise(id = 1, name = "pets")
        val ex2 = Exercise(id = 2, name = "pets")
        val ex3 = Exercise(id = 3, name = "pets")
        val ex4 = Exercise(id = 4, name = "pets")
        val ex11 = Exercise(id = 5, name = "food")
        val ex12 = Exercise(id = 6, name = "food")
        val ex13 = Exercise(id = 7, name = "food")
        val ex21 = Exercise(id = 7, name = "some")
        val listAll = listOf(ex1, ex2, ex3, ex4, ex11, ex12, ex13, ex21)
        val listDone = listOf(1L, 2)
        val studyHistory = StudyHistory(exercise = ex2,
            userAccount = mock(UserAccount::class.java),
            listeningsCount = 12,
            rightAnswersCount = 8,
            tasksCount = 10)
        `when`(studyHistoryRepository.findByUserAccountIdAndExerciseId(1, 2))
            .thenReturn(Optional.of(studyHistory))
        ReflectionTestUtils.setField(exerciseService, "minRepetitionIndex", 0.75)
        ReflectionTestUtils.setField(exerciseService, "minRightAnswersIndex", 0.75)

        // WHEN
        val actualResult = exerciseService.getAvailableExercises(listDone, listAll, 1)
        // THEN
        assertEquals(3, actualResult.size)
        assertTrue(actualResult.containsAll(listOf(1L, 2L, 3L)))
    }
}
