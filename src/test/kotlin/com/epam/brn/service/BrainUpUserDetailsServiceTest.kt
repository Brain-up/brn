package com.epam.brn.service

import com.epam.brn.enums.BrnRole
import com.epam.brn.model.Role
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.Optional

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
        firstLookup.username shouldBe email
        secondLookup.username shouldBe email
        firstLookup.authorities.size shouldBe 1

        verify(exactly = 1) { userAccountRepository.findAuthenticationUserByEmail(email) }
        verify(exactly = 0) { userAccountRepository.findUserAccountByEmail(any()) }
    }

    @Test
    fun `should reload auth user details after cache eviction`() {
        // GIVEN
        val email = "test@test.ru"
        every { userAccountRepository.findAuthenticationUserByEmail(email) } returnsMany
            listOf(
                Optional.of(createUserAccount(email)),
                Optional.of(createUserAccount(email)),
            )

        // WHEN
        val firstLookup = brainUpUserDetailsService.loadUserByUsername(email)
        brainUpUserDetailsService.evictCachedUser(email)
        val secondLookup = brainUpUserDetailsService.loadUserByUsername(email)

        // THEN
        firstLookup.username shouldBe email
        secondLookup.username shouldBe email

        verify(exactly = 2) { userAccountRepository.findAuthenticationUserByEmail(email) }
    }

    @Test
    fun `should throw when auth user is missing`() {
        // GIVEN
        val email = "missing@test.ru"
        every { userAccountRepository.findAuthenticationUserByEmail(email) } returns Optional.empty()

        // WHEN
        val exception =
            shouldThrow<UsernameNotFoundException> {
                brainUpUserDetailsService.loadUserByUsername(email)
            }

        // THEN
        exception.message shouldBe "User with email: $email doesn't exist"
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
