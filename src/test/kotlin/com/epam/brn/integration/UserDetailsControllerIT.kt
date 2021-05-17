package com.epam.brn.integration

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.model.Gender
import com.epam.brn.model.Headphones
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.HeadphonesRepository
import com.epam.brn.repo.UserAccountRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.google.gson.Gson
import org.amshove.kluent.internal.assertNotSame
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class UserDetailsControllerIT : BaseIT() {

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var headphonesRepository: HeadphonesRepository

    @Autowired
    lateinit var authorityRepository: AuthorityRepository

    internal val email: String = "test@test.test"
    internal val password: String = "test"
    private val baseUrl = "/users"
    private val currentUserBaseUrl = "$baseUrl/current"

    @Test
    fun `update avatar for current user`() {
        // GIVEN
        val user = insertUser()
        // WHEN
        val resultAction = mockMvc.perform(
            put("$currentUserBaseUrl/avatar")
                .queryParam("avatar", "/pictures/testAvatar")
        )
        // THEN
        resultAction.andExpect(status().isOk)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BaseSingleObjectResponseDto::class.java)
        val resultUser: UserAccountDto =
            objectMapper.readValue(Gson().toJson(baseResponseDto.data), UserAccountDto::class.java)
        assertEquals(user.id, resultUser.id)
        assertEquals(user.fullName, resultUser.name)
        assertEquals("/pictures/testAvatar", resultUser.avatar)
        assertNotSame(user.changed, resultUser.changed)
    }

    @Test
    fun `update current user`() {
        // GIVEN
        val user = insertUser()
        // WHEN
        val body = objectMapper.writeValueAsString(UserAccountChangeRequest(name = "newName", bornYear = 1950))
        val resultAction = mockMvc.perform(
            patch(currentUserBaseUrl)
                .content(body)
                .contentType("application/json")
        )
        // THEN
        resultAction.andExpect(status().isOk)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BaseSingleObjectResponseDto::class.java)
        val resultUser: UserAccountDto =
            objectMapper.readValue(Gson().toJson(baseResponseDto.data), UserAccountDto::class.java)
        assertEquals(user.id, resultUser.id)
        assertEquals("newName", resultUser.name)
        assertEquals(1950, resultUser.bornYear)
        assertEquals(user.avatar, resultUser.avatar)
        assertEquals(user.photo, resultUser.photo)
        assertEquals(user.description, resultUser.description)
        assertNotSame(user.changed, resultUser.changed)
    }

    @Test
    fun `add headphones to user`() {
        // GIVEN
        val user = insertUser()
        // WHEN
        val body =
            objectMapper.writeValueAsString(HeadphonesDto(name = "first", type = HeadphonesType.IN_EAR_NO_BLUETOOTH))
        val resultAction = mockMvc.perform(
            post("$baseUrl/${user.id}/headphones")
                .content(body)
                .contentType("application/json")
        )
        // THEN
        resultAction.andExpect(status().isCreated)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BaseSingleObjectResponseDto::class.java)
        val addedHeadphones: HeadphonesDto =
            objectMapper.readValue(Gson().toJson(baseResponseDto.data), HeadphonesDto::class.java)
        assertNotNull(addedHeadphones.id)
        assertEquals("first", addedHeadphones.name)
        assertEquals(HeadphonesType.IN_EAR_NO_BLUETOOTH, addedHeadphones.type)
    }

    @Test
    fun `get all headphones from the user`() {
        // GIVEN
        val user = insertUser()
        insertThreeHeadphonesForUser(user)
        // WHEN
        val resultAction = mockMvc.perform(
            get("$baseUrl/${user.id}/headphones")
                .contentType("application/json")
        )
        // THEN
        resultAction.andExpect(status().isOk)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BaseResponseDto::class.java)
        val returnedHeadphones = objectMapper.readValue(
            Gson().toJson(baseResponseDto.data),
            object : TypeReference<List<HeadphonesDto>>() {}
        )
        assertNotNull(returnedHeadphones)
        Assertions.assertThat(returnedHeadphones)
            .hasSize(3)
            .usingElementComparatorOnFields("name", "type")
            .containsAll(
                listOf(
                    HeadphonesDto(name = "first", type = HeadphonesType.IN_EAR_NO_BLUETOOTH),
                    HeadphonesDto(name = "second", type = HeadphonesType.IN_EAR_BLUETOOTH),
                    HeadphonesDto(name = "third", type = HeadphonesType.OVER_EAR_BLUETOOTH)
                )
            )
    }

    private fun insertThreeHeadphonesForUser(user: UserAccount) {
        headphonesRepository.saveAll(
            listOf(
                Headphones(name = "first", type = HeadphonesType.IN_EAR_NO_BLUETOOTH, userAccount = user),
                Headphones(name = "second", type = HeadphonesType.IN_EAR_BLUETOOTH, userAccount = user),
                Headphones(name = "third", type = HeadphonesType.OVER_EAR_BLUETOOTH, userAccount = user)
            )
        )
    }

    @Test
    fun `get all headphones for current user`() {
        // GIVEN
        val user = insertUser()
        insertThreeHeadphonesForUser(user)
        // WHEN
        val resultAction = mockMvc.perform(
            get("$baseUrl/current/headphones")
                .contentType("application/json")
        )
        // THEN
        resultAction.andExpect(status().isOk)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BaseResponseDto::class.java)
        val returnedHeadphones = objectMapper.readValue(
            Gson().toJson(baseResponseDto.data),
            object : TypeReference<List<HeadphonesDto>>() {}
        )
        assertNotNull(returnedHeadphones)
        Assertions.assertThat(returnedHeadphones)
            .hasSize(3)
            .usingElementComparatorOnFields("name", "type")
            .containsAll(
                listOf(
                    HeadphonesDto(name = "first", type = HeadphonesType.IN_EAR_NO_BLUETOOTH),
                    HeadphonesDto(name = "second", type = HeadphonesType.IN_EAR_BLUETOOTH),
                    HeadphonesDto(name = "third", type = HeadphonesType.OVER_EAR_BLUETOOTH)
                )
            )
    }

    @AfterEach
    fun deleteAfterTest() {
        userAccountRepository.deleteAll()
        authorityRepository.deleteAll()
    }

    private fun insertUser(): UserAccount =
        userAccountRepository.save(
            UserAccount(
                fullName = "testUserFirstName",
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                email = email,
                password = password
            )
        )
}
