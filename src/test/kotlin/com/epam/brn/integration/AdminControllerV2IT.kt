package com.epam.brn.integration

import com.epam.brn.dto.response.Response
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.UserDailyDetailStatisticsDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Gender
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.SubGroup
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.UserAccountRepository
import com.fasterxml.jackson.core.type.TypeReference
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class AdminControllerV2IT : BaseIT() {

    private val baseUrl = "/v2/admin"
    private val dayParamName = "day"
    private val fromParamName = "from"
    private val toParameterName = "to"
    private val userIdParameterName = "userId"
    private val exercisingYear = 2000
    private val exercisingMonth = 10

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Autowired
    lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var resourceRepository: ResourceRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    @Autowired
    lateinit var subGroupRepository: SubGroupRepository

    @Autowired
    lateinit var authorityRepository: AuthorityRepository

    @AfterEach
    fun deleteAfterTest() {
        deleteInsertedTestData()
        resourceRepository.deleteAll()
    }

    @Test
    fun `testing get user week statistic`() {
        // GIVEN
        val userAccount = insertDefaultUser()
        val exercise = insertDefaultExercise()
        insertDefaultStudyHistory(
            userAccount,
            exercise,
            LocalDateTime.of(exercisingYear, exercisingMonth, 20, 13, 0),
            25
        )
        insertDefaultStudyHistory(
            userAccount,
            exercise,
            LocalDateTime.of(exercisingYear, exercisingMonth, 20, 14, 0),
            25
        )
        insertDefaultStudyHistory(
            userAccount,
            exercise,
            LocalDateTime.of(exercisingYear, exercisingMonth, 21, 15, 0),
            25
        )
        insertDefaultStudyHistory(
            userAccount,
            exercise,
            LocalDateTime.of(exercisingYear, exercisingMonth, 23, 16, 0),
            30
        )
        insertDefaultStudyHistory(userAccount, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 13, 0))

        // WHEN
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/study/week")
                .param(fromParamName, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 1, 1).format(dateFormat))
                .param(toParameterName, LocalDateTime.of(exercisingYear, exercisingMonth, 27, 1, 1).format(dateFormat))
                .param(userIdParameterName, userAccount.id.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = objectMapper.readValue(response, Response::class.java).data
        val resultStatistic: List<DayStudyStatistic> =
            objectMapper.readValue(
                objectMapper.writeValueAsString(data),
                object : TypeReference<List<DayStudyStatistic>>() {}
            )

        // THEN
        resultStatistic.size shouldBe 3
        resultStatistic.forEach {
            it.progress shouldNotBe null
            it.exercisingTimeSeconds shouldNotBe null
        }
    }

    @Test
    fun `should return user year statistic`() {
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
            MockMvcRequestBuilders.get("$baseUrl/study/year")
                .param(fromParamName, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 1, 1).format(dateFormat))
                .param(toParameterName, LocalDateTime.of(exercisingYear, exercisingMonth, 27, 1, 1).format(dateFormat))
                .param(userIdParameterName, user.id.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = objectMapper.readValue(response, Response::class.java).data
        val resultStatistic: List<MonthStudyStatistic> =
            objectMapper.readValue(
                objectMapper.writeValueAsString(data),
                object : TypeReference<List<MonthStudyStatistic>>() {}
            )

        // THEN
        resultStatistic.size shouldBe 1
        val monthStatistic = resultStatistic.first()
        monthStatistic.date.monthValue shouldBe exercisingMonth
        monthStatistic.exercisingTimeSeconds shouldNotBe null
        monthStatistic.progress shouldNotBe null
    }

    @Test
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
        // insertDefaultStudyHistory(user, exercise3, LocalDateTime.of(exercisingYear, exercisingMonth, 2, 15, 0), 25, 3, 0, 0)
        insertDefaultStudyHistory(user, exercise1, LocalDateTime.of(exercisingYear, exercisingMonth, 3, 13, 0), 5, 2, 1, 1)

        // WHEN
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/study/day")
                .param(dayParamName, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 1, 1).format(dateFormat))
                .param(userIdParameterName, user.id.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = objectMapper.readValue(response, Response::class.java).data
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
    fun `test get histories for user by period`() {
        // GIVEN
        val existingUser = insertUser()
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val existingExerciseSecond = insertExercise(exerciseSecondName, subGroup)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val historyYesterdayOne = insertStudyHistory(existingUser, existingExerciseFirst, now.minusDays(1))
        val historyYesterdayTwo = insertStudyHistory(existingUser, existingExerciseSecond, now.minusDays(1))
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now.plusHours(1))
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.plusHours(2))
        val historySecondExerciseTwo = insertStudyHistory(existingUser, existingExerciseSecond, now.plusHours(3))
        val historyTomorrowOne = insertStudyHistory(existingUser, existingExerciseFirst, now.plusDays(1))
        val historyTomorrowTwo = insertStudyHistory(existingUser, existingExerciseSecond, now.plusDays(1))
        studyHistoryRepository
            .saveAll(
                listOf(
                    historyYesterdayOne,
                    historyYesterdayTwo,
                    historyFirstExerciseOne,
                    historyFirstExerciseTwo,
                    historySecondExerciseOne,
                    historySecondExerciseTwo,
                    historyTomorrowOne,
                    historyTomorrowTwo
                )
            )
        val today = now

        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("$baseUrl/histories")
                .param(fromParamName, today.format(dateFormat))
                .param(toParameterName, today.plusDays(1).minusSeconds(1).format(dateFormat))
                .param(userIdParameterName, existingUser.id.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )

        resultAction.andReturn().response
        // THEN
        resultAction
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].id").value(historyFirstExerciseOne.id!!))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].id").value(historyFirstExerciseTwo.id!!))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[2].id").value(historySecondExerciseOne.id!!))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data[3].id").value(historySecondExerciseTwo.id!!))
    }

    @Test
    fun `test repo get histories for user by period`() {
        // GIVEN
        val existingUser = insertUser()
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val existingExerciseSecond = insertExercise(exerciseSecondName, subGroup)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val historyYesterdayOne = insertStudyHistory(existingUser, existingExerciseFirst, now.minusDays(1))
        val historyYesterdayTwo = insertStudyHistory(existingUser, existingExerciseSecond, now.minusDays(1))
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.plusHours(1))
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now.plusHours(2))
        val historySecondExerciseTwo = insertStudyHistory(existingUser, existingExerciseSecond, now.plusHours(3))
        val historyTomorrowOne = insertStudyHistory(existingUser, existingExerciseFirst, now.plusDays(1))
        val historyTomorrowTwo = insertStudyHistory(existingUser, existingExerciseSecond, now.plusDays(1))
        studyHistoryRepository
            .saveAll(
                listOf(
                    historyYesterdayOne,
                    historyYesterdayTwo,
                    historyFirstExerciseOne,
                    historyFirstExerciseTwo,
                    historySecondExerciseOne,
                    historySecondExerciseTwo,
                    historyTomorrowOne,
                    historyTomorrowTwo
                )
            )
        // WHEN
        val result = studyHistoryRepository.getHistories(
            existingUser.id!!,
            now.toLocalDate().atStartOfDay(),
            now.plusDays(1).toLocalDate().atStartOfDay().minusSeconds(1)
        )
        // THEN
        result.size shouldBe 4
        result.map { it.id }.shouldContainExactlyInAnyOrder(
            historyFirstExerciseOne.id,
            historyFirstExerciseTwo.id,
            historySecondExerciseOne.id,
            historySecondExerciseTwo.id
        )
    }

    @Test
    fun `test repo get histories for user by period without histories`() {
        // GIVEN
        val existingUser = insertUser()
        // WHEN
        val result = studyHistoryRepository.getHistories(
            existingUser.id!!,
            LocalDate.now().atStartOfDay(),
            LocalDate.now().plusDays(1).atStartOfDay()
        )
        // THEN
        result.size shouldBe 0
    }

    private fun insertStudyHistory(
        existingUser: UserAccount,
        existingExercise: Exercise,
        startTime: LocalDateTime
    ): StudyHistory {
        return studyHistoryRepository.save(
            StudyHistory(
                userAccount = existingUser,
                exercise = existingExercise,
                endTime = startTime.plusMinutes(Random.nextLong(1, 5)),
                startTime = startTime,
                executionSeconds = 122,
                tasksCount = 12,
                wrongAnswers = 2,
                replaysCount = 4
            )
        )
    }

    private fun insertUser(): UserAccount {
        return userAccountRepository.save(
            UserAccount(
                fullName = "testUserFirstName",
                email = "test@test.test",
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                active = true
            )
        )
    }

    private fun insertSeries(): Series {
        val exerciseGroup = exerciseGroupRepository.save(
            ExerciseGroup(
                code = "CODE",
                description = "desc",
                name = "group"
            )
        )
        return seriesRepository.save(
            Series(
                type = "type",
                level = 1,
                name = "series",
                exerciseGroup = exerciseGroup,
                description = "desc"
            )
        )
    }

    private fun insertSubGroup(series: Series): SubGroup = subGroupRepository.save(
        SubGroup(series = series, level = 1, code = "code", name = "subGroup name")
    )

    private fun insertExercise(exerciseName: String, subGroup: SubGroup): Exercise {
        return exerciseRepository.save(
            Exercise(
                subGroup = subGroup,
                level = 0,
                name = exerciseName
            )
        )
    }
}
