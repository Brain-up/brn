package com.epam.brn.service.impl

import com.epam.brn.auth.AuthenticationBasicServiceImpl
import com.epam.brn.dto.request.LoginDto
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.service.UserAccountService
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.util.Base64Utils
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
internal class AuthenticationBasicServiceImplTest {

    @InjectMockKs
    lateinit var authenticationBasicServiceImpl: AuthenticationBasicServiceImpl

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    lateinit var authenticationManager: AuthenticationManager

    @Test
    fun `should login exist user`() {
        // GIVEN
        val authenticationMock = mockk<Authentication>()
        val userAccountDtoMockk = mockk<UserAccountDto>()
        every { authenticationManager.authenticate(any()) } returns authenticationMock
        every { userAccountService.addUser(any()) } returns userAccountDtoMockk
        val loginDto = LoginDto(
            username = "testUser".toLowerCase(),
            password = "testPassword"
        )
        val basicHeader = Base64Utils.encodeToString(("testUser".toLowerCase() + ":testPassword").toByteArray())

        // WHEN
        val actualResult = authenticationBasicServiceImpl.login(loginDto)

        // THEN
        verify(exactly = 1) { authenticationManager.authenticate(any()) }
        assertEquals(basicHeader, actualResult)
    }

    @Test
    fun `should not login not exist user`() {
        // GIVEN
        val authenticationMock = mockk<Authentication>()
        every { authenticationManager.authenticate(any()) } returns authenticationMock
        val loginDto = LoginDto("test", "test", "test")
        every { authenticationManager.authenticate(any()) } throws BadCredentialsException("BadCredentialsException")

        // WHEN
        assertThrows(BadCredentialsException::class.java) { authenticationBasicServiceImpl.login(loginDto) }
    }

    @Test
    fun `should register new user`() {
        // GIVEN
        val email = "testUser".toLowerCase()
        val passw = "testPassword"
        val userAccountDto = mockk<UserAccountCreateRequest>()
        val savedUserAccountDto = mockk<UserAccountDto>()
        val authenticationMock = mockk<Authentication>()
        every { userAccountDto.email } returns email
        every { userAccountDto.password } returns passw
        every { savedUserAccountDto.id } returns 1L
        every { userAccountService.addUser(userAccountDto) } returns savedUserAccountDto
        every { authenticationManager.authenticate(any()) } returns authenticationMock
        val basicHeader = Base64Utils.encodeToString(("testUser".toLowerCase() + ":testPassword").toByteArray())

        // WHEN
        val actualResult = authenticationBasicServiceImpl.registration(userAccountDto)

        // THEN
        verify(exactly = 1) { userAccountService.addUser(userAccountDto) }
        verify(exactly = 1) { authenticationManager.authenticate(any()) }
        assertEquals(basicHeader, actualResult)
    }

    @Test
    fun `should not register exist user`() {
        // GIVEN
        val email = "testUser".toLowerCase()
        val passw = "testPassword"
        val userAccountDto = mockk<UserAccountCreateRequest>()
        every { userAccountDto.email } returns email
        every { userAccountDto.password } returns passw
        every { userAccountService.addUser(userAccountDto) } throws BadCredentialsException("")

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
