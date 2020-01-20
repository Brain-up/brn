package com.epam.brn.service

import com.epam.brn.converter.StudyHistoryConverter
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.StudyHistoryRepository
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.verify
import java.time.LocalDateTime
import java.util.Optional
import javax.persistence.EntityManager
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus

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
    fun `should create studyHistory when doesnt exist`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionIndex = 1f,
            tasksCount = 1,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            exerciseId = 1L
        )
        val exerciseMock = Mockito.mock(Exercise::class.java)
        val userAccountMock = Mockito.mock(UserAccount::class.java)
        val studyHistoryEntity = StudyHistory(
            userAccount = userAccountMock,
            exercise = exerciseMock
        )
        `when`(entityManager.getReference(UserAccount::class.java, dto.userId)).thenReturn(userAccountMock)
        `when`(entityManager.getReference(Exercise::class.java, dto.exerciseId)).thenReturn(exerciseMock)
        `when`(
            studyHistoryRepository.findByUserAccountIdAndExerciseId(dto.userId, dto.exerciseId)
        ).thenReturn(Optional.empty())
        // WHEN
        val result = studyHistoryService.saveOrUpdateStudyHistory(dto)
        // THEN
        assertEquals(HttpStatus.CREATED, result.responseCode)
        verify(studyHistoryConverter).updateStudyHistory(dto, studyHistoryEntity)
        verify(studyHistoryRepository).save(any(StudyHistory::class.java))
    }

    @Test
    fun `should update existing studyHistory`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionIndex = 2f,
            tasksCount = 1,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            exerciseId = 1L
        )
        val existingStudyHistoryEntity = Mockito.mock(StudyHistory::class.java)
        `when`(
            studyHistoryRepository.findByUserAccountIdAndExerciseId(dto.userId, dto.exerciseId)
        ).thenReturn(Optional.of(existingStudyHistoryEntity))
        // WHEN
        val result = studyHistoryService.saveOrUpdateStudyHistory(dto)
        // THEN
        assertEquals(HttpStatus.OK, result.responseCode)
        verify(studyHistoryConverter).updateStudyHistory(dto, existingStudyHistoryEntity)
        verify(studyHistoryRepository).save(any(StudyHistory::class.java))
    }

    @Test
    fun `should update study history only with not null elements`() {
        // GIVEN
        val exerciseMock = Mockito.mock(Exercise::class.java)
        val userAccountMock = Mockito.mock(UserAccount::class.java)
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
            userAccount = userAccountMock,
            exercise = exerciseMock,
            endTime = LocalDateTime.now(),
            startTime = LocalDateTime.now(),
            tasksCount = 0,
            repetitionIndex = 1f
        )
        val updatedEntity = StudyHistory(
            id = 10,
            userAccount = userAccountMock,
            exercise = exerciseMock,
            endTime = existingEntity.endTime,
            startTime = existingEntity.endTime,
            tasksCount = 5,
            repetitionIndex = 1f
        )
        `when`(studyHistoryRepository.save(any(StudyHistory::class.java))).thenReturn(updatedEntity)
        doNothing().`when`(studyHistoryConverter).updateStudyHistoryWhereNotNull(dto, existingEntity)
        `when`(
            studyHistoryRepository.findByUserAccountIdAndExerciseId(dto.userId, dto.exerciseId)
        ).thenReturn(Optional.of(existingEntity))
        // WHEN
        studyHistoryService.patchStudyHistory(dto)
        // THEN
        verify(studyHistoryRepository).save(any())
    }
}
