package com.epam.brn.integration

import com.epam.brn.constant.BrnParams.USER_ID
import com.epam.brn.constant.BrnPath
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
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
    fun `test get done exercises by userID`() {
        // GIVEN
        val exerciseName = "SOMENAME"
        val existingUser = insertUser()
        val existingExercise = insertExercise(exerciseName)
        insertStudyHistory(existingUser, existingExercise)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(BrnPath.EXERCISES)
                .param(USER_ID, existingUser.id.toString())
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
        val response = resultAction.andReturn().response.contentAsString
        assertTrue(response.contains(exerciseName))
    }

    @Test
    fun `test get exercises by exerciseID`() {
        // GIVEN
        val exerciseName = "SOMENAME"
        val existingExercise = insertExercise(exerciseName)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(BrnPath.EXERCISES + "/" + existingExercise.id)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
        val response = resultAction.andReturn().response.contentAsString
        assertTrue(response.contains(exerciseName))
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
                doneTasksCount = 2,
                successTasksCount = 1,
                repetitionCount = 3
            )
        )
    }

    private fun insertUser(): UserAccount {
        return userAccountRepository.save(
            UserAccount(
                id = 0,
                name = "manuel",
                birthDate = LocalDate.now(),
                email = "123@123.asd"
            )
        )
    }

    fun insertExercise(exerciseName: String): Exercise {
        val exerciseGroup = exerciseGroupRepository.save(
            ExerciseGroup(
                id = 0,
                description = "desc",
                name = "group"
            )
        )
        val series = seriesRepository.save(
            Series(
                id = 0,
                description = "desc",
                name = "series",
                exerciseGroup = exerciseGroup
            )
        )
        return exerciseRepository.save(
            Exercise(
                id = 0,
                description = toString(),
                series = series,
                level = 0,
                name = exerciseName
            )
        )
    }
}