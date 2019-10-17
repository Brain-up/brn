package com.epam.brn.controller

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.service.StudyHistoryService
import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
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
        doNothing().`when`(studyHistoryService).saveStudyHistory(dto)

        // WHEN
        studyHistoryController.createStudyHistory(dto)

        // THEN
        verify(studyHistoryService).saveStudyHistory(dto)
    }
}