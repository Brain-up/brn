package com.epam.brn.service.statistic.progress.status.impl.retriever

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsManager
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
internal class DayExercisingStatusRetrieverTest {

    @InjectMocks
    private lateinit var retriever: DayExercisingStatusRetriever

    @Mock
    private lateinit var studyHistory: StudyHistory

    @Mock
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
        `when`(requirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY)).thenReturn(requirementsStatuses)
        `when`(studyHistory.executionSeconds).thenReturn(20 * 60)

        val status = retriever.getWorstStatus(listOf(studyHistory))

        assertEquals(UserExercisingProgressStatus.GREAT, status)
    }

    @Test
    fun `getWorstStatus should return GOOD status when user progress in the range of the status`() {
        `when`(requirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY)).thenReturn(requirementsStatuses)
        `when`(studyHistory.executionSeconds).thenReturn(15 * 60)

        val status = retriever.getWorstStatus(listOf(studyHistory))

        assertEquals(UserExercisingProgressStatus.GOOD, status)
    }

    @Test
    fun `getWorstStatus should return BAD status when user progress in the range of the status`() {
        `when`(requirementsManager.getPeriodRequirements(UserExercisingPeriod.DAY)).thenReturn(requirementsStatuses)
        `when`(studyHistory.executionSeconds).thenReturn(5 * 60)

        val status = retriever.getWorstStatus(listOf(studyHistory))

        assertEquals(UserExercisingProgressStatus.BAD, status)
    }

    @Test
    fun `getSupportedPeriods should return WEEK and DAY periods`() {
        val supportedPeriods = retriever.getSupportedPeriods()

        val expectedPeriods = listOf(UserExercisingPeriod.WEEK, UserExercisingPeriod.DAY)

        assertEquals(expectedPeriods, supportedPeriods)
    }
}
