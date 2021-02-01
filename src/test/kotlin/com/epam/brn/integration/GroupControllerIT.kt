package com.epam.brn.integration

import com.epam.brn.enum.Locale
import com.epam.brn.repo.ExerciseGroupRepository
import com.epam.brn.model.ExerciseGroup
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertFalse

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class GroupControllerIT : BaseIT() {

    private val baseUrl = "/groups"

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @AfterEach
    fun deleteAfterTest() {
        exerciseGroupRepository.deleteAll()
    }

    @Test
    fun `test get all groups`() {
        // GIVEN
        val exerciseGroupName1 = "GroupName1"
        val existingExerciseGroup1 = insertExerciseGroup(exerciseGroupName1)
        val exerciseGroupName2 = "GroupName2"
        val existingExerciseGroup2 = insertExerciseGroup(exerciseGroupName2)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.contentAsString
        assertTrue(response.contains(existingExerciseGroup1.name))
        assertTrue(response.contains(existingExerciseGroup2.name))
    }

    @Test
    fun `test find ru groups`() {
        // GIVEN
        val exerciseGroupName1 = "GroupName1"
        val groupRu = insertExerciseGroup(exerciseGroupName1, Locale.RU.name)
        val exerciseGroupName2 = "GroupName2"
        val groupEn = insertExerciseGroup(exerciseGroupName2, Locale.EN.name)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl)
                .param("locale", Locale.RU.locale)
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.contentAsString
        assertTrue(response.contains(groupRu.name))
        assertFalse(response.contains(groupEn.name))
    }

    @Test
    fun `test get group by Id`() {
        // GIVEN
        val exerciseGroupName = "GroupName"
        val existingExerciseGroup = insertExerciseGroup(exerciseGroupName)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl + "/" + existingExerciseGroup.id)
                .contentType(MediaType.APPLICATION_JSON)
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
        val response = resultAction.andReturn().response.contentAsString
        assertTrue(response.contains(existingExerciseGroup.name))
    }

    private fun insertExerciseGroup(exerciseGroupName: String, locale: String = "ru"): ExerciseGroup {
        return exerciseGroupRepository.save(
            ExerciseGroup(
                id = 0,
                description = "desc",
                name = exerciseGroupName,
                locale = locale
            )
        )
    }
}
