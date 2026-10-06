package com.epam.brn.service

import com.epam.brn.dto.ExerciseDto
import com.epam.brn.dto.NoiseDto
import com.epam.brn.dto.request.exercise.ExercisePhrasesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseSentencesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseWordsCreateDto
import com.epam.brn.dto.request.exercise.Phrases
import com.epam.brn.dto.request.exercise.SetOfWords
import com.epam.brn.dto.response.ExerciseWithWordsResponse
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.BrnRole
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Role
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.SubGroup
import com.epam.brn.model.Task
import com.epam.brn.model.UserAccount
import com.epam.brn.model.projection.ExerciseAvailabilityView
import com.epam.brn.model.projection.ExerciseLastAttemptView
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.upload.csv.RecordProcessor
import com.epam.brn.upload.csv.seriesMatrix.SeriesMatrixRecordProcessor
import com.epam.brn.upload.csv.seriesPhrases.SeriesPhrasesRecordProcessor
import com.epam.brn.upload.csv.seriesWords.SeriesWordsRecordProcessor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkClass
import io.mockk.verify
import java.time.LocalDateTime
import java.util.Optional
import java.util.stream.Stream
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.transaction.annotation.Transactional

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
    lateinit var taskService: TaskService

    @MockK
    lateinit var recordProcessors: List<RecordProcessor<out Any, out Any>>

    @SpyK
    var exerciseSuccessCalculator = ExerciseSuccessCalculator()

    private val series =
        Series(
            id = 1L,
            name = "Распознавание простых слов",
            type = "type",
            level = 1,
            description = "Распознавание простых слов",
            exerciseGroup =
                ExerciseGroup(
                    code = "SPEECH_RU_RU",
                    name = "Речевые упражнения",
                    description = "Речевые упражнения",
                ),
        )

    @Test
    fun `should get exercises by user`() {
        // GIVEN
        val exerciseMock: Exercise = mockkClass(Exercise::class)
        val taskMock: Task = mockkClass(Task::class)
        val noiseUrl = "noiseUrl"
        val exerciseDtoMock = ExerciseDto(2, 1, "name", 1, NoiseDto(0, noiseUrl))
        val exerciseId = 1L
        every { exerciseMock.toDto(true) } returns exerciseDtoMock
        every { exerciseMock.id } returns exerciseId
        every { studyHistoryRepository.getDoneExercisesIdList(ofType(Long::class)) } returns listOf(exerciseId)
        every { exerciseRepository.findAll() } returns listOf(exerciseMock)
        every { urlConversionService.makeUrlForNoise(noiseUrl) } returns noiseUrl
        every { exerciseMock.tasks } returns setOf(taskMock) as MutableSet<Task>
        every { taskService.processAnswerOptions(taskMock) } returns Unit

        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserId(22L)

        // THEN
        actualResult shouldBe listOf(exerciseDtoMock)
        verify(exactly = 1) { exerciseRepository.findAll() }
        verify(exactly = 1) { studyHistoryRepository.getDoneExercisesIdList(ofType(Long::class)) }
    }

    @Test
    fun `should get 2 exercises with 2 available for user-admin`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val subGroupId = 2L
        val userId = 2L
        val exercise1 = Exercise(id = 1, name = "pets", level = 2)
        val exercise2 = Exercise(id = 2, name = "pets", level = 100)
        val noiseUrl = "noiseUrl"
        every { userAccountService.getCurrentUserRoles() } returns setOf(BrnRole.ADMIN)
        every { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) } returns
            listOf(
                exercise1,
                exercise2,
            )
        every { urlConversionService.makeUrlForNoise(ofType(String::class)) } returns noiseUrl

        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserIdAndSubGroupId(userId, subGroupId)

        // THEN
        actualResult shouldHaveSize 2
        actualResult.filter { it.available } shouldHaveSize 2
        verify(exactly = 1) { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) }
        actualResult[0].level shouldBe 2
        actualResult[1].level shouldBe 100
    }

    @Test
    fun `should get 2 exercises for user-specialist`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val subGroupId = 2L
        val userId = 2L
        val exercise1 = Exercise(id = 1, name = "pets", level = 2)
        val exercise2 = Exercise(id = 2, name = "pets", level = 100)
        val noiseUrl = "noiseUrl"
        every { userAccountService.getCurrentUserRoles() } returns setOf(BrnRole.SPECIALIST, BrnRole.USER)
        every { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) } returns
            listOf(
                exercise1,
                exercise2,
            )
        every { urlConversionService.makeUrlForNoise(ofType(String::class)) } returns noiseUrl

        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserIdAndSubGroupId(userId, subGroupId)

        // THEN
        actualResult shouldHaveSize 2
        actualResult.filter { it.available } shouldHaveSize 2
        verify(exactly = 1) { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) }
        actualResult[0].level shouldBe 2
        actualResult[1].level shouldBe 100
    }

    @Test
    fun `should get 3 exercises with 1 available for user without history for USER`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val subGroupId = 2L
        val userId = 2L
        val exercise1 = Exercise(id = 1, name = "pets", level = 1)
        val exercise2 = Exercise(id = 2, name = "pets", level = 2)
        val exercise3 = Exercise(id = 3, name = "pets", level = 100)
        val noiseUrl = "noiseUrl"
        every { studyHistoryRepository.getDoneExercises(subGroupId, userId) } returns listOf(exercise1)
        every { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) } returns
            listOf(
                exercise1,
                exercise2,
                exercise3,
            )
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns emptyList()
        every { urlConversionService.makeUrlForNoise(ofType(String::class)) } returns noiseUrl
        every { userAccountService.getCurrentUserRoles() } returns setOf(BrnRole.USER)

        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserIdAndSubGroupId(userId, subGroupId)

        // THEN
        actualResult shouldHaveSize 3
        actualResult.filter { it.available } shouldHaveSize 1
        verify(exactly = 1) { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) }
        verify(exactly = 1) { studyHistoryRepository.getDoneExercises(ofType(Long::class), ofType(Long::class)) }
        verify(exactly = 1) {
            studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(
                ofType(Long::class),
                ofType(Long::class),
            )
        }
        actualResult[0].level shouldBe 1
        actualResult[1].level shouldBe 2
        actualResult[2].level shouldBe 3
    }

    @Test
    fun `should get 3 exercises with 1 available for USER with bad history`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val subGroupId = 2L
        val userId = 2L
        val exercise1 = Exercise(id = 1, name = "pets")
        val exercise2 = Exercise(id = 2, name = "pets")
        val exercise3 = Exercise(id = 3, name = "pets")
        val noiseUrl = "noiseUrl"
        every { studyHistoryRepository.getDoneExercises(subGroupId, userId) } returns listOf(exercise1)
        every { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) } returns
            listOf(
                exercise1,
                exercise2,
                exercise3,
            )
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns
            listOf(
                exerciseLastAttemptView(exerciseId = 1L, replaysCount = 2, wrongAnswers = 5),
            )
        every { urlConversionService.makeUrlForNoise(ofType(String::class)) } returns noiseUrl
        every { userAccountService.getCurrentUserRoles() } returns setOf(BrnRole.USER)

        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserIdAndSubGroupId(userId, subGroupId)

        // THEN
        actualResult shouldHaveSize 3
        actualResult.filter { it.available } shouldHaveSize 1
        verify(exactly = 1) { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) }
        verify(exactly = 1) { studyHistoryRepository.getDoneExercises(ofType(Long::class), ofType(Long::class)) }
        verify(exactly = 1) {
            studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(
                ofType(Long::class),
                ofType(Long::class),
            )
        }
    }

    @Test
    fun `should get 3 exercises with 3 available for USER with all exercises done`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val subGroupId = 2L
        val userId = 2L
        val exercise1 = Exercise(id = 1, name = "pets")
        val exercise2 = Exercise(id = 2, name = "pets")
        val exercise3 = Exercise(id = 3, name = "pets")
        val allExercises = listOf(exercise1, exercise2, exercise3)
        val noiseUrl = "noiseUrl"
        every { studyHistoryRepository.getDoneExercises(subGroupId, userId) } returns allExercises
        every { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) } returns allExercises
        every { urlConversionService.makeUrlForNoise(ofType(String::class)) } returns noiseUrl
        every { userAccountService.getCurrentUserRoles() } returns setOf(BrnRole.USER)

        // WHEN
        val actualResult: List<ExerciseDto> = exerciseService.findExercisesByUserIdAndSubGroupId(userId, subGroupId)

        // THEN
        actualResult shouldHaveSize 3
        actualResult.filter { it.available } shouldHaveSize 3
        verify(exactly = 1) { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) }
        verify(exactly = 1) { studyHistoryRepository.getDoneExercises(ofType(Long::class), ofType(Long::class)) }
    }

    @Test
    fun `should get available exercise ids without loading exercise DTOs for user`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val requestedExerciseId = 1L
        val subGroupId = 2L
        val userId = 3L
        val currentUser = UserAccount(id = userId, email = "user@example.com", fullName = "User")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.USER))

        val exercise1 = exerciseAvailabilityView(id = 1L, name = "pets", level = 1)
        val exercise2 = exerciseAvailabilityView(id = 2L, name = "pets", level = 2)
        val exercise3 = exerciseAvailabilityView(id = 3L, name = "pets", level = 3)

        val lastAttempt = exerciseLastAttemptView(exerciseId = 1L, replaysCount = 0, wrongAnswers = 0)

        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every { exerciseRepository.findExerciseAvailabilityBySubGroupId(subGroupId) } returns
            listOf(
                exercise1,
                exercise2,
                exercise3,
            )
        every { studyHistoryRepository.getDoneExerciseIds(subGroupId, userId) } returns listOf(1L)
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns
            listOf(
                lastAttempt,
            )

        // WHEN
        val actualResult = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId))

        // THEN
        actualResult shouldBe listOf(1L, 2L)
        verify(exactly = 1) { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) }
        verify(exactly = 1) { exerciseRepository.findExerciseAvailabilityBySubGroupId(subGroupId) }
        verify(exactly = 1) { studyHistoryRepository.getDoneExerciseIds(subGroupId, userId) }
        verify(exactly = 1) { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) }
        verify(exactly = 0) { exerciseRepository.findExercisesWithSubGroupBySubGroupId(any()) }
        verify(exactly = 0) { studyHistoryRepository.getDoneExercises(any(), any()) }
    }

    @Test
    fun `should get all subgroup exercise ids for admin without loading exercise DTOs`() {
        // GIVEN
        val requestedExerciseId = 1L
        val subGroupId = 2L
        val currentUser = UserAccount(id = 3L, email = "admin@example.com", fullName = "Admin")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.ADMIN))

        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every { exerciseRepository.findExerciseIdsBySubGroupId(subGroupId) } returns listOf(1L, 2L, 3L)

        // WHEN
        val actualResult = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId))

        // THEN
        actualResult shouldBe listOf(1L, 2L, 3L)
        verify(exactly = 1) { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) }
        verify(exactly = 1) { exerciseRepository.findExerciseIdsBySubGroupId(subGroupId) }
        verify(exactly = 0) { exerciseRepository.findExerciseAvailabilityBySubGroupId(any()) }
        verify(exactly = 0) { studyHistoryRepository.getDoneExerciseIds(any(), any()) }
        verify(exactly = 0) { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(any(), any()) }
        verify(exactly = 0) { exerciseRepository.findExercisesWithSubGroupBySubGroupId(any()) }
    }

    @Test
    fun `should get all subgroup exercise ids for specialist without loading exercise DTOs`() {
        // GIVEN
        val requestedExerciseId = 1L
        val subGroupId = 2L
        val currentUser = UserAccount(id = 3L, email = "specialist@example.com", fullName = "Specialist")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.SPECIALIST))

        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every { exerciseRepository.findExerciseIdsBySubGroupId(subGroupId) } returns listOf(1L, 2L, 3L)

        // WHEN
        val actualResult = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId))

        // THEN
        actualResult shouldBe listOf(1L, 2L, 3L)
        verify(exactly = 1) { exerciseRepository.findExerciseIdsBySubGroupId(subGroupId) }
        verify(exactly = 0) { exerciseRepository.findExerciseAvailabilityBySubGroupId(any()) }
        verify(exactly = 0) { studyHistoryRepository.getDoneExerciseIds(any(), any()) }
        verify(exactly = 0) { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(any(), any()) }
    }

    @Test
    fun `should return all exercise ids for user when every exercise is already done`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val requestedExerciseId = 1L
        val subGroupId = 2L
        val userId = 3L
        val currentUser = UserAccount(id = userId, email = "user@example.com", fullName = "User")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.USER))

        val exercise1 = exerciseAvailabilityView(id = 1L, name = "pets", level = 1)
        val exercise2 = exerciseAvailabilityView(id = 2L, name = "pets", level = 2)
        val exercise3 = exerciseAvailabilityView(id = 3L, name = "pets", level = 3)

        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every { exerciseRepository.findExerciseAvailabilityBySubGroupId(subGroupId) } returns
            listOf(
                exercise1,
                exercise2,
                exercise3,
            )
        every { studyHistoryRepository.getDoneExerciseIds(subGroupId, userId) } returns listOf(1L, 2L, 3L)
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns emptyList()

        // WHEN
        val actualResult = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId))

        // THEN
        actualResult shouldBe listOf(1L, 2L, 3L)
    }

    @Test
    fun `should preserve level order when available exercise names are interleaved`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val requestedExerciseId = 1L
        val subGroupId = 2L
        val userId = 3L
        val currentUser = UserAccount(id = userId, email = "user@example.com", fullName = "User")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.USER))

        val exercise1 = exerciseAvailabilityView(id = 1L, name = "alpha", level = 1)
        val exercise2 = exerciseAvailabilityView(id = 2L, name = "beta", level = 2)
        val exercise3 = exerciseAvailabilityView(id = 3L, name = "alpha", level = 3)
        val exercise4 = exerciseAvailabilityView(id = 4L, name = "beta", level = 4)
        val lastAttempt1 = exerciseLastAttemptView(exerciseId = 1L, replaysCount = 0, wrongAnswers = 0)
        val lastAttempt2 = exerciseLastAttemptView(exerciseId = 2L, replaysCount = 0, wrongAnswers = 0)

        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every {
            exerciseRepository.findExerciseAvailabilityBySubGroupId(subGroupId)
        } returns
            listOf(
                exercise1,
                exercise2,
                exercise3,
                exercise4,
            )
        every { studyHistoryRepository.getDoneExerciseIds(subGroupId, userId) } returns listOf(1L, 2L)
        every {
            studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId)
        } returns
            listOf(
                lastAttempt1,
                lastAttempt2,
            )

        // WHEN
        val actualResult = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId))

        // THEN
        actualResult shouldBe listOf(1L, 2L, 3L, 4L)
    }

    @Test
    fun `should not unlock next exercise when last attempt is missing`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val requestedExerciseId = 1L
        val subGroupId = 2L
        val userId = 3L
        val currentUser = UserAccount(id = userId, email = "user@example.com", fullName = "User")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.USER))

        val exercise1 = exerciseAvailabilityView(id = 1L, name = "pets", level = 1)
        val exercise2 = exerciseAvailabilityView(id = 2L, name = "pets", level = 2)
        val exercise3 = exerciseAvailabilityView(id = 3L, name = "pets", level = 3)

        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every { exerciseRepository.findExerciseAvailabilityBySubGroupId(subGroupId) } returns
            listOf(
                exercise1,
                exercise2,
                exercise3,
            )
        every { studyHistoryRepository.getDoneExerciseIds(subGroupId, userId) } returns listOf(1L)
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns emptyList()

        // WHEN
        val actualResult = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId))

        // THEN
        actualResult shouldBe listOf(1L)
    }

    @Test
    fun `should not unlock next exercise when last attempt is not done well`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val requestedExerciseId = 1L
        val subGroupId = 2L
        val userId = 3L
        val currentUser = UserAccount(id = userId, email = "user@example.com", fullName = "User")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.USER))

        val exercise1 = exerciseAvailabilityView(id = 1L, name = "pets", level = 1)
        val exercise2 = exerciseAvailabilityView(id = 2L, name = "pets", level = 2)
        val exercise3 = exerciseAvailabilityView(id = 3L, name = "pets", level = 3)
        val lastAttempt = exerciseLastAttemptView(exerciseId = 1L, replaysCount = 2, wrongAnswers = 5)

        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every { exerciseRepository.findExerciseAvailabilityBySubGroupId(subGroupId) } returns
            listOf(
                exercise1,
                exercise2,
                exercise3,
            )
        every { studyHistoryRepository.getDoneExerciseIds(subGroupId, userId) } returns listOf(1L)
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns
            listOf(
                lastAttempt,
            )

        // WHEN
        val actualResult = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId))

        // THEN
        actualResult shouldBe listOf(1L)
    }

    @Test
    fun `should return empty available ids when subgroup has no exercises`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val requestedExerciseId = 1L
        val subGroupId = 2L
        val userId = 3L
        val currentUser = UserAccount(id = userId, email = "user@example.com", fullName = "User")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.USER))

        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every { exerciseRepository.findExerciseAvailabilityBySubGroupId(subGroupId) } returns emptyList()
        every { studyHistoryRepository.getDoneExerciseIds(subGroupId, userId) } returns emptyList()
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns emptyList()

        // WHEN
        val actualResult = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId))

        // THEN
        actualResult shouldBe emptyList()
    }

    @Test
    fun `should return empty available set when subgroup has no exercises`() {
        // GIVEN
        val subGroupId = 2L
        val userId = 3L

        // WHEN
        val actualResult = exerciseService.getAvailableExercisesForSubGroup(emptyList(), emptyList(), userId, subGroupId)

        // THEN
        actualResult shouldHaveSize 0
    }

    @Test
    fun `should make only the first exercise of each name available when user has no history at all`() {
        // GIVEN
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val requestedExerciseId = 1L
        val subGroupId = 2L
        val userId = 3L
        val currentUser = UserAccount(id = userId, email = "user@example.com", fullName = "User")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.USER))

        val alphaLevel1 = exerciseAvailabilityView(id = 1L, name = "alpha", level = 1)
        val alphaLevel2 = exerciseAvailabilityView(id = 2L, name = "alpha", level = 2)
        val betaLevel1 = exerciseAvailabilityView(id = 3L, name = "beta", level = 1)

        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every { exerciseRepository.findExerciseAvailabilityBySubGroupId(subGroupId) } returns
            listOf(
                alphaLevel1,
                alphaLevel2,
                betaLevel1,
            )
        every { studyHistoryRepository.getDoneExerciseIds(subGroupId, userId) } returns emptyList()
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns emptyList()

        // WHEN
        val actualResult = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId))

        // THEN
        actualResult shouldBe listOf(1L, 3L)
    }

    @Test
    fun `should make only the first exercise of each name available for subgroup when done list is empty`() {
        // GIVEN
        val subGroupId = 5L
        val userId = 1L
        val alphaLevel1 = Exercise(id = 1, name = "alpha", level = 1)
        val alphaLevel2 = Exercise(id = 2, name = "alpha", level = 2)
        val betaLevel1 = Exercise(id = 3, name = "beta", level = 1)
        val subGroupExercises = listOf(alphaLevel1, alphaLevel2, betaLevel1)
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns emptyList()

        // WHEN
        val actualResult = exerciseService.getAvailableExercisesForSubGroup(emptyList(), subGroupExercises, userId, subGroupId)

        // THEN
        actualResult shouldHaveSize 2
        actualResult shouldContainAll listOf(alphaLevel1, betaLevel1)
    }

    @Test
    fun `isDoneWell should return true when both indices meet the minimums`() {
        // GIVEN — repetitionIndex = 5/(1+5) = 0.833, rightAnswersIndex = 1 - 1/5 = 0.8 (boundary)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val studyHistory =
            StudyHistory(
                exercise = mockk(),
                userAccount = mockk(),
                startTime = LocalDateTime.now(),
                executionSeconds = 100,
                tasksCount = 5,
                wrongAnswers = 1,
                replaysCount = 1,
            )

        // WHEN & THEN
        exerciseService.isDoneWell(studyHistory) shouldBe true
    }

    @Test
    fun `isDoneWell should return false when the repetition index is below the minimum`() {
        // GIVEN — repetitionIndex = 3/(1+3) = 0.75 < 0.8, rightAnswersIndex = 1.0
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val studyHistory =
            StudyHistory(
                exercise = mockk(),
                userAccount = mockk(),
                startTime = LocalDateTime.now(),
                executionSeconds = 100,
                tasksCount = 3,
                wrongAnswers = 0,
                replaysCount = 1,
            )

        // WHEN & THEN
        exerciseService.isDoneWell(studyHistory) shouldBe false
    }

    @Test
    fun `isDoneWell should return false when the right answers index is below the minimum`() {
        // GIVEN — repetitionIndex = 1.0, rightAnswersIndex = 1 - 3/10 = 0.7 < 0.8
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val studyHistory =
            StudyHistory(
                exercise = mockk(),
                userAccount = mockk(),
                startTime = LocalDateTime.now(),
                executionSeconds = 100,
                tasksCount = 10,
                wrongAnswers = 3,
                replaysCount = 0,
            )

        // WHEN & THEN
        exerciseService.isDoneWell(studyHistory) shouldBe false
    }

    @Test
    fun `isDoneWell should judge from the counters and ignore the stored repetition index`() {
        // GIVEN — counters give repetitionIndex = 4/(0+4) = 1.0 (passes), but the stored
        // repetitionIndex field is fabricated below the threshold; the judgement must ignore it.
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val studyHistory =
            StudyHistory(
                exercise = mockk(),
                userAccount = mockk(),
                startTime = LocalDateTime.now(),
                executionSeconds = 100,
                tasksCount = 4,
                wrongAnswers = 0,
                replaysCount = 0,
                repetitionIndex = 0.1f,
            )

        // WHEN & THEN
        exerciseService.isDoneWell(studyHistory) shouldBe true
    }

    @Test
    fun `should get exercise by id`() {
        // GIVEN
        val exerciseMock: Exercise = mockkClass(Exercise::class)
        val taskMock: Task = mockkClass(Task::class)
        val noiseUrl = "noiseUrl"
        val exerciseDtoMock = ExerciseDto(2, 1, "name", 1, NoiseDto(0, noiseUrl))
        every { exerciseMock.toDto() } returns exerciseDtoMock
        every { exerciseRepository.findByIdWithSubGroup(ofType(Long::class)) } returns exerciseMock
        every { urlConversionService.makeUrlForNoise(noiseUrl) }.returns(noiseUrl)
        every { exerciseMock.tasks } returns setOf(taskMock) as MutableSet<Task>
        every { taskService.processAnswerOptions(taskMock) } returns Unit

        // WHEN
        val actualResult: ExerciseDto = exerciseService.findExerciseById(1L)

        // THEN
        actualResult shouldBe exerciseDtoMock
        verify(exactly = 1) { exerciseRepository.findByIdWithSubGroup(ofType(Long::class)) }
    }

    @Test
    fun `should throw EntityNotFoundException when exercise not found by id`() {
        // GIVEN
        val exerciseId = 999L
        every { exerciseRepository.findByIdWithSubGroup(exerciseId) } returns null

        // WHEN & THEN
        shouldThrow<EntityNotFoundException> {
            exerciseService.findExerciseById(exerciseId)
        }
        verify(exactly = 1) { exerciseRepository.findByIdWithSubGroup(exerciseId) }
    }

    @Test
    fun `should get exercise by name and level`() {
        // GIVEN
        val exerciseName = "name"
        val exerciseMock = Exercise(id = 1)
        val exerciseLevel = 1
        every { exerciseRepository.findExerciseByNameAndLevel(exerciseName, exerciseLevel) } returns
            Optional.of(
                exerciseMock,
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
        val exerciseDto = ExerciseDto(id = 1, seriesId = 1, name = "name", noise = NoiseDto(url = "url"))
        every { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) } returns listOf(exerciseMock)
        every { exerciseMock.toDto() } returns (exerciseDto)
        every { urlConversionService.makeUrlForNoise(ofType(String::class)) } returns "updatedNoiseUrl"
        // WHEN
        val actualResults = exerciseService.findExercisesWithTasksBySubGroup(1)
        // THEN
        actualResults shouldContain exerciseDto
        exerciseDto.noise.url shouldBe "updatedNoiseUrl"
        verify(exactly = 1) { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) }
    }

    @Test
    fun `should get exercises by word`() {
        // GIVEN
        val word = "word"
        val exerciseMock: Exercise = mockkClass(Exercise::class)
        val exerciseWithWordsResponseMock = mockkClass(ExerciseWithWordsResponse::class)
        every { exerciseRepository.findExercisesByWord(word) } returns listOf(exerciseMock)
        every { exerciseMock.toDtoWithWords() } returns exerciseWithWordsResponseMock
        // WHEN
        val actualResults = exerciseService.findExercisesByWord(word)
        // THEN
        actualResults shouldContain exerciseWithWordsResponseMock
        verify(exactly = 1) { exerciseRepository.findExercisesByWord(word) }
    }

    @Test
    fun `should keep lazy-loading read methods transactional`() {
        val transactionalReadMethods =
            setOf(
                "findExerciseById",
                "findExercisesByUserId",
                "findExercisesBySubGroupForCurrentUser",
                "findExercisesByUserIdAndSubGroupId",
                "getAvailableExerciseIds",
                "findExercisesWithTasksBySubGroup",
                "findExercisesByWord",
            )

        val methodsByName = ExerciseService::class.java.declaredMethods.associateBy { it.name }

        transactionalReadMethods.forEach { methodName ->
            val method = checkNotNull(methodsByName[methodName]) { "Method $methodName is missing" }
            val annotation =
                checkNotNull(method.getAnnotation(Transactional::class.java)) {
                    "Method $methodName must remain transactional"
                }
            annotation.readOnly shouldBe true
        }
    }

    @Test
    fun `should return 2 availableExercises for one subgroup with last done success`() {
        // GIVEN
        val subGroupId = 5L
        val subGroup =
            SubGroup(
                id = subGroupId,
                series = series,
                level = 1,
                code = "code",
                name = "subGroup name",
            )
        val ex1 = Exercise(id = 1, name = "pets", subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", subGroup = subGroup)
        val ex3 = Exercise(id = 3, name = "pets ddd", subGroup = subGroup)
        val ex4 = Exercise(id = 4, name = "pets ddd", subGroup = subGroup)
        val listAll = listOf(ex1, ex2, ex3, ex4)
        val listDone = listOf(ex1)
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, 1) } returns
            listOf(exerciseLastAttemptView(exerciseId = 1L, tasksCount = 12, replaysCount = 0, wrongAnswers = 5))
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercisesForSubGroup(listDone, listAll, 1, subGroupId)
        // THEN
        actualResult shouldHaveSize 2
        actualResult shouldContainAll listOf(ex1, ex3)
    }

    @Test
    fun `should return availableExercises for one subgroup with last done success`() {
        // GIVEN
        val subGroupId = 5L
        val subGroup =
            SubGroup(
                id = subGroupId,
                series = series,
                level = 1,
                code = "code",
                name = "subGroup name",
            )
        val userId = 1L
        val ex1 = Exercise(id = 1, name = "pets", subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", subGroup = subGroup)
        val ex3 = Exercise(id = 3, name = "pets ddd", subGroup = subGroup)
        val ex4 = Exercise(id = 4, name = "pets ddd", subGroup = subGroup)
        val listAll = listOf(ex1, ex2, ex3, ex4)
        val listDone = listOf(ex1, ex3)
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns
            listOf(
                exerciseLastAttemptView(exerciseId = 1L, tasksCount = 12, replaysCount = 0, wrongAnswers = 0),
                exerciseLastAttemptView(exerciseId = 3L, tasksCount = 12, replaysCount = 1, wrongAnswers = 1),
            )
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercisesForSubGroup(listDone, listAll, 1, subGroupId)
        // THEN
        actualResult shouldHaveSize 4
        actualResult shouldContainAll listOf(ex1, ex2, ex3, ex4)
    }

    @Test
    fun `should return availableExercises for one subgroup with last done UNSUCCESS`() {
        // GIVEN
        val subGroupId = 5L
        val subGroup =
            SubGroup(
                id = subGroupId,
                series = series,
                level = 1,
                code = "code",
                name = "subGroup name",
            )
        val ex1 = Exercise(id = 1, name = "pets", subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", subGroup = subGroup)
        val ex3 = Exercise(id = 3, name = "pets", subGroup = subGroup)
        val ex4 = Exercise(id = 4, name = "pets ddd", subGroup = subGroup)
        val ex5 = Exercise(id = 5, name = "pets ddd", subGroup = subGroup)
        val userAccountId = 1L
        val listAll = listOf(ex1, ex2, ex3, ex4, ex5)
        val listDone = listOf(ex1, ex2)
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userAccountId) } returns
            listOf(
                exerciseLastAttemptView(exerciseId = 1L, tasksCount = 12, replaysCount = 0, wrongAnswers = 0),
                exerciseLastAttemptView(exerciseId = 2L, tasksCount = 12, replaysCount = 1, wrongAnswers = 5),
            )

        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercisesForSubGroup(listDone, listAll, 1, subGroupId)
        // THEN
        actualResult shouldHaveSize 3
        actualResult shouldContainAll listOf(ex1, ex2, ex4)
    }

    @Test
    fun `should return availableExercises for several subgroups`() {
        // GIVEN
        val subGroupId = 5L
        val subGroup1 =
            SubGroup(
                id = subGroupId,
                series = series,
                level = 1,
                code = "code",
                name = "subGroup name",
            )
        val subGroup2 =
            SubGroup(
                id = 6,
                series = series,
                level = 2,
                code = "code2",
                name = "subGroup name2",
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
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userAccountId) } returns
            listOf(
                exerciseLastAttemptView(exerciseId = 1L, tasksCount = 12, replaysCount = 0, wrongAnswers = 0),
                exerciseLastAttemptView(exerciseId = 2L, tasksCount = 12, replaysCount = 2, wrongAnswers = 2),
                exerciseLastAttemptView(exerciseId = 11L, tasksCount = 12, replaysCount = 4, wrongAnswers = 6),
            )

        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)

        // WHEN
        val actualResult = exerciseService.getAvailableExercisesForSubGroup(listDone, listAll, 1, subGroupId)
        // THEN
        actualResult shouldHaveSize 5
        actualResult shouldContainAll listOf(ex1, ex2, ex3, ex11, ex13)
    }

    @Test
    fun `availability is consistent across entity and projection paths for a regular user`() {
        // GIVEN — same subgroup, done ids and last attempts fed to both paths:
        // "pets": ex1 done well -> unlocks ex2; "pets ddd": ex3 done NOT well -> ex4 stays locked.
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val subGroupId = 5L
        val userId = 3L
        val requestedExerciseId = 1L
        val currentUser = UserAccount(id = userId, email = "user@example.com", fullName = "User")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.USER))
        val subGroup = SubGroup(id = subGroupId, series = series, level = 1, code = "code", name = "subGroup name")
        val ex1 = Exercise(id = 1, name = "pets", level = 1, subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", level = 2, subGroup = subGroup)
        val ex3 = Exercise(id = 3, name = "pets ddd", level = 1, subGroup = subGroup)
        val ex4 = Exercise(id = 4, name = "pets ddd", level = 2, subGroup = subGroup)
        val allExercises = listOf(ex1, ex2, ex3, ex4)
        val doneExercises = listOf(ex1, ex3)

        // both paths read the same attempt history
        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every { exerciseRepository.findExerciseAvailabilityBySubGroupId(subGroupId) } returns
            listOf(
                exerciseAvailabilityView(id = 1L, name = "pets", level = 1),
                exerciseAvailabilityView(id = 2L, name = "pets", level = 2),
                exerciseAvailabilityView(id = 3L, name = "pets ddd", level = 1),
                exerciseAvailabilityView(id = 4L, name = "pets ddd", level = 2),
            )
        every { studyHistoryRepository.getDoneExerciseIds(subGroupId, userId) } returns listOf(1L, 3L)
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns
            listOf(
                exerciseLastAttemptView(exerciseId = 1L, replaysCount = 0, wrongAnswers = 0),
                exerciseLastAttemptView(exerciseId = 3L, replaysCount = 0, wrongAnswers = 5),
            )

        // WHEN
        val entityPathIds =
            exerciseService
                .getAvailableExercisesForSubGroup(doneExercises, allExercises, userId, subGroupId)
                .mapNotNull { it.id }
                .toSet()
        val projectionPathIds = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId)).toSet()

        // THEN
        entityPathIds shouldBe projectionPathIds
        entityPathIds shouldBe setOf(1L, 2L, 3L)
    }

    @Test
    fun `unlock stays available after a later unsuccessful attempt on both access paths`() {
        // GIVEN — ex1 (level 1) was done well in an earlier attempt and replayed badly afterwards;
        // the sticky rule keeps ex2 unlocked on both access paths.
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRepetitionIndex", 0.8)
        ReflectionTestUtils.setField(exerciseSuccessCalculator, "minRightAnswersIndex", 0.8)
        val subGroupId = 5L
        val userId = 3L
        val requestedExerciseId = 1L
        val currentUser = UserAccount(id = userId, email = "user@example.com", fullName = "User")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.USER))
        val subGroup = SubGroup(id = subGroupId, series = series, level = 1, code = "code", name = "subGroup name")
        val ex1 = Exercise(id = 1, name = "pets", level = 1, subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", level = 2, subGroup = subGroup)
        val allExercises = listOf(ex1, ex2)
        val doneExercises = listOf(ex1)

        // ex1 history: an earlier successful attempt followed by a recent unsuccessful one
        every { studyHistoryRepository.findAllAttemptsBySubGroupAndUserAccount(subGroupId, userId) } returns
            listOf(
                exerciseLastAttemptView(exerciseId = 1L, replaysCount = 0, wrongAnswers = 0),
                exerciseLastAttemptView(exerciseId = 1L, replaysCount = 5, wrongAnswers = 9),
            )

        // projection path lookups
        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every { exerciseRepository.findExerciseAvailabilityBySubGroupId(subGroupId) } returns
            listOf(
                exerciseAvailabilityView(id = 1L, name = "pets", level = 1),
                exerciseAvailabilityView(id = 2L, name = "pets", level = 2),
            )
        every { studyHistoryRepository.getDoneExerciseIds(subGroupId, userId) } returns listOf(1L)

        // WHEN
        val entityPathIds =
            exerciseService
                .getAvailableExercisesForSubGroup(doneExercises, allExercises, userId, subGroupId)
                .mapNotNull { it.id }
                .toSet()
        val projectionPathIds = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId)).toSet()

        // THEN
        entityPathIds shouldBe projectionPathIds
        entityPathIds shouldBe setOf(1L, 2L)
    }

    @Test
    fun `availability is consistent across both access paths for a privileged role`() {
        // GIVEN — ADMIN sees every exercise available on both access paths
        val subGroupId = 5L
        val userId = 3L
        val requestedExerciseId = 1L
        val currentUser = UserAccount(id = userId, email = "admin@example.com", fullName = "Admin")
        currentUser.roleSet.add(Role(id = 1L, name = BrnRole.ADMIN))
        val subGroup = SubGroup(id = subGroupId, series = series, level = 1, code = "code", name = "subGroup name")
        val ex1 = Exercise(id = 1, name = "pets", level = 1, subGroup = subGroup)
        val ex2 = Exercise(id = 2, name = "pets", level = 2, subGroup = subGroup)
        val ex3 = Exercise(id = 3, name = "pets ddd", level = 1, subGroup = subGroup)
        val allExercises = listOf(ex1, ex2, ex3)

        // entity-based listing path short-circuits for a privileged role
        every { exerciseRepository.findExercisesWithSubGroupBySubGroupId(subGroupId) } returns allExercises
        every { userAccountService.getCurrentUserRoles() } returns setOf(BrnRole.ADMIN)
        every { urlConversionService.makeUrlForNoise(ofType(String::class)) } returns "noiseUrl"

        // projection-based ids path short-circuits for a privileged role
        every { exerciseRepository.findSubGroupIdByExerciseId(requestedExerciseId) } returns subGroupId
        every { userAccountService.getCurrentUser() } returns currentUser
        every { exerciseRepository.findExerciseIdsBySubGroupId(subGroupId) } returns listOf(1L, 2L, 3L)

        // WHEN
        val listingAvailableIds =
            exerciseService
                .findExercisesByUserIdAndSubGroupId(userId, subGroupId)
                .filter { it.available }
                .mapNotNull { it.id }
                .toSet()
        val lookupIds = exerciseService.getAvailableExerciseIds(listOf(requestedExerciseId)).toSet()

        // THEN
        listingAvailableIds shouldBe lookupIds
        lookupIds shouldBe setOf(1L, 2L, 3L)
    }

    @Test
    fun `should be return new exercise from ExerciseWordsCreateDto`() {
        // GIVEN
        val exerciseWordsCreateDto =
            ExerciseWordsCreateDto(
                locale = BrnLocale.RU,
                subGroup = "subGroup",
                level = 1,
                exerciseName = "exerciseName",
                words = listOf("word1", "word2"),
                noiseLevel = 0,
            )
        val exercise = Exercise(name = exerciseWordsCreateDto.exerciseName)
        val wordsRecordProcessor = mockk<SeriesWordsRecordProcessor>()
        every { recordProcessors.stream() } returns Stream.of(wordsRecordProcessor)
        every { wordsRecordProcessor.isApplicable(any()) } returns true
        every { wordsRecordProcessor.process(any(), any()) } returns listOf(exercise)

        // WHEN
        val exerciseDto = exerciseService.createExercise(exerciseWordsCreateDto)

        // THEN
        verify(exactly = 1) { recordProcessors.stream() }
        verify(exactly = 1) { wordsRecordProcessor.isApplicable(any()) }
        verify(exactly = 1) { wordsRecordProcessor.process(any(), any()) }
        exerciseDto.name shouldBe exercise.name
    }

    @Test
    fun `should be throw IllegalArgumentException in createAndGenerateExerciseWords`() {
        // GIVEN
        val exerciseWordsCreateDto =
            ExerciseWordsCreateDto(
                locale = BrnLocale.RU,
                subGroup = "subGroup",
                level = 1,
                exerciseName = "exerciseName",
                words = listOf("word1", "word2"),
                noiseLevel = 0,
            )
        val wordsRecordProcessor = mockk<SeriesWordsRecordProcessor>()
        every { recordProcessors.stream() } returns Stream.of(wordsRecordProcessor)
        every { wordsRecordProcessor.isApplicable(any()) } returns true
        every { wordsRecordProcessor.process(any(), any()) } returns listOf()

        // WHEN
        val exception = shouldThrow<IllegalArgumentException> { exerciseService.createExercise(exerciseWordsCreateDto) }

        // THEN
        verify(exactly = 1) { recordProcessors.stream() }
        verify(exactly = 1) { wordsRecordProcessor.isApplicable(any()) }
        verify(exactly = 1) { wordsRecordProcessor.process(any(), any()) }
        exception.message shouldBe "Exercise with this name (${exerciseWordsCreateDto.exerciseName}) already exist"
    }

    @Test
    fun `should be return new exercise from ExercisePhrasesCreateDto`() {
        // GIVEN
        val exercisePhrasesCreateDto =
            ExercisePhrasesCreateDto(
                locale = BrnLocale.RU,
                subGroup = "subGroup",
                level = 1,
                exerciseName = "exerciseName",
                phrases = Phrases("short phrase", "long phrase"),
                noiseLevel = 0,
            )
        val exercise = Exercise(name = exercisePhrasesCreateDto.exerciseName)
        val seriesPhrasesRecordProcessor = mockk<SeriesPhrasesRecordProcessor>()
        every { recordProcessors.stream() } returns Stream.of(seriesPhrasesRecordProcessor)
        every { seriesPhrasesRecordProcessor.isApplicable(any()) } returns true
        every { seriesPhrasesRecordProcessor.process(any(), any()) } returns listOf(exercise)

        // WHEN
        val exerciseDto = exerciseService.createExercise(exercisePhrasesCreateDto)

        // THEN
        verify(exactly = 1) { recordProcessors.stream() }
        verify(exactly = 1) { seriesPhrasesRecordProcessor.isApplicable(any()) }
        verify(exactly = 1) { seriesPhrasesRecordProcessor.process(any(), any()) }
        exerciseDto.name shouldBe exercise.name
    }

    @Test
    fun `should be throw IllegalArgumentException in createAndGenerateExercisePhrases`() {
        // GIVEN
        val exercisePhrasesCreateDto =
            ExercisePhrasesCreateDto(
                locale = BrnLocale.RU,
                subGroup = "subGroup",
                level = 1,
                exerciseName = "exerciseName",
                phrases = Phrases("short phrase", "long phrase"),
                noiseLevel = 0,
            )
        val seriesPhrasesRecordProcessor = mockk<SeriesPhrasesRecordProcessor>()
        every { recordProcessors.stream() } returns Stream.of(seriesPhrasesRecordProcessor)
        every { seriesPhrasesRecordProcessor.isApplicable(any()) } returns true
        every { seriesPhrasesRecordProcessor.process(any(), any()) } returns listOf()

        // WHEN
        val exception =
            shouldThrow<IllegalArgumentException> { exerciseService.createExercise(exercisePhrasesCreateDto) }

        // THEN
        verify(exactly = 1) { recordProcessors.stream() }
        verify(exactly = 1) { seriesPhrasesRecordProcessor.isApplicable(any()) }
        verify(exactly = 1) { seriesPhrasesRecordProcessor.process(any(), any()) }
        exception.message shouldBe "Exercise with this name (${exercisePhrasesCreateDto.exerciseName}) already exist"
    }

    @Test
    fun `should be return new exercise from ExerciseSentencesCreateDto`() {
        // GIVEN
        val exerciseSentencesCreateDto =
            ExerciseSentencesCreateDto(
                locale = BrnLocale.RU,
                subGroup = "subGroup",
                level = 1,
                exerciseName = "exerciseName",
                orderNumber = 1,
                words = SetOfWords(listOf("count1", "count2")),
            )
        val exercise = Exercise(name = exerciseSentencesCreateDto.exerciseName)
        val seriesMatrixRecordProcessor = mockk<SeriesMatrixRecordProcessor>()
        every { recordProcessors.stream() } returns Stream.of(seriesMatrixRecordProcessor)
        every { seriesMatrixRecordProcessor.isApplicable(any()) } returns true
        every { seriesMatrixRecordProcessor.process(any(), any()) } returns listOf(exercise)

        // WHEN
        val exerciseDto = exerciseService.createExercise(exerciseSentencesCreateDto)

        // THEN
        verify(exactly = 1) { recordProcessors.stream() }
        verify(exactly = 1) { seriesMatrixRecordProcessor.isApplicable(any()) }
        verify(exactly = 1) { seriesMatrixRecordProcessor.process(any(), any()) }
        exerciseDto.name shouldBe exercise.name
    }

    @Test
    fun `should be IllegalArgumentException in createAndGenerateExerciseSentences`() {
        // GIVEN
        val exerciseSentencesCreateDto =
            ExerciseSentencesCreateDto(
                locale = BrnLocale.RU,
                subGroup = "subGroup",
                level = 1,
                exerciseName = "exerciseName",
                orderNumber = 1,
                words = SetOfWords(listOf("count1", "count2")),
            )
        val seriesMatrixRecordProcessor = mockk<SeriesMatrixRecordProcessor>()
        every { recordProcessors.stream() } returns Stream.of(seriesMatrixRecordProcessor)
        every { seriesMatrixRecordProcessor.isApplicable(any()) } returns true
        every { seriesMatrixRecordProcessor.process(any(), any()) } returns listOf()

        // WHEN
        val exception =
            shouldThrow<IllegalArgumentException> { exerciseService.createExercise(exerciseSentencesCreateDto) }

        // THEN
        verify(exactly = 1) { recordProcessors.stream() }
        verify(exactly = 1) { seriesMatrixRecordProcessor.isApplicable(any()) }
        verify(exactly = 1) { seriesMatrixRecordProcessor.process(any(), any()) }
        exception.message shouldBe "Exercise with this name (${exerciseSentencesCreateDto.exerciseName}) already exist"
    }

    private fun exerciseAvailabilityView(
        id: Long,
        name: String,
        level: Int,
    ): ExerciseAvailabilityView {
        val exercise = mockk<ExerciseAvailabilityView>()
        every { exercise.id } returns id
        every { exercise.name } returns name
        every { exercise.level } returns level
        return exercise
    }

    private fun exerciseLastAttemptView(
        exerciseId: Long,
        tasksCount: Short = 10,
        replaysCount: Int,
        wrongAnswers: Int,
    ): ExerciseLastAttemptView {
        val lastAttempt = mockk<ExerciseLastAttemptView>()
        every { lastAttempt.exerciseId } returns exerciseId
        every { lastAttempt.tasksCount } returns tasksCount
        every { lastAttempt.replaysCount } returns replaysCount
        every { lastAttempt.wrongAnswers } returns wrongAnswers
        return lastAttempt
    }
}
