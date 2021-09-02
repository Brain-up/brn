package com.epam.brn.service.impl

import com.epam.brn.auth.AuthenticationBasicServiceImpl
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.service.FirebaseUserService
import com.epam.brn.service.UserAccountService
import com.google.firebase.auth.UserRecord
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication

@ExtendWith(MockKExtension::class)
internal class AuthenticationBasicServiceImplTest {

    @InjectMockKs
    lateinit var authenticationBasicServiceImpl: AuthenticationBasicServiceImpl

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    lateinit var firebaseUserService: FirebaseUserService

    @MockK
    lateinit var authenticationManager: AuthenticationManager

    @Test
    fun `should register new user`() {
        // GIVEN
        val email = "testUser".toLowerCase()
        val password = "testPassword"
        val uid = "uid"
        val userAccountDto = mockk<UserAccountCreateRequest>()
        val firebaseUserRecord = mockk<UserRecord>()
        val savedUserAccountResponse = mockk<UserAccountResponse>()
        val authenticationMock = mockk<Authentication>()

        every { firebaseUserRecord.uid } returns uid
        every { userAccountDto.email } returns email
        every { userAccountDto.password } returns password
        every { savedUserAccountResponse.id } returns 1L
        every { userAccountService.createUser(userAccountDto, firebaseUserRecord) } returns savedUserAccountResponse
        every { authenticationManager.authenticate(any()) } returns authenticationMock
        every { firebaseUserService.getUserByEmail(email) } returns null
        every { firebaseUserService.addUser(userAccountDto) } returns firebaseUserRecord

//        val securityContextMockk = mockk<SecurityContext>()
//        every { securityContextMockk.authentication } returns authenticationMock
//        every { securityContextMockk.authentication = any() } returns Unit
//        SecurityContextHolder.setContext(securityContextMockk)
        // WHEN
        val actualResult = authenticationBasicServiceImpl.registration(userAccountDto)

        // THEN
        verify(exactly = 1) { userAccountService.createUser(userAccountDto, firebaseUserRecord) }
//        verify(exactly = 1) { authenticationManager.authenticate(any()) }
        // TODO: correct assert
//        assertEquals(basicHeader, actualResult)
    }
}
