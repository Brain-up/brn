package com.epam.brn.service

import com.epam.brn.converter.StudyHistoryConverter
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.StudyHistoryRepository
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Optional
import javax.persistence.EntityManager

@ExtendWith(MockitoExtension::class)
internal class StudyHistoryServiceTest {

    @Mock
    lateinit var entityManager: EntityManager
    @Mock
    lateinit var studyHistoryRepository: StudyHistoryRepository
    @Mock
    lateinit var studyHistoryConverter: StudyHistoryConverter
    @InjectMocks
    lateinit var studyHistoryService: StudyHistoryService

    @Test
    fun `should insert study history when doesnt exist`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionIndex = 1f,
            tasksCount = 1,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            exerciseId = 1L
        )
        val entity = StudyHistory(
            id = null,
            userAccount = getUserAccount(),
            exercise = getExercise(),
            endTime = dto.endTime,
            startTime = dto.startTime,
            tasksCount = dto.tasksCount,
            repetitionIndex = dto.repetitionIndex
        )
        `when`(entityManager.getReference(UserAccount::class.java, dto.userId)).thenReturn(getUserAccount())
        `when`(entityManager.getReference(Exercise::class.java, dto.exerciseId)).thenReturn(getExercise())
        `when`(studyHistoryRepository.save(any(StudyHistory::class.java))).thenReturn(entity)
        `when`(
            studyHistoryRepository.findByUserAccountIdAndExerciseId(
                dto.userId, dto.exerciseId
            )
        ).thenReturn(Optional.empty())
        // WHEN
        studyHistoryService.saveOrReplaceStudyHistory(dto)

        // THEN
        verify(studyHistoryRepository).save(entity)
    }

    @Test
    fun `should replace study history when already exists`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionIndex = 2f,
            tasksCount = 1,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            exerciseId = 1L
        )
        val existingEntity = StudyHistory(
            id = 10,
            userAccount = getUserAccount(),
            exercise = getExercise(),
            endTime = dto.endTime,
            startTime = dto.startTime,
            tasksCount = dto.tasksCount,
            repetitionIndex = dto.repetitionIndex
        )
        val updatedEntity = StudyHistory(
            id = 10,
            userAccount = getUserAccount(),
            exercise = getExercise(),
            endTime = dto.endTime,
            startTime = dto.startTime,
            tasksCount = dto.tasksCount,
            repetitionIndex = dto.repetitionIndex
        )
        `when`(studyHistoryRepository.save(any(StudyHistory::class.java))).thenReturn(updatedEntity)
        `when`(
            studyHistoryRepository.findByUserAccountIdAndExerciseId(
                dto.userId,
                dto.exerciseId
            )
        ).thenReturn(Optional.of(existingEntity))
        // WHEN
        studyHistoryService.saveOrReplaceStudyHistory(dto)

        // THEN
        verify(studyHistoryRepository).save(updatedEntity)
    }

    @Test
    fun `should update study history only with not null elements`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionIndex = null,
            tasksCount = 5,
            startTime = null,
            endTime = null,
            exerciseId = 1L
        )
        val existingEntity = StudyHistory(
            id = 10,
            userAccount = getUserAccount(),
            exercise = getExercise(),
            endTime = LocalDateTime.now(),
            startTime = LocalDateTime.now(),
            tasksCount = 0,
            repetitionIndex = 1f
        )
        val updatedEntity = StudyHistory(
            id = 10,
            userAccount = getUserAccount(),
            exercise = getExercise(),
            endTime = existingEntity.endTime,
            startTime = existingEntity.endTime,
            tasksCount = 5,
            repetitionIndex = 1f
        )
        `when`(studyHistoryRepository.save(any(StudyHistory::class.java))).thenReturn(updatedEntity)
        doNothing().`when`(studyHistoryConverter).updateStudyHistoryWhereNotNull(dto, existingEntity)
        `when`(
            studyHistoryRepository.findByUserAccountIdAndExerciseId(
                dto.userId, dto.exerciseId
            )
        ).thenReturn(Optional.of(existingEntity))
        // WHEN
        studyHistoryService.patchStudyHistory(dto)

        // THEN
        verify(studyHistoryRepository).save(any())
    }

    @Test
    fun `should replace study history`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionIndex = 2f,
            tasksCount = null,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            exerciseId = 1L
        )
        val existingEntity = StudyHistory(
            id = 10,
            userAccount = getUserAccount(),
            exercise = getExercise(),
            endTime = null,
            startTime = dto.startTime,
            tasksCount = 0,
            repetitionIndex = 1f
        )
        val updatedEntity = StudyHistory(
            id = 10,
            userAccount = getUserAccount(),
            exercise = getExercise(),
            endTime = dto.endTime,
            startTime = dto.startTime,
            tasksCount = null,
            repetitionIndex = 2f
        )
        `when`(studyHistoryRepository.save(any(StudyHistory::class.java))).thenReturn(updatedEntity)
        doNothing().`when`(studyHistoryConverter).updateStudyHistory(dto, existingEntity)
        `when`(
            studyHistoryRepository.findByUserAccountIdAndExerciseId(
                dto.userId, dto.exerciseId
            )
        ).thenReturn(Optional.of(existingEntity))
        // WHEN
        studyHistoryService.replaceStudyHistory(dto)

        // THEN
        verify(studyHistoryRepository).save(any())
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