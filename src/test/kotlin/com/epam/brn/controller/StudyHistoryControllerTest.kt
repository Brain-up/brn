package com.epam.brn.controller

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.service.StudyHistoryService
import com.nhaarman.mockito_kotlin.verify
import java.time.LocalDateTime
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
    fun `should save study history`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionIndex = 1f,
            tasksCount = 1,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            exerciseId = 1L,
            responseCode = HttpStatus.CREATED
        )
        `when`(studyHistoryService.saveOrUpdateStudyHistory(dto)).thenReturn(dto)
        // WHEN
        val result = studyHistoryController.saveOrUpdateStudyHistory(dto)
        // THEN
        verify(studyHistoryService).saveOrUpdateStudyHistory(dto)
        assertEquals(HttpStatus.CREATED, result.statusCode)
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
