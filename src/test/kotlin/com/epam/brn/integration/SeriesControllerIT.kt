package com.epam.brn.integration

import com.epam.brn.constant.BrnPath
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.SeriesRepository
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

    fun loadGroupWithExercises(): Long {
        val group = ExerciseGroup(name = "речевые упражения тест", description = "речевые упражения тест")
        val series1 =
            Series(name = "распознование слов тест", description = "descr1", exerciseGroup = group)
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
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
        val response = resultAction.andReturn().response.contentAsString
        Assertions.assertTrue(response.contains("распознование слов тест"))
        Assertions.assertTrue(response.contains("диахоничкеское слушание тест"))
        Assertions.assertTrue(response.contains("exercises"))
    }

    @Test
    fun `test get series for seriesId`() {
        // GIVEN
        loadGroupWithExercises()
        val seriesId = seriesRepository.findByNameLike("распознование слов тест")[0].id
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("${BrnPath.SERIES}/$seriesId")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8))
        val response = resultAction.andReturn().response.contentAsString
        Assertions.assertTrue(response.contains("распознование слов тест"))
        Assertions.assertTrue(response.contains("exercises"))
    }
}
