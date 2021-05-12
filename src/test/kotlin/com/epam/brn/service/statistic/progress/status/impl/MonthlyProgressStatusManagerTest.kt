package com.epam.brn.service.statistic.progress.status.impl

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsManager
import com.nhaarman.mockito_kotlin.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

/**
 * @author Nikolai Lazarev
 */

@ExtendWith(MockitoExtension::class)
internal class MonthlyProgressStatusManagerTest {

    @InjectMocks
    private lateinit var manager: MonthlyProgressStatusManager

    @Mock
    private lateinit var statusRequirementsManager: StatusRequirementsManager

    @Mock
    private lateinit var coolDownRetriever: UserCoolDownRetrieverImpl

    @Mock
    private lateinit var studyHistory: StudyHistory

    private val weeklyRequirements = listOf(
        StatusRequirements(
            UserExercisingProgressStatus.BAD,
            0,
            5
        ),
        StatusRequirements(
            UserExercisingProgressStatus.GOOD,
            5,
            6
        ),
        StatusRequirements(
            UserExercisingProgressStatus.GREAT,
            6,
            8
        )
    )

    private val dailyRequirements = listOf(
        StatusRequirements(
            status = UserExercisingProgressStatus.BAD,
            minimalRequirements = 0,
            maximalRequirements = 15
        ),
        StatusRequirements(
            status = UserExercisingProgressStatus.GOOD,
            minimalRequirements = 15,
            maximalRequirements = 20
        ),
        StatusRequirements(
            status = UserExercisingProgressStatus.GREAT,
            minimalRequirements = 20,
            maximalRequirements = 60 * 24
        )
    )

    @BeforeEach
    fun init() {
        `when`(statusRequirementsManager.getPeriodRequirements(UserExercisingPeriod.WEEK)).thenReturn(weeklyRequirements)
        `when`(statusRequirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY)).thenReturn(dailyRequirements)
    }

    @Test
    fun `getStatus should return great status when both month and days goals achieved`() {
        `when`(coolDownRetriever.getMaximalUserCoolDown(any())).thenReturn(1)
        `when`(studyHistory.executionSeconds).thenReturn(20 * 60)

        val status = manager.getStatus(listOf(studyHistory))

        assertEquals(UserExercisingProgressStatus.GREAT, status)
    }

    @Test
    fun `getStatus should return good status when user cool down is 2 days`() {
        `when`(coolDownRetriever.getMaximalUserCoolDown(any())).thenReturn(2)
        `when`(studyHistory.executionSeconds).thenReturn(20 * 60)

        val status = manager.getStatus(listOf(studyHistory))

        assertEquals(UserExercisingProgressStatus.GOOD, status)
    }

    @Test
    fun `getStatus should return bad status when user cool down more than 2 days`() {
        `when`(coolDownRetriever.getMaximalUserCoolDown(any())).thenReturn(3)
        `when`(studyHistory.executionSeconds).thenReturn(20 * 60)

        val status = manager.getStatus(listOf(studyHistory))

        assertEquals(UserExercisingProgressStatus.BAD, status)
    }

    @Test
    fun `getStatus should return good status when minimal user exercising time in range for good status`() {
        `when`(coolDownRetriever.getMaximalUserCoolDown(any())).thenReturn(1)
        `when`(studyHistory.executionSeconds).thenReturn(16 * 60)

        val status = manager.getStatus(listOf(studyHistory))

        assertEquals(UserExercisingProgressStatus.GOOD, status)
    }

    @Test
    fun `getStatus should return bad status when minimal user exercising time in range for bad status`() {
        `when`(coolDownRetriever.getMaximalUserCoolDown(any())).thenReturn(1)
        `when`(studyHistory.executionSeconds).thenReturn(14 * 60)

        val status = manager.getStatus(listOf(studyHistory))

        assertEquals(UserExercisingProgressStatus.BAD, status)
    }
}
