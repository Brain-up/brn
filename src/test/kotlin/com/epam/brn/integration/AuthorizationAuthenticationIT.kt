package com.epam.brn.integration

import com.epam.brn.constant.BrnPath
import com.epam.brn.model.Authority
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
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

    internal val userName: String = "admin"
    internal val password: String = "admin"

    @BeforeEach
    fun initBeforeEachTest() {
        val password = BCryptPasswordEncoder().encode(password)
        val userAccount =
            UserAccount(userName = userName, password = password, email = "admin@admin.com", active = true)
        userAccount.authoritySet.addAll(setOf(Authority(authority = "ROLE_ADMIN", userAccount = userAccount)))
        userAccountRepository.save(userAccount)

    }

    @AfterEach
    fun deleteAfterTest() {
        userAccountRepository.deleteAll()
    }

    @Test
    fun `test get groups with valid credentials`() {
        // WHEN
        val resultAction = this.mockMvc.perform(
            get(BrnPath.GROUPS)
                .with(user(this.userName).password(this.password).roles("USER", "ADMIN"))
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
                .with(user(this.userName).password(password).roles())
        )
        // THEN
        resultAction
            .andExpect(status().`is`(403))
    }

    @Test
    fun `test login with valid credentials`() {
        // WHEN
        val resultAction = this.mockMvc.perform(formLogin().user(this.userName).password(this.password))
        // THEN
        resultAction
            .andExpect(authenticated())
    }

    @Test
    fun `test login with invalid credentials`() {
        // WHEN
        val resultAction = this.mockMvc.perform(formLogin().user(this.userName).password("wrong"))
        // THEN
        resultAction
            .andExpect(unauthenticated())
    }

    @Test
    fun `test get groups with no authorities`() {
        // WHEN
        val resultAction = this.mockMvc.perform(
            get(BrnPath.GROUPS)
                .with(user(this.userName).password("wrong").roles())
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
                .with(httpBasic(this.userName, this.password))
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
                .with(httpBasic(this.userName, "wrong"))
        )
        // THEN
        resultAction
            .andExpect(status().isUnauthorized)
    }


}