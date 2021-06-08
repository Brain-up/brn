package com.epam.brn.integration

import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Gender
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.SubGroup
import com.epam.brn.model.UserAccount
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.random.Random
import kotlin.test.assertEquals

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class StudyHistoryIT : BaseIT() {

    private val baseUrl = "/study-history"

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

    @AfterEach
    fun deleteAfterTest() {
        studyHistoryRepository.deleteAll()
        exerciseRepository.deleteAll()
        subGroupRepository.deleteAll()
        seriesRepository.deleteAll()
        exerciseGroupRepository.deleteAll()
        userAccountRepository.deleteAll()
    }

    @Test
    fun `test repo get last study histories for user`() {
        // GIVEN
        val existingUser = insertUser()
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val existingExerciseSecond = insertExercise(exerciseSecondName, subGroup)
        val now = LocalDateTime.now()
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now.minusHours(1))
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.minusHours(1))
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
        val result = existingUser.id?.let { studyHistoryRepository.findLastByUserAccountId(it) }
        // THEN
        assertEquals(2, result?.size)
    }

    @Test
    fun `test repo get last study histories for user and exercises`() {
        // GIVEN
        val existingUser = insertUser()
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExerciseFirst = insertExercise(exerciseFirstName, subGroup)
        val existingExerciseSecond = insertExercise(exerciseSecondName, subGroup)
        val now = LocalDateTime.now()
        val historyFirstExerciseOne = insertStudyHistory(existingUser, existingExerciseFirst, now.minusHours(1))
        val historyFirstExerciseTwo = insertStudyHistory(existingUser, existingExerciseFirst, now)
        val historySecondExerciseOne = insertStudyHistory(existingUser, existingExerciseSecond, now.minusHours(1))
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
        val result = existingUser.id?.let {
            studyHistoryRepository.findLastByUserAccountIdAndExercises(
                it,
                listOf(existingExerciseFirst.id!!)
            )
        }
        // THEN
        assertEquals(1, result?.size)
    }

    @Test
    fun `test repo day timer for user`() {
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
        val result = existingUser.id?.let {
            studyHistoryRepository
                .getDayTimer(it, Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()))
        }
        // THEN
        assertEquals(488, result)
    }

    @Test
    fun `test get day timer for current user`() {
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
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("$baseUrl/todayTimer")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data").value(488))
    }

    @Test
    fun `test get today timer for current user without study history records`() {
        // GIVEN
        insertUser()
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("$baseUrl/todayTimer")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data").value(0))
    }

    @Test
    fun `test repo get today timer for current user without study history records`() {
        // GIVEN
        val user = insertUser()
        // WHEN
        val result = studyHistoryRepository.getTodayDayTimer(user.id!!)
        // THEN
        assertEquals(0, result)
    }

    @Test
    fun `test get histories for current user by period`() {
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
        val today = LocalDateTime.now()
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("$baseUrl/histories?from=$today&to=${today.plusDays(1)}")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data[0].id").value(historyFirstExerciseOne.id!!))
            .andExpect(jsonPath("$.data[1].id").value(historyFirstExerciseTwo.id!!))
            .andExpect(jsonPath("$.data[2].id").value(historySecondExerciseOne.id!!))
            .andExpect(jsonPath("$.data[3].id").value(historySecondExerciseTwo.id!!))
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
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                email = "test@test.test",
                password = "password",
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
                description = "desc",
                name = "series",
                exerciseGroup = exerciseGroup,
                level = 1,
                type = "type"
            )
        )
    }

    private fun insertSubGroup(series: Series): SubGroup = subGroupRepository.save(
        SubGroup(series = series, level = 1, code = "code", name = "subGroup name")
    )

    fun insertExercise(exerciseName: String, subGroup: SubGroup): Exercise {
        return exerciseRepository.save(
            Exercise(
                subGroup = subGroup,
                level = 0,
                name = exerciseName
            )
        )
    }
}
