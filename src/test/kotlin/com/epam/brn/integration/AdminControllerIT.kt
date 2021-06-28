package com.epam.brn.integration

import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.dto.statistic.DayStudyStatistic
import com.epam.brn.dto.statistic.DayStudyStatisticDto
import com.epam.brn.dto.statistic.MonthStudyStatistic
import com.epam.brn.dto.statistic.MonthStudyStatisticDto
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Gender
import com.epam.brn.model.Resource
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.SubGroup
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.ResourceRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.UserAccountRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.google.gson.Gson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import java.nio.charset.StandardCharsets
import java.sql.Date
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class AdminControllerIT : BaseIT() {

    private val baseUrl = "/admin"
    private val fromParamName = "from"
    private val toParameterName = "to"
    private val userIdParameterName = "userId"
    private val exercisingYear = 2000
    private val exercisingMonth = 10

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    lateinit var subGroupRepository: SubGroupRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    @Autowired
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var resourceRepository: ResourceRepository

    @Autowired
    private lateinit var gson: Gson

    private val legacyDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val versionParameterName = "version"
    private val version = "2"

    @AfterEach
    fun deleteAfterTest() {
        studyHistoryRepository.deleteAll()
        exerciseRepository.deleteAll()
        subGroupRepository.deleteAll()
        seriesRepository.deleteAll()
        exerciseGroupRepository.deleteAll()
        userAccountRepository.deleteAll()
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
                .param(fromParamName, LocalDate.of(exercisingYear, exercisingMonth, 1).format(legacyDateFormatter))
                .param(toParameterName, LocalDate.of(exercisingYear, exercisingMonth, 27).format(legacyDateFormatter))
                .param(userIdParameterName, userAccount.id.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = gson.fromJson(response, BaseSingleObjectResponseDto::class.java).data
        val resultStatistic: List<DayStudyStatisticDto> =
            objectMapper.readValue(gson.toJson(data), object : TypeReference<List<DayStudyStatisticDto>>() {})

        // THEN
        Assertions.assertEquals(3, resultStatistic.size)
        resultStatistic.forEach {
            assertNotNull(it.progress)
            assertNotNull(it.exercisingTimeSeconds)
        }
    }

    @Test
    fun `testing get user week statistic API version 2`() {
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
                .param(versionParameterName, version)
                .param(fromParamName, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 1, 1).format(dateFormat))
                .param(toParameterName, LocalDateTime.of(exercisingYear, exercisingMonth, 27, 1, 1).format(dateFormat))
                .param(userIdParameterName, userAccount.id.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = gson.fromJson(response, BaseSingleObjectResponseDto::class.java).data
        val resultStatistic: List<DayStudyStatistic> =
            objectMapper.readValue(gson.toJson(data), object : TypeReference<List<DayStudyStatistic>>() {})

        // THEN
        Assertions.assertEquals(3, resultStatistic.size)
        resultStatistic.forEach {
            assertNotNull(it.progress)
            assertNotNull(it.exercisingTimeSeconds)
        }
    }

    @Test
    fun `should return user year statistic`() {
        // GIVEN
        val user = insertDefaultUser()
        val exercise = insertDefaultExercise()
        val studyHistories: List<StudyHistory> = listOf(
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 13, 0), 25),
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 14, 0), 25),
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 21, 15, 0), 25),
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 16, 0), 30),
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 13, 0))
        )

        // WHEN
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/study/year")
                .param(fromParamName, LocalDate.of(exercisingYear, exercisingMonth, 1).format(legacyDateFormatter))
                .param(toParameterName, LocalDate.of(exercisingYear, exercisingMonth, 27).format(legacyDateFormatter))
                .param(userIdParameterName, user.id.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = gson.fromJson(response, BaseSingleObjectResponseDto::class.java).data
        val resultStatistic: List<MonthStudyStatisticDto> =
            objectMapper.readValue(gson.toJson(data), object : TypeReference<List<MonthStudyStatisticDto>>() {})

        // THEN
        Assertions.assertEquals(1, resultStatistic.size)
        val monthStatistic = resultStatistic.first()
        Assertions.assertEquals(exercisingMonth, YearMonth.parse(monthStatistic.date).monthValue)
        assertNotNull(monthStatistic.exercisingTimeSeconds)
        assertNotNull(monthStatistic.progress)
    }

    @Test
    fun `should return user year statistic API version 2`() {
        // GIVEN
        val user = insertDefaultUser()
        val exercise = insertDefaultExercise()
        val studyHistories: List<StudyHistory> = listOf(
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 13, 0), 25),
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 14, 0), 25),
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 21, 15, 0), 25),
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 16, 0), 30),
            insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 23, 13, 0))
        )

        // WHEN
        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("$baseUrl/study/year")
                .param(versionParameterName, version)
                .param(fromParamName, LocalDateTime.of(exercisingYear, exercisingMonth, 1, 1, 1).format(dateFormat))
                .param(toParameterName, LocalDateTime.of(exercisingYear, exercisingMonth, 27, 1, 1).format(dateFormat))
                .param(userIdParameterName, user.id.toString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        val data = gson.fromJson(response, BaseSingleObjectResponseDto::class.java).data
        val resultStatistic: List<MonthStudyStatistic> =
            objectMapper.readValue(gson.toJson(data), object : TypeReference<List<MonthStudyStatistic>>() {})

        // THEN
        Assertions.assertEquals(1, resultStatistic.size)
        val monthStatistic = resultStatistic.first()
        Assertions.assertEquals(exercisingMonth, monthStatistic.date.monthValue)
        assertNotNull(monthStatistic.exercisingTimeSeconds)
        assertNotNull(monthStatistic.progress)
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
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now.plusHours(1))
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.plusHours(1))
        val historySecondExerciseTwo = insertStudyHistory(existingUser, existingExerciseSecond, now)
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
            Date.valueOf(now.toLocalDate()),
            Date.valueOf(now.plusDays(1).toLocalDate())
        )
        // THEN
        assertEquals(4, result.size)
        result.map { it.id }.containsAll(
            listOf(
                historyFirstExerciseOne.id,
                historyFirstExerciseTwo.id,
                historySecondExerciseOne.id,
                historySecondExerciseTwo.id
            )
        )
    }

    @Test
    fun `test repo get histories for user by period without histories`() {
        // GIVEN
        val existingUser = insertUser()
        // WHEN
        val result = studyHistoryRepository.getHistories(
            existingUser.id!!,
            Date.valueOf(LocalDate.now()),
            Date.valueOf(LocalDate.now().plusDays(1))
        )
        // THEN
        assertEquals(0, result.size)
    }

    @Test
    fun `test repo get month histories for user`() {
        // GIVEN
        val existingUser = insertUser()
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val existingExerciseSecond = insertExercise(exerciseSecondName, subGroup)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now.plusHours(1))
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.plusHours(1))
        val historySecondExerciseTwo = insertStudyHistory(existingUser, existingExerciseSecond, now)
        studyHistoryRepository
            .saveAll(
                listOf(
                    historyFirstExerciseOne,
                    historyFirstExerciseTwo,
                    historySecondExerciseOne,
                    historySecondExerciseTwo
                )
            )
        // WHEN
        val result = studyHistoryRepository.getMonthHistories(
            existingUser.id!!,
            LocalDate.now().monthValue,
            LocalDate.now().year
        )
        // THEN
        assertEquals(4, result.size)
        assertTrue(
            result.map { it.id }.containsAll(
                listOf(
                    historyFirstExerciseOne.id,
                    historyFirstExerciseTwo.id,
                    historySecondExerciseOne.id,
                    historySecondExerciseTwo.id
                )
            )
        )
    }

    @Test
    fun `test repo get today histories for user`() {
        // GIVEN
        val existingUser = insertUser()
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val existingExerciseSecond = insertExercise(exerciseSecondName, subGroup)
        val now = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now.plusHours(1))
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.plusHours(1))
        val historySecondExerciseTwo = insertStudyHistory(existingUser, existingExerciseSecond, now)
        studyHistoryRepository
            .saveAll(
                listOf(
                    historyFirstExerciseOne,
                    historyFirstExerciseTwo,
                    historySecondExerciseOne,
                    historySecondExerciseTwo
                )
            )
        // WHEN
        val result = studyHistoryRepository.getTodayHistories(existingUser.id!!)
        // THEN
        assertEquals(4, result.size)
        assertTrue(
            result.map { it.id }.containsAll(
                listOf(
                    historyFirstExerciseOne.id,
                    historyFirstExerciseTwo.id,
                    historySecondExerciseOne.id,
                    historySecondExerciseTwo.id
                )
            )
        )
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
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now.plusHours(1))
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.plusHours(1))
        val historySecondExerciseTwo = insertStudyHistory(existingUser, existingExerciseSecond, now)
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
                .param(fromParamName, today.format(legacyDateFormatter))
                .param(toParameterName, today.plusDays(1).format(legacyDateFormatter))
                .param(userIdParameterName, existingUser.id.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )

        resultAction.andReturn().response
        // THEN
        resultAction
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data[0].id").value(historyFirstExerciseOne.id!!))
            .andExpect(jsonPath("$.data[1].id").value(historyFirstExerciseTwo.id!!))
            .andExpect(jsonPath("$.data[2].id").value(historySecondExerciseOne.id!!))
            .andExpect(jsonPath("$.data[3].id").value(historySecondExerciseTwo.id!!))
    }

    @Test
    fun `should update resource description successfully`() {
        // GIVEN
        val resource = resourceRepository.save(Resource(description = "description", wordType = "OBJECT"))
        val descriptionForUpdate = "new description"
        val requestJson = objectMapper.writeValueAsString(UpdateResourceDescriptionRequest(descriptionForUpdate))

        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .patch("$baseUrl/resources/${resource.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )

        // THEN
        resultAction
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data.id").value(resource.id))
            .andExpect(jsonPath("$.data.description").value(descriptionForUpdate))
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
                password = "password",
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
