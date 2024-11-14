package com.epam.brn.service

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.dto.statistics.UserDailyDetailStatisticsDto
import com.epam.brn.enums.BrnGender
import com.epam.brn.model.Exercise
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
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@ExtendWith(MockKExtension::class)
internal class StudyHistoryServiceTest {

    @SpyK
    @InjectMockKs
    lateinit var studyHistoryService: StudyHistoryService

    @MockK
    lateinit var exerciseRepositoryMock: ExerciseRepository

    @MockK
    lateinit var studyHistoryRepositoryMock: StudyHistoryRepository

    @MockK
    lateinit var userAccountServiceMock: UserAccountService

    @MockK
    lateinit var studyHistoryDtoMock: StudyHistoryDto

    @MockK
    lateinit var currentUserMock: UserAccountDto

    @MockK
    lateinit var studyHistoryMock: StudyHistory

    @MockK
    lateinit var fromMock: LocalDateTime

    @MockK
    lateinit var toMock: LocalDateTime

    @Test
    fun `should return today timer`() {
        // GIVEN
        val timer = 1
        every { userAccountServiceMock.getCurrentUserDto() } returns currentUserMock
        every { currentUserMock.id } returns 1L
        every { studyHistoryRepositoryMock.getTodayDayTimer(1L) } returns timer

        // WHEN
        val todayTimer = studyHistoryService.getTodayTimer()

        // THEN
        todayTimer shouldBe timer
    }

    @Test
    fun `should create studyHistory when doesn't exist`() {
        // GIVEN
        val now = LocalDateTime.now()

        val userAccount = UserAccount(
            id = 1L,
            fullName = "testUserFirstName",
            gender = BrnGender.MALE.toString(),
            bornYear = 2000,
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
        every { userAccountServiceMock.getCurrentUser() } returns userAccount
        every { studyHistoryDtoMock.toEntity(userAccount, exercise) } returns studyHistoryNew
        every { studyHistoryDtoMock.exerciseId } returns 2L

        every { exerciseRepositoryMock.findById(2L) } returns Optional.of(exercise)
        every { studyHistoryRepositoryMock.save(studyHistoryNew) } returns studyHistorySaved

        // WHEN
        val result = studyHistoryService.save(studyHistoryDtoMock)

        // THEN
        verify(exactly = 1) { studyHistoryRepositoryMock.save(ofType(StudyHistory::class)) }
        result shouldBe studyHistorySaved.toDto()
    }

    @Test
    fun `should calculate diff in seconds between start and end time`() {
        // GIVEN
        val expectedTimer = 60
        val now = LocalDateTime.now()

        // WHEN
        val result = studyHistoryService.calculateDiffInSeconds(now, now.plusMinutes(1))

        // THEN
        result shouldBe expectedTimer
    }

    @Test
    fun `should return histories for current user`() {
        // GIVEN
        val expectedStudyHistoryDto = listOf(studyHistoryDtoMock)
        every { userAccountServiceMock.getCurrentUserDto() } returns currentUserMock
        every { currentUserMock.id } returns 1L
        every { studyHistoryService.getHistories(1L, fromMock, toMock) } returns expectedStudyHistoryDto

        // WHEN
        val historiesForCurrentUser = studyHistoryService.getHistoriesForCurrentUser(fromMock, toMock)

        // THEN
        historiesForCurrentUser shouldBe (expectedStudyHistoryDto)
    }

    @Test
    fun `should return histories`() {
        // GIVEN
        val expectedStudyHistory = listOf(studyHistoryMock)
        every { studyHistoryMock.toDto() } returns studyHistoryDtoMock
        every { studyHistoryRepositoryMock.getHistories(1L, fromMock, toMock) } returns expectedStudyHistory

        // WHEN
        val histories = studyHistoryService.getHistories(1L, fromMock, toMock)

        // THEN
        val expectedStudyHistoryDto = expectedStudyHistory.map { it.toDto() }
        histories shouldBe expectedStudyHistoryDto
    }

    @Test
    fun `should return month histories for current user`() {
        // GIVEN
        val expectedStudyHistoryDto = listOf(studyHistoryDtoMock)
        every { userAccountServiceMock.getCurrentUserDto() } returns currentUserMock
        every { currentUserMock.id } returns 1L
        every { studyHistoryService.getMonthHistories(1L, 1, 1) } returns expectedStudyHistoryDto

        // WHEN
        val monthHistoriesForCurrentUser = studyHistoryService.getMonthHistoriesForCurrentUser(1, 1)

        // THEN
        monthHistoriesForCurrentUser shouldBe (expectedStudyHistoryDto)
    }

    @Test
    fun `should return month histories`() {
        // GIVEN
        val expectedStudyHistory = listOf(studyHistoryMock)
        every { studyHistoryMock.toDto() } returns studyHistoryDtoMock
        every { studyHistoryRepositoryMock.getMonthHistories(1L, 1, 1) } returns expectedStudyHistory

        // WHEN
        val histories = studyHistoryService.getMonthHistories(1L, 1, 1)

        // THEN
        val expectedStudyHistoryDto = expectedStudyHistory.map { it.toDto() }
        histories shouldBe expectedStudyHistoryDto
    }

    @Test
    fun `getDailyStatistics should return statistics for day`() {
        // GIVEN
        val userId = 1L
        val exerciseDate = LocalDateTime.now()
        val seriesId = 1L
        val exerciseId = 1L
        val seriesName = "seriesName"
        val listenWordsCount = 15

        val studyHistoryMockK = mockk<StudyHistory>()
        every { studyHistoryMockK.exercise.subGroup!!.series.name } returns seriesName
        every { studyHistoryMockK.exercise.subGroup!!.series.id } returns seriesId
        every { studyHistoryMockK.exercise.id } returns exerciseId
        every { studyHistoryMockK.tasksCount } returns listenWordsCount.toShort()
        every { studyHistoryMockK.spentTimeInSeconds } returns 3600L

        val userDailyDetailStatistics = listOf(studyHistoryMockK)
        every {
            studyHistoryRepositoryMock.getHistories(userId, any(), any())
        } returns userDailyDetailStatistics

        val expectedStatistic = UserDailyDetailStatisticsDto(
            seriesName = seriesName,
            allDoneExercises = 1,
            uniqueDoneExercises = 1,
            repeatedExercises = 0,
            doneExercisesSuccessfullyFromFirstTime = 1,
            listenWordsCount = listenWordsCount,
            duration = 60.toDuration(DurationUnit.MINUTES)
        )

        // WHEN
        val statisticsForPeriod = studyHistoryService.getUserDailyStatistics(exerciseDate, userId)
        val statistics = statisticsForPeriod.first()

        // THEN
        assertEquals(expectedStatistic, statistics)
    }

    @Test
    fun `getDailyStatistics without user should return statistics for day for user from current session`() {
        // GIVEN
        val userId = 1L
        val exerciseDate = LocalDateTime.now()
        val seriesId = 1L
        val exerciseId = 1L
        val seriesName = "seriesName"
        val repeatedExercises = 0
        val listenWordsCount = 15

        val studyHistoryMockK = mockk<StudyHistory>()
        every { studyHistoryMockK.exercise.subGroup!!.series.name } returns seriesName
        every { studyHistoryMockK.exercise.subGroup!!.series.id } returns seriesId
        every { studyHistoryMockK.exercise.id } returns exerciseId
        every { studyHistoryMockK.tasksCount } returns listenWordsCount.toShort()
        every { userAccountServiceMock.getCurrentUserDto().id } returns userId
        every { studyHistoryMockK.spentTimeInSeconds } returns 3600L

        val userDailyDetailStatistics = listOf(studyHistoryMockK)
        every {
            studyHistoryRepositoryMock.getHistories(userId, any(), any())
        } returns userDailyDetailStatistics

        val expectedStatistic = UserDailyDetailStatisticsDto(
            seriesName = seriesName,
            allDoneExercises = 1,
            uniqueDoneExercises = 1,
            repeatedExercises = repeatedExercises,
            doneExercisesSuccessfullyFromFirstTime = 1,
            listenWordsCount = listenWordsCount,
            duration = 60.toDuration(DurationUnit.MINUTES)
        )

        // WHEN
        val statisticsForPeriod = studyHistoryService.getUserDailyStatistics(exerciseDate)
        val statistics = statisticsForPeriod.first()

        // THEN
        assertEquals(expectedStatistic, statistics)
    }

    @Test
    fun `getDailyStatistics (2 equals exercise) should return statistics for day`() {
        // GIVEN
        val userId = 1L
        val exerciseDate = LocalDateTime.now()
        val seriesId = 1L
        val exerciseId = 1L
        val seriesName = "seriesName"
        val listenWordsCount = 15

        val studyHistoryMockK = mockk<StudyHistory>()
        every { studyHistoryMockK.exercise.subGroup!!.series.name } returns seriesName
        every { studyHistoryMockK.exercise.subGroup!!.series.id } returns seriesId
        every { studyHistoryMockK.exercise.id } returns exerciseId
        every { studyHistoryMockK.tasksCount } returns listenWordsCount.toShort()
        every { studyHistoryMockK.spentTimeInSeconds } returns 3600L
        val userDailyDetailStatistics = listOf(studyHistoryMockK, studyHistoryMockK)
        every {
            studyHistoryRepositoryMock.getHistories(userId, any(), any())
        } returns userDailyDetailStatistics

        val expectedStatistic = UserDailyDetailStatisticsDto(
            seriesName = seriesName,
            allDoneExercises = userDailyDetailStatistics.size,
            uniqueDoneExercises = 1,
            repeatedExercises = userDailyDetailStatistics.size,
            doneExercisesSuccessfullyFromFirstTime = 0,
            listenWordsCount = userDailyDetailStatistics.size * listenWordsCount,
            duration = 120.toDuration(DurationUnit.MINUTES)
        )

        // WHEN
        val statisticsForPeriod = studyHistoryService.getUserDailyStatistics(exerciseDate, userId)

        // THEN
        assertEquals(1, statisticsForPeriod.size)
        val statistics = statisticsForPeriod.first()
        assertEquals(expectedStatistic, statistics)
    }

    @Test
    fun `getDailyStatistics (2 different series with one repeated exercise) should return statistics for day`() {
        // GIVEN
        val userId = 1L
        val exerciseDate = LocalDateTime.now()
        val seriesId = 1L
        val exerciseId = 1L
        val seriesName = "seriesName"
        val listenWordsCount = 15
        val spentTimeInSeconds1 = 4515L

        val seriesId2 = 2L
        val exerciseId2 = 10L
        val seriesName2 = "seriesName2"
        val listenWordsCount2 = 5
        val spentTimeInSeconds2 = 1931L

        val exerciseId3 = 5L
        val listenWordsCount3 = 10
        val spentTimeInSeconds3 = 3600L

        val studyHistoryMockK = mockk<StudyHistory>()
        every { studyHistoryMockK.exercise.subGroup!!.series.name }.returnsMany(
            seriesName,
            seriesName2,
            seriesName2,
            seriesName2
        )
        every { studyHistoryMockK.exercise.subGroup!!.series.id }.returnsMany(
            seriesId,
            seriesId2,
            seriesId2,
            seriesId2
        )
        every { studyHistoryMockK.exercise.id }.returnsMany(exerciseId, exerciseId2, exerciseId3, exerciseId2)
        every { studyHistoryMockK.tasksCount }.returnsMany(
            listenWordsCount.toShort(),
            listenWordsCount2.toShort(),
            listenWordsCount3.toShort(),
            listenWordsCount2.toShort()
        )
        every { studyHistoryMockK.spentTimeInSeconds }.returnsMany(
            spentTimeInSeconds1,
            spentTimeInSeconds2,
            spentTimeInSeconds3
        )

        val userDailyDetailStatistics = listOf(
            studyHistoryMockK,
            studyHistoryMockK,
            studyHistoryMockK,
            studyHistoryMockK
        )
        every {
            studyHistoryRepositoryMock.getHistories(userId, any(), any())
        } returns userDailyDetailStatistics

        val expectedStatistic1 = UserDailyDetailStatisticsDto(
            seriesName = seriesName,
            allDoneExercises = 1,
            uniqueDoneExercises = 1,
            repeatedExercises = 0,
            doneExercisesSuccessfullyFromFirstTime = 1,
            listenWordsCount = listenWordsCount,
            duration = Duration.parse("75m 15s")
        )

        val expectedStatistic2 = UserDailyDetailStatisticsDto(
            seriesName = seriesName2,
            allDoneExercises = 3,
            uniqueDoneExercises = 2,
            repeatedExercises = 2,
            doneExercisesSuccessfullyFromFirstTime = 1,
            listenWordsCount = 2 * listenWordsCount2 + listenWordsCount3,
            duration = Duration.parse("152m 11s")
        )
        // WHEN
        val statisticsForPeriod = studyHistoryService.getUserDailyStatistics(exerciseDate, userId)

        // THEN
        assertEquals(2, statisticsForPeriod.size)

        assertEquals(expectedStatistic1, statisticsForPeriod[0])
        assertEquals(expectedStatistic2, statisticsForPeriod[1])
    }

    @Test
    fun `getDailyStatistics should return empty list when there are not study histories for the day`() {
        // GIVEN
        val userId = 1L
        val day = LocalDateTime.now()
        every {
            studyHistoryRepositoryMock.getHistories(
                userId,
                any(),
                any()
            )
        } returns emptyList()

        // WHEN
        val statisticForPeriod = studyHistoryService.getUserDailyStatistics(day, userId)

        // THEN
        assertTrue(statisticForPeriod.isEmpty())
    }
}
