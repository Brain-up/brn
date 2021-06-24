package com.epam.brn.service.statistic.progress

import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistic.impl.UserDayStatisticService
import com.epam.brn.service.statistic.progress.status.ProgressStatusManager
import com.nhaarman.mockito_kotlin.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Nikolai Lazarev
 */

@ExtendWith(MockitoExtension::class)
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

    @Mock
    private lateinit var progressManager: ProgressStatusManager<List<StudyHistory>>

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
    private val userProgress = UserExercisingProgressStatus.GREAT

    @BeforeEach
    fun init() {
        `when`(userAccountService.getUserFromTheCurrentSession()).thenReturn(userAccount)
        `when`(userAccount.id).thenReturn(userAccountId)
    }

    @Test
    fun `getStatisticForPeriod should return statistic for a day`() {
        // GIVEN
        val studyHistories = listOf(studyHistory)
        `when`(progressManager.getStatus(UserExercisingPeriod.DAY, studyHistories)).thenReturn(UserExercisingProgressStatus.GREAT)
        `when`(studyHistory.startTime).thenReturn(studyHistoryDate)
        `when`(studyHistory.executionSeconds).thenReturn(exercisingSeconds)
        `when`(studyHistoryRepository.getHistories(anyLong(), any(), any())).thenReturn(
            studyHistories
        )
        val expectedStatistic = DayStudyStatistic(
            date = studyHistoryDate.toLocalDate(),
            exercisingTimeSeconds = exercisingSeconds,
            progress = userProgress
        )

        // WHEN
        val statisticForPeriod = userDayStatisticService.getStatisticForPeriod(from, to)

        // THEN
        assertEquals(expectedStatistic, statisticForPeriod.first())
    }

    @Test
    fun `getStatisticForPeriod should return empty list when there are not histories for the period`() {
        // GIVEN
        `when`(studyHistoryRepository.getHistories(anyLong(), any(), any())).thenReturn(emptyList())

        // WHEN
        val statisticForPeriod = userDayStatisticService.getStatisticForPeriod(from, to)

        // THEN
        assertTrue(statisticForPeriod.isEmpty())
    }
}
