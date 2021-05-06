package com.epam.brn.service.statistic.progress

import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.impl.UserDayStatisticService
import com.nhaarman.mockito_kotlin.any
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Nikolai Lazarev
 */

@RunWith(MockitoJUnitRunner::class)
internal class UserDayStatisticServiceTest {

    @InjectMocks
    private lateinit var userDayStatisticService: UserDayStatisticService

    @Mock
    private lateinit var studyHistoryRepository: StudyHistoryRepository

    @Mock
    private lateinit var userAccountService: UserAccountService

    @Mock
    private lateinit var userAccount: UserAccountDto

    @Mock
    private lateinit var studyHistory: StudyHistory

    private val userAccountId = 1L
    private val month: Int = 2
    private val day: Int = 23
    private val year: Int = 2021
    private val hour: Int = 13
    private val minute: Int = 20
    private val studyHistoryDate = LocalDateTime.of(year, month, day, hour, minute)
    private val from: LocalDate = LocalDate.of(year, month, day)
    private val to: LocalDate = LocalDate.of(year, month.plus(2), day)
    private val exercisingSeconds = 3500
    private val userProgress = UserExercisingProgressStatus.GOOD

    @Before
    fun init() {
        `when`(userAccountService.getUserFromTheCurrentSession()).thenReturn(userAccount)
        `when`(userAccount.id).thenReturn(userAccountId)
        `when`(studyHistoryRepository.getHistories(userAccountId, Date.valueOf(from), Date.valueOf(to))).thenReturn(
            listOf(studyHistory)
        )
        `when`(studyHistory.startTime).thenReturn(studyHistoryDate)
        `when`(studyHistory.executionSeconds).thenReturn(exercisingSeconds)
    }

    @Test
    fun `getStatisticForPeriod should return statistic for a day`() {
        val expectedStatistic = DayStudyStatistic(
            date = studyHistoryDate.toLocalDate(),
            exercisingTime = exercisingSeconds,
            progress = userProgress
        )

        val statisticForPeriod = userDayStatisticService.getStatisticForPeriod(from, to)

        assertEquals(expectedStatistic, statisticForPeriod.first())
    }

    @Test
    fun `getStatisticForPeriod should return empty list when there are not histories for the period`() {
        `when`(studyHistoryRepository.getHistories(anyLong(), any(), any())).thenReturn(emptyList())

        val statisticForPeriod = userDayStatisticService.getStatisticForPeriod(from, to)

        assertTrue(statisticForPeriod.isEmpty())
    }
}
