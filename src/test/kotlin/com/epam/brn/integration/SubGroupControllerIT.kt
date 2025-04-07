package com.epam.brn.integration

import com.epam.brn.dto.request.SubGroupRequest
import com.epam.brn.enums.BrnRole
import com.epam.brn.model.ExerciseGroup
import com.epam.brn.model.Series
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

@WithMockUser(username = "test@test.test", roles = [BrnRole.ADMIN, BrnRole.USER])
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
        seriesRepository.deleteAll()
        subGroupRepository.deleteAll()
    }

    @Test
    fun `test get subGroups for series`() {
        // GIVEN
        val series = insertDefaultSeries()
        insertDefaultSubGroup(series, 1)
        insertDefaultSubGroup(series, 2)
        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .get(baseUrl)
                    .param("seriesId", series.id!!.toString())
                    .contentType(MediaType.APPLICATION_JSON),
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
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .get("$baseUrl/${subGroup.id}")
                    .contentType(MediaType.APPLICATION_JSON),
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
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .delete("$baseUrl/${subGroup.id}")
                    .contentType(MediaType.APPLICATION_JSON),
            )
        // THEN
        resultAction.andExpect(status().isOk)
    }

    @Test
    fun `test delete subGroup by subGroupId should trow exception when subGroup has exercises`() {
        // GIVEN
        val series = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(series, 1)
        insertDefaultExercise(subGroup)
        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .delete("$baseUrl/${subGroup.id}")
                    .contentType(MediaType.APPLICATION_JSON),
            )
        // THEN
        resultAction
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers
                    .content()
                    .string(containsString("Can not delete subGroup because there are exercises that refer to the subGroup.")),
            )
    }

    @Test
    fun `test delete subGroup by subGroupId should trow exception when subGroup is not found`() {
        // GIVEN
        val series = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(series, 1)
        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .delete("$baseUrl/${subGroup.id}" + "1")
                    .contentType(MediaType.APPLICATION_JSON),
            )
        // THEN
        resultAction
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers
                    .content()
                    .string(containsString("Can not delete subGroup because subGroup is not found by this id.")),
            )
    }

    @Test
    fun `should add new subgroup to existed series`() {
        // GIVEN
        val subGroupRequest = SubGroupRequest("Test name", 1, "shortWords", "Test description")
        val existedSeries = insertSeries()
        val seriesId = existedSeries.id
        val requestJson = objectMapper.writeValueAsString(subGroupRequest)

        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(baseUrl)
                    .param("seriesId", seriesId.toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson),
            )

        // THEN
        resultAction
            .andExpect(status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.name").value(subGroupRequest.name))
    }

    @Test
    @Throws(Exception::class)
    fun `when post request to subGroup and invalid parameters in subGroup then correct response`() {
        // GIVEN
        val subGroupRequest =
            """{"name":"","code":"","level":"","description":"Test description" }"""

        // WHEN
        val response =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .post(baseUrl)
                    .param("seriesId", "0")
                    .content(subGroupRequest)
                    .contentType(MediaType.APPLICATION_JSON),
            )
        // THEN
        response
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers
                    .content()
                    .json("{\"errors\":[\"не должно быть пустым\",\"не должно равняться null\",\"не должно быть пустым\"] }"),
            )
    }

    @Test
    fun `should update subGroup`() {
        // GIVEN
        val series = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(series, 1)

        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .patch("$baseUrl/${subGroup.id}")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"withPictures": true}"""),
            )

        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.withPictures").value(true))
    }

    @Test
    fun `should throw exception when subGroup is not found`() {
        // GIVEN
        val series = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(series, 1)

        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .patch("$baseUrl/${subGroup.id}" + "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"withPictures": true}"""),
            )

        // THEN
        resultAction
            .andExpect(status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.content().string(
                    containsString("Can not update subGroup because subGroup is not found by this id."),
                ),
            )
    }

    private fun insertSeries(): Series {
        val exerciseGroup =
            exerciseGroupRepository.save(
                ExerciseGroup(
                    code = "CODE",
                    description = "desc",
                    name = "group",
                ),
            )
        return seriesRepository.save(
            Series(
                type = "type",
                level = 1,
                name = "series",
                exerciseGroup = exerciseGroup,
                description = "desc",
            ),
        )
    }
}
