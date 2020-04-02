package com.epam.brn.controller

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.service.StudyHistoryService
import com.nhaarman.mockito_kotlin.verify
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus

@ExtendWith(MockitoExtension::class)
internal class StudyHistoryControllerTest {

    @Mock
    lateinit var studyHistoryService: StudyHistoryService

    @InjectMocks
    lateinit var studyHistoryController: StudyHistoryController

    @Test
    fun `should create new study history`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionIndex = 1f,
            tasksCount = 1,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            exerciseId = 1L
        )
        `when`(studyHistoryService.findBy(dto.userId, dto.exerciseId)).thenReturn(Optional.empty())
        `when`(studyHistoryService.create(dto)).thenReturn(dto)

        // WHEN
        val result = studyHistoryController.save(dto)

        // THEN
        verify(studyHistoryService).create(dto)
        assertEquals(HttpStatus.CREATED, result.statusCode)
    }

    @Test
    fun `should update existing study history`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionIndex = 1f,
            tasksCount = 1,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            exerciseId = 1L
        )
        val studyHistory = StudyHistory(
            userAccount = UserAccount(
                firstName = "testUserFirstName",
                lastName = "testUserLastName",
                password = "test",
                email = "test@gmail.com",
                active = true
            ),
            exercise = Exercise(
                id = 1L
            )
        )

        `when`(studyHistoryService.findBy(dto.userId, dto.exerciseId)).thenReturn(Optional.of(studyHistory))
        `when`(studyHistoryService.update(studyHistory, dto)).thenReturn(dto)

        // WHEN
        val result = studyHistoryController.save(dto)

        // THEN
        verify(studyHistoryService).update(studyHistory, dto)
        assertEquals(HttpStatus.OK, result.statusCode)
    }

    @Test
    fun `should update study history`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionIndex = 1f,
            tasksCount = null,
            startTime = null,
            endTime = null,
            exerciseId = 1L
        )
        `when`(studyHistoryService.patchStudyHistory(dto)).thenReturn(dto)
        // WHEN
        studyHistoryController.patchStudyHistory(dto)
        // THEN
        verify(studyHistoryService).patchStudyHistory(dto)
    }
}
