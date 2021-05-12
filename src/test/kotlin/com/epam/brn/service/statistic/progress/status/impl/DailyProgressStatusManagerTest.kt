package com.epam.brn.service.statistic.progress.status.impl

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsManager
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
internal class DailyProgressStatusManagerTest {

    @InjectMocks
    private lateinit var manager: DailyProgressStatusManager

    @Mock
    private lateinit var statusRequirementsManager: StatusRequirementsManager

    private val requirementsStatuses = listOf(
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
        `when`(statusRequirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY)).thenReturn(
            requirementsStatuses
        )
    }

    @Test
    fun `getStatus should return BAD status when user progress in the range of the status`() {
        val userProgress = 5 * 60

        val status = manager.getStatus(userProgress)

        assertEquals(UserExercisingProgressStatus.BAD, status)
    }

    @Test
    fun `getStatus should return GOOD status when user progress in the range of the status`() {
        val userProgress = 15 * 60

        val status = manager.getStatus(userProgress)

        assertEquals(UserExercisingProgressStatus.GOOD, status)
    }

    @Test
    fun `getStatus should return GREAT status when user progress in the range of the status`() {
        val userProgress = 20 * 60

        val status = manager.getStatus(userProgress)

        assertEquals(UserExercisingProgressStatus.GREAT, status)
    }
}
