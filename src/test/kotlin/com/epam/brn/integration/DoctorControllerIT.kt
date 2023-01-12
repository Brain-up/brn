package com.epam.brn.integration

import com.epam.brn.dto.request.AddPatientToDoctorRequest
import com.epam.brn.dto.UserAccountDto
import com.epam.brn.enums.BrnRole
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.RoleRepository
import com.epam.brn.repo.UserAccountRepository
import com.fasterxml.jackson.databind.type.TypeFactory
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WithMockUser(username = "currentDoctor@default.ru", roles = [BrnRole.SPECIALIST])
class DoctorControllerIT : BaseIT() {

    @Autowired
    lateinit var roleRepository: RoleRepository

    @Autowired
    private lateinit var userAccountRepository: UserAccountRepository

    private lateinit var user1: UserAccount
    private lateinit var user2: UserAccount
    private lateinit var currentSpecialist: UserAccount
    private lateinit var anotherSpecialist: UserAccount

    @BeforeEach
    fun setUp() {
        val userRole = createRole(BrnRole.USER)
        val specialistRole = createRole(BrnRole.SPECIALIST)

        user1 = createUser(email = "user1@default.ru", roles = mutableSetOf(userRole))
        user2 = createUser(email = "user2@default.ru", roles = mutableSetOf(userRole))
        currentSpecialist =
            createUser(email = "currentDoctor@default.ru", roles = mutableSetOf(userRole, specialistRole))
        anotherSpecialist =
            createUser(email = "anotherDoctor@default.ru", roles = mutableSetOf(userRole, specialistRole))
    }

    @AfterEach
    fun tearDown() {
        userAccountRepository.deleteAll()
        roleRepository.deleteAll()
    }

    @Test
    fun `should add patient to doctor`() {
        // GIVEN
        val requestJson = objectMapper.writeValueAsString(AddPatientToDoctorRequest(user1.id!!, "user"))

        // WHEN
        val resultAction = mockMvc
            .perform(
                post("/doctors/${currentSpecialist.id}/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )

        // THEN
        resultAction.andExpect(status().isOk)

        userAccountRepository.findUserAccountById(user1.id!!).get().doctor?.id shouldBe currentSpecialist.id
    }

    @Test
    fun `should not add patient to doctor if patient is doctor`() {
        // GIVEN
        val requestJson = objectMapper.writeValueAsString(AddPatientToDoctorRequest(anotherSpecialist.id!!, "user"))

        // WHEN
        val resultAction = mockMvc
            .perform(
                post("/doctors/${currentSpecialist.id}/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )

        // THEN
        resultAction.andExpect(status().isBadRequest)

        userAccountRepository.findUserAccountById(anotherSpecialist.id!!).get().doctor shouldBe null
    }

    @Test
    @WithMockUser(username = "user1@default.ru", roles = [BrnRole.USER])
    fun `should not add patient if current user is not a doctor`() {
        // GIVEN
        val requestJson = objectMapper.writeValueAsString(AddPatientToDoctorRequest(user2.id!!, "user"))

        // WHEN
        val resultAction = mockMvc
            .perform(
                post("/doctors/${user1.id}/patients")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson)
            )

        // THEN
        resultAction
            .andExpect(status().isForbidden)

        userAccountRepository.findUserAccountById(user2.id!!).get().doctor shouldBe null
    }

    @Test
    fun `should get all patients for doctor`() {
        // GIVEN
        userAccountRepository.save(user1.apply { doctor = currentSpecialist })
        userAccountRepository.save(user2.apply { doctor = currentSpecialist })

        // WHEN
        val resultAction = mockMvc.perform(get("/doctors/${currentSpecialist.id}/patients"))

        // THEN
        resultAction
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))

        val collectionType = TypeFactory.defaultInstance()
            .constructCollectionType(MutableList::class.java, UserAccountDto::class.java)
        val users: List<UserAccountDto> = objectMapper.readerFor(collectionType)
            .readValue(objectMapper.readTree(resultAction.andReturn().response.contentAsString).path("data"))

        users.size shouldBe 2
        users.map { it.email }.contains(user1.email) shouldBe true
        users.map { it.email }.contains(user2.email) shouldBe true
    }

    @Test
    fun `should remove patient from doctor`() {
        // GIVEN
        userAccountRepository.save(user1.apply { doctor = currentSpecialist })

        // WHEN
        val resultAction = mockMvc.perform(delete("/doctors/${currentSpecialist.id}/patients/${user1.id}"))

        // THEN
        resultAction.andExpect(status().isOk)

        val user1FromDb = userAccountRepository.findUserAccountById(user1.id!!).get()
        user1FromDb.email shouldBe user1.email
        user1FromDb.id shouldBe user1.id
        user1FromDb.doctor shouldBe null
    }
}
