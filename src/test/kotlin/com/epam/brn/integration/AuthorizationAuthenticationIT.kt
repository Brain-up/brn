package com.epam.brn.integration

import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.model.Authority
import com.epam.brn.model.Gender
import com.epam.brn.model.UserAccount
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AuthorizationAuthenticationIT : BaseIT() {

    @Autowired
    lateinit var userAccountRepository: UserAccountRepository

    @Autowired
    lateinit var authorityRepository: AuthorityRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    internal val email: String = "testAdmin@admin.com"
    internal val password: String = "testAdmin"
    private val baseUrl = "/groups"

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
    }

    @AfterEach
    fun deleteAfterTest() {
        userAccountRepository.deleteAll()
        authorityRepository.deleteAll()
    }

    @Test
    fun `test get groups with valid credentials`() {
        // WHEN
        val resultAction = this.mockMvc.perform(
            get(baseUrl)
                .with(user(this.email).password(this.password).roles("USER", "ADMIN"))
        )
        // THEN
        resultAction.andExpect(status().isOk)
    }

    @Test
    fun `test get groups with with no authorities`() {
        // WHEN
        val resultAction = this.mockMvc
            .perform(
                get(baseUrl).with(user(this.email).password(password).roles())
            )
        // THEN
        resultAction.andExpect(status().`is`(403))
    }

    @Test
    fun `test login with valid credentials`() {
        // WHEN
        val resultAction = this.mockMvc
            .perform(formLogin().user(this.email).password(this.password))
        // THEN
        resultAction.andExpect(authenticated())
    }

    @Test
    fun `test login with invalid credentials`() {
        // WHEN
        val resultAction = this.mockMvc
            .perform(formLogin().user(this.email).password("wrong"))
        // THEN
        resultAction.andExpect(unauthenticated())
    }

    @Test
    fun `test get groups with no authorities`() {
        // WHEN
        val resultAction = this.mockMvc
            .perform(
                get(baseUrl).with(user(this.email).password("wrong").roles())
            )
        // THEN
        resultAction.andExpect(status().`is`(403))
    }

    @Test
    fun `test get groups basic authentication`() {
        // WHEN
        val resultAction = this.mockMvc
            .perform(
                get(baseUrl).with(httpBasic(this.email, this.password))
            )
        // THEN
        resultAction.andExpect(status().isOk)
    }

    @Test
    fun `test get groups basic authentication invalid password`() {
        // WHEN
        val resultAction = this.mockMvc
            .perform(
                get(baseUrl).with(httpBasic(this.email, "wrong"))
            )
        // THEN
        resultAction.andExpect(status().isUnauthorized)
    }
}
