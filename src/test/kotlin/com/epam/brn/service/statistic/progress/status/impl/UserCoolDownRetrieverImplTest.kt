package com.epam.brn.service.statistic.progress.status.impl

import com.epam.brn.model.StudyHistory
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.LocalDateTime
import kotlin.test.assertEquals

/**
 * @author Nikolai Lazarev
 */

@ExtendWith(MockKExtension::class)
internal class UserCoolDownRetrieverImplTest {

    @InjectMockKs
    private lateinit var userCoolDownRetrieverImpl: UserCoolDownRetrieverImpl

    @MockK
    private lateinit var studyHistory1: StudyHistory

    @MockK
    private lateinit var studyHistory2: StudyHistory

    @MockK
    private lateinit var studyHistory3: StudyHistory

    private val time: LocalDateTime = LocalDateTime.now()

    @Test
    fun `getMaximalUserCoolDown should return 1 day maximal cool down`() {
        // GIVEN
        every { studyHistory1.startTime } returns time
        every { studyHistory2.startTime } returns time.plusDays(1)
        every { studyHistory3.startTime } returns time.plusDays(2)
        val progress = listOf(studyHistory1, studyHistory3, studyHistory2)

        // WHEN
        val maximalUserCoolDown = userCoolDownRetrieverImpl.getMaximalUserCoolDown(progress)

        // THEN
        assertEquals(1, maximalUserCoolDown)
    }

    @Test
    fun `getMaximalUserCoolDown should return 3 day maximal cool down`() {
        // GIVEN
        every { studyHistory1.startTime } returns time
        every { studyHistory2.startTime } returns time.plusDays(1)
        every { studyHistory3.startTime } returns time.plusDays(4)
        val progress = listOf(studyHistory1, studyHistory3, studyHistory2)

        // WHEN
        val maximalUserCoolDown = userCoolDownRetrieverImpl.getMaximalUserCoolDown(progress)

        // THEN
        assertEquals(3, maximalUserCoolDown)
    }
}
