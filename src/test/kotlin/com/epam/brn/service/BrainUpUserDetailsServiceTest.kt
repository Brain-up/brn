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
        every { userAccountRepository.findAuthenticationUserByEmail(email) } returns Optional.of(createUserAccount(email))

        // WHEN
        val firstLookup = brainUpUserDetailsService.loadUserByUsername(email)
        val secondLookup = brainUpUserDetailsService.loadUserByUsername(email)

        // THEN
        assertEquals(email, firstLookup.username)
        assertEquals(email, secondLookup.username)
        assertEquals(1, firstLookup.authorities.size)

        verify(exactly = 1) { userAccountRepository.findAuthenticationUserByEmail(email) }
        verify(exactly = 0) { userAccountRepository.findUserAccountByEmail(any()) }
    }

    @Test
    fun `should throw when auth user is missing`() {
        // GIVEN
        val email = "missing@test.ru"
        every { userAccountRepository.findAuthenticationUserByEmail(email) } returns Optional.empty()

        // WHEN
        val exception =
            assertFailsWith<UsernameNotFoundException> {
                brainUpUserDetailsService.loadUserByUsername(email)
            }

        // THEN
        assertEquals("User with email: $email doesn't exist", exception.message)
        verify(exactly = 1) { userAccountRepository.findAuthenticationUserByEmail(email) }
    }

    private fun createUserAccount(email: String): UserAccount {
        val userAccount =
            UserAccount(
                id = 1L,
                userId = "123123",
                email = email,
                fullName = "Full Name",
            )
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
