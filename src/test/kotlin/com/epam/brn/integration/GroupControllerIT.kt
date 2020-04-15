package com.epam.brn.integration

import com.epam.brn.model.ExerciseGroup
import com.epam.brn.repo.ExerciseGroupRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
class GroupControllerIT {

    private val baseUrl = "/groups"

    @Autowired
    lateinit var exerciseGroupRepository: ExerciseGroupRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    @AfterEach
    fun deleteAfterTest() {
        exerciseGroupRepository.deleteAll()
    }

    @Test
    fun `test get all groups`() {
        // GIVEN
        val exerciseGroupName1 = "SOMEGROUPNAME1"
        val existingExerciseGroup1 = insertExerciseGroup(exerciseGroupName1)
        val exerciseGroupName2 = "SOMEGROUPNAME2"
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
    fun `test get group by Id`() {
        // GIVEN
        val exerciseGroupName = "SOMEGROUPNAME"
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

    fun insertExerciseGroup(exerciseGroupName: String): ExerciseGroup {
        return exerciseGroupRepository.save(
            ExerciseGroup(
                id = 0,
                description = "desc",
                name = exerciseGroupName
            )
        )
    }
}
