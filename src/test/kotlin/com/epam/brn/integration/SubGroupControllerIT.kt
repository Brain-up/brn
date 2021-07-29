package com.epam.brn.integration

import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import org.hamcrest.CoreMatchers.containsString
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
class SubGroupControllerIT : BaseIT() {

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var seriesRepository: SeriesRepository

    @Autowired
    lateinit var subGroupRepository: SubGroupRepository

    private val baseUrl = "/subgroups"

    @AfterEach
    fun deleteAfterTest() {
        exerciseGroupRepository.deleteAll()
    }

    @Test
    fun `test get subGroups for series`() {
        // GIVEN
        val series = insertDefaultSeries()
        insertDefaultSubGroup(series, 1)
        insertDefaultSubGroup(series, 2)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl)
                .param("seriesId", series.id!!.toString())
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        Assertions.assertTrue(response.contains("subGroupName1"))
        Assertions.assertTrue(response.contains("subGroupName2"))
        Assertions.assertTrue(response.contains("exercises"))
    }

    @Test
    fun `test get subGroup for subGroupId`() {
        // GIVEN
        val series = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(series, 1)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("$baseUrl/${subGroup.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        Assertions.assertTrue(response.contains("subGroupName1"))
        Assertions.assertTrue(response.contains("exercises"))
    }

    @Test
    fun `test delete subGroup by subGroupId`() {
        // GIVEN
        val series = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(series, 1)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .delete("$baseUrl/${subGroup.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
    }

    @Test
    fun `test delete subGroup by subGroupId should trow exception when subGroup has exercises`() {
        // GIVEN
        val series = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(series, 1)
        insertDefaultExercise(subGroup)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .delete("$baseUrl/${subGroup.id}")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().string(containsString("Can not delete subGroup because there are exercises that refer to the subGroup.")))
    }

    @Test
    fun `test delete subGroup by subGroupId should trow exception when subGroup is not found`() {
        // GIVEN
        val series = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(series, 1)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .delete("$baseUrl/${subGroup.id}" + "1")
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.content().string(containsString("Can not delete subGroup because subGroup is not found by this id.")))
    }
}
