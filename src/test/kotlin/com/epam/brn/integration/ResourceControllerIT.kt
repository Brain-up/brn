package com.epam.brn.integration

import com.epam.brn.dto.request.UpdateResourceDescriptionRequest
import com.epam.brn.enums.RoleConstants
import com.epam.brn.model.Resource
import com.epam.brn.repo.ResourceRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

@WithMockUser(username = "test@test.test", roles = [RoleConstants.ADMIN])
class ResourceControllerIT : BaseIT() {

    private val baseUrl = "/resources"

    @Autowired
    lateinit var resourceRepository: ResourceRepository

    @AfterEach
    fun deleteAfterTest() {
        resourceRepository.deleteAll()
    }

    @Test
    fun `should update resource description successfully`() {
        // GIVEN
        val resource = resourceRepository.save(Resource(description = "description", wordType = "OBJECT"))
        val descriptionForUpdate = "new description"
        val requestJson = objectMapper.writeValueAsString(UpdateResourceDescriptionRequest(descriptionForUpdate))

        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .patch("$baseUrl/resources/${resource.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson)
        )

        // THEN
        resultAction
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(resource.id))
            .andExpect(MockMvcResultMatchers.jsonPath("$.data.description").value(descriptionForUpdate))
    }
}
