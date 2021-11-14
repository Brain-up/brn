package com.epam.brn.integration

import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertNotNull

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class UserStatisticControllerV2IT : BaseIT() {

    private val baseUrl = "/v2/statistics"
    private val exercisingYear = 2020
    private val exercisingMonth = 11
    private val fromParamName = "from"
    private val toParameterName = "to"

    @AfterEach
    fun deleteAfterTest() {
        deleteInsertedTestData()
    }
    @Test
    fun `should return user statistic for days API version 2`() {
        // GIVEN
        val user = insertDefaultUser()
        val exercise = insertDefaultExercise()
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 13, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 14, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 21, 15, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 16, 0), 30)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 13, 0))
        val dateFormat = DateTimeFormatter.ISO_DATE_TIME

        // WHEN
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/study/week")
                .param(fromParamName, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 1, 1).format(dateFormat))
                .param(toParameterName, LocalDateTime.of(exercisingYear, exercisingMonth, 27, 1, 1).format(dateFormat))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = objectMapper.readValue(response, BaseSingleObjectResponseDto::class.java).data
        val resultStatistic: List<DayStudyStatistic> =
            objectMapper.readValue(objectMapper.writeValueAsString(data), object : TypeReference<List<DayStudyStatistic>>() {})

        // THEN
        Assertions.assertEquals(3, resultStatistic.size)
        resultStatistic.forEach {
            assertNotNull(it.progress)
            assertNotNull(it.exercisingTimeSeconds)
        }
    }

    @Test
    fun `should return user statistic for month API version 2`() {
        // GIVEN
        val user = insertDefaultUser()
        val exercise = insertDefaultExercise()
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 13, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 14, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 21, 15, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 16, 0), 30)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 13, 0))

        val dateFormat = DateTimeFormatter.ISO_DATE_TIME

        // WHEN
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/study/year")
                .param(fromParamName, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 1, 1).format(dateFormat))
                .param(toParameterName, LocalDateTime.of(exercisingYear, exercisingMonth, 27, 1, 1).format(dateFormat))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = objectMapper.readValue(response, BaseSingleObjectResponseDto::class.java).data
        val resultStatistic: List<MonthStudyStatistic> =
            objectMapper.readValue(objectMapper.writeValueAsString(data), object : TypeReference<List<MonthStudyStatistic>>() {})

        // THEN
        Assertions.assertEquals(1, resultStatistic.size)
        val monthStatistic = resultStatistic.first()
        Assertions.assertEquals(exercisingMonth, monthStatistic.date.monthValue)
        assertNotNull(monthStatistic.exercisingTimeSeconds)
        assertNotNull(monthStatistic.progress)
    }
}
