package com.epam.brn.integration

import com.epam.brn.dto.BaseResponseDto
import com.epam.brn.dto.BaseSingleObjectResponseDto
import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.model.Gender
import com.epam.brn.model.Headphones
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.HeadphonesRepository
import com.epam.brn.repo.UserAccountRepository
import com.fasterxml.jackson.core.type.TypeReference
import com.google.gson.Gson
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
class UserDetailsControllerIT : BaseIT() {

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var headphonesRepository: HeadphonesRepository

    @Autowired
    private lateinit var gson: Gson

    @Autowired
    lateinit var authorityRepository: AuthorityRepository

    internal val email: String = "test@test.test"
    private val baseUrl = "/users"
    private val currentUserBaseUrl = "$baseUrl/current"

    @Test
    fun `test findUserAccountsByDoctor and findUserAccountsByDoctorId`() {
        // GIVEN
        val doctor = insertUser()
        val patient1 = insertUser("patient1@patient.ru", doctor)
        val patient2 = insertUser("patient2@patient.ru", doctor)
        // WHEN
        val patientsByDoctor = userAccountRepository.findUserAccountsByDoctor(doctor)
        val patientsByDoctorId = userAccountRepository.findUserAccountsByDoctorId(doctor.id!!)
        // THEN
        patientsByDoctor.size shouldBe 2
        patientsByDoctorId.size shouldBe 2
        patientsByDoctor shouldContainAll listOf(patient1, patient2)
        patientsByDoctorId shouldContainAll listOf(patient1, patient2)
    }

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
        val resultUser: UserAccountResponse =
            objectMapper.readValue(gson.toJson(baseResponseDto.data), UserAccountResponse::class.java)
        resultUser.id shouldBe user.id
        resultUser.name shouldBe user.fullName
        resultUser.avatar shouldBe "/pictures/testAvatar"
        resultUser.changed shouldNotBe user.changed
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
        val resultUser: UserAccountResponse =
            objectMapper.readValue(gson.toJson(baseResponseDto.data), UserAccountResponse::class.java)
        resultUser.id shouldBe user.id
        resultUser.name shouldBe "newName"
        resultUser.bornYear shouldBe 1950
        resultUser.avatar shouldBe user.avatar
        resultUser.photo shouldBe user.photo
        resultUser.description shouldBe user.description
    }

    @Test
    fun `add headphones to user`() {
        // GIVEN
        val user = insertUser()
        // WHEN
        val body =
            objectMapper.writeValueAsString(HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH))
        val resultAction = mockMvc.perform(
            post("$baseUrl/${user.id}/headphones")
                .content(body)
                .contentType("application/json")
        )
        // THEN
        assertHeadphonesAreCreated(resultAction)
    }

    @Test
    fun `add headphones to current user as admin`() {
        // GIVEN
        insertUser()
        // WHEN
        val body =
            objectMapper.writeValueAsString(HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH))
        val resultAction = mockMvc.perform(
            post("$baseUrl/current/headphones")
                .content(body)
                .contentType("application/json")
        )
        // THEN
        assertHeadphonesAreCreated(resultAction)
    }

    @Test
    @WithMockUser(username = "test@test.test", roles = ["USER"])
    fun `add headphones to current user not as admin`() {
        // GIVEN
        insertUser()
        // WHEN
        val body =
            objectMapper.writeValueAsString(HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH))
        val resultAction = mockMvc.perform(
            post("$baseUrl/current/headphones")
                .content(body)
                .contentType("application/json")
        )
        // THEN
        assertHeadphonesAreCreated(resultAction)
    }

    private fun assertHeadphonesAreCreated(resultAction: ResultActions) {
        resultAction.andExpect(status().isCreated)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BaseSingleObjectResponseDto::class.java)
        val addedHeadphones: HeadphonesDto =
            objectMapper.readValue(gson.toJson(baseResponseDto.data), HeadphonesDto::class.java)
        addedHeadphones.id shouldNotBe null
        addedHeadphones.name shouldBe "first"
        addedHeadphones.active shouldBe true
        addedHeadphones.type shouldBe HeadphonesType.IN_EAR_NO_BLUETOOTH
    }

    @Test
    fun `delete headphones to current user`() {
        // GIVEN
        val user = insertUser()
        insertThreeHeadphonesForUser(user)
        val headphonesId = userAccountRepository.findUserAccountByName("testUserFirstName")
            .get().headphones.first().id
        // WHEN
        val resultAction = mockMvc.perform(
            delete("$baseUrl/current/headphones/$headphonesId")
                .contentType("application/json")
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
        userAccountRepository.findUserAccountById(user.id!!).get()
            .headphones.filter { it.active }.size shouldBe 2
    }

    @Test
    @WithMockUser(username = "test@test.test", roles = ["USER"])
    fun `add default headphones to current user`() {
        // GIVEN
        insertUser()
        // WHEN
        val body =
            objectMapper.writeValueAsString(HeadphonesDto(name = "first", active = true, type = null))
        val resultAction = mockMvc.perform(
            post("$baseUrl/current/headphones")
                .content(body)
                .contentType("application/json")
        )
        // THEN
        resultAction.andExpect(status().isCreated)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BaseSingleObjectResponseDto::class.java)
        val addedHeadphones: HeadphonesDto =
            objectMapper.readValue(objectMapper.writeValueAsString(baseResponseDto.data), HeadphonesDto::class.java)
        addedHeadphones.id shouldNotBe null
        addedHeadphones.name shouldBe "first"
        addedHeadphones.type shouldBe HeadphonesType.NOT_DEFINED
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
            gson.toJson(baseResponseDto.data),
            object : TypeReference<List<HeadphonesDto>>() {}
        )
        Assertions.assertThat(returnedHeadphones)
            .hasSize(3)
            .usingElementComparatorOnFields("name", "type")
            .containsAll(
                listOf(
                    HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH),
                    HeadphonesDto(name = "second", active = true, type = HeadphonesType.IN_EAR_BLUETOOTH),
                    HeadphonesDto(name = "third", active = true, type = HeadphonesType.OVER_EAR_BLUETOOTH)
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
            gson.toJson(baseResponseDto.data),
            object : TypeReference<List<HeadphonesDto>>() {}
        )
        returnedHeadphones shouldNotBe null
        Assertions.assertThat(returnedHeadphones)
            .hasSize(3)
            .usingElementComparatorOnFields("name", "type")
            .containsAll(
                listOf(
                    HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH),
                    HeadphonesDto(name = "second", active = true, type = HeadphonesType.IN_EAR_BLUETOOTH),
                    HeadphonesDto(name = "third", active = true, type = HeadphonesType.OVER_EAR_BLUETOOTH)
                )
            )
    }

    @AfterEach
    fun deleteAfterTest() {
        userAccountRepository.deleteAll()
        authorityRepository.deleteAll()
        headphonesRepository.deleteAll()
    }

    private fun insertUser(email_: String = email, doctor_: UserAccount? = null): UserAccount =
        userAccountRepository.save(
            UserAccount(
                fullName = "testUserFirstName",
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                email = email_,
                doctor = doctor_
            )
        )

    private fun insertThreeHeadphonesForUser(user: UserAccount) {
        headphonesRepository.saveAll(
            listOf(
                Headphones(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH, userAccount = user),
                Headphones(name = "second", active = true, type = HeadphonesType.IN_EAR_BLUETOOTH, userAccount = user),
                Headphones(name = "third", active = true, type = HeadphonesType.OVER_EAR_BLUETOOTH, userAccount = user)
            )
        )
    }
}
