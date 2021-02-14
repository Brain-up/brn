package com.epam.brn.integration

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.response.SubGroupStatisticDto
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.google.gson.Gson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.time.LocalDateTime
import java.util.Calendar

/**
 *@author Nikolai Lazarev
 */
@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class UserStatisticControllerIT(
    val exerciseRepository: ExerciseRepository
) : BaseIT() {

    val baseUrl = "/statistics"

    @AfterEach
    fun deleteAfterTest() {
        deleteInsertedTestData()
    }

    @Test
    fun `should return user progress for subGroup`() {

        val currentUser = insertDefaultUser()
        val series = insertDefaultSeries()
        val subGroups = listOf(
            insertDefaultSubGroup(series, 1),
            insertDefaultSubGroup(series, 10)
        )
        val subGroupIds = subGroups.map { it.id }
        val exercises =
            exerciseRepository.saveAll(
                listOf(
                    Exercise(
                        name = "Test exercise ${subGroups[0].id}",
                        subGroup = subGroups[0]
                    ),
                    Exercise(
                        name = "Test exercise ${subGroups[1].id}",
                        subGroup = subGroups[1]
                    )
                )
            )

        insertDefaultStudyHistory(currentUser, exercises.first())

        val resultAction = mockMvc.perform(
            get("$baseUrl/subgroups")
                .param("ids", "${subGroupIds.get(0)},${subGroupIds.get(1)}")
        )
        println(currentUser)
        val response = resultAction
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val baseResponseDto = objectMapper.readValue(response, BaseResponseDto::class.java)
        val baseResponseJson = Gson().toJson(baseResponseDto.data)
        val resultStatistic: List<SubGroupStatisticDto> =
            objectMapper.readValue(baseResponseJson, object : TypeReference<List<SubGroupStatisticDto>>() {})

        Assertions.assertEquals(1, resultStatistic.first().totalExercises)
        Assertions.assertEquals(1, resultStatistic.first().completedExercises)

        Assertions.assertEquals(0, resultStatistic[1].completedExercises)
        Assertions.assertEquals(1, resultStatistic[1].totalExercises)
        val baseUrl = "/statistic"

        @Test
        fun `should return user statistic for specific month of specific year`() {
            val user = insertDefaultUser()
            val startTime = LocalDateTime.now()
            val endTime = startTime.plusMinutes(5)
            val startEndDiv = Duration.between(startTime, endTime)
            val exercise = insertDefaultExercise()
            insertDefaultStudyHistory(user, exercise)

            val result = mockMvc.perform(
                get("$baseUrl/month")
                    .param("month", Calendar.MONTH.toString())
                    .param("year", Calendar.getInstance()[Calendar.YEAR].toString())
                    .contentType(MediaType.APPLICATION_JSON)
            ).andReturn().response.getContentAsString(StandardCharsets.UTF_8)
            val baseResponseDto = objectMapper.readValue(result, BaseSingleObjectResponseDto::class.java)
            val statisticJson = Gson().toJson(baseResponseDto.data)
            val statistic: Map<Int, Int> =
                objectMapper.readValue(statisticJson, object : TypeReference<Map<Int, Int>>() {})

            Assertions.assertTrue(statistic.containsKey(Calendar.getInstance()[Calendar.DAY_OF_MONTH]))
            Assertions.assertTrue(statistic.containsValue(startEndDiv.toMinutes().toInt()))
        }
    }
}
