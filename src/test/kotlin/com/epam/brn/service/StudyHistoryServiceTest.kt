package com.epam.brn.service

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.Gender
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockKExtension::class)
internal class StudyHistoryServiceTest {

    @SpyK
    @InjectMockKs
    lateinit var studyHistoryService: StudyHistoryService

    @MockK
    lateinit var exerciseRepository: ExerciseRepository

    @MockK
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    lateinit var studyHistoryDtoMock: StudyHistoryDto

    @MockK
    lateinit var currentUser: UserAccountDto

    @MockK
    lateinit var studyHistory: StudyHistory

    @MockK
    lateinit var from: LocalDateTime

    @MockK
    lateinit var to: LocalDateTime

    @Test
    fun `should return today timer`() {
        // GIVEN
        val timer = 1
        every { userAccountService.getUserFromTheCurrentSession() } returns currentUser
        every { currentUser.id } returns 1L
        every { studyHistoryRepository.getTodayDayTimer(1L) } returns timer

        // WHEN
        val todayTimer = studyHistoryService.getTodayTimer()

        // THEN
        todayTimer.shouldBe(timer)
    }

    @Test
    fun `should create studyHistory when doesn't exist`() {
        // GIVEN
        val now = LocalDateTime.now()

        val userAccount = UserAccount(
            id = 1L,
            fullName = "testUserFirstName",
            gender = Gender.MALE.toString(),
            bornYear = 2000,
            password = "test",
            email = "test@gmail.com",
            active = true
        )
        val exercise = Exercise(id = 1L)
        val studyHistoryNew = StudyHistory(
            userAccount = userAccount,
            exercise = exercise,
            startTime = now,
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 3,
            replaysCount = 3
        )
        val studyHistorySaved = StudyHistory(
            id = 1L,
            userAccount = userAccount,
            exercise = exercise,
            startTime = now,
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 3,
            replaysCount = 3
        )
        every { userAccountService.getCurrentUser() } returns userAccount
        every { studyHistoryDtoMock.toEntity(userAccount, exercise) } returns studyHistoryNew
        every { studyHistoryDtoMock.exerciseId } returns 2L

        every { exerciseRepository.findById(2L) } returns Optional.of(exercise)
        every { studyHistoryRepository.save(studyHistoryNew) } returns studyHistorySaved

        // WHEN
        val result = studyHistoryService.save(studyHistoryDtoMock)

        // THEN
        verify(exactly = 1) { studyHistoryRepository.save(ofType(StudyHistory::class)) }
        result.shouldBe(studyHistorySaved.toDto())
    }

    @Test
    fun `should calculate diff in seconds between start and end time`() {
        // GIVEN
        val expectedTimer = 60
        val now = LocalDateTime.now()

        // WHEN
        val result = studyHistoryService.calculateDiffInSeconds(now, now.plusMinutes(1))

        // THEN
        result.shouldBe(expectedTimer)
    }

    @Test
    fun `should return histories for current user`() {
        // GIVEN
        val expectedStudyHistoryDto = listOf(studyHistoryDtoMock)
        every { userAccountService.getUserFromTheCurrentSession() } returns currentUser
        every { currentUser.id } returns 1L
        every { studyHistoryService.getHistories(1L, from, to) } returns expectedStudyHistoryDto

        // WHEN
        val historiesForCurrentUser = studyHistoryService.getHistoriesForCurrentUser(from, to)

        // THEN
        historiesForCurrentUser.shouldBe(expectedStudyHistoryDto)
    }

    @Test
    fun `should return histories`() {
        // GIVEN
        val expectedStudyHistory = listOf(studyHistory)
        every { studyHistory.toDto() } returns studyHistoryDtoMock
        every { studyHistoryRepository.findAllByUserAccountIdAndStartTimeBetween(1L, from, to) } returns expectedStudyHistory

        // WHEN
        val histories = studyHistoryService.getHistories(1L, from, to)

        // THEN
        val expectedStudyHistoryDto = expectedStudyHistory.map { it.toDto() }
        histories.shouldBe(expectedStudyHistoryDto)
    }

    @Test
    fun `should return month histories for current user`() {
        // GIVEN
        val expectedStudyHistoryDto = listOf(studyHistoryDtoMock)
        every { userAccountService.getUserFromTheCurrentSession() } returns currentUser
        every { currentUser.id } returns 1L
        every { studyHistoryService.getMonthHistories(1L, 1, 1) } returns expectedStudyHistoryDto

        // WHEN
        val monthHistoriesForCurrentUser = studyHistoryService.getMonthHistoriesForCurrentUser(1, 1)

        // THEN
        monthHistoriesForCurrentUser.shouldBe(expectedStudyHistoryDto)
    }

    @Test
    fun `should return month histories`() {
        // GIVEN
        val expectedStudyHistory = listOf(studyHistory)
        every { studyHistory.toDto() } returns studyHistoryDtoMock
        every { studyHistoryRepository.getMonthHistories(1L, 1, 1) } returns expectedStudyHistory

        // WHEN
        val histories = studyHistoryService.getMonthHistories(1L, 1, 1)

        // THEN
        val expectedStudyHistoryDto = expectedStudyHistory.map { it.toDto() }
        histories.shouldBe(expectedStudyHistoryDto)
    }
}
