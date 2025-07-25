package com.epam.brn.integration

import com.epam.brn.dto.request.ExerciseRequest
import com.epam.brn.dto.request.exercise.ExercisePhrasesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseSentencesCreateDto
import com.epam.brn.dto.request.exercise.ExerciseWordsCreateDto
import com.epam.brn.dto.request.exercise.Phrases
import com.epam.brn.dto.request.exercise.SetOfWords
import com.epam.brn.enums.BrnGender
import com.epam.brn.enums.BrnLocale
import com.epam.brn.enums.BrnRole
import com.epam.brn.model.Exercise
import com.epam.brn.model.ExerciseGroup
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime
import kotlin.random.Random
import kotlin.test.assertFalse

@WithMockUser(username = "test@test.test", roles = [BrnRole.USER])
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
        val existingExercise = insertExercise(subGroup, exerciseName)
        insertStudyHistory(existingUser, existingExercise, LocalDateTime.now().minusHours(1))
        insertStudyHistory(existingUser, existingExercise, LocalDateTime.now())
        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .get(baseUrl)
                    .param("subGroupId", subGroup.id.toString())
                    .contentType(MediaType.APPLICATION_JSON),
            )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.data[0].name").value(exerciseName))
    }

    @Test
    fun `test get exercise by exerciseId`() {
        // GIVEN
        val exerciseName = "ExerciseNameTest"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val existingExercise = insertExercise(subGroup, exerciseName)
        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .get("$baseUrl/${existingExercise.id}")
                    .contentType(MediaType.APPLICATION_JSON),
            )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        val jsonResponse = JSONObject(resultAction.andReturn().response.contentAsString)
        val jsonDataObject = jsonResponse.getJSONObject("data")
        Assertions.assertEquals(exerciseName, jsonDataObject.get("name"))
        Assertions.assertEquals(50, jsonDataObject.getJSONObject("noise").get("level"))
        Assertions.assertEquals(
            "https://somebucket.s3.us-east-2.amazonaws.com/testNoiseUrl",
            jsonDataObject.getJSONObject("noise").get("url"),
        )
    }

    @Test
    fun `test get exercises by ids`() {
        // GIVEN
        insertUser()
        val exerciseName = "ExerciseNameTest"
        val existingSeries = insertSeries()
        val subGroup = insertSubGroup(existingSeries)
        val exercise = insertExercise(subGroup, exerciseName)
        val requestJson: String = objectMapper.writeValueAsString(ExerciseRequest(listOf(exercise.id!!)))
        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post("$baseUrl/byIds")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson),
            )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        val jsonResponse = JSONObject(resultAction.andReturn().response.contentAsString)
        val jsonDataObject = jsonResponse.getJSONArray("data").getLong(0)
        Assertions.assertTrue(jsonDataObject == exercise.id!!)
    }

    @Test
    fun `test change active status`() {
        val existingExercise = insertExercise("ExerciseNameTest")
        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .put("$baseUrl/${existingExercise.id}/active/false")
                    .contentType(MediaType.APPLICATION_JSON),
            )
        // THEN
        resultAction
            .andExpect(status().isOk)
        exerciseRepository.findById(existingExercise.id!!)
        assertFalse(exerciseRepository.findById(existingExercise.id!!).get().active)
    }

    @Test
    fun `should not be validated ExerciseWordsCreateDto`() {
        // GIVEN
        val exerciseWordsCreateDto =
            ExerciseWordsCreateDto(
                locale = BrnLocale.RU,
                subGroup = "",
                level = 0,
                exerciseName = "",
                words = emptyList(),
                noiseLevel = 0,
            )
        val requestBody = objectMapper.writeValueAsString(exerciseWordsCreateDto)

        // WHEN
        val response =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            )

        // THEN
        response
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors.length()").value(3))
    }

    @Test
    fun `should not be validated ExercisePhrasesCreateDto`() {
        // GIVEN
        val exercisePhrasesCreateDto =
            ExercisePhrasesCreateDto(
                locale = BrnLocale.RU,
                subGroup = "",
                level = 0,
                exerciseName = "",
                phrases = Phrases("short phrase.", "long phrase"),
                noiseLevel = 0,
            )
        val requestBody = objectMapper.writeValueAsString(exercisePhrasesCreateDto)

        // WHEN
        val response =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            )

        // THEN
        response
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors.length()").value(3))
    }

    @Test
    fun `should not be validated ExerciseSentencesCreateDto`() {
        // GIVEN
        val exerciseSentencesCreateDto =
            ExerciseSentencesCreateDto(
                locale = BrnLocale.RU,
                subGroup = "",
                level = 0,
                exerciseName = "",
                orderNumber = 1,
                words = SetOfWords(emptyList()),
            )
        val requestBody = objectMapper.writeValueAsString(exerciseSentencesCreateDto)

        // WHEN
        val response =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            )

        // THEN
        response
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errors.length()").value(2))
    }

    private fun insertStudyHistory(
        existingUser: UserAccount,
        existingExercise: Exercise,
        startTime: LocalDateTime,
    ): StudyHistory = studyHistoryRepository.save(
        StudyHistory(
            userAccount = existingUser,
            exercise = existingExercise,
            endTime = startTime.plusMinutes(Random.nextLong(1, 5)),
            startTime = startTime,
            executionSeconds = 122,
            tasksCount = 12,
            wrongAnswers = 3,
            replaysCount = 4,
        ),
    )

    private fun insertUser(): UserAccount = userAccountRepository.save(
        UserAccount(
            fullName = "testUserFirstName",
            gender = BrnGender.MALE.toString(),
            bornYear = 2000,
            email = "test@test.test",
            active = true,
        ),
    )

    private fun insertSeries(): Series {
        val exerciseGroup =
            exerciseGroupRepository.save(
                ExerciseGroup(
                    code = "CODE",
                    description = "desc",
                    name = "group ExercisesControllerIT",
                ),
            )
        return seriesRepository.save(
            Series(
                description = "desc",
                name = "series for ExercisesControllerIT",
                exerciseGroup = exerciseGroup,
                type = "type",
                level = 1,
            ),
        )
    }

    private fun insertSubGroup(series: Series): SubGroup =
        subGroupRepository.save(SubGroup(series = series, level = 1, code = "code", name = "subGroup name"))

    fun insertExercise(
        subGroup: SubGroup,
        exerciseName: String,
    ): Exercise = exerciseRepository.save(
        Exercise(
            subGroup = subGroup,
            level = 0,
            name = exerciseName,
            noiseLevel = 50,
            noiseUrl = "/testNoiseUrl",
        ),
    )

    fun insertExercise(exerciseName: String): Exercise = exerciseRepository.save(Exercise(name = exerciseName))
}
