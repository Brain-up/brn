package com.epam.brn.service.statistic.progress.status.impl.retriever

import com.epam.brn.dto.statistic.StatusRequirements
import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.service.statistic.progress.status.UserRestTimeRetriever
import com.epam.brn.service.statistic.progress.status.requirements.StatusRequirementsManager
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class WeekExercisingStatusRetrieverTest {

    @InjectMockKs
    private lateinit var retriever: WeekExercisingStatusRetriever

    @MockK
    private lateinit var requirementsManager: StatusRequirementsManager

    @MockK
    private lateinit var coolDownRetriever: UserRestTimeRetriever

    @MockK
    private lateinit var studyHistory: StudyHistory

    @MockK
    private lateinit var userAccount: UserAccount

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
        val userAccountId = 1L
        val time = LocalDateTime.now()
        every { studyHistory.userAccount } returns userAccount
        every { userAccount.id } returns userAccountId
        every { studyHistory.startTime } returns time
        every {
            coolDownRetriever.getMaximalUserRestTime(
                userAccountId,
                time.toLocalDate(),
                time.toLocalDate()
            )
        } returns 1
        every { requirementsManager.getPeriodRequirements(UserExercisingPeriod.WEEK) } returns requirementsStatuses

        // WHEN
        val worstStatus = retriever.getWorstStatus(period)

        // THEN
        assertEquals(UserExercisingProgressStatus.GREAT, worstStatus)
    }

    @Test
    fun `getWorstStatus should return GOOD status when user progress in the range of the status`() {
        // GIVEN
        val period = listOf(studyHistory)
        val userAccountId = 1L
        val time = LocalDateTime.now()
        every { studyHistory.userAccount } returns userAccount
        every { userAccount.id } returns userAccountId
        every { studyHistory.startTime } returns time
        every {
            coolDownRetriever.getMaximalUserRestTime(
                userAccountId,
                time.toLocalDate(),
                time.toLocalDate()
            )
        } returns 2
        every { requirementsManager.getPeriodRequirements(UserExercisingPeriod.WEEK) } returns requirementsStatuses

        // WHEN
        val worstStatus = retriever.getWorstStatus(period)

        // THEN
        assertEquals(UserExercisingProgressStatus.GOOD, worstStatus)
    }

    @Test
    fun `getWorstStatus should return BAD status when user progress in the range of the status`() {
        // GIVEN
        val period = listOf(studyHistory)
        val userAccountId = 1L
        val time = LocalDateTime.now()
        every { studyHistory.userAccount } returns userAccount
        every { userAccount.id } returns userAccountId
        every { studyHistory.startTime } returns time
        every {
            coolDownRetriever.getMaximalUserRestTime(
                userAccountId,
                time.toLocalDate(),
                time.toLocalDate()
            )
        } returns 5
        every { requirementsManager.getPeriodRequirements(UserExercisingPeriod.WEEK) } returns requirementsStatuses

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
