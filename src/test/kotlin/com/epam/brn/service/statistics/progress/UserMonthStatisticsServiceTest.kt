package com.epam.brn.service.statistics.progress

import com.epam.brn.dto.UserAccountDto
import com.epam.brn.dto.statistics.MonthStudyStatistics
import com.epam.brn.dto.statistics.UserExercisingPeriod
import com.epam.brn.dto.statistics.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistics.impl.UserMonthStatisticsService
import com.epam.brn.service.statistics.progress.status.ProgressStatusManager
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
internal class UserMonthStatisticsServiceTest {
    @InjectMockKs
    private lateinit var userMonthStatisticsService: UserMonthStatisticsService

    @MockK
    private lateinit var userAccountService: UserAccountService

    @MockK
    private lateinit var studyHistoryRepository: StudyHistoryRepository

    @MockK
    private lateinit var userAccount: UserAccountDto

    @MockK
    private lateinit var studyHistory: StudyHistory

    @MockK
    private lateinit var studyHistorySecond: StudyHistory

    @MockK
    private lateinit var studyHistoryThird: StudyHistory

    @MockK
    private lateinit var progressStatusManager: ProgressStatusManager<List<StudyHistory>>

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
    private val from = LocalDateTime.of(year, month, day, hour, minute)
    private val to = LocalDateTime.of(year, month.plus(2), day, hour, minute)

    @BeforeEach
    fun init() {
        every { userAccountService.getCurrentUserDto() } returns userAccount
        every { userAccount.id } returns userId
        every { studyHistory.startTime } returns studyHistoryDate
        every { studyHistory.executionSeconds } returns executionSeconds
    }

    @Test
    fun `getStatisticForPeriod should return statistics for period from to`() {
        // GIVEN
        every { studyHistorySecond.startTime } returns studyHistoryDate
        every { studyHistorySecond.executionSeconds } returns executionSeconds
        val studyHistories = listOf(studyHistory, studyHistorySecond)
        every {
            studyHistoryRepository.getHistories(userId, from, to)
        } returns studyHistories
        every {
            progressStatusManager.getStatus(
                UserExercisingPeriod.WEEK,
                studyHistories,
            )
        } returns progress

        val expectedStatistic =
            MonthStudyStatistics(
                date = studyHistory.startTime,
                exercisingTimeSeconds = executionSeconds * 2,
                exercisingDays = 1,
                progress = progress,
            )

        // WHEN
        val statisticsForPeriod = userMonthStatisticsService.getStatisticsForPeriod(from, to)
        val statistics = statisticsForPeriod.first()

        // THEN
        assertEquals(expectedStatistic, statistics)
    }

    @Test
    fun `getStatisticsForPeriod should return statistics for period when there are histories for some month`() {
        val studyHistories =
            listOf(
                studyHistory,
                studyHistorySecond,
            )
        // GIVEN
        every { studyHistorySecond.startTime } returns secondStudyHistoryDate
        every { studyHistorySecond.executionSeconds } returns executionSeconds

        every { studyHistoryThird.startTime } returns secondStudyHistoryDate
        every { studyHistoryThird.executionSeconds } returns executionSeconds

        every { progressStatusManager.getStatus(UserExercisingPeriod.WEEK, listOf(studyHistory)) } returns progress
        every {
            progressStatusManager.getStatus(
                UserExercisingPeriod.WEEK,
                listOf(studyHistorySecond),
            )
        } returns progress
        every {
            studyHistoryRepository.getHistories(userId, from, to)
        } returns studyHistories

        val firstExpectedStudyStatistic =
            MonthStudyStatistics(
                date = studyHistoryDate,
                exercisingTimeSeconds = executionSeconds,
                exercisingDays = 1,
                progress = progress,
            )
        val secondExpectedStudyStatistic =
            MonthStudyStatistics(
                date = studyHistoryDate,
                exercisingTimeSeconds = executionSeconds,
                exercisingDays = 1,
                progress = progress,
            )

        // WHEN
        val statisticForPeriod = userMonthStatisticsService.getStatisticsForPeriod(from, to)

        // THEN
        assertEquals(2, statisticForPeriod.size)
        assertEquals(
            firstExpectedStudyStatistic,
            statisticForPeriod.first { it.date.month == firstExpectedStudyStatistic.date.month },
        )
        assertEquals(
            secondExpectedStudyStatistic,
            statisticForPeriod.first { it.date.month == secondExpectedStudyStatistic.date.month },
        )
    }

    @Test
    fun `getStatisticForPeriod should return empty list when there are not study histories for the period`() {
        // GIVEN
        every { studyHistoryRepository.getHistories(userId, from, to) } returns emptyList()

        // WHEN
        val statisticForPeriod = userMonthStatisticsService.getStatisticsForPeriod(from, to)

        // THEN
        assertTrue(statisticForPeriod.isEmpty())
    }
}
