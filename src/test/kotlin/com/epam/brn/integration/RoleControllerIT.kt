package com.epam.brn.integration

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.enums.AuthorityType
import com.epam.brn.enums.RoleConstants
import com.epam.brn.model.Authority
import com.epam.brn.repo.AuthorityRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.google.gson.Gson
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.nio.charset.StandardCharsets

@WithMockUser(username = "test@test.test", roles = [RoleConstants.ADMIN])
class RoleControllerIT : BaseIT() {

    private val baseUrl = "/roles"

    @Autowired
    lateinit var authorityRepository: AuthorityRepository

    @Autowired
    lateinit var gson: Gson

    @AfterEach
    fun deleteAfterTest() {
        authorityRepository.deleteAll()
    }

    @Test
    fun `should return authorities list`() {
        // GIVEN
        insertRole(AuthorityType.ROLE_ADMIN.name)
        insertRole(AuthorityType.ROLE_USER.name)
        insertRole(AuthorityType.ROLE_DOCTOR.name)
        // WHEN
        val resultAction = mockMvc.perform(
            MockMvcRequestBuilders
                .get("$baseUrl")
        )

        // THEN
        resultAction
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))

        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponse = objectMapper.readValue(responseJson, BaseResponse::class.java)
        val authorities = objectMapper.readValue(
            gson.toJson(baseResponse.data),
            object : TypeReference<List<Authority>>() {}
        )
        authorities.size shouldBe 3
    }

    private fun insertRole(authorityName: String): Authority {
        return authorityRepository.save(
            Authority(
                authorityName = authorityName
            )
        )
    }
}
