package com.epam.brn.service.statistic.progress.status.impl

import com.epam.brn.dto.statistic.UserExercisingPeriod
import com.epam.brn.dto.statistic.UserExercisingProgressStatus
import com.epam.brn.model.StudyHistory
import com.epam.brn.service.statistic.progress.status.ExercisingStatusRetriever
import com.nhaarman.mockito_kotlin.any
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertEquals

/**
 * @author Nikolai Lazarev
 */

@ExtendWith(MockitoExtension::class)
internal class ProgressStatusManagerImplTest {

    @InjectMocks
    private lateinit var manager: ProgressStatusManagerImpl

    @Mock
    private lateinit var weekRetriever: ExercisingStatusRetriever<List<StudyHistory>>

    @Mock
    private lateinit var dayRetriever: ExercisingStatusRetriever<List<StudyHistory>>

    @Mock
    private lateinit var studyHistory: StudyHistory

    @Spy
    private var retrievers = ArrayList<ExercisingStatusRetriever<List<StudyHistory>>>()

    @Test
    fun `getStatus should call only retrievers which support WEEK period and return GOOD status`() {
        retrievers.add(weekRetriever)
        retrievers.add(dayRetriever)
        val progress = listOf(studyHistory)

        `when`(dayRetriever.getSupportedPeriods()).thenReturn(
            listOf(
                UserExercisingPeriod.WEEK,
                UserExercisingPeriod.DAY
            )
        )
        `when`(weekRetriever.getSupportedPeriods()).thenReturn(listOf(UserExercisingPeriod.WEEK))
        `when`(dayRetriever.getWorstStatus(any())).thenReturn(UserExercisingProgressStatus.GOOD)
        `when`(weekRetriever.getWorstStatus(any())).thenReturn(UserExercisingProgressStatus.GOOD)

        val status = manager.getStatus(UserExercisingPeriod.WEEK, progress)

        verify(dayRetriever, times(1)).getWorstStatus(progress)
        verify(weekRetriever, times(1)).getWorstStatus(progress)

        assertEquals(UserExercisingProgressStatus.GOOD, status)
    }

    @Test
    fun `getStatus should call only retrievers which support DAY period and return GOOD status`() {
        retrievers.add(weekRetriever)
        retrievers.add(dayRetriever)
        val progress = listOf(studyHistory)

        `when`(dayRetriever.getSupportedPeriods()).thenReturn(
            listOf(
                UserExercisingPeriod.WEEK,
                UserExercisingPeriod.DAY
            )
        )
        `when`(weekRetriever.getSupportedPeriods()).thenReturn(listOf(UserExercisingPeriod.WEEK))
        `when`(dayRetriever.getWorstStatus(any())).thenReturn(UserExercisingProgressStatus.GOOD)

        val status = manager.getStatus(UserExercisingPeriod.DAY, progress)

        verify(dayRetriever, times(1)).getWorstStatus(progress)
        verify(weekRetriever, times(0)).getWorstStatus(progress)

        assertEquals(UserExercisingProgressStatus.GOOD, status)
    }
}
