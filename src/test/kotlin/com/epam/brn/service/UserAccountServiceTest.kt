package com.epam.brn.service

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountDto
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.model.Gender
import com.epam.brn.model.Headphones
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.impl.UserAccountServiceImpl
import com.nhaarman.mockito_kotlin.verify
import org.apache.commons.lang3.math.NumberUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyString
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
@DisplayName("UserAccountService test using mockito")
internal class UserAccountServiceTest {

    @InjectMocks
    lateinit var userAccountService: UserAccountServiceImpl

    @Mock
    lateinit var userAccountRepository: UserAccountRepository

    @Mock
    lateinit var timeService: TimeService

    @Mock
    lateinit var passwordEncoder: PasswordEncoder

    @Mock
    lateinit var authorityService: AuthorityService

    @Mock
    lateinit var userAccount: UserAccount

    @Mock
    lateinit var userAccountDto: UserAccountDto

    @Mock
    lateinit var userAccountCreateRequest: UserAccountCreateRequest

    @Mock
    lateinit var authority: Authority

    @Mock
    lateinit var headphonesService: HeadphonesService

    @Captor
    lateinit var userArgumentCaptor: ArgumentCaptor<UserAccount>

    private fun <T> any(type: Class<T>): T = Mockito.any(type)

    @Nested
    @DisplayName("Tests for getting users")
    inner class GetUserAccounts {
        @Test
        fun `should find a user by id`() {
            // GIVEN
            val userName = "Tested"
            `when`(userAccount.toDto()).thenReturn(userAccountDto)
            `when`(userAccountDto.name).thenReturn(userName)
            `when`(userAccountRepository.findUserAccountById(NumberUtils.LONG_ONE))
                .thenReturn(Optional.of(userAccount))
            // WHEN
            val userAccountDtoReturned = userAccountService.findUserById(NumberUtils.LONG_ONE)
            // THEN
            assertThat(userAccountDtoReturned.name).isEqualTo(userName)
        }

        @Test
        fun `should find a user by name`() {
            // GIVEN
            val fullName = "Ivan"
            `when`(userAccount.toDto()).thenReturn(userAccountDto)
            `when`(userAccountDto.name).thenReturn(fullName)
            `when`(userAccountRepository.findUserAccountByName(fullName))
                .thenReturn(Optional.of(userAccount))
            // WHEN
            val userAccountDtoReturned = userAccountService.findUserByName(fullName)
            // THEN
            assertThat(userAccountDtoReturned.name).isEqualTo(fullName)
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
            assertFailsWith<EntityNotFoundException> {
                userAccountService.findUserById(NumberUtils.LONG_ONE)
            }
        }
    }

    @Nested
    @DisplayName("Tests for user creation")
    inner class CreateUserAccounts {
        @Test
        fun `should create new user`() {
            // GIVEN
            val userName = "Tested"
            `when`(userAccountCreateRequest.toModel(ArgumentMatchers.anyString())).thenReturn(userAccount)
            `when`(userAccountDto.name).thenReturn("Tested")
            `when`(userAccount.toDto()).thenReturn(userAccountDto)
            `when`(userAccountRepository.save(userAccount))
                .thenReturn(userAccount)
            `when`(authorityService.findAuthorityByAuthorityName(anyString()))
                .thenReturn(authority)
            `when`(passwordEncoder.encode(userAccountCreateRequest.password))
                .thenReturn("password")
            // WHEN
            val userAccountDtoReturned = userAccountService.addUser(userAccountCreateRequest)
            // THEN
            assertThat(userAccountDtoReturned.name).isEqualTo(userName)
        }
    }

    @Nested
    @DisplayName("Test for update current user")
    inner class UpdateUserAccount {

        @Test
        fun `should update avatar current session user`() {
            // GIVEN
            val avatarUrl = "test/avatar"
            val email = "test@test.ru"
            val authentication = Mockito.mock(Authentication::class.java)
            val securityContext: SecurityContext = Mockito.mock(SecurityContext::class.java)
            val userAccount = UserAccount(
                id = 1L,
                fullName = "testUserFirstName",
                email = email,
                password = "password",
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                changed = LocalDateTime.now().minusMinutes(5),
                avatar = null
            )
            val userAccountUpdated = userAccount.copy()
            userAccountUpdated.avatar = avatarUrl

            SecurityContextHolder.setContext(securityContext)
            `when`(securityContext.authentication).thenReturn(authentication)
            `when`(authentication.name).thenReturn(email)
            `when`(userAccountRepository.findUserAccountByEmail(email))
                .thenReturn(Optional.of(userAccount))
            `when`(timeService.now()).thenReturn(LocalDateTime.now())
            `when`(userAccountRepository.save(Mockito.any(UserAccount::class.java)))
                .thenReturn(userAccountUpdated)
            // WHEN
            userAccountService.updateAvatarForCurrentUser(avatarUrl)
            // THEN
            verify(userAccountRepository).findUserAccountByEmail(email)
            verify(timeService).now()
            verify(userAccountRepository).save(userArgumentCaptor.capture())
            val userForSave = userArgumentCaptor.value
            assertThat(userForSave.avatar).isEqualTo(avatarUrl)
            assertThat(userForSave.id).isEqualTo(userAccount.id)
            assertThat(userForSave.fullName).isEqualTo(userAccount.fullName)
        }

        @Test
        fun `should update current session user`() {
            // GIVEN
            val avatarUrl = "test/avatar"
            val email = "test@test.ru"
            val authentication = Mockito.mock(Authentication::class.java)
            val securityContext: SecurityContext = Mockito.mock(SecurityContext::class.java)
            val userAccount = UserAccount(
                id = 1L,
                fullName = "testUserFirstName",
                email = email,
                password = "password",
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                changed = LocalDateTime.now().minusMinutes(5),
                avatar = null
            )
            val userAccountChangeRequest = UserAccountChangeRequest(avatar = avatarUrl, name = "newName")
            val userAccountUpdated = userAccount.copy()
            userAccountUpdated.avatar = avatarUrl
            userAccountUpdated.fullName = "newName"

            SecurityContextHolder.setContext(securityContext)
            `when`(securityContext.authentication).thenReturn(authentication)
            `when`(authentication.name).thenReturn(email)
            `when`(userAccountRepository.findUserAccountByEmail(email))
                .thenReturn(Optional.of(userAccount))
            `when`(timeService.now()).thenReturn(LocalDateTime.now())
            `when`(userAccountRepository.save(Mockito.any(UserAccount::class.java)))
                .thenReturn(userAccountUpdated)
            // WHEN
            userAccountService.updateCurrentUser(userAccountChangeRequest)
            // THEN
            verify(userAccountRepository).findUserAccountByEmail(email)
            verify(timeService).now()
            verify(userAccountRepository).save(userArgumentCaptor.capture())
            val userForSave = userArgumentCaptor.value
            assertThat(userForSave.avatar).isEqualTo(avatarUrl)
            assertThat(userForSave.fullName).isEqualTo("newName")
            assertThat(userForSave.id).isEqualTo(userAccount.id)
        }
    }

    @Nested
    @DisplayName("Tests for user headphones functionality")
    inner class HeadphonesFunctionality {
        @Test
        fun `should return all headphones for user`() {
            // GIVEN
            val listOfHeadphones = setOf(
                HeadphonesDto(name = "first", type = HeadphonesType.IN_EAR_NO_BLUETOOTH),
                HeadphonesDto(name = "second", type = HeadphonesType.ON_EAR_BLUETOOTH)
            )
            `when`(headphonesService.getAllHeadphonesForUser(1L)).thenReturn(listOfHeadphones)
            // WHEN
            val returnedListOfHeadphones = userAccountService.getAllHeadphonesForUser(1L)
            // THEN
            assertThat(returnedListOfHeadphones).isEqualTo(listOfHeadphones)
        }

        @Test
        fun `should add new headphones to the user`() {
            val headphonesToAdd = HeadphonesDto(name = "first", type = HeadphonesType.IN_EAR_NO_BLUETOOTH)

            `when`(userAccountRepository.findUserAccountById(1L)).thenReturn(Optional.of(userAccount))
            `when`(headphonesService.save(any(Headphones::class.java))).thenReturn(headphonesToAdd)
            // WHEN
            val returnedListOfHeadphones = userAccountService.addHeadphonesToUser(1L, headphonesToAdd)
            // THEN
            assertThat(returnedListOfHeadphones).isEqualTo(headphonesToAdd)
        }

        @Test
        fun `should add new headphones to the current user`() {
            val headphonesToAdd = HeadphonesDto(name = "first", type = HeadphonesType.IN_EAR_NO_BLUETOOTH)
            val userAccount = UserAccount(
                id = 1L,
                fullName = "testUserFirstName",
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                password = "test",
                email = "test@gmail.com",
                active = true
            )
            val authentication = Mockito.mock(Authentication::class.java)
            val securityContext = Mockito.mock(SecurityContext::class.java)
            SecurityContextHolder.setContext(securityContext)
            // WHEN
            val email = "test@test.com"
            `when`(authentication.name).thenReturn(email)
            `when`(securityContext.authentication).thenReturn(authentication)
            `when`(headphonesService.save(any(Headphones::class.java))).thenReturn(headphonesToAdd)
            `when`(userAccountRepository.findUserAccountByEmail(email)).thenReturn(Optional.of(userAccount))

            // WHEN
            val returnedListOfHeadphones = userAccountService.addHeadphonesToCurrentUser(headphonesToAdd)
            // THEN
            assertThat(returnedListOfHeadphones).isEqualTo(headphonesToAdd)
        }

        @Test
        fun `should return all headphones for the user`() {
            val headphonesToAdd = setOf(HeadphonesDto(name = "first", type = HeadphonesType.IN_EAR_NO_BLUETOOTH))

            `when`(headphonesService.getAllHeadphonesForUser(1L)).thenReturn(headphonesToAdd)
            // WHEN
            val returnedListOfHeadphones = userAccountService.getAllHeadphonesForUser(1L)
            // THEN
            assertThat(returnedListOfHeadphones).isEqualTo(headphonesToAdd)
        }

        @Test
        fun `should return all headphones for current the user`() {
            val headphones = Headphones(name = "first", type = HeadphonesType.IN_EAR_NO_BLUETOOTH)
            val headphonesToAdd = mutableSetOf(headphones)
            val userAccount = UserAccount(
                id = 1L,
                fullName = "testUserFirstName",
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                password = "test",
                email = "test@gmail.com",
                active = true,
                headphones = headphonesToAdd
            )
            val authentication = Mockito.mock(Authentication::class.java)
            val securityContext = Mockito.mock(SecurityContext::class.java)
            SecurityContextHolder.setContext(securityContext)
            // WHEN
            val email = "test@test.com"
            `when`(authentication.name).thenReturn(email)
            `when`(securityContext.authentication).thenReturn(authentication)
            `when`(userAccountRepository.findUserAccountByEmail(email)).thenReturn(Optional.of(userAccount))

            val returnedListOfHeadphones = userAccountService.getAllHeadphonesForCurrentUser()

            // THEN
            assertThat(returnedListOfHeadphones)
                .hasSize(NumberUtils.INTEGER_ONE)
                .usingElementComparatorOnFields("name", "type")
                .containsExactly(headphones.toDto())
        }
    }
}
