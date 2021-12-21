package com.epam.brn.integration

import com.epam.brn.enums.Role
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.repo.SeriesRepository
import com.epam.brn.repo.SubGroupRepository
import com.epam.brn.repo.UserAccountRepository
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
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

    @Autowired
    lateinit var authorityRepository: AuthorityRepository

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    private val baseUrl = "/subgroups"

    @AfterEach
    fun deleteAfterTest() {
        seriesRepository.deleteAll()
        exerciseGroupRepository.deleteAll()
        userAccountRepository.deleteAll()
        authorityRepository.deleteAll()
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

    @Test
    fun `test update subGroup by subGroupId`() {
        // GIVEN
        val authority = createAuthority(Role.ROLE_ADMIN.name)
        createUser(fullName = "testUserFirstName", email = "test@test.test", authorities = mutableSetOf(authority))
        val series = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(series, 1)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .patch("$baseUrl/${subGroup.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"withPictures": true}""")
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.withPictures").value(true))
    }

    @Test
    fun `test update subGroup by subGroupId should trow exception when subGroup is not found`() {
        // GIVEN
        val authority = createAuthority(Role.ROLE_ADMIN.name)
        createUser(fullName = "testUserFirstName", email = "test@test.test", authorities = mutableSetOf(authority))
        val series = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(series, 1)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .patch("$baseUrl/${subGroup.id}" + "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"withPictures": true}""")
        )
        // THEN
        resultAction
            .andExpect(status().isNotFound)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(containsString("Can not update subGroup because subGroup is not found by this id."))
            )
    }

    @Test
    fun `test update subGroup by subGroupId should trow exception when current user is not admin`() {
        // GIVEN
        insertDefaultUser()
        val series = insertDefaultSeries()
        val subGroup = insertDefaultSubGroup(series, 1)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .patch("$baseUrl/${subGroup.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"withPictures": true}""")
        )
        // THEN
        resultAction
            .andExpect(status().isBadRequest)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(
                MockMvcResultMatchers.content()
                    .string(containsString("It is forbidden to update subGroup."))
            )
    }
}
