package com.epam.brn.integration

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.StudyHistoryDto
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class StudyHistoryControllerV2IT : BaseIT() {

    private val baseUrl = "/v2/study-history"
    private val fromParameterName = "from"
    private val toParameterName = "to"

    @Test
    fun `getHistories should return histories for period of time`() {
        // GIVEN
        val user = insertDefaultUser()
        val exercise = insertDefaultExercise()
        val exercisingYear = 2019
        val exercisingMonth = 3
        val studyHistoryFirst =
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 13, 0), 25)
        val studyHistorySecond =
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 14, 0), 25)
        val from = LocalDateTime.of(exercisingYear, exercisingMonth, 1, 1, 1)
        val to = LocalDateTime.of(exercisingYear, exercisingMonth, 28, 1, 1)
        val datePattern = DateTimeFormatter.ISO_DATE_TIME
        val expectedStudyHistories = listOf(studyHistoryFirst.toDto(), studyHistorySecond.toDto())

        // WHEN
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/histories")
                .param(fromParameterName, from.format(datePattern))
                .param(toParameterName, to.format(datePattern))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        // THEN
        val data = objectMapper.readValue(response, BaseResponseDto::class.java).data
        val studyHistories: List<StudyHistoryDto> =
            objectMapper.readValue(objectMapper.writeValueAsString(data), object : TypeReference<List<StudyHistoryDto>>() {})

        assertNotNull(studyHistories)
        assertEquals(expectedStudyHistories, studyHistories)
    }
}
