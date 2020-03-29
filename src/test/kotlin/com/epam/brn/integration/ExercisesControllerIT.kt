package com.epam.brn.integration

import com.epam.brn.constant.BrnPath
import com.epam.brn.constant.ExerciseType
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.model.StudyHistory
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.ExerciseRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.StudyHistoryRepository
import com.epam.brn.repo.UserAccountRepository
import java.time.LocalDate
import java.time.LocalDateTime
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
@WithMockUser(username = "admin", roles = ["ADMIN"])
class ExercisesControllerIT {

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
    fun `test get done exercises by userId and seriesId`() {
        // GIVEN
        val exerciseName = "SOMENAME"
        val existingSeries = insertSeries()
        val existingUser = insertUser()
        val existingExercise = insertExercise(exerciseName, existingSeries)
        insertStudyHistory(existingUser, existingExercise)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(BrnPath.EXERCISES)
                .param("userId", existingUser.id.toString())
                .param("seriesId", existingSeries.id.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data[0].name").value(exerciseName))
    }

    @Test
    fun `test get exercises by exerciseId`() {
        // GIVEN
        val exerciseName = "SOMENAME"
        val existingSeries = insertSeries()
        val existingExercise = insertExercise(exerciseName, existingSeries)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(BrnPath.EXERCISES + "/" + existingExercise.id)
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

    private fun insertStudyHistory(
        existingUser: UserAccount,
        existingExercise: Exercise
    ): StudyHistory {
        return studyHistoryRepository.save(
            StudyHistory(
                id = 0,
                userAccount = existingUser,
                exercise = existingExercise,
                endTime = LocalDateTime.now(),
                startTime = LocalDateTime.now(),
                tasksCount = 2,
                repetitionIndex = 1f
            )
        )
    }

    private fun insertUser(): UserAccount {
        return userAccountRepository.save(
            UserAccount(
                id = 0,
                firstName = "testUserFirstName",
                lastName = "testUserLastName",
                birthday = LocalDate.now(),
                email = "123@123.asd",
                password = "password",
                active = true
            )
        )
    }

    private fun insertSeries(): Series {
        val exerciseGroup = exerciseGroupRepository.save(
            ExerciseGroup(
                id = 0,
                description = "desc",
                name = "group"
            )
        )
        return seriesRepository.save(
            Series(
                id = 0,
                description = "desc",
                name = "series",
                exerciseGroup = exerciseGroup
            )
        )
    }

    fun insertExercise(exerciseName: String, series: Series): Exercise {
        return exerciseRepository.save(
            Exercise(
                id = 0,
                description = toString(),
                series = series,
                level = 0,
                name = exerciseName,
                exerciseType = ExerciseType.SINGLE_WORDS.toString()
            )
        )
    }
}
