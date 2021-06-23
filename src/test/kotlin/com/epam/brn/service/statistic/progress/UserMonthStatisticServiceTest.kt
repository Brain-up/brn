package com.epam.brn.service.statistic.progress

import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.impl.UserMonthStatisticService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.sql.Timestamp
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(MockKExtension::class)
internal class UserMonthStatisticServiceTest {

    @InjectMockKs
    private lateinit var userMonthStatisticService: UserMonthStatisticService

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
        every { userAccountService.getUserFromTheCurrentSession() } returns userAccount
        every { userAccount.id } returns userId
        every { studyHistory.startTime } returns studyHistoryDate
        every { studyHistory.executionSeconds } returns executionSeconds
    }

    @Test
    fun `getStatisticForPeriod should return statistic for period from to`() {
        // GIVEN
        every { studyHistorySecond.startTime } returns studyHistoryDate
        every { studyHistorySecond.executionSeconds } returns executionSeconds
        every {
            studyHistoryRepository.getHistories(
                ofType(Long::class),
                ofType(Timestamp::class),
                ofType(Timestamp::class)
            )
        } returns listOf(studyHistory, studyHistorySecond)

        val expectedStatistic = MonthStudyStatistic(
            date = studyHistory.startTime,
            exercisingTimeSeconds = executionSeconds * 2,
            exercisingDays = 2,
            progress = UserExercisingProgressStatus.GREAT
        )

        // WHEN
        val statisticsForPeriod = userMonthStatisticService.getStatisticForPeriod(from, to)
        val statistic = statisticsForPeriod.first()

        // THEN
        assertEquals(expectedStatistic, statistic)
    }

    @Test
    fun `getStatisticForPeriod should return statistic for period when there are histories for some month`() {
        // GIVEN
        every { studyHistorySecond.startTime } returns secondStudyHistoryDate
        every { studyHistorySecond.executionSeconds } returns executionSeconds
        val studyHistories = listOf(
            studyHistory,
            studyHistorySecond
        )
        every {
            studyHistoryRepository.getHistories(
                ofType(Long::class),
                ofType(Timestamp::class),
                ofType(Timestamp::class)
            )
        } returns studyHistories

        val firstExpectedStudyStatistic = MonthStudyStatistic(
            date = studyHistoryDate,
            exercisingTimeSeconds = executionSeconds,
            exercisingDays = 1,
            progress = progress
        )
        val secondExpectedStudyStatistic = MonthStudyStatistic(
            date = studyHistoryDate,
            exercisingTimeSeconds = executionSeconds,
            exercisingDays = 1,
            progress = progress
        )

        // WHEN
        val statisticForPeriod = userMonthStatisticService.getStatisticForPeriod(from, to)

        // THEN
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
