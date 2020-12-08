package com.epam.brn.service.impl

import com.epam.brn.auth.AuthenticationBasicServiceImpl
import com.epam.brn.dto.request.LoginDto
import com.epam.brn.dto.request.UserAccountRequest
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.service.UserAccountService
import com.nhaarman.mockito_kotlin.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.mockito.Mockito.lenient
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.util.Base64Utils
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
internal class AuthenticationBasicServiceImplTest {

    @Mock
    lateinit var userAccountService: UserAccountService

    @Mock
    lateinit var authenticationManager: AuthenticationManager

    @InjectMocks
    lateinit var authenticationBasicServiceImpl: AuthenticationBasicServiceImpl

    @Test
    fun `should login exist user`() {
        // GIVEN
        val authenticationMock = mock(Authentication::class.java)
        `when`(authenticationManager.authenticate(any())).thenReturn(authenticationMock)
        val loginDto = LoginDto(
            username = "testUser".toLowerCase(),
            password = "testPassword"
        )
        val basicHeader = Base64Utils.encodeToString(("testUser".toLowerCase() + ":testPassword").toByteArray())
        // WHEN
        val actualResult = authenticationBasicServiceImpl.login(loginDto)
        // THEN
        verify(authenticationManager).authenticate(any())
        assertEquals(basicHeader, actualResult)
    }

    @Test
    fun `should not login not exist user`() {
        // GIVEN
        val authenticationMock = mock(Authentication::class.java)
        `when`(authenticationManager.authenticate(any())).thenReturn(authenticationMock)
        val loginDto = LoginDto("test", "test", "test")
        `when`(authenticationManager.authenticate(any())).thenThrow(BadCredentialsException("BadCredentialsException"))
        // WHEN
        assertThrows(BadCredentialsException::class.java) { authenticationBasicServiceImpl.login(loginDto) }
    }

    @Test
    fun `should register new user`() {
        // GIVEN
        val email = "testUser".toLowerCase()
        val passw = "testPassword"
        val userAccountDto = mock(UserAccountRequest::class.java)
        val savedUserAccountDto = mock(UserAccountDto::class.java)
        val authenticationMock = mock(Authentication::class.java)
        lenient().`when`(userAccountDto.email).thenReturn(email)
        lenient().`when`(userAccountDto.password).thenReturn(passw)
        lenient().`when`(userAccountService.addUser(userAccountDto)).thenReturn(savedUserAccountDto)
        `when`(authenticationManager.authenticate(any())).thenReturn(authenticationMock)
        val basicHeader = Base64Utils.encodeToString(("testUser".toLowerCase() + ":testPassword").toByteArray())
        // WHEN
        val actualResult = authenticationBasicServiceImpl.registration(userAccountDto)
        // THEN
        verify(userAccountService).addUser(userAccountDto)
        verify(authenticationManager).authenticate(any())
        assertEquals(basicHeader, actualResult)
    }

    @Test
    fun `should not register exist user`() {
        // GIVEN
        val email = "testUser".toLowerCase()
        val passw = "testPassword"
        val userAccountDto = mock(UserAccountRequest::class.java)
        lenient().`when`(userAccountDto.email).thenReturn(email)
        lenient().`when`(userAccountDto.password).thenReturn(passw)
        lenient().`when`(userAccountService.addUser(userAccountDto)).thenThrow(BadCredentialsException::class.java)
        // WHEN
        assertThrows(BadCredentialsException::class.java) { authenticationBasicServiceImpl.registration(userAccountDto) }
    }

    @Test
    fun `should create BasicHeader with Base64`() {
        // GIVEN
        val email = "admin@admin.com"
        val passw = "admin"
        val basicHeader = "YWRtaW5AYWRtaW4uY29tOmFkbWlu"
        // WHEN
        val actualResult = authenticationBasicServiceImpl.getBasicHeader(email, passw)
        // THEN
        assertEquals(basicHeader, actualResult)
    }
}
