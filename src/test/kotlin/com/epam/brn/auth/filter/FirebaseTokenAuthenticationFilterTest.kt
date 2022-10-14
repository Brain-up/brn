package com.epam.brn.auth.filter

import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.enums.BrnRole
import com.epam.brn.model.Role
import com.epam.brn.auth.model.CustomUserDetails
import com.epam.brn.model.UserAccount
import com.epam.brn.service.FirebaseUserService
import com.epam.brn.service.TokenHelperUtils
import com.epam.brn.service.UserAccountService
import com.google.firebase.ErrorCode
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseToken
import com.google.firebase.auth.UserRecord
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpMethod
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import javax.servlet.FilterChain
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@ExtendWith(MockKExtension::class)
@DisplayName("FirebaseTokenAuthenticationFilter test using MockK")
internal class FirebaseTokenAuthenticationFilterTest {

    @InjectMockKs
    lateinit var firebaseTokenAuthenticationFilter: FirebaseTokenAuthenticationFilter

    @MockK
    lateinit var brainUpUserDetailsService: UserDetailsService

    @MockK
    lateinit var firebaseUserService: FirebaseUserService

    @MockK
    lateinit var userAccountService: UserAccountService

    @MockK
    lateinit var firebaseAuth: FirebaseAuth

    @MockK
    lateinit var tokenHelperUtils: TokenHelperUtils

    @MockK
    lateinit var firebaseTokenMock: FirebaseToken

    private val email = "test@test.ru"
    private val uuid = "123123"
    private val fullName = "Full Name"

    @BeforeEach
    fun init() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `should set authentication by UsernamePasswordAuthenticationToken when user exist in local DB and firebase and not create new user in local database`() {
        // GIVEN
        val request = MockHttpServletRequest(HttpMethod.GET.name, "/test")
        val token = "firebaseTokenMock"
        request.addHeader("Authorization", "Bearer $token")
        val response = MockHttpServletResponse()
        val filterChain = FilterChain { _, _ -> }
        val customUserDetailsMock = CustomUserDetails(createUserAccountMock())

        every { tokenHelperUtils.getBearerToken(request) } returns token
        every { firebaseAuth.verifyIdToken(token, true) } returns firebaseTokenMock
        every { firebaseTokenMock.email } returns email
        every { brainUpUserDetailsService.loadUserByUsername(email) } returns customUserDetailsMock
        // WHEN
        firebaseTokenAuthenticationFilter.doFilter(request, response, filterChain)
        // THEN
        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        authentication.shouldBeInstanceOf(UsernamePasswordAuthenticationToken::class.java)
        assertEquals(email, authentication.name)
        assertEquals(1, authentication.authorities.size)

        verify(exactly = 1) { tokenHelperUtils.getBearerToken(request) }
        verify(exactly = 1) { firebaseAuth.verifyIdToken(token, true) }
        verify(exactly = 1) { brainUpUserDetailsService.loadUserByUsername(email) }
        verify(exactly = 0) { firebaseUserService.getUserByUuid(any()) }
        verify(exactly = 0) { userAccountService.createUser(any()) }
    }

    @Test
    fun `should set authentication by UsernamePasswordAuthenticationToken when user not exist in local DB and exist in firebase DB and create new user in local database`() {
        // GIVEN
        val requestMock = MockHttpServletRequest(HttpMethod.GET.name, "/test")
        val tokenMock = "firebaseTokenMock"
        requestMock.addHeader("Authorization", "Bearer $tokenMock")
        val responseMock = MockHttpServletResponse()
        val filterChain = FilterChain { _, _ -> }
        val customUserDetailsMock = CustomUserDetails(createUserAccountMock())
        val userRecordMock = mockk<UserRecord>()

        every { tokenHelperUtils.getBearerToken(requestMock) } returns tokenMock
        every { firebaseAuth.verifyIdToken(tokenMock, true) } returns firebaseTokenMock
        every { firebaseTokenMock.email } returns email
        every { firebaseTokenMock.uid } returns uuid
        every { brainUpUserDetailsService.loadUserByUsername(email) } throws (UsernameNotFoundException("USER_NOT_FOUND")) andThen customUserDetailsMock
        every { firebaseUserService.getUserByUuid(uuid) } returns userRecordMock
        every { userAccountService.createUser(userRecordMock) } returns UserAccountResponse(
            email = email,
            bornYear = 0,
            gender = null,
            name = fullName
        )

        // WHEN
        firebaseTokenAuthenticationFilter.doFilter(requestMock, responseMock, filterChain)
        // THEN
        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        authentication.shouldBeInstanceOf(UsernamePasswordAuthenticationToken::class.java)
        assertEquals(email, authentication.name)
        assertEquals(1, authentication.authorities.size)

        verify(exactly = 1) { tokenHelperUtils.getBearerToken(requestMock) }
        verify(exactly = 1) { firebaseAuth.verifyIdToken(tokenMock, true) }
        verify(exactly = 2) { brainUpUserDetailsService.loadUserByUsername(email) }
        verify(exactly = 1) { firebaseUserService.getUserByUuid(uuid) }
        verify(exactly = 1) { userAccountService.createUser(any()) }
    }

    @Test
    fun `should set authentication by NULL when token invalid`() {
        // GIVEN
        val requestMock = MockHttpServletRequest(HttpMethod.GET.name, "/test")
        val tokenMock = "firebaseTokenMock"
        requestMock.addHeader("Authorization", "Bearer $tokenMock")
        val responseMock = MockHttpServletResponse()
        val filterChain = FilterChain { _, _ -> }

        every { tokenHelperUtils.getBearerToken(requestMock) } returns tokenMock
        every { firebaseAuth.verifyIdToken(tokenMock, true) } throws (FirebaseAuthException(FirebaseException(ErrorCode.INVALID_ARGUMENT, "Token invalid", null)))
        // WHEN
        firebaseTokenAuthenticationFilter.doFilter(requestMock, responseMock, filterChain)
        // THEN
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)

        verify(exactly = 1) { tokenHelperUtils.getBearerToken(requestMock) }
        verify(exactly = 1) { firebaseAuth.verifyIdToken(tokenMock, true) }
        verify(exactly = 0) { brainUpUserDetailsService.loadUserByUsername(any()) }
        verify(exactly = 0) { firebaseUserService.getUserByUuid(any()) }
        verify(exactly = 0) { userAccountService.createUser(any()) }
    }

    @Test
    fun `should set authentication by NULL when error occurred`() {
        // GIVEN
        val requestMock = MockHttpServletRequest(HttpMethod.GET.name, "/test")
        val tokenMock = "firebaseTokenMock"
        requestMock.addHeader("Authorization", "Bearer $tokenMock")
        val responseMock = MockHttpServletResponse()
        val filterChain = FilterChain { _, _ -> }

        every { tokenHelperUtils.getBearerToken(requestMock) } returns tokenMock
        every { firebaseAuth.verifyIdToken(tokenMock, true) } throws (IllegalArgumentException())
        // WHEN
        firebaseTokenAuthenticationFilter.doFilter(requestMock, responseMock, filterChain)
        // THEN
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)

        verify(exactly = 1) { tokenHelperUtils.getBearerToken(requestMock) }
        verify(exactly = 1) { firebaseAuth.verifyIdToken(tokenMock, true) }
        verify(exactly = 0) { brainUpUserDetailsService.loadUserByUsername(any()) }
        verify(exactly = 0) { firebaseUserService.getUserByUuid(any()) }
        verify(exactly = 0) { userAccountService.createUser(any()) }
    }

    @Test
    fun `should set authentication by NULL when user not exist in local DB and not returning from firebase DB`() {
        // GIVEN
        val requestMock = MockHttpServletRequest(HttpMethod.GET.name, "/test")
        val tokenMock = "firebaseTokenMock"
        requestMock.addHeader("Authorization", "Bearer $tokenMock")
        val responseMock = MockHttpServletResponse()
        val filterChain = FilterChain { _, _ -> }
        val customUserDetailsMock = CustomUserDetails(createUserAccountMock())

        every { tokenHelperUtils.getBearerToken(requestMock) } returns tokenMock
        every { firebaseAuth.verifyIdToken(tokenMock, true) } returns firebaseTokenMock
        every { firebaseTokenMock.email } returns email
        every { firebaseTokenMock.uid } returns uuid
        every { brainUpUserDetailsService.loadUserByUsername(email) } throws (UsernameNotFoundException("USER_NOT_FOUND")) andThen customUserDetailsMock
        every { firebaseUserService.getUserByUuid(uuid) } returns null

        // WHEN
        firebaseTokenAuthenticationFilter.doFilter(requestMock, responseMock, filterChain)
        // THEN
        val authentication = SecurityContextHolder.getContext().authentication
        assertNull(authentication)

        verify(exactly = 1) { tokenHelperUtils.getBearerToken(requestMock) }
        verify(exactly = 1) { firebaseAuth.verifyIdToken(tokenMock, true) }
        verify(exactly = 1) { brainUpUserDetailsService.loadUserByUsername(email) }
        verify(exactly = 1) { firebaseUserService.getUserByUuid(uuid) }
        verify(exactly = 0) { userAccountService.createUser(any()) }
    }

    private fun createUserAccountMock(): UserAccount {
        val userAccount = UserAccount(
            id = 1L,
            userId = uuid,
            email = email,
            fullName = fullName
        )
        userAccount.roleSet = mutableSetOf(
            Role(
                id = 1L,
                name = BrnRole.USER.name
            )
        )
        return userAccount
    }
}
