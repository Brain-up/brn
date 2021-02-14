package com.epam.brn.integration

import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.model.Exercise
import com.fasterxml.jackson.core.type.TypeReference
import com.google.gson.Gson
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDateTime
import java.util.Calendar

/**
 *@author Nikolai Lazarev
 */
@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class UserStatisticControllerIT : BaseIT() {

    val baseUrl = "/statistic"

    @Test
    fun `should return user statistic for specific month of specific year`() {
        val user = insertDefaultUser()
        val startTime = LocalDateTime.now()
        val endTime = startTime.plusMinutes(5)
        val startEndDiv = Duration.between(startTime, endTime)
        val exercise = insertExercise(
            Exercise(
                id = 1,
                level = 0,
                name = "exercise"
            )
        )
        insertStudyHistory(
            user,
            exercise,
            LocalDateTime.now(),
            endTime = LocalDateTime.now().plusMinutes(5),
            tasksCount = 5,
            wrongAnswers = 2,
            replayCount = 1
        )

        val result = mockMvc.perform(
            get("$baseUrl/month")
                .param("month", Calendar.MONTH.toString())
                .param("year", Calendar.getInstance()[Calendar.YEAR].toString())
                .contentType(MediaType.APPLICATION_JSON)
        ).andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(result, BaseSingleObjectResponseDto::class.java)
        val statisticJson = Gson().toJson(baseResponseDto.data)
        val statistic: Map<Int, Int> = objectMapper.readValue(statisticJson, object : TypeReference<Map<Int, Int>>() {})

        Assertions.assertTrue(statistic.containsKey(Calendar.getInstance()[Calendar.DAY_OF_MONTH]))
        Assertions.assertTrue(statistic.containsValue(startEndDiv.toMinutes().toInt()))
    }
}
