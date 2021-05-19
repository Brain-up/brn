package com.epam.brn.integration

import com.epam.brn.dto.request.ExerciseRequest
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Gender
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.SubGroup
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.UserAccountRepository
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.test.assertFalse

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class ExercisesControllerIT : BaseIT() {

    private val baseUrl = "/exercises"

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
    fun `test get exercises by subGroupId`() {
        // GIVEN
        val existingUser = insertUser()
        val exerciseName = "ExerciseNameTest"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExercise = insertExercise(exerciseName, subGroup)
        insertStudyHistory(existingUser, existingExercise, LocalDateTime.now().minusHours(1))
        insertStudyHistory(existingUser, existingExercise, LocalDateTime.now())
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl)
                .param("subGroupId", subGroup.id.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data[0].name").value(exerciseName))
    }

    @Test
    fun `test get exercise by exerciseId`() {
        // GIVEN
        val exerciseName = "ExerciseNameTest"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExercise = insertExercise(exerciseName, subGroup)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("$baseUrl/${existingExercise.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val jsonResponse = JSONObject(resultAction.andReturn().response.contentAsString)
        val jsonDataObject = jsonResponse.getJSONObject("data")
        Assertions.assertEquals(exerciseName, jsonDataObject.get("name"))
    }

    @Test
    fun `test get exercises by ids`() {
        // GIVEN
        insertUser()
        val exerciseName = "ExerciseNameTest"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val exercise = insertExercise(exerciseName, subGroup)
        val requestJson: String = objectMapper.writeValueAsString(ExerciseRequest(listOf(exercise.id!!)))
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .post("$baseUrl/byIds")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val jsonResponse = JSONObject(resultAction.andReturn().response.contentAsString)
        val jsonDataObject = jsonResponse.getJSONArray("data").getLong(0)
        Assertions.assertTrue(jsonDataObject == exercise.id!!)
    }

    @Test
    fun `test change active status`() {
        val existingExercise = insertExercise("ExerciseNameTest")
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .put("$baseUrl/${existingExercise.id}/active/false")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
        exerciseRepository.findById(existingExercise.id!!)
        assertFalse(exerciseRepository.findById(existingExercise.id!!).get().active)
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
                wrongAnswers = 3,
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
                description = "desc",
                name = "group ExercisesControllerIT"
            )
        )
        return seriesRepository.save(
            Series(
                description = "desc",
                name = "series for ExercisesControllerIT",
                exerciseGroup = exerciseGroup,
                type = "type",
                level = 1
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

    fun insertExercise(exerciseName: String): Exercise {
        return exerciseRepository.save(
            Exercise(
                name = exerciseName
            )
        )
    }
}
