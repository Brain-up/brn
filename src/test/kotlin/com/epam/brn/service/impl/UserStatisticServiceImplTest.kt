package com.epam.brn.service.impl

import com.epam.brn.model.Exercise
import com.epam.brn.model.Gender
import com.epam.brn.model.Series
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.service.UserAccountService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertNotNull

/**
 * @author Nikolai Lazarev
 */
@ExtendWith(MockitoExtension::class)
@DisplayName("User statistic service test using mockito")
internal class UserStatisticServiceImplTest {

    @InjectMocks
    lateinit var userStatisticService: UserStatisticServiceImpl

    @Mock
    lateinit var subGroupRepository: SubGroupRepository

    @Mock
    lateinit var userAccountService: UserAccountService

    @Mock
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Mock
    lateinit var exerciseRepository: ExerciseRepository

    @Mock
    lateinit var series: Series

    @Test
    fun `should return 0 to 2 user progress for subGroup`() {
        val userAccount = UserAccount(
            id = 1L,
            fullName = "testUserFirstName",
            gender = Gender.MALE.toString(),
            bornYear = 2000,
            password = "test",
            email = "test@gmail.com",
            active = true
        )
        val subGroupIds: List<Long> = listOf(777)
        val allExercisesForSubGroup: List<Exercise> = listOf(Exercise(1), Exercise(2))
        `when`(studyHistoryRepository.getDoneExercises(anyLong(), anyLong())).thenReturn(emptyList())
        `when`(userAccountService.getUserFromTheCurrentSession()).thenReturn(userAccount.toDto())
        `when`(exerciseRepository.findExercisesBySubGroupId(anyLong())).thenReturn(allExercisesForSubGroup)

        val result = userStatisticService.getSubGroupStatistic(subGroupIds)

        verify(studyHistoryRepository, times(1)).getDoneExercises(anyLong(), anyLong())
        verify(exerciseRepository, times(1)).findExercisesBySubGroupId(anyLong())

        assertNotNull(result)
        Assertions.assertTrue(result.first().subGroupId.equals(subGroupIds.first()))
        Assertions.assertTrue(result.first().completedExercises.equals(0))
        Assertions.assertTrue(result.first().totalExercises.equals(2))
    }

    @Test
    fun `should return empty map when empty IDs list was passed`() {
        val result = userStatisticService.getSubGroupStatistic(emptyList())

        Assertions.assertTrue(result.isEmpty())
    }
}
