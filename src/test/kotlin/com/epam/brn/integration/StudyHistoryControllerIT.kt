package com.epam.brn.integration

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.model.StudyHistory
import com.epam.brn.repo.StudyHistoryRepository
import com.google.gson.Gson
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.assertNotNull

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class StudyHistoryControllerIT : BaseIT() {

    private val baseUrl = "/study-history"
    private val fromParameterName = "from"
    private val toParameterName = "to"

    @Autowired
    private lateinit var repository: StudyHistoryRepository

    @Test
    fun `save should save StudyHistory to the repository`() {
        // GIVEN
        val exercise = insertDefaultExercise()
        insertDefaultUser()
        val studyHistoryDtoId = 1L
        val studyHistoryDto = StudyHistoryDto(
            id = studyHistoryDtoId,
            exerciseId = exercise.id!!,
            startTime = LocalDateTime.now(),
            endTime = LocalDateTime.now().plusMinutes(5),
            executionSeconds = 300,
            tasksCount = 3,
            replaysCount = 3,
            wrongAnswers = 1
        )

        // WHEN
        val response = mockMvc.perform(
            post(baseUrl)
                .content(
                    Gson().toJson(studyHistoryDto)
                )
        )
            .andExpect(status().isOk)

        // THEN
        assertNotNull(repository.findById(studyHistoryDtoId))
    }

    @Test
    fun `getTodayWorkDurationInSeconds should return today's timer`() {
        // GIVEN
        insertDefaultUser()
        val gson = Gson()

        // WHEN
        val response = mockMvc.perform(get("$baseUrl/todayTimer"))
            .andExpect(status().isOk)
            .andReturn()
            .response
            .getContentAsString(StandardCharsets.UTF_8)

        val singleObjectResponseDto = gson.fromJson(response, BaseSingleObjectResponseDto::class.java)

        // THEN
        assertNotNull(singleObjectResponseDto)
    }

    @Test
    fun `getHistories should return histories for period of time`() {
        // GIVEN
        val user = insertDefaultUser()
        val exercise = insertDefaultExercise()
        val exercisingYear = 2019
        val exercisingMonth = 3
        val gson = Gson()
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 13, 0), 25)
        insertDefaultStudyHistory(user, exercise, LocalDateTime.of(exercisingYear, exercisingMonth, 20, 14, 0), 25)
        val from = LocalDateTime.of(exercisingYear, exercisingMonth, 1, 1, 1)
        val to = LocalDateTime.of(exercisingYear, exercisingMonth, 28, 1, 1)
        val datePattern = DateTimeFormatter.ISO_DATE_TIME

        // WHEN
        val response = mockMvc.perform(
            get("$baseUrl/histories")
                .param(fromParameterName, from.format(datePattern))
                .param(toParameterName, to.format(datePattern))
        )
            .andExpect(status().isOk)
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)

        // THEN
        val baseResponseDto = gson.fromJson(response, BaseResponseDto::class.java)
        val studyHistories = baseResponseDto.data as List<StudyHistory>

        assertNotNull(studyHistories)
    }
}
