package com.epam.brn.service

import com.epam.brn.auth.AuthorityService
import com.epam.brn.dto.HeadphonesDto
import com.epam.brn.dto.request.UserAccountChangeRequest
import com.epam.brn.dto.request.UserAccountCreateRequest
import com.epam.brn.dto.response.UserAccountResponse
import com.epam.brn.enums.HeadphonesType
import com.epam.brn.enums.Role.ROLE_USER
import com.epam.brn.exception.EntityNotFoundException
import com.epam.brn.model.Authority
import com.epam.brn.model.Gender
import com.epam.brn.model.Headphones
import com.epam.brn.model.UserAccount
import com.epam.brn.repo.UserAccountRepository
import com.epam.brn.service.impl.UserAccountServiceImpl
import com.google.firebase.auth.UserRecord
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockkClass
import io.mockk.slot
import io.mockk.verify
import org.apache.commons.lang3.math.NumberUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.domain.Pageable
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import java.time.LocalDateTime
import java.util.Optional
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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

    @MockK(relaxed = true)
    lateinit var doctorAccount: UserAccount

    @MockK
    lateinit var userAccountResponse: UserAccountResponse

    @MockK
    lateinit var userAccountCreateRequest: UserAccountCreateRequest

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

    @MockK
    lateinit var pageable: Pageable

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
        fun `should throw EntityNotFoundException while get user by email`() {
            // GIVEN
            val email = "email"
            every { userAccountRepository.findUserAccountByEmail(email) } returns Optional.empty()
            // THEN
            shouldThrow<EntityNotFoundException> { userAccountService.findUserByEmail(email) }
        }

        @Test
        fun `should find a user by uuid`() {
            // GIVEN
            val uuid = "uuid"
            every { userAccount.toDto() } returns userAccountResponse
            every { userAccountResponse.userId } returns uuid
            every { userAccountRepository.findByUserId(uuid) } returns userAccount
            // WHEN
            val userAccountDtoReturned = userAccountService.findUserByUuid(uuid)
            // THEN
            assertNotNull(userAccountDtoReturned)
            assertThat(userAccountDtoReturned.userId).isEqualTo(uuid)
        }

        @Test
        fun `should return NULL while get user by uuid`() {
            // GIVEN
            val uuid = "uuid"
            every { userAccount.toDto() } returns userAccountResponse
            every { userAccountResponse.userId } returns uuid
            every { userAccountRepository.findByUserId(uuid) } returns null
            // WHEN
            val userAccountDtoReturned = userAccountService.findUserByUuid(uuid)
            // THEN
            assertNull(userAccountDtoReturned)
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
            val email = "test@gmail.com"
            every { authorityService.findAuthorityByAuthorityName(ofType(String::class)) } returns Authority(
                id = 1L,
                authorityName = ROLE_USER.name
            )
            every { firebaseUserRecord.uid } returns uid
            every { firebaseUserRecord.email } returns email
            every { firebaseUserRecord.displayName } returns userName
            every { userAccountRepository.findUserAccountByEmail(ofType(String::class)) } returns Optional.empty()
            val captureMyObject = slot<UserAccount>()
            every { userAccountRepository.save(capture(captureMyObject)) } answers { captureMyObject.captured }
            // WHEN
            val userAccountDtoReturned = userAccountService.createUser(firebaseUserRecord)
            // THEN
            assertThat(userAccountDtoReturned.name).isEqualTo(userName)
            assertThat(userAccountDtoReturned.userId).isEqualTo(uid)
            assertThat(userAccountDtoReturned.email).isEqualTo(email)
            assertNotNull(userAccountDtoReturned.authorities)
            assertThat(userAccountDtoReturned.authorities!!.size).isEqualTo(1)

            verify(exactly = 1) { userAccountRepository.findUserAccountByEmail(email) }
            verify(exactly = 1) { userAccountRepository.save(captureMyObject.captured) }
        }

        @Test
        fun `should throw IllegalArgumentException when create new user which exist in database already`() {
            // GIVEN
            val userName = "Tested"
            val uid = "UID"
            val email = "test@gmail.com"
            every { firebaseUserRecord.uid } returns uid
            every { firebaseUserRecord.email } returns email
            every { firebaseUserRecord.displayName } returns userName
            every { userAccountRepository.findUserAccountByEmail(ofType(String::class)) } returns Optional.of(
                userAccount
            )
            // THEN
            assertFailsWith<IllegalArgumentException> {
                userAccountService.createUser(firebaseUserRecord)
            }
            verify(exactly = 1) { userAccountRepository.findUserAccountByEmail(email) }
            verify(exactly = 0) { userAccountRepository.save(userAccount) }
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
                HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH),
                HeadphonesDto(name = "second", active = true, type = HeadphonesType.ON_EAR_BLUETOOTH)
            )
            every { headphonesService.getAllHeadphonesForUser(1L) } returns listOfHeadphones
            // WHEN
            val returnedListOfHeadphones = userAccountService.getAllHeadphonesForUser(1L)
            // THEN
            assertThat(returnedListOfHeadphones).isEqualTo(listOfHeadphones)
        }

        @Test
        fun `should add new headphones to the user`() {
            val headphonesToAdd =
                HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH)

            every { userAccountRepository.findUserAccountById(1L) } returns Optional.of(userAccount)
            every { headphonesService.save(ofType(Headphones::class)) } returns headphonesToAdd
            // WHEN
            val returnedListOfHeadphones = userAccountService.addHeadphonesToUser(1L, headphonesToAdd)
            // THEN
            assertThat(returnedListOfHeadphones).isEqualTo(headphonesToAdd)
        }

        @Test
        fun `should add new headphones to the current user`() {
            val headphonesToAdd =
                HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH)
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
            val headphonesToAdd =
                setOf(HeadphonesDto(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH))

            every { headphonesService.getAllHeadphonesForUser(1L) } returns headphonesToAdd
            // WHEN
            val returnedListOfHeadphones = userAccountService.getAllHeadphonesForUser(1L)
            // THEN
            assertThat(returnedListOfHeadphones).isEqualTo(headphonesToAdd)
        }

        @Test
        fun `should return all headphones for current the user`() {
            val headphones = Headphones(name = "first", active = true, type = HeadphonesType.IN_EAR_NO_BLUETOOTH)
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

        @Test
        fun `should delete headphones to current user`() {
            // GIVEN
            val headphonesId = 1L
            val headphones = Headphones(
                id = headphonesId,
                name = "test",
                active = true,
                type = HeadphonesType.IN_EAR_BLUETOOTH
            )

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
            val email = "test@test.com"
            val headphonesDto = mockkClass(HeadphonesDto::class)
            every { authentication.name } returns email
            every { securityContext.authentication } returns authentication
            every { userAccountRepository.findUserAccountByEmail(email) } returns Optional.of(userAccount)
            every { headphonesService.save(headphones) } returns headphonesDto
            // WHEN
            userAccountService.deleteHeadphonesForCurrentUser(headphonesId)

            // THEN
            verify(exactly = 1) { headphonesService.save(headphones) }
        }

        @Test
        fun `should trow exception when headphones for current user is not found`() {
            // GIVEN
            val headphonesId = 1L
            val headphones = Headphones(
                id = 2L,
                name = "test",
                active = true,
                type = HeadphonesType.IN_EAR_BLUETOOTH
            )

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
            val email = "test@test.com"
            every { authentication.name } returns email
            every { securityContext.authentication } returns authentication
            every { userAccountRepository.findUserAccountByEmail(email) } returns Optional.of(userAccount)

            // THEN
            shouldThrow<EntityNotFoundException> { userAccountService.deleteHeadphonesForCurrentUser(headphonesId) }
        }

        @Test
        fun `should return all users`() {
            // GIVEN
            val usersList = listOf(userAccount, userAccount, userAccount)
            every { userAccountRepository.findUsersAccountsByRole(ROLE_USER.name) } returns usersList
            // WHEN
            val userAccountDtos = userAccountService.getUsers(pageable = pageable, ROLE_USER.name)
            // THEN
            userAccountDtos.size shouldBe 3
        }
    }

    @Nested
    @DisplayName("Doctor related tests")
    inner class DoctorFunctionality {

        @Test
        fun `should update doctor for patient`() {
            // GIVEN
            val userId: Long = 1
            val doctorId: Long = 2
            every { userAccountRepository.findUserAccountById(userId) } returns Optional.of(userAccount)
            every { doctorAccount.id } returns doctorId
            every { userAccountRepository.findUserAccountById(doctorId) } returns Optional.of(doctorAccount)
            every { userAccountRepository.save(any()) } returns userAccount

            // WHEN
            userAccountService.updateDoctorForPatient(userId, doctorId)

            // THEN
            verify { userAccountRepository.findUserAccountById(userId) }
            verify { userAccountRepository.findUserAccountById(doctorId) }
            verify { userAccountRepository.save(userAccount) }
        }

        @Test
        fun `should remove doctor from patient`() {
            // GIVEN
            val userId: Long = 1
            val opDoctor = Optional.of(userAccount.apply { doctor = doctorAccount })
            every { userAccountRepository.findUserAccountById(userId) } returns opDoctor
            every { userAccountRepository.save(any()) } returns userAccount

            // WHEN
            userAccountService.removeDoctorFromPatient(userId)

            // THEN
            verify { userAccountRepository.findUserAccountById(userId) }
            verify { userAccountRepository.save(userAccount) }
        }

        @Test
        fun `should get patients for doctor`() {
            // GIVEN
            val doctorId: Long = 2
            val patients = listOf(userAccount, userAccount)
            every { userAccountRepository.findUserAccountById(doctorId) } returns Optional.of(doctorAccount)
            every { userAccountRepository.findUserAccountsByDoctor(doctorAccount) } returns patients

            // WHEN
            val patientsForDoctor = userAccountService.getPatientsForDoctor(doctorId)

            // THEN
            verify { userAccountRepository.findUserAccountById(doctorId) }
            verify { userAccountRepository.findUserAccountsByDoctor(doctorAccount) }

            patientsForDoctor.size shouldBe patients.size
        }
    }
}
