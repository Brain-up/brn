package com.epam.brn.integration

import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.model.Role
import com.epam.brn.repo.RoleRepository
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

@WithMockUser(username = "test@test.test", roles = [BrnRole.ADMIN])
class RoleControllerIT : BaseIT() {
    private val baseUrl = "/roles"

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    lateinit var gson: Gson

    @AfterEach
    fun deleteAfterTest() {
        roleRepository.deleteAll()
    }

    @Test
    fun `should return roles list`() {
        // GIVEN
        roleRepository.deleteAll()
        insertRole(BrnRole.ADMIN)
        insertRole(BrnRole.USER)
        insertRole(BrnRole.SPECIALIST)
        // WHEN
        val resultAction =
            mockMvc.perform(
                MockMvcRequestBuilders
                    .get("$baseUrl"),
            )

        // THEN
        resultAction
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))

        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponse = objectMapper.readValue(responseJson, BrnResponse::class.java)
        val roles =
            objectMapper.readValue(
                gson.toJson(baseResponse.data),
                object : TypeReference<List<Role>>() {},
            )
        roles.size shouldBe 3
    }

    private fun insertRole(roleName: String): Role = roleRepository.save(
        Role(
            name = roleName,
        ),
    )
}
