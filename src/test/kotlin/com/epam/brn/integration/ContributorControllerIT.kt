package com.epam.brn.integration

import com.epam.brn.dto.request.contributor.ContactRequest
import com.epam.brn.dto.request.contributor.ContributorRequest
import com.epam.brn.dto.response.ContributorResponse
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.enums.ContributorType
import com.epam.brn.model.Contact
import com.epam.brn.model.Contributor
import com.epam.brn.repo.ContributorRepository
import com.fasterxml.jackson.core.type.TypeReference
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals

@WithMockUser(username = "test@test.test", roles = [BrnRole.ADMIN])
class ContributorControllerIT : BaseIT() {

    private val baseUrl = "/contributors"

    @Autowired
    lateinit var contributorRepository: ContributorRepository

    @AfterEach
    fun deleteAfterTest() {
        contributorRepository.deleteAll()
    }

    @Test
    fun `test get all contributors`() {
        // GIVEN
        insertContributor("Specialist", ContributorType.SPECIALIST)
        insertContributor("Developer", ContributorType.DEVELOPER)

        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
        )

        // THEN
        val response = resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val data = objectMapper.readValue(response, BrnResponse::class.java).data
        val contributors =
            objectMapper.readValue(
                objectMapper.writeValueAsString(data),
                object : TypeReference<List<ContributorResponse>>() {}
            )
        assertEquals(2, contributors.size)
    }

    @Test
    fun `test get contributors by type QA`() {
        // GIVEN
        insertContributor("Specialist", ContributorType.SPECIALIST)
        insertContributor("Developer", ContributorType.DEVELOPER)
        insertContributor("QA", ContributorType.QA)

        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get(baseUrl)
                .param("type", ContributorType.QA.name)
                .contentType(MediaType.APPLICATION_JSON)
        )

        // THEN
        val response = resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val data = objectMapper.readValue(response, BrnResponse::class.java).data
        val contributors =
            objectMapper.readValue(
                objectMapper.writeValueAsString(data),
                object : TypeReference<List<ContributorResponse>>() {}
            )
        assertEquals(1, contributors.size)
        assertEquals("QA", contributors[0].name)
    }

    @Test
    fun `test add contributor`() {
        // GIVEN
        val contributorRequest = createContributorRequest("New Contributor")

        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .post(baseUrl)
                .content(objectMapper.writeValueAsString(contributorRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )

        // THEN
        val response = resultAction
            .andExpect(status().isCreated)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val data = objectMapper.readValue(response, BrnResponse::class.java).data
        val newContributor =
            objectMapper.readValue(
                objectMapper.writeValueAsString(data),
                object : TypeReference<ContributorResponse>() {}
            )
        assertEquals(contributorRequest.name, newContributor.name)
        assertEquals(contributorRequest.type, newContributor.type)
    }

    @Test
    fun `test update contributor`() {
        // GIVEN
        val contributorSpecialist = insertContributor("Specialist", ContributorType.SPECIALIST)
        val contributorRequest = createContributorRequest(name = "Updated Contributor")

        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .put("$baseUrl/${contributorSpecialist.id}")
                .content(objectMapper.writeValueAsString(contributorRequest))
                .contentType(MediaType.APPLICATION_JSON)
        )

        // THEN
        val response = resultAction
            .andExpect(status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
            .andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val data = objectMapper.readValue(response, BrnResponse::class.java).data
        val updatedContributor =
            objectMapper.readValue(
                objectMapper.writeValueAsString(data),
                object : TypeReference<ContributorResponse>() {}
            )
        assertEquals(contributorRequest.name, updatedContributor.name)
    }

    private fun insertContributor(
        name: String,
        type: ContributorType,
        contribution: Long = 1,
        contacts: MutableSet<Contact> = mutableSetOf()
    ) = contributorRepository.save(createContributor(name, type, contribution, contacts))

    private fun createContributor(
        name: String,
        type: ContributorType,
        contribution: Long = 1,
        contacts: MutableSet<Contact> = mutableSetOf()
    ) = Contributor(
        name = name,
        nameEn = name,
        type = type,
        contribution = contribution
    ).apply { this.contacts = contacts.toMutableList() }

    private fun createContributorRequest(
        name: String,
        contribution: Long = 1,
        type: ContributorType = ContributorType.SPECIALIST,
        contacts: Set<ContactRequest> = setOf()
    ): ContributorRequest {
        return ContributorRequest(
            name = name,
            nameEn = name,
            description = name,
            descriptionEn = name,
            contribution = contribution,
            type = type,
            contacts = contacts,
            company = null,
            companyEn = null,
            pictureUrl = null,
            active = true,
        )
    }
}
