package com.epam.brn.service

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.model.Exercise
import com.epam.brn.enums.Gender
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class StudyHistoryServiceTest {

    @Mock
    lateinit var userAccountRepository: UserAccountRepository

    @Mock
    lateinit var exerciseRepository: ExerciseRepository

    @Mock
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Mock
    lateinit var userAccountService: UserAccountService

    @InjectMocks
    lateinit var studyHistoryService: StudyHistoryService

    @Test
    fun `should create studyHistory when doesn't exist`() {
        // GIVEN
        val now = LocalDateTime.now()

        val studyHistoryDtoMock = mock(StudyHistoryDto::class.java)
        val userAccount = UserAccount(
            id = 1L,
            fullName = "testUserFirstName",
            gender = Gender.MALE.toString(),
            bornYear = 2000,
            password = "test",
            email = "test@gmail.com",
            active = true
        )
        val exercise = Exercise(id = 1L, name = "name")
        val studyHistoryNew = StudyHistory(
            userAccount = userAccount,
            exercise = exercise,
            startTime = now,
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 3,
            replaysCount = 3
        )
        val studyHistorySaved = StudyHistory(
            id = 1L,
            userAccount = userAccount,
            exercise = exercise,
            startTime = now,
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 3,
            replaysCount = 3
        )
        `when`(userAccountService.getUserFromTheCurrentSession()).thenReturn(
            UserAccountDto(
                1L,
                "ivan",
                "mail",
                2000,
                Gender.MALE,
                true
            )
        )
        `when`(userAccountRepository.findUserAccountById(1L)).thenReturn(Optional.of(userAccount))
        `when`(studyHistoryDtoMock.toEntity(userAccount, exercise)).thenReturn(studyHistoryNew)
        `when`(studyHistoryDtoMock.exerciseId).thenReturn(2L)

        `when`(exerciseRepository.findById(2L)).thenReturn(Optional.of(exercise))
        `when`(studyHistoryRepository.save(studyHistoryNew)).thenReturn(studyHistorySaved)

        // WHEN
        val result = studyHistoryService.save(studyHistoryDtoMock)

        // THEN
        Assertions.assertThat(result).isEqualToIgnoringGivenFields(studyHistorySaved, "id", "exerciseId")
        verify(studyHistoryRepository).save(any(StudyHistory::class.java))
    }

    @Test
    fun `should calculate diff in seconds between start and end time`() {
        val now = LocalDateTime.now()
        val result = studyHistoryService.calculateDiffInSeconds(now, now.plusMinutes(1))
        assertEquals(60, result)
    }
}
