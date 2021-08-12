package com.epam.brn.service.statistic.progress.status.impl.retriever

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsManager
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

/**
 * @author Nikolai Lazarev
 */

@ExtendWith(MockKExtension::class)
internal class DayExercisingStatusRetrieverTest {

    @InjectMockKs
    private lateinit var retriever: DayExercisingStatusRetriever

    @MockK
    private lateinit var studyHistory: StudyHistory

    @MockK
    private lateinit var requirementsManager: StatusRequirementsManager

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
            maximalRequirements = 24 * 60
        )
    )

    @Test
    fun `getWorstStatus should return GREAT status when user progress in the range of the status`() {
        // GIVEN
        every { requirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY) } returns requirementsStatuses
        every { studyHistory.executionSeconds } returns 20 * 60

        // WHEN
        val status = retriever.getStatus(listOf(studyHistory))

        // THEN
        assertEquals(UserExercisingProgressStatus.GREAT, status)
    }

    @Test
    fun `getWorstStatus should return GOOD status when user progress in the range of the status`() {
        // GIVEN
        every { requirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY) } returns requirementsStatuses
        every { studyHistory.executionSeconds } returns 15 * 60

        // WHEN
        val status = retriever.getStatus(listOf(studyHistory))

        // THEN
        assertEquals(UserExercisingProgressStatus.GOOD, status)
    }

    @Test
    fun `getWorstStatus should return BAD status when user progress in the range of the status`() {
        // GIVEN
        every { requirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY) } returns requirementsStatuses
        every { studyHistory.executionSeconds } returns 5 * 60

        // WHEN
        val status = retriever.getStatus(listOf(studyHistory))

        // THEN
        assertEquals(UserExercisingProgressStatus.BAD, status)
    }

    @Test
    fun `getSupportedPeriods should return WEEK and DAY periods`() {
        // GIVEN
        val supportedPeriods = retriever.getSupportedPeriods()

        // WHEN
        val expectedPeriods = listOf(UserExercisingPeriod.WEEK, UserExercisingPeriod.DAY)

        // THEN
        assertEquals(expectedPeriods, supportedPeriods)
    }
}
