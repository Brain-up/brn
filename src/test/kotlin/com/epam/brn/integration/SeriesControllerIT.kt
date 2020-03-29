package com.epam.brn.integration

import com.epam.brn.constant.BrnPath
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
                .get(BrnPath.SERIES)
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
                .get("${BrnPath.SERIES}/$seriesId")
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
                .get("${BrnPath.SERIES}/fileFormat/$seriesId")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.getContentAsString(Charset.defaultCharset())
        val expectedResponse =
            """{"data":"level exerciseName orderNumber word audioFileName pictureFileName words wordType\n1 \"Однослоговые слова без шума\" 1 бал no_noise/бал.mp3 pictures/бал.jpg (бам,сам,дам,зал,бум) OBJECT\n1 \"Однослоговые слова без шума\" 2 бум no_noise/бум.mp3 pictures/бум.jpg (зум,кум,шум,зуб,куб) OBJECT\n1 \"Однослоговые слова без шума\" 3 быль no_noise/быль.mp3 pictures/быль.jpg (пыль,соль,мыль,дыль,киль) OBJECT\n1 \"Однослоговые слова без шума\" 4 вить no_noise/вить.mp3 pictures/вить.jpg (бить,жить,мыль,выть,лить) OBJECT_ACTION","errors":[],"meta":[]}"""
        Assertions.assertEquals(expectedResponse, response)
    }
}
