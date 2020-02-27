package com.epam.brn.integration

import com.epam.brn.constant.BrnPath
import com.epam.brn.model.Authority
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.AuthorityRepository
import com.epam.brn.repo.UserAccountRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-tests")
@Tag("integration-test")
class AuthorizationAuthenticationIT {

    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var userAccountRepository: UserAccountRepository
    @Autowired
    lateinit var authorityRepository: AuthorityRepository
    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    internal val email: String = "admin@admin.com"
    internal val password: String = "admin"

    @BeforeEach
    fun initBeforeEachTest() {
        val authName = "ROLE_ADMIN"
        authorityRepository.save(Authority(authorityName = authName))
        val savedAuth = authorityRepository.findAuthorityByAuthorityName(authName)
        val password = passwordEncoder.encode(password)
        val userAccount =
            UserAccount(
                firstName = "testUserFirstName",
                lastName = "testUserLastName",
                password = password,
                email = email,
                active = true
            )
        userAccount.authoritySet.add(savedAuth!!)
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
            get(BrnPath.GROUPS)
                .with(user(this.email).password(this.password).roles("USER", "ADMIN"))
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
    }

    @Test
    fun `test get groups with with no authorities`() {
        // WHEN
        val resultAction = this.mockMvc.perform(
            get(BrnPath.GROUPS)
                .with(user(this.email).password(password).roles())
        )
        // THEN
        resultAction
            .andExpect(status().`is`(403))
    }

    @Test
    fun `test login with valid credentials`() {
        // WHEN
        val resultAction = this.mockMvc.perform(formLogin().user(this.email).password(this.password))
        // THEN
        resultAction
            .andExpect(authenticated())
    }

    @Test
    fun `test login with invalid credentials`() {
        // WHEN
        val resultAction = this.mockMvc.perform(formLogin().user(this.email).password("wrong"))
        // THEN
        resultAction
            .andExpect(unauthenticated())
    }

    @Test
    fun `test get groups with no authorities`() {
        // WHEN
        val resultAction = this.mockMvc.perform(
            get(BrnPath.GROUPS)
                .with(user(this.email).password("wrong").roles())
        )
        // THEN
        resultAction
            .andExpect(status().`is`(403))
    }

    @Test
    fun `test get groups basic authentication`() {
        // WHEN
        val resultAction = this.mockMvc.perform(
            get(BrnPath.GROUPS)
                .with(httpBasic(this.email, this.password))
        )
        // THEN
        resultAction
            .andExpect(status().isOk)
    }

    @Test
    fun `test get groups basic authentication invalid password`() {
        // WHEN
        val resultAction = this.mockMvc.perform(
            get(BrnPath.GROUPS)
                .with(httpBasic(this.email, "wrong"))
        )
        // THEN
        resultAction
            .andExpect(status().isUnauthorized)
    }
}
