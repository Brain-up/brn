package com.epam.brn.controller

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.response.AuthorityResponse
import com.epam.brn.dto.response.Response
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.dto.response.UserWithAnalyticsResponse
import com.epam.brn.enums.Role.ROLE_USER
import com.epam.brn.service.UserAccountService
import com.epam.brn.service.UserAnalyticsService
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.apache.http.HttpStatus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Pageable

@ExtendWith(MockKExtension::class)
internal class AdminUserFlowControllerTest {

    @InjectMockKs
    private lateinit var adminUserFlowController: AdminUserFlowController

    @MockK
    private lateinit var userAccountService: UserAccountService

    @MockK
    private lateinit var userAnalyticsService: UserAnalyticsService

    @MockK
    private lateinit var authorityService: AuthorityService

    @MockK
    private lateinit var pageable: Pageable

    @MockK
    private lateinit var userWithAnalyticsResponse: UserWithAnalyticsResponse

    @MockK
    private lateinit var userAccountResponse: UserAccountResponse

    @MockK
    private lateinit var authorityResponse: AuthorityResponse

    @Test
    fun `getUsers should return users with statistic when withAnalytics is true`() {
        // GIVEN
        val withAnalytics = true
        val role = ROLE_USER.name
        every { userAnalyticsService.getUsersWithAnalytics(pageable, role) } returns listOf(userWithAnalyticsResponse)
        // WHEN
        val users = adminUserFlowController.getUsers(withAnalytics, role, pageable)
        // THEN
        verify(exactly = 1) { userAnalyticsService.getUsersWithAnalytics(pageable, role) }
        users.statusCodeValue shouldBe HttpStatus.SC_OK
        (users.body as Response).data shouldBe listOf(userWithAnalyticsResponse)
    }

    @Test
    fun `getUsers should return users when withAnalytics is false`() {
        // GIVEN
        val withAnalytics = false
        val role = ROLE_USER.name
        every { userAccountService.getUsers(pageable, role) } returns listOf(userAccountResponse)

        // WHEN
        val users = adminUserFlowController.getUsers(withAnalytics, role, pageable)
        // THEN
        verify(exactly = 1) { userAccountService.getUsers(pageable, role) }
        users.statusCodeValue shouldBe HttpStatus.SC_OK
        (users.body as Response).data shouldBe listOf(userAccountResponse)
    }

    @Test
    fun `getRoles should return http status 200`() {
        // GIVEN
        every { authorityService.findAll() } returns listOf(authorityResponse)
        // WHEN
        val authorities = adminUserFlowController.getRoles()
        // THEN
        authorities.statusCodeValue shouldBe HttpStatus.SC_OK
        authorities.body!!.data.size shouldBe 1
    }
}
