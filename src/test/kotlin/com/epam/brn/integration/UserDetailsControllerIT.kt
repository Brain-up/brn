package com.epam.brn.integration

import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.model.Authority
import com.epam.brn.model.Gender
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.UserAccountService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WithMockUser(username = "test@test.test", roles = ["ADMIN"])
@Disabled("need work.")
class UserDetailsControllerIT : BaseIT() {

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var authorityRepository: AuthorityRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Mock
    lateinit var userAccountService: UserAccountService

    internal val email: String = "testAdmin@admin.com"
    internal val password: String = "testAdmin"
    private val baseUrl = "users"
    private val currentUserBaseUrl = "$baseUrl/current"

    @BeforeEach
    fun initBeforeEachTest() {
        val authName = "ROLE_ADMIN"
        val authority = authorityRepository.findAuthorityByAuthorityName(authName)
            ?: authorityRepository.save(Authority(authorityName = authName))

        val password = passwordEncoder.encode(password)
        val userAccount =
            UserAccount(
                fullName = "testUserFirstName",
                password = password,
                email = email,
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                active = true
            )
        userAccount.authoritySet.add(authority)
        userAccountRepository.save(userAccount)
        val userAccountResponse = mock(UserAccountResponse::class.java)
        `when`(userAccountService.getUserFromTheCurrentSession()).thenReturn(userAccountResponse)
    }

    @Test
    fun `update avatar for current user`() {
        // WHEN
        val resultAction = this.mockMvc.perform(
            put("$currentUserBaseUrl/avatar")
                .queryParam("avatar", "test")
        )
        // THEN
        resultAction.andExpect(status().isOk)
    }

    @AfterEach
    fun deleteAfterTest() {
        userAccountRepository.deleteAll()
        authorityRepository.deleteAll()
    }
}
