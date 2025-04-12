package com.epam.brn.service.statistics.progress.status.impl

import com.epam.brn.dto.statistics.UserExercisingPeriod
import com.epam.brn.dto.statistics.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistics.progress.status.ExercisingStatusRetriever
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

/**
 * @author Nikolai Lazarev
 */

@ExtendWith(MockKExtension::class)
internal class StudyHistoriesProgressStatusManagerTest {
    @InjectMockKs
    private lateinit var managerStudyHistories: StudyHistoriesProgressStatusManager

    @MockK
    private lateinit var weekRetriever: ExercisingStatusRetriever<List<StudyHistory>>

    @MockK
    private lateinit var dayRetriever: ExercisingStatusRetriever<List<StudyHistory>>

    @MockK
    private lateinit var studyHistory: StudyHistory

    @SpyK
    private var retrievers = ArrayList<ExercisingStatusRetriever<List<StudyHistory>>>()

    // @Test
    // todo: do not work in Kotlin 2.1 - fix later
    fun `getStatus should call only retrievers which support WEEK period and return GOOD status`() {
        // GIVEN
        retrievers.add(weekRetriever)
        retrievers.add(dayRetriever)
        val progress = listOf(studyHistory)
        every { dayRetriever.getSupportedPeriods() } returns listOf(UserExercisingPeriod.WEEK, UserExercisingPeriod.DAY)
        every { weekRetriever.getSupportedPeriods() } returns listOf(UserExercisingPeriod.WEEK)
        every { dayRetriever.getStatus(any()) } returns UserExercisingProgressStatus.GOOD
        every { weekRetriever.getStatus(any()) } returns UserExercisingProgressStatus.GOOD

        // WHEN
        val status = managerStudyHistories.getStatus(UserExercisingPeriod.WEEK, progress)

        // THEN
        verify(exactly = 1) { dayRetriever.getStatus(progress) }
        verify(exactly = 1) { weekRetriever.getStatus(progress) }
        assertEquals(UserExercisingProgressStatus.GOOD, status)
    }

    // @Test
    // todo: do not work in Kotlin 2.1 - fix later
    fun `getStatus should call only retrievers which support DAY period and return GOOD status`() {
        // GIVEN
        retrievers.add(weekRetriever)
        retrievers.add(dayRetriever)
        val progress = listOf(studyHistory)
        every { dayRetriever.getSupportedPeriods() } returns
            listOf(
                UserExercisingPeriod.WEEK,
                UserExercisingPeriod.DAY,
            )
        every { weekRetriever.getSupportedPeriods() } returns listOf(UserExercisingPeriod.WEEK)
        every { dayRetriever.getStatus(any()) } returns UserExercisingProgressStatus.GOOD

        // WHEN
        val status = managerStudyHistories.getStatus(UserExercisingPeriod.DAY, progress)

        // THEN
        verify(exactly = 1) { dayRetriever.getStatus(progress) }
        verify(exactly = 0) { weekRetriever.getStatus(progress) }
        assertEquals(UserExercisingProgressStatus.GOOD, status)
    }
}
