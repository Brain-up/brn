package com.epam.brn.integration

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.response.BaseSingleObjectResponse
import com.epam.brn.dto.response.SubGroupStatisticResponse
import com.epam.brn.dto.statistic.MonthStudyStatisticDto
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.google.gson.Gson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.test.assertNotNull

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class UserStatisticControllerIT : BaseIT() {

    @Autowired
    private lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    private lateinit var gson: Gson

    private val baseUrl = "/statistics"
    private val fromParamName = "from"
    private val toParameterName = "to"
    private val exercisingYear = 2020
    private val exercisingMonth = 11
    private val legacyDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    @AfterEach
    fun deleteAfterTest() {
        deleteInsertedTestData()
    }

    @Test
    fun `should return user progress for subGroup`() {
        // GIVEN
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

        // WHEN
        val resultAction = mockMvc.perform(
            get("$baseUrl/subgroups")
                .param("ids", "${subGroupIds.get(0)},${subGroupIds.get(1)}")
        )

        val response = resultAction
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val baseResponse = objectMapper.readValue(response, BaseResponse::class.java)
        val baseResponseJson = gson.toJson(baseResponse.data)
        val resultStatistic: List<SubGroupStatisticResponse> =
            objectMapper.readValue(baseResponseJson, object : TypeReference<List<SubGroupStatisticResponse>>() {})

        // THEN
        assertEquals(1, resultStatistic.first().totalExercises)
        assertEquals(1, resultStatistic.first().completedExercises)

        assertEquals(0, resultStatistic[1].completedExercises)
        assertEquals(1, resultStatistic[1].totalExercises)
    }

    @Test
    fun `should return user statistic for month`() {
        // GIVEN
        val user = insertDefaultUser()
        val exercise = insertDefaultExercise()
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 13, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 14, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 21, 15, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 16, 0), 30)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 13, 0))

        // WHEN
        val response = mockMvc.perform(
            get("$baseUrl/study/year")
                .param(fromParamName, LocalDate.of(exercisingYear, exercisingMonth, 1).format(legacyDateFormatter))
                .param(toParameterName, LocalDate.of(exercisingYear, exercisingMonth, 27).format(legacyDateFormatter))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = gson.fromJson(response, BaseSingleObjectResponse::class.java).data
        val resultStatistic: List<MonthStudyStatisticDto> =
            objectMapper.readValue(gson.toJson(data), object : TypeReference<List<MonthStudyStatisticDto>>() {})

        // THEN
        assertEquals(1, resultStatistic.size)
        val monthStatistic = resultStatistic.first()
        assertEquals(exercisingMonth, YearMonth.parse(monthStatistic.date).monthValue)
        assertNotNull(monthStatistic.exercisingTimeSeconds)
        assertNotNull(monthStatistic.progress)
    }
}
