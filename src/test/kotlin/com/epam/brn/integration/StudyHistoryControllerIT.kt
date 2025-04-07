package com.epam.brn.integration

import com.epam.brn.dto.StudyHistoryDto
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.repo.StudyHistoryRepository
import com.google.gson.Gson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import kotlin.test.assertNotNull

@WithMockUser(username = "test@test.test", roles = [BrnRole.USER])
class StudyHistoryControllerIT : BaseIT() {
    private val baseUrl = "/study-history"

    @Autowired
    private lateinit var gson: Gson

    @Autowired
    private lateinit var repository: StudyHistoryRepository

    @AfterEach
    fun rollback() {
        deleteInsertedTestData()
    }

    @Test
    fun `save should save StudyHistory to the repository`() {
        // GIVEN
        val exercise = insertDefaultExercise()
        insertDefaultUser()
        val studyHistoryDtoId = 1L
        val studyHistoryDto =
            StudyHistoryDto(
                id = studyHistoryDtoId,
                exerciseId = exercise.id!!,
                startTime = LocalDateTime.now(),
                endTime = LocalDateTime.now().plusMinutes(5),
                executionSeconds = 300,
                tasksCount = 3,
                replaysCount = 3,
                wrongAnswers = 1,
            )
        val requestBody = objectMapper.writeValueAsString(studyHistoryDto)
        // WHEN
        mockMvc
            .perform(
                post(baseUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody),
            ).andExpect(status().isOk)

        // THEN
        assertNotNull(repository.findById(studyHistoryDtoId))
    }

    @Test
    fun `getTodayWorkDurationInSeconds should return today's timer`() {
        // GIVEN
        insertDefaultUser()

        // WHEN
        val response =
            mockMvc
                .perform(get("$baseUrl/todayTimer"))
                .andExpect(status().isOk)
                .andReturn()
                .response
                .getContentAsString(StandardCharsets.UTF_8)

        val singleObjectResponseDto = gson.fromJson(response, BrnResponse::class.java)

        // THEN
        assertNotNull(singleObjectResponseDto)
    }
}
