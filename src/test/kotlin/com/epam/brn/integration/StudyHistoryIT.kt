package com.epam.brn.integration

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import kotlin.test.assertEquals

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class StudyHistoryIT {

    private val baseUrl = "/study-history"

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var exerciseRepository: ExerciseRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    @Autowired
    lateinit var studyHistoryRepository: StudyHistoryRepository

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    @AfterEach
    fun deleteAfterTest() {
        studyHistoryRepository.deleteAll()
        exerciseRepository.deleteAll()
        seriesRepository.deleteAll()
        exerciseGroupRepository.deleteAll()
        userAccountRepository.deleteAll()
    }

    @Test
    fun `test get last study histories for user`() {
        // GIVEN
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val existingUser = insertUser()
        val existingExerciseFirst = insertExercise(exerciseFirstName, existingSeries)
        val existingExerciseSecond = insertExercise(exerciseSecondName, existingSeries)
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
    fun `test get last study histories for user and exercises`() {
        // GIVEN
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val existingUser = insertUser()
        val existingExerciseFirst = insertExercise(exerciseFirstName, existingSeries)
        val existingExerciseSecond = insertExercise(exerciseSecondName, existingSeries)
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
    fun `test today timer for current user`() {
        // GIVEN
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val existingUser = insertUser()
        val existingExerciseFirst = insertExercise(exerciseFirstName, existingSeries)
        val existingExerciseSecond = insertExercise(exerciseSecondName, existingSeries)
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
        val result = existingUser.id?.let { studyHistoryRepository.getDayTimer(it, LocalDate.now()) }
        // THEN
        assertEquals(488, result)
    }

    @Test
    fun `test get day timer for current user`() {
        // GIVEN
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val existingUser = insertUser()
        val existingExerciseFirst = insertExercise(exerciseFirstName, existingSeries)
        val existingExerciseSecond = insertExercise(exerciseSecondName, existingSeries)
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
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
                firstName = "testUserFirstName",
                lastName = "testUserLastName",
                birthday = LocalDate.now(),
                email = "test@test.test",
                password = "password",
                active = true
            )
        )
    }

    private fun insertSeries(): Series {
        val exerciseGroup = exerciseGroupRepository.save(
            ExerciseGroup(
                description = "desc",
                name = "group"
            )
        )
        return seriesRepository.save(
            Series(
                description = "desc",
                name = "series",
                exerciseGroup = exerciseGroup
            )
        )
    }

    fun insertExercise(exerciseName: String, series: Series): Exercise {
        return exerciseRepository.save(
            Exercise(
                description = toString(),
                series = series,
                level = 0,
                name = exerciseName,
                exerciseType = ExerciseType.WORDS_SEQUENCES.toString()
            )
        )
    }
}
