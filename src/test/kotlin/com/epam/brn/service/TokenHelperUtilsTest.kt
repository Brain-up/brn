package com.epam.brn.service

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import javax.servlet.http.HttpServletRequest
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
@DisplayName("TokenHelperUtils test using MockK")
internal class TokenHelperUtilsTest {

    @InjectMockKs
    private lateinit var tokenHelperUtils: TokenHelperUtils

    @MockK
    private lateinit var httpServletRequest: HttpServletRequest

    @Test
    fun `should return null when Authorization header is null`() {
        // GIVEN
        every { httpServletRequest.getHeader("Authorization") } returns (null)
        // WHEN
        val bearerToken = tokenHelperUtils.getBearerToken(httpServletRequest)
        // THEN
        assertNull(bearerToken)
        verify(exactly = 1) { httpServletRequest.getHeader("Authorization") }
    }

    @Test
    fun `should return null when Authorization header is empty`() {
        // GIVEN
        every { httpServletRequest.getHeader("Authorization") } returns ("")
        // WHEN
        val bearerToken = tokenHelperUtils.getBearerToken(httpServletRequest)
        // THEN
        assertNull(bearerToken)
        verify(exactly = 1) { httpServletRequest.getHeader("Authorization") }
    }

    @Test
    fun `should return null when Authorization header not start with "Bearer "`() {
        // GIVEN
        every { httpServletRequest.getHeader("Authorization") } returns ("SOME_INFO_TOKEN")
        // WHEN
        val bearerToken = tokenHelperUtils.getBearerToken(httpServletRequest)
        // THEN
        assertNull(bearerToken)
        verify(exactly = 1) { httpServletRequest.getHeader("Authorization") }
    }

    @Test
    fun `should return token data when Authorization header start with "Bearer " and has some info`() {
        // GIVEN
        val expectedToken = "SOME_INFO_TOKEN"
        every { httpServletRequest.getHeader("Authorization") } returns ("Bearer $expectedToken")
        // WHEN
        val bearerToken = tokenHelperUtils.getBearerToken(httpServletRequest)
        // THEN
        assertEquals(expectedToken, bearerToken)
        verify(exactly = 1) { httpServletRequest.getHeader("Authorization") }
    }
}
