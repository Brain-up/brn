package com.epam.brn.auth.filter

import com.epam.brn.service.UserAccountService
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpMethod
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.FilterChain

@ExtendWith(MockKExtension::class)
@DisplayName("RememberLastVisitFilter test using MockK")
internal class RememberLastVisitFilterTest {
    @InjectMockKs
    lateinit var rememberLastVisitFilter: RememberLastVisitFilter

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    lateinit var authentication: UsernamePasswordAuthenticationToken

    @BeforeEach
    fun init() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should mark visit for current user`() {
        // GIVEN
        val requestMock = MockHttpServletRequest(HttpMethod.GET.name, "/test")
        val tokenMock = "firebaseTokenMock"
        requestMock.addHeader("Authorization", "Bearer $tokenMock")
        val responseMock = MockHttpServletResponse()
        val filterChain = FilterChain { _, _ -> }
        SecurityContextHolder.getContext().authentication = authentication

        justRun { userAccountService.markVisitForCurrentUser() }

        // WHEN
        rememberLastVisitFilter.doFilter(requestMock, responseMock, filterChain)

        // THEN
        verify(exactly = 1) { userAccountService.markVisitForCurrentUser() }
    }

    @Test
    fun `should not mark visit when request is anonymous`() {
        // GIVEN
        val requestMock = MockHttpServletRequest(HttpMethod.GET.name, "/test")
        val tokenMock = "firebaseTokenMock"
        requestMock.addHeader("Authorization", "Bearer $tokenMock")
        val responseMock = MockHttpServletResponse()
        val filterChain = FilterChain { _, _ -> }

        justRun { userAccountService.markVisitForCurrentUser() }

        // WHEN
        rememberLastVisitFilter.doFilter(requestMock, responseMock, filterChain)

        // THEN
        verify(exactly = 0) { userAccountService.markVisitForCurrentUser() }
    }
}
