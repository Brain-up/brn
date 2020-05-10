package com.epam.brn.integration

import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.SeriesRepository
import java.nio.charset.Charset
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
@WithMockUser(username = "admin", roles = ["ADMIN"])
class SeriesControllerIT {

    private val baseUrl = "/series"

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    private val seriesName = "распознавание слов тест"

    fun loadGroupWithExercises(): Long {
        val group = ExerciseGroup(name = "речевые упражнения тест", description = "речевые упражнения тест")
        val series1 =
            Series(name = seriesName, description = "descr1", exerciseGroup = group)
        val series2 =
            Series(name = "диахоничкеское слушание тест", description = "descr2", exerciseGroup = group)
        group.series.addAll(setOf(series1, series2))
        val savedGroup = exerciseGroupRepository.save(group)
        return savedGroup.id ?: 1
    }

    @AfterEach
    fun deleteAfterTest() {
        exerciseGroupRepository.deleteAll()
    }

    @Test
    fun `test get series for group`() {
        // GIVEN
        val idGroup = loadGroupWithExercises()
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl)
                .param("groupId", idGroup.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.getContentAsString(Charset.defaultCharset())
        Assertions.assertTrue(response.contains(seriesName))
        Assertions.assertTrue(response.contains("диахоничкеское слушание тест"))
        Assertions.assertTrue(response.contains("exercises"))
    }

    @Test
    fun `test get series for seriesId`() {
        // GIVEN
        loadGroupWithExercises()
        val seriesId = seriesRepository.findByNameLike(seriesName)[0].id
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/series/$seriesId")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.getContentAsString(Charset.defaultCharset())
        Assertions.assertTrue(response.contains(seriesName))
        Assertions.assertTrue(response.contains("exercises"))
    }

    @Test
    fun `test get file format for seriesId`() {
        val seriesId = 1
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/series/fileFormat/$seriesId")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.getContentAsString(Charset.defaultCharset())
        val expectedResponse =
            """{"data":"level,exerciseName,words,noise\n1,Слова без шума,(бал бум быль вить гад дуб),no_noise\n2,Слова без шума,(линь лис моль пар пять раб),no_noise\n3,Слова без шума,(рак рожь сеть топь ход шеф),no_noise\n4,Слова с малым шумом,(бал бум быль вить гад дуб),noise_6db","errors":[],"meta":[]}"""
        Assertions.assertEquals(expectedResponse, response)
    }
}
