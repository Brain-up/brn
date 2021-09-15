package com.epam.brn.service.statistic.progress.status.impl

import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.service.UserAccountService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class UserRestTimeRetrieverImplTest {

    @InjectMockKs
    private lateinit var userCoolDownRetrieverImpl: UserRestTimeRetrieverImpl

    @MockK
    private lateinit var studyHistoryRepository: StudyHistoryRepository

    @MockK
    private lateinit var studyHistoryComparator: Comparator<StudyHistory>

    @MockK
    private lateinit var userAccountService: UserAccountService

    @MockK
    private lateinit var studyHistory1: StudyHistory

    @MockK
    private lateinit var studyHistory2: StudyHistory

    @MockK
    private lateinit var studyHistory3: StudyHistory

    @MockK
    private lateinit var userAccount: UserAccountResponse

    private val time: LocalDateTime = LocalDateTime.now()
    private val userId: Long = 1

    @BeforeEach
    fun init() {
        every { userAccountService.getUserFromTheCurrentSession() } returns userAccount
        every { userAccount.id } returns userId
        every { studyHistoryComparator.compare(any(), any()) } returns -1
    }

    @Test
    fun `getMaximalUserCoolDown should return 1 day maximal cool down`() {
        // GIVEN
        val progress = listOf(studyHistory1, studyHistory3, studyHistory2)
        val sqlDate = Date.valueOf(LocalDate.now())
        val date = LocalDate.now()
        every { studyHistoryRepository.getHistories(userId, sqlDate, sqlDate) } returns progress
        every { studyHistory1.startTime } returns time
        every { studyHistory2.startTime } returns time.plusDays(1)
        every { studyHistory3.startTime } returns time.plusDays(2)

        // WHEN
        val maximalUserCoolDown = userCoolDownRetrieverImpl.getMaximalUserRestTime(userId, date, date)

        // THEN
        assertEquals(1, maximalUserCoolDown)
    }

    @Test
    fun `getMaximalUserCoolDown should return 3 day maximal cool down`() {
        // GIVEN
        val sqlDate = Date.valueOf(LocalDate.now())
        val date = LocalDate.now()
        val progress = listOf(studyHistory1, studyHistory3, studyHistory2)
        every { studyHistoryRepository.getHistories(userId, sqlDate, sqlDate) } returns progress
        every { studyHistory1.startTime } returns time
        every { studyHistory2.startTime } returns time.plusDays(1)
        every { studyHistory3.startTime } returns time.plusDays(4)

        // WHEN
        val maximalUserCoolDown = userCoolDownRetrieverImpl.getMaximalUserRestTime(userId, date, date)

        // THEN
        assertEquals(3, maximalUserCoolDown)
    }
}
