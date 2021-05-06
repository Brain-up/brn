package com.epam.brn.service.statistic.progress

import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.impl.UserMonthStatisticService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
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
internal class UserMonthStatisticServiceTest {

    @InjectMocks
    private lateinit var userMonthStatisticService: UserMonthStatisticService

    @Mock
    private lateinit var userAccountService: UserAccountService

    @Mock
    private lateinit var studyHistoryRepository: StudyHistoryRepository

    @Mock
    private lateinit var userAccount: UserAccountDto

    @Mock
    private lateinit var studyHistory: StudyHistory

    @Mock
    private lateinit var studyHistorySecond: StudyHistory

    private val month: Int = 2
    private val day: Int = 23
    private val year: Int = 2021
    private val userId: Long = 1L
    private val hour: Int = 13
    private val minute: Int = 20
    private val executionSeconds: Int = 1200
    private val progress = UserExercisingProgressStatus.GREAT
    private val studyHistoryDate = LocalDateTime.of(year, month, day, hour, minute)
    private val secondStudyHistoryDate = LocalDateTime.of(year, month, day, hour, minute).plusMonths(1)
    private val from: LocalDate = LocalDate.of(year, month, day)
    private val to: LocalDate = LocalDate.of(year, month.plus(2), day)

    @BeforeEach
    fun init() {
        `when`(userAccountService.getUserFromTheCurrentSession()).thenReturn(userAccount)
        `when`(userAccount.id).thenReturn(userId)
        `when`(studyHistory.startTime).thenReturn(studyHistoryDate)
        `when`(studyHistory.executionSeconds).thenReturn(executionSeconds)
    }

    @Test
    fun `getStatisticForPeriod should return statistic for period from to`() {
        `when`(studyHistoryRepository.getHistories(userAccount.id!!, Date.valueOf(from), Date.valueOf(to))).thenReturn(
            listOf(
                studyHistory,
                studyHistory
            )
        )
        val statisticsForPeriod = userMonthStatisticService.getStatisticForPeriod(from, to)
        val statistic = statisticsForPeriod.first()
        assertEquals(studyHistory.executionSeconds * 2, statistic.exercisingTime)
        assertEquals(YearMonth.of(studyHistory.startTime.year, studyHistory.startTime.month), statistic.date)
        assertEquals(progress, statistic.progress)
    }

    @Test
    fun `getStatisticForPeriod should return statistic for period when there are histories for some month`() {
        `when`(studyHistorySecond.startTime).thenReturn(secondStudyHistoryDate)
        `when`(studyHistorySecond.executionSeconds).thenReturn(executionSeconds)
        `when`(studyHistoryRepository.getHistories(userAccount.id!!, Date.valueOf(from), Date.valueOf(to))).thenReturn(
            listOf(
                studyHistory,
                studyHistorySecond
            )
        )

        val firstExpectedStudyStatistic = MonthStudyStatistic(
            YearMonth.of(studyHistoryDate.year, studyHistoryDate.month),
            executionSeconds,
            progress
        )
        val secondExpectedStudyStatistic = MonthStudyStatistic(
            YearMonth.of(secondStudyHistoryDate.year, secondStudyHistoryDate.month),
            executionSeconds,
            progress
        )

        val statisticForPeriod = userMonthStatisticService.getStatisticForPeriod(from, to)
        assertTrue(statisticForPeriod.size > 1)
        assertEquals(
            firstExpectedStudyStatistic,
            statisticForPeriod.first { it.date.month == firstExpectedStudyStatistic.date.month }
        )
        assertEquals(
            secondExpectedStudyStatistic,
            statisticForPeriod.first { it.date.month == secondExpectedStudyStatistic.date.month }
        )
    }
}
