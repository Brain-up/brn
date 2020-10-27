package com.epam.brn.service

import com.epam.brn.converter.StudyHistoryConverter
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
internal class StudyHistoryServiceTest {

    @Mock
    lateinit var userAccountRepository: UserAccountRepository

    @Mock
    lateinit var exerciseRepository: ExerciseRepository

    @Mock
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Mock
    lateinit var studyHistoryConverter: StudyHistoryConverter

    @InjectMocks
    lateinit var studyHistoryService: StudyHistoryService

    @Test
    fun `should create studyHistory when doesn't exist`() {
        // GIVEN
        val now = LocalDateTime.now()

        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionIndex = 1f,
            tasksCount = 1,
            startTime = now,
            endTime = now,
            exerciseId = 1L,
            rightAnswersIndex = 0.75f
        )
        val userAccount = UserAccount(
            id = 1L,
            firstName = "testUserFirstName",
            lastName = "testUserLastName",
            password = "test",
            email = "test@gmail.com",
            active = true
        )
        val exercise = Exercise(
            id = 1L
        )
        val studyHistoryEntity = StudyHistory(
            id = 1L,
            userAccount = userAccount,
            exercise = exercise,
            startTime = now,
            endTime = now,
            tasksCount = 1,
            repetitionIndex = 1f,
            rightAnswersIndex = 0.75f
        )

        `when`(userAccountRepository.findUserAccountById(dto.userId)).thenReturn(Optional.of(userAccount))
        `when`(exerciseRepository.findById(dto.exerciseId)).thenReturn(Optional.of(exercise))
        `when`(studyHistoryConverter.updateStudyHistory(eq(dto), anyOrNull()))
            .thenReturn(studyHistoryEntity)
        `when`(studyHistoryRepository.save(studyHistoryEntity)).thenReturn(studyHistoryEntity)

        // WHEN
        val result = studyHistoryService.save(dto)

        // THEN
        assertThat(result).isEqualToIgnoringGivenFields(dto, "id")
        verify(studyHistoryConverter).updateStudyHistory(eq(dto), anyOrNull())
        verify(studyHistoryRepository).save(any(StudyHistory::class.java))
    }
}
