package com.epam.brn.integration

import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.response.BrnResponse
import com.epam.brn.enums.BrnGender
import com.epam.brn.enums.BrnRole
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.model.Headphones
import com.epam.brn.model.Role
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.HeadphonesRepository
import com.epam.brn.repo.RoleRepository
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets

@WithMockUser(username = "test@test.test", roles = [BrnRole.ADMIN, BrnRole.USER])
class UserDetailsControllerIT : BaseIT() {
    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var headphonesRepository: HeadphonesRepository

    @Autowired
    private lateinit var gson: Gson

    @Autowired
    lateinit var roleRepository: RoleRepository

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
        val resultAction =
            mockMvc.perform(
                put("$currentUserBaseUrl/avatar")
                    .queryParam("avatar", "/pictures/testAvatar"),
            )
        // THEN
        resultAction.andExpect(status().isOk)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BrnResponse::class.java)
        val resultUser: UserAccountDto =
            objectMapper.readValue(gson.toJson(baseResponseDto.data), UserAccountDto::class.java)
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
        val resultAction =
            mockMvc.perform(
                patch(currentUserBaseUrl)
                    .content(body)
                    .contentType("application/json"),
            )
        // THEN
        resultAction.andExpect(status().isOk)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BrnResponse::class.java)
        val resultUser: UserAccountDto =
            objectMapper.readValue(gson.toJson(baseResponseDto.data), UserAccountDto::class.java)
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
        val resultAction =
            mockMvc.perform(
                post("$baseUrl/${user.id}/headphones")
                    .content(body)
                    .contentType("application/json"),
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
        val resultAction =
            mockMvc.perform(
                post("$baseUrl/current/headphones")
                    .content(body)
                    .contentType("application/json"),
            )
        // THEN
        assertHeadphonesAreCreated(resultAction)
    }

    @Test
    @WithMockUser(username = "test@test.test", roles = [BrnRole.USER])
    fun `add headphones to current user not as admin`() {
        // GIVEN
        insertUser()
        // WHEN
        val body =
            objectMapper.writeValueAsString(HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH))
        val resultAction =
            mockMvc.perform(
                post("$baseUrl/current/headphones")
                    .content(body)
                    .contentType("application/json"),
            )
        // THEN
        assertHeadphonesAreCreated(resultAction)
    }

    private fun assertHeadphonesAreCreated(resultAction: ResultActions) {
        resultAction.andExpect(status().isCreated)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BrnResponse::class.java)
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
        val headphonesId =
            userAccountRepository
                .findUserAccountByName("testUserFirstName")
                .get()
                .headphones
                .first()
                .id
        // WHEN
        val resultAction =
            mockMvc.perform(
                delete("$baseUrl/current/headphones/$headphonesId")
                    .contentType("application/json"),
            )
        // THEN
        resultAction
            .andExpect(status().isOk)
        userAccountRepository
            .findUserAccountById(user.id!!)
            .get()
            .headphones
            .filter { it.active }
            .size shouldBe 2
    }

    @Test
    @WithMockUser(username = "test@test.test", roles = [BrnRole.USER])
    fun `add default headphones to current user`() {
        // GIVEN
        insertUser()
        // WHEN
        val body =
            objectMapper.writeValueAsString(HeadphonesDto(name = "first", active = true, type = null))
        val resultAction =
            mockMvc.perform(
                post("$baseUrl/current/headphones")
                    .content(body)
                    .contentType("application/json"),
            )
        // THEN
        resultAction.andExpect(status().isCreated)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponseDto = objectMapper.readValue(responseJson, BrnResponse::class.java)
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
        val resultAction =
            mockMvc.perform(
                get("$baseUrl/${user.id}/headphones")
                    .contentType("application/json"),
            )
        // THEN
        resultAction.andExpect(status().isOk)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponse = objectMapper.readValue(responseJson, BrnResponse::class.java)
        val returnedHeadphones =
            objectMapper.readValue(
                gson.toJson(baseResponse.data),
                object : TypeReference<List<HeadphonesDto>>() {},
            )
        Assertions
            .assertThat(returnedHeadphones)
            .hasSize(3)
            .usingElementComparatorOnFields("name", "type")
            .containsAll(
                listOf(
                    HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH),
                    HeadphonesDto(name = "second", active = true, type = HeadphonesType.IN_EAR_BLUETOOTH),
                    HeadphonesDto(name = "third", active = true, type = HeadphonesType.OVER_EAR_BLUETOOTH),
                ),
            )
    }

    @Test
    fun `get all headphones for current user`() {
        // GIVEN
        val user = insertUser()
        insertThreeHeadphonesForUser(user)
        // WHEN
        val resultAction =
            mockMvc.perform(
                get("$baseUrl/current/headphones")
                    .contentType("application/json"),
            )
        // THEN
        resultAction.andExpect(status().isOk)
        val responseJson = resultAction.andReturn().response.getContentAsString(StandardCharsets.UTF_8)
        val baseResponse = objectMapper.readValue(responseJson, BrnResponse::class.java)
        val returnedHeadphones =
            objectMapper.readValue(
                gson.toJson(baseResponse.data),
                object : TypeReference<List<HeadphonesDto>>() {},
            )
        returnedHeadphones shouldNotBe null
        Assertions
            .assertThat(returnedHeadphones)
            .hasSize(3)
            .usingElementComparatorOnFields("name", "type")
            .containsAll(
                listOf(
                    HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH),
                    HeadphonesDto(name = "second", active = true, type = HeadphonesType.IN_EAR_BLUETOOTH),
                    HeadphonesDto(name = "third", active = true, type = HeadphonesType.OVER_EAR_BLUETOOTH),
                ),
            )
    }

    @Test
    fun `should get users by role`() {
        // GIVEN
        val roleAdmin = insertRole(BrnRole.ADMIN)
        val roleUser = insertRole(BrnRole.USER)

        val user1 =
            UserAccount(
                fullName = "testUserFirstName",
                email = "test@test.test",
                gender = BrnGender.MALE.toString(),
                bornYear = 2000,
                active = true,
            )
        user1.roleSet = mutableSetOf(roleAdmin, roleUser)

        val user2 =
            UserAccount(
                fullName = "testUserFirstName2",
                email = "test2@test.test",
                gender = BrnGender.MALE.toString(),
                bornYear = 2000,
                active = true,
            )
        user2.roleSet = mutableSetOf(roleUser)

        userAccountRepository.save(user1)
        userAccountRepository.save(user2)

        // WHEN
        val response =
            mockMvc
                .perform(
                    MockMvcRequestBuilders
                        .get(baseUrl)
                        .param("role", BrnRole.ADMIN),
                ).andExpect(status().isOk)
                .andReturn()
                .response
                .getContentAsString(StandardCharsets.UTF_8)

        val data = gson.fromJson(response, BrnResponse::class.java).data
        val users: List<UserAccountDto> =
            objectMapper.readValue(gson.toJson(data), object : TypeReference<List<UserAccountDto>>() {})

        // THEN
        users.size shouldBe 1
    }

    @Test
    fun `deleteAutoTestUsers should delete all auto test users`() {
        // GIVEN
        val roleUser = insertRole(BrnRole.USER)

        val user1 =
            UserAccount(
                fullName = "autotest_n1",
                email = "autotest_n@1704819771.8820736.com",
                gender = BrnGender.MALE.toString(),
                bornYear = 2000,
                active = true,
            )
        user1.roleSet = mutableSetOf(roleUser)

        val user2 =
            UserAccount(
                fullName = "autotest_n1",
                email = "autotest_n@170472339.1784415.com",
                gender = BrnGender.MALE.toString(),
                bornYear = 2000,
                active = true,
            )
        user2.roleSet = mutableSetOf(roleUser)

        userAccountRepository.save(user1)
        userAccountRepository.save(user2)

        // WHEN
        val response =
            mockMvc
                .perform(
                    delete("$baseUrl/autotest/del")
                        .contentType("application/json"),
                ).andExpect(status().isOk)
                .andReturn()
                .response
                .getContentAsString(StandardCharsets.UTF_8)

        val data = gson.fromJson(response, BrnResponse::class.java).data

        // THEN
        data shouldBe 2
    }

    @Test
    fun `deleteAutoTestUserByEmail should delete auto test user by email`() {
        // GIVEN
        val roleUser = insertRole(BrnRole.USER)
        val email = "autotest_n@1704819771.8820736.com"

        val user1 =
            UserAccount(
                fullName = "autotest_n1",
                email = email,
                gender = BrnGender.MALE.toString(),
                bornYear = 2000,
                active = true,
            )
        user1.roleSet = mutableSetOf(roleUser)
        userAccountRepository.save(user1)

        // WHEN
        val response =
            mockMvc
                .perform(
                    delete("$baseUrl/autotest/del/$email")
                        .contentType("application/json"),
                ).andExpect(status().isOk)
                .andReturn()
                .response
                .getContentAsString(StandardCharsets.UTF_8)

        val data = gson.fromJson(response, BrnResponse::class.java).data

        // THEN
        data shouldBe 1
    }

    @Test
    fun `deleteAutoTestUserByEmail should return 500 when email not autotest`() {
        // GIVEN
        val roleUser = insertRole(BrnRole.USER)
        val email = "abc@xyz.com"

        val user1 =
            UserAccount(
                fullName = "user1",
                email = email,
                gender = BrnGender.MALE.toString(),
                bornYear = 2000,
                active = true,
            )
        user1.roleSet = mutableSetOf(roleUser)
        userAccountRepository.save(user1)

        // WHEN
        mockMvc
            .perform(
                delete("$baseUrl/autotest/del/$email")
                    .contentType("application/json"),
            ).andExpect(status().isBadRequest)
    }

    @AfterEach
    fun deleteAfterTest() {
        userAccountRepository.deleteAll()
        roleRepository.deleteAll()
        headphonesRepository.deleteAll()
    }

    private fun insertUser(
        email_: String = email,
        doctor_: UserAccount? = null,
    ): UserAccount = userAccountRepository.save(
        UserAccount(
            fullName = "testUserFirstName",
            gender = BrnGender.MALE.toString(),
            bornYear = 2000,
            email = email_,
            doctor = doctor_,
        ),
    )

    private fun insertThreeHeadphonesForUser(user: UserAccount) {
        headphonesRepository.saveAll(
            listOf(
                Headphones(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH, userAccount = user),
                Headphones(name = "second", active = true, type = HeadphonesType.IN_EAR_BLUETOOTH, userAccount = user),
                Headphones(name = "third", active = true, type = HeadphonesType.OVER_EAR_BLUETOOTH, userAccount = user),
            ),
        )
    }

    private fun insertRole(roleName: String): Role = roleRepository.save(
        Role(
            name = roleName,
        ),
    )
}
