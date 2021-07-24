package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.ExerciseWithTasksDto
import com.epam.brn.dto.NoiseDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.SubGroup
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.upload.csv.RecordProcessor
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class ExerciseServiceTest {
    @InjectMockKs
    lateinit var exerciseService: ExerciseService

    @MockK
    lateinit var exerciseRepository: ExerciseRepository

    @MockK
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    lateinit var urlConversionService: UrlConversionService

    @MockK
    lateinit var recordProcessors: List<RecordProcessor<out Any, out Any>>

    @MockK
    lateinit var audioFilesGenerationService: AudioFilesGenerationService

    @MockK
    lateinit var wordsService: WordsService

    private val series = Series(
        id = 1L,
        name = "Распознавание простых слов",
        type = "type",
        level = 1,
        description = "Распознавание простых слов",
        exerciseGroup = ExerciseGroup(
            code = "SPEECH_RU_RU",
            name = "Речевые упражнения",
            description = "Речевые упражнения"
        )
    )

    @Test
    fun `should get exercises by user`() {
        // GIVEN
        val exerciseMock: Exercise = mockkClass(Exercise::class)
        val noiseUrl = "noiseUrl"
        val exerciseDtoMock = ExerciseDto(2, 1, "name", 1, NoiseDto(0, noiseUrl))
        val exerciseId = 1L
        every { exerciseMock.toDto(true) } returns exerciseDtoMock
        every { exerciseMock.id } returns exerciseId
        every { studyHistoryRepository.getDoneExercisesIdList(ofType(Long::class)) } returns listOf(exerciseId)
        every { exerciseRepository.findAll() } returns listOf(exerciseMock)
        every { urlConversionService.makeUrlForNoise(noiseUrl) } returns noiseUrl

        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserId(22L)

        // THEN
        actualResult shouldBe listOf(exerciseDtoMock)
        verify(exactly = 1) { exerciseRepository.findAll() }
        verify(exactly = 1) { studyHistoryRepository.getDoneExercisesIdList(ofType(Long::class)) }
    }

    @Test
    fun `should get exercises by user and series`() {
        // GIVEN
        val subGroupId = 2L
        val userId = 2L
        val exercise1 = Exercise(id = 1, name = "pets")
        val exercise2 = Exercise(id = 2, name = "pets")
        val noiseUrl = "noiseUrl"
        every { studyHistoryRepository.getDoneExercises(subGroupId, userId) } returns listOf(exercise1)
        every { exerciseRepository.findExercisesBySubGroupId(subGroupId) } returns listOf(exercise1, exercise2)
        every { studyHistoryRepository.findLastByUserAccountId(userId) } returns emptyList()
        every { urlConversionService.makeUrlForNoise(ofType(String::class)) } returns noiseUrl

        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserIdAndSubGroupId(userId, subGroupId)

        // THEN
        actualResult shouldHaveSize 2
        verify(exactly = 1) { exerciseRepository.findExercisesBySubGroupId(subGroupId) }
        verify(exactly = 1) { studyHistoryRepository.getDoneExercises(ofType(Long::class), ofType(Long::class)) }
    }

    @Test
    fun `should get exercise by id`() {
        // GIVEN
        val exerciseMock: Exercise = mockkClass(Exercise::class)
        val noiseUrl = "noiseUrl"
        val exerciseDtoMock = ExerciseDto(2, 1, "name", 1, NoiseDto(0, noiseUrl))
        every { exerciseMock.toDto() } returns exerciseDtoMock
        every { exerciseRepository.findById(ofType(Long::class)) } returns Optional.of(exerciseMock)
        every { urlConversionService.makeUrlForNoise(noiseUrl) }.returns(noiseUrl)

        // WHEN
        val actualResult: ExerciseDto = exerciseService.findExerciseById(1L)

        // THEN
        actualResult shouldBe exerciseDtoMock
        verify(exactly = 1) { exerciseRepository.findById(ofType(Long::class)) }
    }

    @Test
    fun `should get exercise by name and level`() {
        // GIVEN
        val exerciseName = "name"
        val exerciseMock = Exercise(id = 1)
        val exerciseLevel = 1
        every { exerciseRepository.findExerciseByNameAndLevel(exerciseName, exerciseLevel) } returns Optional.of(
            exerciseMock
        )
        // WHEN
        val actualResult: Exercise = exerciseService.findExerciseByNameAndLevel("name", 1)
        // THEN
        verify(exactly = 1) { exerciseRepository.findExerciseByNameAndLevel(exerciseName, exerciseLevel) }
        actualResult.id shouldBe exerciseMock.id
    }

    @Test
    fun `should get exercises by subGroupId`() {
        // GIVEN
        val exerciseMock: Exercise = mockkClass(Exercise::class)
        val subGroupId = 1L
        val exerciseDtoMock: ExerciseWithTasksDto = mockkClass(ExerciseWithTasksDto::class)
        every { exerciseRepository.findExercisesBySubGroupId(subGroupId) } returns listOf(exerciseMock)
        every { exerciseMock.toDtoWithTasks() } returns (exerciseDtoMock)
        // WHEN
        val actualResults: List<ExerciseWithTasksDto> = exerciseService.findExercisesWithTasksBySubGroup(1)
        // THEN
        actualResults shouldContain exerciseDtoMock
        verify(exactly = 1) { exerciseRepository.findExercisesBySubGroupId(subGroupId) }
    }

    @Test
    fun `should return 2 availableExercises for one subgroup with last done success`() {
        // GIVEN
        val subGroup = SubGroup(
            series = series,
            level = 1,
            code = "code",
            name = "subGroup name"
        )
        val ex1 = Exercise(id = 1, name = "pets", subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", subGroup = subGroup)
        val ex3 = Exercise(id = 3, name = "pets ddd", subGroup = subGroup)
        val ex4 = Exercise(id = 4, name = "pets ddd", subGroup = subGroup)
        val listAll = listOf(ex1, ex2, ex3, ex4)
        val listDone = listOf(ex1)
        val studyHistory1 = StudyHistory(
            exercise = ex1,
            userAccount = mockkClass(UserAccount::class),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 5,
            replaysCount = 0
        )
        every { studyHistoryRepository.findLastByUserAccountId(1) } returns listOf(studyHistory1)
        ReflectionTestUtils.setField(exerciseService, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseService, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercises(listDone, listAll, 1)
        // THEN
        actualResult shouldHaveSize 2
        actualResult shouldContainAll listOf(ex1, ex3)
    }

    @Test
    fun `should return availableExercises for one subgroup with last done success`() {
        // GIVEN
        val subGroup = SubGroup(
            series = series,
            level = 1,
            code = "code",
            name = "subGroup name"
        )
        val userId = 1L
        val ex1 = Exercise(id = 1, name = "pets", subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", subGroup = subGroup)
        val ex3 = Exercise(id = 3, name = "pets ddd", subGroup = subGroup)
        val ex4 = Exercise(id = 4, name = "pets ddd", subGroup = subGroup)
        val listAll = listOf(ex1, ex2, ex3, ex4)
        val listDone = listOf(ex1, ex3)
        val studyHistoryWithExercise1 = StudyHistory(
            exercise = ex1,
            userAccount = mockkClass(UserAccount::class),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 0,
            replaysCount = 0
        )
        val studyHistoryWithExercise3 = StudyHistory(
            exercise = ex3,
            userAccount = mockkClass(UserAccount::class),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 1,
            replaysCount = 1
        )
        every { studyHistoryRepository.findLastByUserAccountId(userId) } returns listOf(
            studyHistoryWithExercise1,
            studyHistoryWithExercise3
        )
        ReflectionTestUtils.setField(exerciseService, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseService, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercises(listDone, listAll, 1)
        // THEN
        actualResult shouldHaveSize 4
        actualResult shouldContainAll listOf(ex1, ex2, ex3, ex4)
    }

    @Test
    fun `should return availableExercises for one subgroup with last done UNSUCCESS`() {
        // GIVEN
        val subGroup = SubGroup(
            series = series,
            level = 1,
            code = "code",
            name = "subGroup name"
        )
        val ex1 = Exercise(id = 1, name = "pets", subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", subGroup = subGroup)
        val ex3 = Exercise(id = 3, name = "pets", subGroup = subGroup)
        val ex4 = Exercise(id = 4, name = "pets ddd", subGroup = subGroup)
        val ex5 = Exercise(id = 5, name = "pets ddd", subGroup = subGroup)
        val userAccountId = 1L
        val listAll = listOf(ex1, ex2, ex3, ex4, ex5)
        val listDone = listOf(ex1, ex2)
        val studyHistoryWithExercise1 = StudyHistory(
            exercise = ex1,
            userAccount = mockk(),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 0,
            replaysCount = 0
        )
        val studyHistoryWithExercise2 = StudyHistory(
            exercise = ex2,
            userAccount = mockk(),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 5,
            replaysCount = 1
        )
        every { studyHistoryRepository.findLastByUserAccountId(userAccountId) } returns listOf(
            studyHistoryWithExercise1,
            studyHistoryWithExercise2
        )

        ReflectionTestUtils.setField(exerciseService, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseService, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercises(listDone, listAll, 1)
        // THEN
        actualResult shouldHaveSize 3
        actualResult shouldContainAll listOf(ex1, ex2, ex4)
    }

    @Test
    fun `should return availableExercises for several subgroups`() {
        // GIVEN
        val subGroup1 = SubGroup(
            series = series,
            level = 1,
            code = "code1",
            name = "subGroup name1"
        )
        val subGroup2 = SubGroup(
            series = series,
            level = 2,
            code = "code2",
            name = "subGroup name2"
        )
        val ex1 = Exercise(id = 1, name = "pets", subGroup = subGroup1)
        val ex2 = Exercise(id = 2, name = "pets", subGroup = subGroup1)
        val ex3 = Exercise(id = 3, name = "pets ddd", subGroup = subGroup1)
        val ex4 = Exercise(id = 4, name = "pets ddd", subGroup = subGroup1)
        val ex11 = Exercise(id = 11, name = "food", subGroup = subGroup2)
        val ex12 = Exercise(id = 12, name = "food", subGroup = subGroup2)
        val ex13 = Exercise(id = 13, name = "food eee", subGroup = subGroup2)
        val ex14 = Exercise(id = 14, name = "food eee", subGroup = subGroup2)
        val listAll = listOf(ex1, ex2, ex3, ex4, ex11, ex12, ex13, ex14)
        val userAccountId = 1L
        val listDone = listOf(ex1, ex2, ex11)
        val studyHistoryWithExercise1 = StudyHistory(
            exercise = ex1,
            userAccount = mockkClass(UserAccount::class),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 0,
            replaysCount = 0
        )
        val studyHistoryWithExercise2 = StudyHistory(
            exercise = ex2,
            userAccount = mockkClass(UserAccount::class),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 2,
            replaysCount = 2
        )

        val studyHistoryWithExercise11 = StudyHistory(
            exercise = ex11,
            userAccount = mockk(),
            startTime = LocalDateTime.now(),
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 6,
            replaysCount = 4
        )

        every { studyHistoryRepository.findLastByUserAccountId(userAccountId) } returns listOf(
            studyHistoryWithExercise1,
            studyHistoryWithExercise2,
            studyHistoryWithExercise11
        )

        ReflectionTestUtils.setField(exerciseService, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseService, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercises(listDone, listAll, 1)
        // THEN
        actualResult shouldHaveSize 5
        actualResult shouldContainAll listOf(ex1, ex2, ex3, ex11, ex13)
    }
}
