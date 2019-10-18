package com.epam.brn.service

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.StudyHistoryRepository
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyObject
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EntityManager

@ExtendWith(MockitoExtension::class)
internal class StudyHistoryServiceTest {

    @Mock
    lateinit var entityManager: EntityManager
    @Mock
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @InjectMocks
    lateinit var studyHistoryService: StudyHistoryService

    @Test
    fun `should insert study history`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionCount = 1,
            successTasksCount = 1,
            doneTasksCount = 1,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            exerciseId = 1L
        )
        `when`(entityManager.getReference(UserAccount::class.java, dto.userId)).thenReturn(getUserAccount())
        `when`(entityManager.getReference(Exercise::class.java, dto.exerciseId)).thenReturn(getExercise())
        `when`(studyHistoryRepository.save(any(StudyHistory::class.java))).thenReturn(getStudyHistoryEntity(dto))

        // WHEN
        val newUserHistoryId = studyHistoryService.saveStudyHistory(dto)

        // THEN
        verify(studyHistoryRepository).save(anyObject())
        assertNotNull(newUserHistoryId)
    }

    private fun getStudyHistoryEntity(dto: StudyHistoryDto): StudyHistory {
        return StudyHistory(
            id = 10,
            userAccount = getUserAccount(),
            exercise = getExercise(),
            endTime = dto.endTime,
            startTime = dto.startTime,
            doneTasksCount = dto.doneTasksCount,
            successTasksCount = dto.successTasksCount,
            repetitionCount = dto.repetitionCount
        )
    }

    private fun getExercise(): Exercise {
        return Exercise(
            id = 0,
            description = toString(),
            series = Series(
                id = 0,
                description = "desc",
                name = "group",
                exerciseGroup = ExerciseGroup(
                    id = 0,
                    description = "desc",
                    name = "group"
                )
            ),
            level = 0,
            name = "exercise"
        )
    }

    private fun getUserAccount(): UserAccount {
        return UserAccount(
            id = 0,
            name = "manuel",
            birthDate = LocalDate.now(),
            email = "123@123.asd"
        )
    }
}