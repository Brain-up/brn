package com.epam.brn.integration

import com.epam.brn.dto.response.BaseSingleObjectResponse
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.UserDailyDetailStatisticsDto
import com.fasterxml.jackson.core.type.TypeReference
import io.kotest.matchers.shouldBe
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
class UserSubGroupStatisticControllerV2IT : BaseIT() {

    private val baseUrl = "/v2/statistics"
    private val exercisingYear = 2020
    private val exercisingMonth = 11
    private val dayParamName = "day"
    private val fromParamName = "from"
    private val toParameterName = "to"

    @AfterEach
    fun deleteAfterTest() {
        deleteInsertedTestData()
    }
    @Test
    @WithMockUser(username = "test@test.test", roles = ["ADMIN"])
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

        val data = objectMapper.readValue(response, BaseSingleObjectResponse::class.java).data
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
    @WithMockUser(username = "test@test.test", roles = ["USER"])
    fun `should return user statistic for days API version 2 for user with role user`() {
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

        val data = objectMapper.readValue(response, BaseSingleObjectResponse::class.java).data
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
    @WithMockUser(username = "test@test.test", roles = ["ADMIN"])
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

        val data = objectMapper.readValue(response, BaseSingleObjectResponse::class.java).data
        val resultStatistic: List<MonthStudyStatistic> =
            objectMapper.readValue(objectMapper.writeValueAsString(data), object : TypeReference<List<MonthStudyStatistic>>() {})

        // THEN
        Assertions.assertEquals(1, resultStatistic.size)
        val monthStatistic = resultStatistic.first()
        Assertions.assertEquals(exercisingMonth, monthStatistic.date.monthValue)
        assertNotNull(monthStatistic.exercisingTimeSeconds)
        assertNotNull(monthStatistic.progress)
    }

    @Test
    @WithMockUser(username = "test@test.test", roles = ["USER"])
    fun `should return user statistic for month API version 2 for user with role user`() {
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

        val data = objectMapper.readValue(response, BaseSingleObjectResponse::class.java).data
        val resultStatistic: List<MonthStudyStatistic> =
            objectMapper.readValue(objectMapper.writeValueAsString(data), object : TypeReference<List<MonthStudyStatistic>>() {})

        // THEN
        Assertions.assertEquals(1, resultStatistic.size)
        val monthStatistic = resultStatistic.first()
        Assertions.assertEquals(exercisingMonth, monthStatistic.date.monthValue)
        assertNotNull(monthStatistic.exercisingTimeSeconds)
        assertNotNull(monthStatistic.progress)
    }

    @Test
    @WithMockUser(username = "test@test.test", roles = ["ADMIN"])
    fun `should return user daily details statistic`() {
        // GIVEN
        val seriesName1 = "seriesName1"
        val seriesName2 = "seriesName2"
        val user = insertDefaultUser()
        val series1 = insertDefaultSeries(seriesName1)
        val subGroup1 = insertDefaultSubGroup(series1, 0)
        val subGroup2 = insertDefaultSubGroup(series1, 1)
        val series2 = insertDefaultSeries(seriesName2)
        val subGroup3 = insertDefaultSubGroup(series2, 0)

        val exercise1 = insertDefaultExercise(subGroup1, "exercise1")
        val exercise2 = insertDefaultExercise(subGroup1, "exercise2")
        val exercise3 = insertDefaultExercise(subGroup2, "exercise3")
        val exercise4 = insertDefaultExercise(subGroup3, "exercise4")

        insertDefaultStudyHistory(user, exercise1, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 10, 0), 25, 2, 0, 0)
        insertDefaultStudyHistory(user, exercise2, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 11, 0), 25, 4, 1, 1)
        insertDefaultStudyHistory(user, exercise3, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 14, 0), 25, 3, 0, 1)
        insertDefaultStudyHistory(user, exercise3, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 15, 0), 25, 3, 0, 0)
        insertDefaultStudyHistory(user, exercise4, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 16, 0), 30, 5, 5, 5)
        // statistic for other days
        insertDefaultStudyHistory(user, exercise3, LocalDateTime.of(exercisingYear, exercisingMonth, 2, 15, 0), 25, 3, 0, 0)
        insertDefaultStudyHistory(user, exercise1, LocalDateTime.of(exercisingYear, exercisingMonth, 3, 13, 0), 5, 2, 1, 1)

        // WHEN
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/study/day")
                .param(dayParamName, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 1, 1).format(dateFormat))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = objectMapper.readValue(response, BaseSingleObjectResponse::class.java).data
        val resultStatistic: List<UserDailyDetailStatisticsDto> =
            objectMapper.readValue(
                objectMapper.writeValueAsString(data),
                object : TypeReference<List<UserDailyDetailStatisticsDto>>() {}
            )

        // THEN
        resultStatistic.size shouldBe 2
        val userDailyDetailStatisticsDto1 = resultStatistic[0]
        val userDailyDetailStatisticsDto2 = resultStatistic[1]
        userDailyDetailStatisticsDto1.seriesName shouldBe seriesName1
        userDailyDetailStatisticsDto1.allDoneExercises shouldBe 4
        userDailyDetailStatisticsDto1.uniqueDoneExercises shouldBe 3
        userDailyDetailStatisticsDto1.repeatedExercises shouldBe 2
        userDailyDetailStatisticsDto1.doneExercisesSuccessfullyFromFirstTime shouldBe 2
        userDailyDetailStatisticsDto1.listenWordsCount shouldBe 12

        userDailyDetailStatisticsDto2.seriesName shouldBe seriesName2
        userDailyDetailStatisticsDto2.allDoneExercises shouldBe 1
        userDailyDetailStatisticsDto2.uniqueDoneExercises shouldBe 1
        userDailyDetailStatisticsDto2.repeatedExercises shouldBe 0
        userDailyDetailStatisticsDto2.doneExercisesSuccessfullyFromFirstTime shouldBe 1
        userDailyDetailStatisticsDto2.listenWordsCount shouldBe 5
    }

    @Test
    @WithMockUser(username = "test@test.test", roles = ["USER"])
    fun `should return user daily details statistic for user with role user`() {
        // GIVEN
        val seriesName1 = "seriesName1"
        val seriesName2 = "seriesName2"
        val user = insertDefaultUser()
        val series1 = insertDefaultSeries(seriesName1)
        val subGroup1 = insertDefaultSubGroup(series1, 0)
        val subGroup2 = insertDefaultSubGroup(series1, 1)
        val series2 = insertDefaultSeries(seriesName2)
        val subGroup3 = insertDefaultSubGroup(series2, 0)

        val exercise1 = insertDefaultExercise(subGroup1, "exercise1")
        val exercise2 = insertDefaultExercise(subGroup1, "exercise2")
        val exercise3 = insertDefaultExercise(subGroup2, "exercise3")
        val exercise4 = insertDefaultExercise(subGroup3, "exercise4")

        insertDefaultStudyHistory(user, exercise1, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 10, 0), 25, 2, 0, 0)
        insertDefaultStudyHistory(user, exercise2, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 11, 0), 25, 4, 1, 1)
        insertDefaultStudyHistory(user, exercise3, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 14, 0), 25, 3, 0, 1)
        insertDefaultStudyHistory(user, exercise3, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 15, 0), 25, 3, 0, 0)
        insertDefaultStudyHistory(user, exercise4, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 16, 0), 30, 5, 5, 5)
        // statistic for other days
        insertDefaultStudyHistory(user, exercise3, LocalDateTime.of(exercisingYear, exercisingMonth, 2, 15, 0), 25, 3, 0, 0)
        insertDefaultStudyHistory(user, exercise1, LocalDateTime.of(exercisingYear, exercisingMonth, 3, 13, 0), 5, 2, 1, 1)

        // WHEN
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/study/day")
                .param(dayParamName, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 1, 1).format(dateFormat))
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = objectMapper.readValue(response, BaseSingleObjectResponse::class.java).data
        val resultStatistic: List<UserDailyDetailStatisticsDto> =
            objectMapper.readValue(
                objectMapper.writeValueAsString(data),
                object : TypeReference<List<UserDailyDetailStatisticsDto>>() {}
            )

        // THEN
        resultStatistic.size shouldBe 2
        val userDailyDetailStatisticsDto1 = resultStatistic[0]
        val userDailyDetailStatisticsDto2 = resultStatistic[1]
        userDailyDetailStatisticsDto1.seriesName shouldBe seriesName1
        userDailyDetailStatisticsDto1.allDoneExercises shouldBe 4
        userDailyDetailStatisticsDto1.uniqueDoneExercises shouldBe 3
        userDailyDetailStatisticsDto1.repeatedExercises shouldBe 2
        userDailyDetailStatisticsDto1.doneExercisesSuccessfullyFromFirstTime shouldBe 2
        userDailyDetailStatisticsDto1.listenWordsCount shouldBe 12

        userDailyDetailStatisticsDto2.seriesName shouldBe seriesName2
        userDailyDetailStatisticsDto2.allDoneExercises shouldBe 1
        userDailyDetailStatisticsDto2.uniqueDoneExercises shouldBe 1
        userDailyDetailStatisticsDto2.repeatedExercises shouldBe 0
        userDailyDetailStatisticsDto2.doneExercisesSuccessfullyFromFirstTime shouldBe 1
        userDailyDetailStatisticsDto2.listenWordsCount shouldBe 5
    }
}
