package com.epam.brn.integration

import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.SeriesRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class SeriesControllerIT : BaseIT() {

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    private val baseUrl = "/series"
    private val series1Name = "серия 1 тест"
    private val series2Name = "серия 2 тест"

    @AfterEach
    fun deleteAfterTest() {
        exerciseGroupRepository.deleteAll()
    }

    private fun loadGroupWith2Series(): Long {
        val group = ExerciseGroup(name = "речевые упражнения тест", description = "речевые упражнения тест")
        val series1 =
            Series(name = series1Name, description = "descr1", exerciseGroup = group)
        val series2 =
            Series(name = series2Name, description = "descr2", exerciseGroup = group)
        group.series.addAll(setOf(series1, series2))
        val savedGroup = exerciseGroupRepository.save(group)
        return savedGroup.id ?: 1
    }

    @Test
    fun `test get series for group`() {
        // GIVEN
        val idGroup = loadGroupWith2Series()
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
        val response = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        Assertions.assertTrue(response.contains(series1Name))
        Assertions.assertTrue(response.contains(series2Name))
        Assertions.assertTrue(response.contains("exercises"))
    }

    @Test
    fun `test get series for seriesId`() {
        // GIVEN
        loadGroupWith2Series()
        val seriesId = seriesRepository.findByNameLike(series1Name)[0].id
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
        val response = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        Assertions.assertTrue(response.contains(series1Name))
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
        val response = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val expectedResponse =
            """{"data":"level,pictureUrl,exerciseName,words,noiseLevel,noiseUrl\n1,family,Семья,(сын ребёнок мама),0,\n2,family,Семья,(отец брат дедушка),0,\n3,family,Семья,(бабушка муж внучка),0,\n4,family,Семья,(сын ребёнок родители дочь мама папа),0,","errors":[],"meta":[]}"""
        Assertions.assertTrue(response.contains("1,family,Семья,(сын ребёнок мама),0,"))
        Assertions.assertEquals(expectedResponse, response)
    }
}
