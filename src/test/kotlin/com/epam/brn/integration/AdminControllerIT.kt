package com.epam.brn.integration

import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.ExerciseType
import com.epam.brn.model.Gender
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
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
import java.time.temporal.ChronoUnit
import kotlin.random.Random
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class AdminControllerIT : BaseIT() {

    private val baseUrl = "/admin"

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

    @AfterEach
    fun deleteAfterTest() {
        studyHistoryRepository.deleteAll()
        exerciseRepository.deleteAll()
        seriesRepository.deleteAll()
        exerciseGroupRepository.deleteAll()
        userAccountRepository.deleteAll()
    }

    @Test
    fun `test repo get histories for user by period`() {
        // GIVEN
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val existingUser = insertUser()
        val existingExerciseFirst = insertExercise(exerciseFirstName, existingSeries)
        val existingExerciseSecond = insertExercise(exerciseSecondName, existingSeries)
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
        val result = studyHistoryRepository.getHistories(existingUser.id!!,
            java.sql.Date.valueOf(now.toLocalDate()),
            java.sql.Date.valueOf(now.toLocalDate().plusDays(1)))
        // THEN
        assertEquals(4, result.size)
        result.map { it.id }.containsAll(listOf(historyFirstExerciseOne.id, historyFirstExerciseTwo.id, historySecondExerciseOne.id, historySecondExerciseTwo.id))
    }

    @Test
    fun `test repo get histories for user by period without histories`() {
        // GIVEN
        val existingUser = insertUser()
        // WHEN
        val result = studyHistoryRepository.getHistories(existingUser.id!!,
            java.sql.Date.valueOf(LocalDate.now()),
            java.sql.Date.valueOf(LocalDate.now().plusDays(1)))
        // THEN
        assertEquals(0, result.size)
    }

    @Test
    fun `test repo get month histories for user`() {
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
        val result = studyHistoryRepository.getMonthHistories(existingUser.id!!, LocalDate.now().monthValue, LocalDate.now().year)
        // THEN
        assertEquals(4, result.size)
        assertTrue(result.map { it.id }.containsAll(listOf(historyFirstExerciseOne.id, historyFirstExerciseTwo.id, historySecondExerciseOne.id, historySecondExerciseTwo.id)))
    }

    @Test
    fun `test repo get today histories for user`() {
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
        val result = studyHistoryRepository.getTodayHistories(existingUser.id!!)
        // THEN
        assertEquals(4, result.size)
        assertTrue(result.map { it.id }.containsAll(listOf(historyFirstExerciseOne.id, historyFirstExerciseTwo.id, historySecondExerciseOne.id, historySecondExerciseTwo.id)))
    }

    @Test
    fun `test get histories for user by period`() {
        // GIVEN
        val exerciseFirstName = "FirstName"
        val exerciseSecondName = "SecondName"
        val existingSeries = insertSeries()
        val existingUser = insertUser()
        val existingExerciseFirst = insertExercise(exerciseFirstName, existingSeries)
        val existingExerciseSecond = insertExercise(exerciseSecondName, existingSeries)
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
        val today = LocalDate.now()
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("$baseUrl/histories?userId=${existingUser.id}&from=$today&to=${today.plusDays(1)}")
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

    private fun insertExercise(exerciseName: String, series: Series): Exercise {
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
