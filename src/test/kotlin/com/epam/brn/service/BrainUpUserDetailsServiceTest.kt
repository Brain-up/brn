package com.epam.brn.service

import com.epam.brn.enums.BrnRole
import com.epam.brn.model.Role
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
internal class BrainUpUserDetailsServiceTest {
    @InjectMockKs
    private lateinit var brainUpUserDetailsService: BrainUpUserDetailsService

    @MockK
    private lateinit var userAccountRepository: UserAccountRepository

    @Test
    fun `should cache auth user details between lookups`() {
        // GIVEN
        val email = "test@test.ru"
        val authStateChangedAt = LocalDateTime.now()
        every { userAccountRepository.findAuthenticationUserByEmail(email) } returns
            Optional.of(createUserAccount(email, authStateChangedAt))

        // WHEN
        val firstLookup = brainUpUserDetailsService.loadUserByUsername(email, authStateChangedAt)
        val secondLookup = brainUpUserDetailsService.loadUserByUsername(email, authStateChangedAt)

        // THEN
        assertEquals(email, firstLookup.username)
        assertEquals(email, secondLookup.username)
        assertEquals(1, firstLookup.authorities.size)

        verify(exactly = 1) { userAccountRepository.findAuthenticationUserByEmail(email) }
        verify(exactly = 0) { userAccountRepository.findUserAccountByEmail(any()) }
    }

    @Test
    fun `should refresh cached auth user details when auth state changes`() {
        // GIVEN
        val email = "test@test.ru"
        val firstAuthStateChangedAt = LocalDateTime.now().minusMinutes(5)
        val secondAuthStateChangedAt = firstAuthStateChangedAt.plusMinutes(1)
        every { userAccountRepository.findAuthenticationUserByEmail(email) } returnsMany
            listOf(
                Optional.of(createUserAccount(email, firstAuthStateChangedAt)),
                Optional.of(createUserAccount(email, secondAuthStateChangedAt)),
            )

        // WHEN
        val firstLookup = brainUpUserDetailsService.loadUserByUsername(email, firstAuthStateChangedAt)
        val secondLookup = brainUpUserDetailsService.loadUserByUsername(email, secondAuthStateChangedAt)

        // THEN
        assertEquals(email, firstLookup.username)
        assertEquals(email, secondLookup.username)

        verify(exactly = 2) { userAccountRepository.findAuthenticationUserByEmail(email) }
    }

    @Test
    fun `should throw when auth user is missing`() {
        // GIVEN
        val email = "missing@test.ru"
        every { userAccountRepository.findAuthenticationUserByEmail(email) } returns Optional.empty()

        // WHEN
        val exception =
            assertFailsWith<UsernameNotFoundException> {
                brainUpUserDetailsService.loadUserByUsername(email, null)
            }

        // THEN
        assertEquals("User with email: $email doesn't exist", exception.message)
        verify(exactly = 1) { userAccountRepository.findAuthenticationUserByEmail(email) }
    }

    @Test
    fun `should expose current auth state timestamp`() {
        // GIVEN
        val email = "test@test.ru"
        val authStateChangedAt = LocalDateTime.now()
        every { userAccountRepository.findAuthenticationStateChangedAtByEmail(email) } returns authStateChangedAt

        // WHEN
        val result = brainUpUserDetailsService.findAuthenticationStateChangedAt(email)

        // THEN
        assertEquals(authStateChangedAt, result)
        verify(exactly = 1) { userAccountRepository.findAuthenticationStateChangedAtByEmail(email) }
    }

    private fun createUserAccount(
        email: String,
        authStateChangedAt: LocalDateTime,
    ): UserAccount {
        val userAccount =
            UserAccount(
                id = 1L,
                userId = "123123",
                email = email,
                fullName = "Full Name",
            )
        userAccount.authStateChanged = authStateChangedAt
        userAccount.roleSet =
            mutableSetOf(
                Role(
                    id = 1L,
                    name = BrnRole.USER,
                ),
            )
        return userAccount
    }
}
