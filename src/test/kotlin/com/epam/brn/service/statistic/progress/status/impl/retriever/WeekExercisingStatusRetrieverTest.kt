package com.epam.brn.service.statistic.progress.status.impl.retriever

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.UserCoolDownRetriever
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
internal class WeekExercisingStatusRetrieverTest {

    @InjectMocks
    private lateinit var retriever: WeekExercisingStatusRetriever

    @Mock
    private lateinit var requirementsManager: StatusRequirementsManager

    @Mock
    private lateinit var coolDownRetriever: UserCoolDownRetriever

    @Mock
    private lateinit var studyHistory: StudyHistory

    private val requirementsStatuses = listOf(
        StatusRequirements(
            status = UserExercisingProgressStatus.BAD,
            minimalRequirements = 0,
            maximalRequirements = 5
        ),
        StatusRequirements(
            status = UserExercisingProgressStatus.GOOD,
            minimalRequirements = 5,
            maximalRequirements = 6
        ),
        StatusRequirements(
            status = UserExercisingProgressStatus.GREAT,
            minimalRequirements = 6,
            maximalRequirements = 8
        )
    )

    @Test
    fun `getWorstStatus should return GREAT status when user progress in the range of the status`() {
        // GIVEN
        val period = listOf(studyHistory)
        `when`(coolDownRetriever.getMaximalUserCoolDown(period)).thenReturn(1)
        `when`(requirementsManager.getPeriodRequirements(UserExercisingPeriod.WEEK)).thenReturn(requirementsStatuses)

        // WHEN
        val worstStatus = retriever.getWorstStatus(period)

        // THEN
        assertEquals(UserExercisingProgressStatus.GREAT, worstStatus)
    }

    @Test
    fun `getWorstStatus should return GOOD status when user progress in the range of the status`() {
        // GIVEN
        val period = listOf(studyHistory)
        `when`(coolDownRetriever.getMaximalUserCoolDown(period)).thenReturn(2)
        `when`(requirementsManager.getPeriodRequirements(UserExercisingPeriod.WEEK)).thenReturn(requirementsStatuses)

        // WHEN
        val worstStatus = retriever.getWorstStatus(period)

        // THEN
        assertEquals(UserExercisingProgressStatus.GOOD, worstStatus)
    }

    @Test
    fun `getWorstStatus should return BAD status when user progress in the range of the status`() {
        // GIVEN
        val period = listOf(studyHistory)
        `when`(coolDownRetriever.getMaximalUserCoolDown(period)).thenReturn(5)
        `when`(requirementsManager.getPeriodRequirements(UserExercisingPeriod.WEEK)).thenReturn(requirementsStatuses)

        // WHEN
        val worstStatus = retriever.getWorstStatus(period)

        // THEN
        assertEquals(UserExercisingProgressStatus.BAD, worstStatus)
    }

    @Test
    fun `getSupportedPeriods should return WEEK period`() {
        // GIVEN
        val supportedPeriods = retriever.getSupportedPeriods()

        // WHEN
        val expectedPeriods = listOf(UserExercisingPeriod.WEEK)

        // THEN
        assertEquals(expectedPeriods, supportedPeriods)
    }
}
