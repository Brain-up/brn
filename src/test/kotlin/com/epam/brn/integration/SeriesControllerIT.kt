package com.epam.brn.integration

import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.SeriesDto
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.enums.ExerciseType
import com.epam.brn.model.Series
import com.fasterxml.jackson.core.type.TypeReference
import com.google.gson.Gson
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
    private val series1Name = "series1Name"
    private val series2Name = "series2Name"

    @AfterEach
    fun deleteAfterTest() {
        exerciseGroupRepository.deleteAll()
    }

    private fun insertGroup(): ExerciseGroup {
        val group = ExerciseGroup(name = "GroupName", description = "GroupDescription SeriesControllerIT")
        return exerciseGroupRepository.save(group)
    }

    private fun insertSeries(group: ExerciseGroup, name: String): Series {
        val series = Series(name = name, description = "description", exerciseGroup = group, level = 1, type = ExerciseType.SINGLE_SIMPLE_WORDS.name)
        return seriesRepository.save(series)
    }

    @Test
    fun `test get series for group`() {
        // GIVEN
        val group = insertGroup()
        val series1 = insertSeries(group, series1Name)
        val series2 = insertSeries(group, series2Name)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl)
                .param("groupId", group.id.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BaseSingleObjectResponseDto::class.java)
        val seriesJson = Gson().toJson(baseResponseDto.data)
        val resultSeries: List<SeriesDto> =
            objectMapper.readValue(seriesJson, object : TypeReference<List<SeriesDto>>() {})
        Assertions.assertEquals(2, resultSeries.size)
        Assertions.assertEquals(series1.toDto(), resultSeries[0])
        Assertions.assertEquals(series2.toDto(), resultSeries[1])
    }

    @Test
    fun `test get series for seriesId`() {
        // GIVEN
        val group = insertGroup()
        val series = insertSeries(group, "series")
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("/series/${series.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BaseSingleObjectResponseDto::class.java)
        val resultSeries: SeriesDto = objectMapper.readValue(Gson().toJson(baseResponseDto.data), SeriesDto::class.java)
        Assertions.assertEquals(series.toDto(), resultSeries)
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
            """{"data":"level,code,exerciseName,words,noiseLevel,noiseUrl\n1,family,Семья,(сын ребёнок мама),0,\n2,family,Семья,(отец брат дедушка),0,\n3,family,Семья,(бабушка муж внучка),0,\n4,family,Семья,(сын ребёнок родители дочь мама папа),0,","errors":[],"meta":[]}"""
        Assertions.assertTrue(response.contains("1,family,Семья,(сын ребёнок мама),0,"))
        Assertions.assertEquals(expectedResponse, response)
    }
}
