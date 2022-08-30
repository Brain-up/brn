package com.epam.brn.integration

import com.epam.brn.dto.response.Response
import com.epam.brn.dto.response.SubGroupStatisticResponse
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
import java.time.format.DateTimeFormatter

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

        val baseResponse = objectMapper.readValue(response, Response::class.java)
        val baseResponseJson = gson.toJson(baseResponse.data)
        val resultStatistic: List<SubGroupStatisticResponse> =
            objectMapper.readValue(baseResponseJson, object : TypeReference<List<SubGroupStatisticResponse>>() {})

        // THEN
        assertEquals(1, resultStatistic.first().totalExercises)
        assertEquals(1, resultStatistic.first().completedExercises)

        assertEquals(0, resultStatistic[1].completedExercises)
        assertEquals(1, resultStatistic[1].totalExercises)
    }
}
