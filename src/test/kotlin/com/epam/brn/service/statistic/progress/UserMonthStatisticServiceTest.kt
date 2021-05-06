package com.epam.brn.service.statistic.progress

import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.Exercise
import com.epam.brn.model.Gender
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.impl.UserMonthStatisticService
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.junit.jupiter.MockitoExtension
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Nikolai Lazarev
 */

@ExtendWith(MockitoExtension::class)
@RunWith(MockitoJUnitRunner::class)
internal class UserMonthStatisticServiceTest {

    @InjectMocks
    private lateinit var userMonthStatisticService: UserMonthStatisticService

    @Mock
    private lateinit var userAccountService: UserAccountService

    @Mock
    private lateinit var studyHistoryRepository: StudyHistoryRepository

    private lateinit var studyHistory: StudyHistory
    private lateinit var userAccount: UserAccount
    private lateinit var exercise: Exercise
    private lateinit var studyHistories: List<StudyHistory>

    private val month: Int = 2
    private val day: Int = 23
    private val year: Int = 2021
    private val hour: Int = 13
    private val minute: Int = 20
    private val progress = UserExercisingProgressStatus.GOOD
    private val studyHistoryDate = LocalDateTime.of(year, month, day, hour, minute)
    private val from: LocalDate = LocalDate.of(year, month, day)
    private val to: LocalDate = LocalDate.of(year, month.plus(2), day)

    @Before
    fun init() {
        userAccount = UserAccount(
            id = 1L,
            fullName = "testUserFirstName",
            gender = Gender.MALE.toString(),
            bornYear = 2000,
            password = "test",
            email = "test@gmail.com",
            active = true
        )
        exercise = Exercise(id = 1L)
        studyHistory = StudyHistory(
            userAccount = userAccount,
            exercise = exercise,
            startTime = studyHistoryDate,
            executionSeconds = 120,
            tasksCount = 12,
            wrongAnswers = 3,
            replaysCount = 3
        )

        studyHistories = listOf(
            studyHistory,
            studyHistory
        )

        `when`(userAccountService.getUserFromTheCurrentSession()).thenReturn(userAccount.toDto())
        `when`(studyHistoryRepository.getHistories(userAccount.id!!, Date.valueOf(from), Date.valueOf(to))).thenReturn(
            studyHistories
        )
    }

    @Test
    fun `getStatisticForPeriod should return statistic for period from to`() {
        val statisticsForPeriod = userMonthStatisticService.getStatisticForPeriod(from, to)
        val statistic = statisticsForPeriod.first()
        assertEquals(studyHistories.sumBy { it.executionSeconds }, statistic.exercisingTime)
        assertEquals(YearMonth.of(studyHistory.startTime.year, studyHistory.startTime.month), statistic.date)
        assertEquals(progress, statistic.progress)
    }

    @Test
    fun `getStatisticForPeriod should return statistic for period when there are histories for some month`() {
        val secondStudyHistory = StudyHistory(
            userAccount = userAccount,
            exercise = exercise,
            startTime = studyHistoryDate.plusMonths(2),
            executionSeconds = 120,
            tasksCount = 12,
            wrongAnswers = 3,
            replaysCount = 3
        )
        studyHistories = listOf(
            studyHistory,
            secondStudyHistory
        )

        val expectedStatisticFirst = MonthStudyStatistic(
            YearMonth.of(studyHistory.startTime.year, studyHistory.startTime.month),
            studyHistory.executionSeconds,
            progress
        )
        val expectedStatisticSecond = MonthStudyStatistic(
            YearMonth.of(secondStudyHistory.startTime.year, secondStudyHistory.startTime.month),
            secondStudyHistory.executionSeconds,
            progress
        )

        `when`(studyHistoryRepository.getHistories(userAccount.id!!, Date.valueOf(from), Date.valueOf(to))).thenReturn(
            studyHistories
        )

        val statisticForPeriod = userMonthStatisticService.getStatisticForPeriod(from, to)
        assertTrue(statisticForPeriod.size > 1)
        assertTrue(statisticForPeriod.contains(expectedStatisticFirst))
        assertTrue(statisticForPeriod.contains(expectedStatisticSecond))
    }
}
