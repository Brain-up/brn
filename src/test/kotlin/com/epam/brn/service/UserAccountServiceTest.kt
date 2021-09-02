package com.epam.brn.service

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.model.Gender
import com.epam.brn.model.Headphones
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.impl.UserAccountServiceImpl
import com.google.firebase.auth.UserRecord
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.apache.commons.lang3.math.NumberUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
@DisplayName("UserAccountService test using MockK")
internal class UserAccountServiceTest {

    @InjectMockKs
    lateinit var userAccountService: UserAccountServiceImpl

    @MockK
    lateinit var userAccountRepository: UserAccountRepository

    @MockK
    lateinit var passwordEncoder: PasswordEncoder

    @MockK
    lateinit var authorityService: AuthorityService

    @MockK(relaxed = true)
    lateinit var userAccount: UserAccount

    @MockK
    lateinit var userAccountResponse: UserAccountResponse

    @MockK
    lateinit var userAccountCreateRequest: UserAccountCreateRequest

    @MockK
    lateinit var firebaseUserService: FirebaseUserService

    @MockK
    lateinit var firebaseUserRecord: UserRecord

    @MockK
    lateinit var authority: Authority

    @MockK
    lateinit var authentication: Authentication

    @MockK
    lateinit var securityContext: SecurityContext

    @MockK
    lateinit var headphonesService: HeadphonesService

    @Nested
    @DisplayName("Tests for getting users")
    inner class GetUserAccounts {
        @Test
        fun `should find a user by id`() {
            // GIVEN
            val userName = "Tested"
            every { userAccount.toDto() } returns userAccountResponse
            every { userAccountResponse.name } returns userName
            every { userAccountRepository.findUserAccountById(NumberUtils.LONG_ONE) } returns Optional.of(userAccount)
            // WHEN
            val userAccountDtoReturned = userAccountService.findUserById(NumberUtils.LONG_ONE)
            // THEN
            assertThat(userAccountDtoReturned.name).isEqualTo(userName)
        }

        @Test
        fun `should find a user by name`() {
            // GIVEN
            val fullName = "Ivan"
            every { userAccount.toDto() } returns userAccountResponse
            every { userAccountResponse.name } returns fullName
            every { userAccountRepository.findUserAccountByName(fullName) } returns Optional.of(userAccount)
            // WHEN
            val userAccountDtoReturned = userAccountService.findUserByName(fullName)
            // THEN
            assertThat(userAccountDtoReturned.name).isEqualTo(fullName)
        }

        @Test
        fun `should find a user by email`() {
            // GIVEN
            val email = "email"
            every { userAccount.toDto() } returns userAccountResponse
            every { userAccountResponse.email } returns email
            every { userAccountRepository.findUserAccountByEmail(email) } returns Optional.of(userAccount)
            // WHEN
            val userAccountDtoReturned = userAccountService.findUserByEmail(email)
            // THEN
            assertThat(userAccountDtoReturned.email).isEqualTo(email)
        }

        @Test
        fun `should throw an exception when there is no user by specified id`() {
            // GIVEN
            every { userAccountRepository.findUserAccountById(NumberUtils.LONG_ONE) } returns Optional.empty()
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
            val uid = "UID"
            every { firebaseUserRecord.uid } returns uid
            every { userAccountCreateRequest.email } returns "test@gmail.com"
            every { userAccountRepository.findUserAccountByEmail(ofType(String::class)) } returns Optional.empty()
            every { userAccountCreateRequest.authorities } returns mutableSetOf("ROLE_USER")
            every { passwordEncoder.encode(ofType(String::class)) } returns "password"
            every { userAccountCreateRequest.toModel() } returns userAccount
            every { userAccountCreateRequest.password } returns "password"
            every { userAccountResponse.name } returns userName
            every { userAccountResponse.userId } returns uid
            every { userAccount.toDto() } returns userAccountResponse
            every { userAccountRepository.save(userAccount) } returns userAccount
            every { authorityService.findAuthorityByAuthorityName(ofType(String::class)) } returns authority
            // WHEN
            val userAccountDtoReturned = userAccountService.createUser(userAccountCreateRequest, firebaseUserRecord)
            // THEN
            assertThat(userAccountDtoReturned.name).isEqualTo(userName)
            assertThat(userAccountDtoReturned.userId).isEqualTo(uid)
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
            val userAccount = UserAccount(
                id = 1L,
                fullName = "testUserFirstName",
                email = email,
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                changed = LocalDateTime.now().minusMinutes(5),
                avatar = null
            )
            val userAccountUpdated = userAccount.copy()
            userAccountUpdated.avatar = avatarUrl
            val userArgumentCaptor = slot<UserAccount>()

            SecurityContextHolder.setContext(securityContext)
            every { securityContext.authentication } returns authentication
            every { authentication.name } returns email
            every { userAccountRepository.findUserAccountByEmail(email) } returns Optional.of(userAccount)
            every { userAccountRepository.save(ofType(UserAccount::class)) } returns userAccountUpdated
            every { userAccountRepository.save(capture(userArgumentCaptor)) } returns userAccount
            // WHEN
            userAccountService.updateAvatarForCurrentUser(avatarUrl)
            // THEN
            verify { userAccountRepository.findUserAccountByEmail(email) }
            verify { userAccountRepository.save(userArgumentCaptor.captured) }
            val userForSave = userArgumentCaptor.captured
            assertThat(userForSave.avatar).isEqualTo(avatarUrl)
            assertThat(userForSave.id).isEqualTo(userAccount.id)
            assertThat(userForSave.fullName).isEqualTo(userAccount.fullName)
        }

        @Test
        fun `should update current session user`() {
            // GIVEN
            val avatarUrl = "test/avatar"
            val photoUrl = "test/picture"
            val description = "Some description about the user"
            val email = "test@test.ru"
            val userAccount = UserAccount(
                id = 1L,
                fullName = "testUserFirstName",
                email = email,
                gender = Gender.MALE.toString(),
                bornYear = 2000,
                changed = LocalDateTime.now().minusMinutes(5),
                avatar = null,
                photo = null,
                description = null
            )
            val userAccountChangeRequest = UserAccountChangeRequest(
                avatar = avatarUrl,
                photo = photoUrl,
                description = description,
                name = "newName"
            )
            val userAccountUpdated = userAccount.copy()
            userAccountUpdated.avatar = avatarUrl
            userAccountUpdated.photo = photoUrl
            userAccountUpdated.description = description
            userAccountUpdated.fullName = "newName"
            val userArgumentCaptor = slot<UserAccount>()

            SecurityContextHolder.setContext(securityContext)
            every { securityContext.authentication } returns authentication
            every { authentication.name } returns email
            every { userAccountRepository.findUserAccountByEmail(email) } returns Optional.of(userAccount)
            every { userAccountRepository.save(ofType(UserAccount::class)) } returns userAccountUpdated
            every { userAccountRepository.save(capture(userArgumentCaptor)) } returns userAccount
            // WHEN
            userAccountService.updateCurrentUser(userAccountChangeRequest)
            // THEN
            verify { userAccountRepository.findUserAccountByEmail(email) }
            verify { userAccountRepository.save(userArgumentCaptor.captured) }
            val userForSave = userArgumentCaptor.captured
            assertThat(userForSave.avatar).isEqualTo(avatarUrl)
            assertThat(userForSave.photo).isEqualTo(photoUrl)
            assertThat(userForSave.description).isEqualTo(description)
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
            every { headphonesService.getAllHeadphonesForUser(1L) } returns listOfHeadphones
            // WHEN
            val returnedListOfHeadphones = userAccountService.getAllHeadphonesForUser(1L)
            // THEN
            assertThat(returnedListOfHeadphones).isEqualTo(listOfHeadphones)
        }

        @Test
        fun `should add new headphones to the user`() {
            val headphonesToAdd = HeadphonesDto(name = "first", type = HeadphonesType.IN_EAR_NO_BLUETOOTH)

            every { userAccountRepository.findUserAccountById(1L) } returns Optional.of(userAccount)
            every { headphonesService.save(ofType(Headphones::class)) } returns headphonesToAdd
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
                email = "test@gmail.com",
                active = true
            )
            SecurityContextHolder.setContext(securityContext)
            // WHEN
            val email = "test@test.com"
            every { authentication.name } returns email
            every { securityContext.authentication } returns authentication
            every { headphonesService.save(ofType(Headphones::class)) } returns headphonesToAdd
            every { userAccountRepository.findUserAccountByEmail(email) } returns Optional.of(userAccount)

            // WHEN
            val returnedListOfHeadphones = userAccountService.addHeadphonesToCurrentUser(headphonesToAdd)
            // THEN
            assertThat(returnedListOfHeadphones).isEqualTo(headphonesToAdd)
        }

        @Test
        fun `should return all headphones for the user`() {
            val headphonesToAdd = setOf(HeadphonesDto(name = "first", type = HeadphonesType.IN_EAR_NO_BLUETOOTH))

            every { headphonesService.getAllHeadphonesForUser(1L) } returns headphonesToAdd
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
                email = "test@gmail.com",
                active = true,
                headphones = headphonesToAdd
            )
            SecurityContextHolder.setContext(securityContext)
            // WHEN
            val email = "test@test.com"
            every { authentication.name } returns email
            every { securityContext.authentication } returns authentication
            every { userAccountRepository.findUserAccountByEmail(email) } returns Optional.of(userAccount)

            val returnedListOfHeadphones = userAccountService.getAllHeadphonesForCurrentUser()

            // THEN
            assertThat(returnedListOfHeadphones)
                .hasSize(NumberUtils.INTEGER_ONE)
                .usingElementComparatorOnFields("name", "type")
                .containsExactly(headphones.toDto())
        }
    }
}
