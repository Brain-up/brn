package com.epam.brn.service

import com.epam.brn.dto.UserAccountDto
import com.epam.brn.exception.NoDataFoundException
import com.epam.brn.model.Authority
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.impl.UserAccountServiceImpl
import java.util.Optional
import kotlin.test.assertFailsWith
import org.apache.commons.lang3.math.NumberUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
@DisplayName("UserAccountService test using mockito")
internal class UserAccountServiceTest {

    @InjectMocks
    lateinit var userAccountService: UserAccountServiceImpl

    @Mock
    lateinit var userAccountRepository: UserAccountRepository

    @Mock
    lateinit var passwordEncoder: PasswordEncoder

    @Mock
    lateinit var authorityService: AuthorityService

    @Mock
    lateinit var userAccount: UserAccount

    @Mock
    lateinit var userAccountDto: UserAccountDto

    @Mock
    lateinit var authority: Authority

    @Nested
    @DisplayName("Tests for getting users")
    inner class GetUserAccounts {
        @Test
        fun `should find a user by id`() {
            // GIVEN
            val userName = "Tested"
            `when`(userAccount.toDto()).thenReturn(userAccountDto)
            `when`(userAccountDto.userName).thenReturn(userName)
            `when`(userAccountRepository.findUserAccountById(NumberUtils.LONG_ONE))
                .thenReturn(Optional.of(userAccount))
            // WHEN
            val userAccountDtoReturned = userAccountService.findUserById(NumberUtils.LONG_ONE)
            // THEN
            assertThat(userAccountDtoReturned.userName).isEqualTo(userName)
        }

        @Test
        fun `should find a user by userName`() {
            // GIVEN
            val userName = "Tested"
            `when`(userAccount.toDto()).thenReturn(userAccountDto)
            `when`(userAccountDto.userName).thenReturn(userName)
            `when`(userAccountRepository.findUserAccountByUserName(userName))
                .thenReturn(Optional.of(userAccount))
            // WHEN
            val userAccountDtoReturned = userAccountService.findUserByName(userName)
            // THEN
            assertThat(userAccountDtoReturned.userName).isEqualTo(userName)
        }

        @Test
        fun `should find a user by email`() {
            // GIVEN
            val email = "email"
            `when`(userAccount.toDto()).thenReturn(userAccountDto)
            `when`(userAccountDto.email).thenReturn(email)
            `when`(userAccountRepository.findUserAccountByEmail(email))
                .thenReturn(Optional.of(userAccount))
            // WHEN
            val userAccountDtoReturned = userAccountService.findUserByEmail(email)
            // THEN
            assertThat(userAccountDtoReturned.email).isEqualTo(email)
        }

        @Test
        fun `should throw an exception when there is no user by specified id`() {
            // THEN
            assertFailsWith<NoDataFoundException> {
                userAccountService.findUserById(NumberUtils.LONG_ONE)
            }
        }
    }

    @Nested
    @DisplayName("Tests for creation of users")
    inner class CreateUserAccounts {
        @Test
        fun `should find a user by id`() {
            // GIVEN
            val userName = "Tested"
            `when`(userAccountDto.toModel()).thenReturn(userAccount)
            `when`(userAccount.toDto()).thenReturn(userAccountDto)
            `when`(userAccountDto.userName).thenReturn(userName)
            `when`(userAccountRepository.save(userAccount))
                .thenReturn(userAccount)
            `when`(authorityService.findAuthorityByAuthorityName(anyString()))
                .thenReturn(authority)
            // WHEN
            val userAccountDtoReturned = userAccountService.addUser(userAccountDto)
            // THEN
            assertThat(userAccountDtoReturned.userName).isEqualTo(userName)
        }
    }
}
