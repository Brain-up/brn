package com.epam.brn.controller

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.service.StudyHistoryService
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
internal class StudyHistoryControllerTest {

    @InjectMocks
    lateinit var studyHistoryController: StudyHistoryController
    @Mock
    lateinit var studyHistoryService: StudyHistoryService

    @Test
    fun `should save study history`() {
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
        `when`(studyHistoryService.saveOrReplaceStudyHistory(dto)).thenReturn(1L)

        // WHEN
        studyHistoryController.saveOrReplaceStudyHistory(dto)

        // THEN
        verify(studyHistoryService).saveOrReplaceStudyHistory(dto)
    }

    @Test
    fun `should update study history`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionCount = null,
            successTasksCount = 2,
            doneTasksCount = null,
            startTime = null,
            endTime = null,
            exerciseId = 1L
        )
        `when`(studyHistoryService.patchStudyHistory(dto)).thenReturn(1L)

        // WHEN
        studyHistoryController.patchStudyHistory(dto)

        // THEN
        verify(studyHistoryService).patchStudyHistory(dto)
    }

    @Test
    fun `should replace study history`() {
        // GIVEN
        val dto = StudyHistoryDto(
            userId = 1L,
            repetitionCount = 10,
            successTasksCount = 1,
            doneTasksCount = 1,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now(),
            exerciseId = 1L
        )
        `when`(studyHistoryService.replaceStudyHistory(dto)).thenReturn(1L)

        // WHEN
        studyHistoryController.replaceStudyHistory(dto)

        // THEN
        verify(studyHistoryService).replaceStudyHistory(dto)
    }
}