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
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import kotlin.test.assertEquals

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
            exerciseId = 1L,
            startTime = LocalDateTime.now().minusMinutes(1),
            endTime = LocalDateTime.now(),
            executionSeconds = 60,
            tasksCount = 1,
            replaysCount = 4,
            wrongAnswers = 3
        )
        `when`(studyHistoryService.save(dto)).thenReturn(dto)

        // WHEN
        val result = studyHistoryController.save(dto)

        // THEN
        verify(studyHistoryService).save(dto)
        assertEquals(HttpStatus.OK, result.statusCode)
    }
}
