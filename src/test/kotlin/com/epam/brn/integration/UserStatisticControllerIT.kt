package com.epam.brn.integration

import com.epam.brn.controller.UserStatisticController
import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.response.SubGroupStatisticDto
import com.epam.brn.dto.statistic.StudyStatistic
import com.epam.brn.model.Exercise
import com.epam.brn.repo.ExerciseRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.google.gson.Gson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertTrue

/**
 *@author Nikolai Lazarev
 */
@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class UserStatisticControllerIT : BaseIT() {

    @Autowired
    private lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    private lateinit var userStatisticController: UserStatisticController
    private val baseUrl = "/statistics"
    private val fromParamName = "from"
    private val toParameterName = "to"
    private val localDateTime: LocalDateTime = LocalDateTime.now()
    private val currentDay: Int = localDateTime.dayOfMonth
    private val currentYear: Int = localDateTime.year
    private val currentMonth: Int = localDateTime.monthValue

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
    }

    @Test
    fun `should return user statistic for period`() {
        val user = insertDefaultUser()
        val exercise = insertDefaultExercise()
        val exercisingYear = 2020
        val exercisingMonth = 11
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 13, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 14, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 21, 15, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 16, 0), 30)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 13, 0))

        val response = userStatisticController.getUserStudyStatistic(
            from = LocalDate.of(exercisingYear, exercisingMonth, 1),
            to = LocalDate.of(exercisingYear, exercisingMonth, 23)
        ).body?.data

        assertTrue(response is List<*>)
        assertTrue(response.size > 0)

        val expectedStudyStatisticFor20 = StudyStatistic(
            date = LocalDate.of(exercisingYear, exercisingMonth, 20),
            exercisingTime = 3000
        )

        assertTrue(response.contains(expectedStudyStatisticFor20))
    }
}
