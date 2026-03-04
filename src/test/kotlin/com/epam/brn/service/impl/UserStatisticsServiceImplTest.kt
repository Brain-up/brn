package com.epam.brn.service.impl

import com.epam.brn.dto.UserAccountDto
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.statistics.impl.UserStatisticServiceImpl
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertNotNull

/**
 * @author Nikolai Lazarev
 */
@ExtendWith(MockKExtension::class)
internal class UserStatisticsServiceImplTest {
    @InjectMockKs
    lateinit var userStatisticService: UserStatisticServiceImpl

    @MockK
    lateinit var subGroupRepository: SubGroupRepository

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @MockK
    lateinit var exerciseRepository: ExerciseRepository

    @MockK
    lateinit var userAccount: UserAccountDto

    private val userAccountID = 1L

    @Test
    fun `should return 0 to 2 user progress for subGroup`() {
        // GIVEN
        val subGroupIds: List<Long> = listOf(777)
        every { studyHistoryRepository.getDoneExercises(any(), any()) } returns emptyList()
        every { userAccountService.getCurrentUserDto() } returns userAccount
        every { userAccount.id } returns userAccountID
        every { exerciseRepository.findExerciseIdsBySubGroupId(any()) } returns listOf(1L, 2L)

        // WHEN
        val result = userStatisticService.getSubGroupStatistic(subGroupIds)

        // THEN
        verify(exactly = 1) { studyHistoryRepository.getDoneExercises(any(), any()) }
        verify(exactly = 1) { exerciseRepository.findExerciseIdsBySubGroupId(any()) }
        assertNotNull(result)
        Assertions.assertTrue(result.first().subGroupId == subGroupIds.first())
        Assertions.assertTrue(result.first().completedExercises == 0)
        Assertions.assertTrue(result.first().totalExercises == 2)
    }

    @Test
    fun `should return empty map when empty IDs list was passed`() {
        // GIVEN
        every { userAccountService.getCurrentUserDto() } returns userAccount

        // WHEN
        val result = userStatisticService.getSubGroupStatistic(emptyList())

        // THEN
        verify(exactly = 1) { userAccountService.getCurrentUserDto() }
        Assertions.assertTrue(result.isEmpty())
    }
}
