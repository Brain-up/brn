package com.epam.brn.service.statistic.progress.status.impl

import com.epam.brn.model.StudyHistory
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import kotlin.test.assertEquals

/**
 * @author Nikolai Lazarev
 */

@ExtendWith(MockitoExtension::class)
internal class UserCoolDownRetrieverImplTest {

    @InjectMocks
    private lateinit var userCoolDownRetrieverImpl: UserCoolDownRetrieverImpl

    @Mock
    private lateinit var studyHistory1: StudyHistory

    @Mock
    private lateinit var studyHistory2: StudyHistory

    @Mock
    private lateinit var studyHistory3: StudyHistory

    private val time: LocalDateTime = LocalDateTime.now()

    @Test
    fun `getMaximalUserCoolDown should return 1 day maximal cool down`() {
        `when`(studyHistory1.startTime).thenReturn(time)
        `when`(studyHistory2.startTime).thenReturn(time.plusDays(1))
        `when`(studyHistory3.startTime).thenReturn(time.plusDays(2))
        val progress = listOf(studyHistory1, studyHistory3, studyHistory2)

        val maximalUserCoolDown = userCoolDownRetrieverImpl.getMaximalUserCoolDown(progress)

        assertEquals(1, maximalUserCoolDown)
    }

    @Test
    fun `getMaximalUserCoolDown should return 3 day maximal cool down`() {
        `when`(studyHistory1.startTime).thenReturn(time)
        `when`(studyHistory2.startTime).thenReturn(time.plusDays(1))
        `when`(studyHistory3.startTime).thenReturn(time.plusDays(4))
        val progress = listOf(studyHistory1, studyHistory3, studyHistory2)

        val maximalUserCoolDown = userCoolDownRetrieverImpl.getMaximalUserCoolDown(progress)

        assertEquals(3, maximalUserCoolDown)
    }
}
